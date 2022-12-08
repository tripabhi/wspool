package pool;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.locks.LockSupport;


public abstract class FJTask<V> {

    public static final int COMPLETED = -1;
    public static final int RUNNING = 0;
    public static final int NOT_YET_STARTED = 1;

    volatile int status =  NOT_YET_STARTED;
    private transient volatile Node head;

    // Variables to atomically change status and head;
    private static final VarHandle STATUS;
    private static final VarHandle HEAD;

    static {
        try {
            MethodHandles.Lookup l = MethodHandles.lookup();
            STATUS = l.findVarHandle(FJTask.class, "status", int.class);
            HEAD = l.findVarHandle(FJTask.class, "head", Node.class);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean casStatus(int c, int v) {
        return STATUS.compareAndSet(this, c, v);
    }

    public boolean casAux(Node c, Node v) {
        return HEAD.compareAndSet(this, c, v);
    }

    private int getStatus() {
        return (int) STATUS.getAcquire(this);
    }

    /**
     * Inner class to store a Linked List of threads that are waiting on this task
     */
    static final class Node {
        final Thread thread;
        final Throwable ex;
        Node next;
        Node(Thread thread, Throwable ex) {
            this.thread = thread;
            this.ex = ex;
        }
        private static final VarHandle NEXT;
        static {
            try {
                NEXT = MethodHandles.lookup()
                        .findVarHandle(Node.class, "next", Node.class);
            } catch (ReflectiveOperationException e) {
                throw new ExceptionInInitializerError(e);
            }
        }
    }

    public FJTask() {

    }

    private int awaitDone() {
        Thread t; WorkerThread wt; boolean internal = false;
        WSPool p = null;
        ConcurrentLinkedDeque<FJTask<?>> q = null;

        if((t = Thread.currentThread()) instanceof WorkerThread) {
            wt = (WorkerThread) t;
            p = wt.pool;
            q = wt.workQueue;
            internal = true;
        }

        int s;

        if(internal) {
            if (casStatus(NOT_YET_STARTED, RUNNING)) {
                // If nobody is working on this task, remove it
                // from the queue and exec it
                q.remove(this);
                doExec();
                return getStatus();
            } else if(getStatus() == COMPLETED) {
                return getStatus();
            } else {
                // If task has been stolen, try to help join that task,
                // by executing the subtasks that are being created.
                if(p.helpJoin(this) == COMPLETED) {
                    return COMPLETED;
                }
            }
        }

        // Block thread until status is COMPLETE
        boolean parked = false, queued = false, interrupted = false;
        Node node = null;
        while((s = getStatus()) >= 0) {
            Node a;
            if(parked && Thread.interrupted()) {
                interrupted = true;
            } else if(queued) {
                LockSupport.park();
                parked = true;
            } else if(node != null) {
               if((a = this.head) != null && casAux(node.next = a, node)) {
                    queued = true;
                    LockSupport.setCurrentBlocker(this);
                }
            } else {
                try {
                    node = new Node(Thread.currentThread(), null);
                } catch (Exception e) {
                    throw new RuntimeException();
                }
            }
        }

        signalWaiters();
        if(interrupted) {
            Thread.currentThread().interrupt();
        }
        return s;
    }

    public V get() {
        int s = awaitDone();
        return getResult();
    }

    protected abstract V getResult();

    private int setDone() {
        STATUS.setVolatile(this, COMPLETED);
        signalWaiters();
        return getStatus();
    }

    /**
     *
     */
    private void signalWaiters() {
        for (Node a; (a = head) != null && a.ex == null; ) {
            if (casAux(a, null)) {
                for (Thread t; a != null; a = a.next) {
                    if ((t = a.thread) != Thread.currentThread() && t != null)
                        LockSupport.unpark(t);
                }
                break;
            }
        }
    }

    protected abstract boolean exec();

    final int doExec() {
        int s; boolean completed;
        if((s = status) >= 0) {
            try {
                completed = exec();
            } catch (Exception ignore) {
                completed = false;
            }
            if(completed) {
                s = setDone();
            }
        }
        return s;
    }

    public static void invokeAll(FJTask<?> ...tasks) {
        int last = tasks.length - 1;
        for(int i = last; i >= 0; --i) {
            FJTask<?> t;
            t = tasks[i];
            if(i == 0) {
                int s;
                if((s = t.doExec()) >= 0) {
                    s = t.awaitDone();
                }
                break;
            }
            t.fork();
        }

        for(int i = 1; i <= last; i++) {
            FJTask<?> t;
            if((t = tasks[i]) != null) {
                int s;
                if((s = t.getStatus()) >= 0) {
                    s = t.awaitDone();
                }
                break;
            }
        }
    }


    public static <T extends FJTask<?>> Collection<T> invokeAll(Collection<T> tasks) {
        if(!(tasks instanceof List<?>)) {
            invokeAll(tasks.toArray(new FJTask<?>[0]));
            return tasks;
        }

        List<? extends FJTask<?>> tsks = (List<? extends FJTask<?>>) tasks;
        int last = tasks.size() - 1;
        for(int i = last; i >= 0; --i) {
            FJTask<?> t;
            t = tsks.get(i);
            if(i == 0) {
                int s;
                if((s = t.doExec()) >= 0) {
                    s = t.awaitDone();
                }
                break;
            }
            t.fork();
        }

        for(int i = 1; i <= last; i++) {
            FJTask<?> t;
            if((t = tsks.get(i)) != null) {
                int s;
                if((s = t.getStatus()) >= 0) {
                    s = t.awaitDone();
                }
                break;
            }
        }

        return tasks;
    }

    public final FJTask<?> fork() {
        Thread t; WorkerThread wt;
        if((t = Thread.currentThread()) instanceof WorkerThread) {
            (wt = (WorkerThread) t).workQueue.add(this);
            wt.pool.signalWork();
        }
        return this;
    }

    public final V join() {
        int s;
        if((s = getStatus()) >= 0) {
            s = awaitDone();
        }
        return getResult();
    }
}
