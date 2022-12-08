package bench.tasks;

import pool.RTask;

public class Fibonacci extends RTask<Integer> {

    private final int n;

    public Fibonacci(int n) {
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

        Fibonacci large = new Fibonacci(n - 1);
        Fibonacci small = new Fibonacci(n - 2);

        large.fork();

        return small.compute() + large.join();
    }
}
