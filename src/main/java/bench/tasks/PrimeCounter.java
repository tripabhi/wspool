package bench.tasks;

import pool.RTask;
import pool.WSPool;

public class PrimeCounter extends RTask<Integer> {

    private final int start, end;

    PrimeCounter(int start, int end) {
        this.start = start;
        this.end = end;
    }
    @Override
    protected Integer compute() {
        int count = 0;
        if (end - start <= 250) {
            for (int i = start; i < end; i++) {
                if (isPrime(i)) count += 1;
            }
        } else {
            int mid = ((end - start) / 2) + start;
            PrimeCounter left = new PrimeCounter(start, mid);
            PrimeCounter right = new PrimeCounter(mid, end);
            right.fork();
            count += left.compute();
            count += right.join();
        }
        return count;
    }

    static boolean isPrime(int n) {
        if (n == 1) return false;
        if (n == 2 || n == 3) return true;
        if (n % 2 == 0) return false;
        int limit = (int)(Math.sqrt(n)+ 0.5);
        for (int i = 3; i <= limit; i += 2) {
            if (n % i == 0) return false;
        }
        return true;
    }

    public static void main(String[] args) {
        WSPool pool = new WSPool(8);
        PrimeCounter task = new PrimeCounter(10, 100000);
        pool.start();
        pool.submit(task);
        System.out.println("Count = " + task.get());
    }
}
