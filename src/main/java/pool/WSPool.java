package pool;

import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import static pool.FJTask.NOT_YET_STARTED;
import static pool.FJTask.RUNNING;

public class WSPool implements Pool {

    int nthreads;
    volatile boolean shutDown;
    ConcurrentLinkedDeque<FJTask<?>>[] queues;
    WorkerThread[] workers;
    final ReentrantLock wait4workLock;
    final ReentrantLock submissionQueueLock;
    final Condition wait4work;

    @SuppressWarnings("unchecked")
    public WSPool(int threads) {
        this.nthreads = threads;
        this.workers = new WorkerThread[this.nthreads];
        this.queues = new ConcurrentLinkedDeque[this.nthreads + 1];
        for(int i = 0; i < this.nthreads + 1; i++) {
            this.queues[i] = new ConcurrentLinkedDeque<>();
            if (i < this.nthreads) {
                this.workers[i] = new WorkerThread(this, i, this.queues[i]);
            }
        }
        this.wait4workLock = new ReentrantLock();
        this.submissionQueueLock = new ReentrantLock();
        this.wait4work = this.wait4workLock.newCondition();
        this.shutDown = false;
    }

    @Override
    public FJTask<?> submit(FJTask<?> task) {
        Thread t; WorkerThread wt; ConcurrentLinkedDeque<FJTask<?>>[] q = queues;
        if(task == null) {
            throw new NullPointerException();
        }
        if((t = Thread.currentThread()) instanceof WorkerThread) {
            wt = (WorkerThread) t;
            q[wt.idx()].add(task);

        } else {
            int r = ThreadLocalRandom.current().nextInt(0, this.nthreads);
            q[r].add(task);
        }
        signalWork();
        return task;
    }

    @Override
    public boolean shutdown() throws InterruptedException {
        boolean ex = false;
        try {
            this.wait4workLock.lock();
            this.shutDown = true;
            this.wait4work.signalAll();
        } catch (Exception e) {
            ex = true;
        } finally {
            this.wait4workLock.unlock();
            for(int i = 0; i < this.nthreads; i++) {
                this.workers[i].interrupt();
                this.workers[i].join();
            }
        }
        return !ex;
    }

    public void signalWork() {
        try {
            this.wait4workLock.lock();
            this.wait4work.signalAll();
        } finally {
            this.wait4workLock.unlock();
        }
    }

    /**
     * Worker thread routine called in WorkerThread.run()
     * Thread executes tasks in its local work queue until it is empty.
     * If no task is left in local queue, thread scans other worker's
     * local queue to steal tasks. If couldn't steal, thread waits for
     * work.
     */
    final void runWorker() {
        Thread t; WorkerThread wt;
        if((t = Thread.currentThread()) instanceof WorkerThread) {
            wt = (WorkerThread) t;
            int me = wt.idx();
            FJTask<?> task;
            while(!shutDown) {
                task = queues[me].pollFirst();
                while(task != null) {
                    if(!task.casStatus(NOT_YET_STARTED, RUNNING)) {
                        System.out.println("This should not happen for thread-" + me);
                    }
                    task.doExec();
                    task = queues[me].pollFirst();
                }

                boolean stolen = false;
                for (int i = 0; i < this.nthreads; i++) {
                    if (i != me) {
                        task = queues[i].pollLast();
                        if (task != null) {
                            if (task.casStatus(NOT_YET_STARTED, RUNNING)) {
                                stolen = true;
                                task.doExec();
                                break;
                            }
                        }
                    }
                }
                if(!stolen) {
                    awaitWork();
                }
            }
        }
    }

    public void start() {
        for(int i = 0; i < this.nthreads; i++) {
            this.workers[i].start();
        }
    }

    public void awaitWork() {
        try {
            this.wait4workLock.lock();
            if(!shutDown) {
                this.wait4work.await();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            this.wait4workLock.unlock();
        }
    }

    /**
     * Helps join the task by executing its subtasks from the end
     * of their work queue
     * @param task FJTask to join
     * @return COMPLETED if task is finished, -2 if thread couldn't find
     * tasks to execute and should block
     */
    final int helpJoin(FJTask<?> task) {
        int s = 0;
        Thread t; WorkerThread wt;
        if((t = Thread.currentThread()) instanceof WorkerThread) {
            wt = (WorkerThread) t;
            int me = wt.idx;
            boolean found;
            while ((s = task.status) >= 0) {
                found = false;
                FJTask<?> ft = this.queues[me].pollLast();
                while (ft != null) {
                    found = true;
                    ft.casStatus(NOT_YET_STARTED, RUNNING);
                    ft.doExec();
                    ft = this.queues[me].pollLast();
                }

                if (!found) {
                    return -2;
                }
            }

        }
        return s;
    }
}
