package bench.control;

import bench.tasks.Fibonacci;

import java.util.concurrent.RecursiveTask;

public class FibonacciTask extends RecursiveTask<Integer> {

    private final int n;

    public FibonacciTask(int n) {
        this.n = n;
    }

    @Override
    protected Integer compute() {
        if(n == 0 || n == 1) {
            return n;
        }

        if(n == 2) {
            return 1;
        }

        FibonacciTask large = new FibonacciTask(n - 1);
        FibonacciTask small = new FibonacciTask(n - 2);

        large.fork();

        return small.compute() + large.join();
    }
}
