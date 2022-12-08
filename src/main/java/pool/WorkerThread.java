package pool;

import java.util.concurrent.ConcurrentLinkedDeque;

public class WorkerThread extends Thread {
    final WSPool pool;
    final int idx;
    final ConcurrentLinkedDeque<FJTask<?>> workQueue;

    public WorkerThread(WSPool pool, int idx, ConcurrentLinkedDeque<FJTask<?>> queue) {
        this.pool = pool;
        this.idx = idx;
        this.workQueue = queue;
        this.setDaemon(true);
    }

    public int idx() {
        return idx;
    }

    @Override
    public void run() {
        try {
            pool.runWorker();
        } catch (Exception e) {

        }
    }
}