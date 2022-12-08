package bench.control;

import java.util.concurrent.RecursiveTask;

public class PrimeCounterTask extends RecursiveTask<Integer> {

    private final int start, end;

    PrimeCounterTask(int start, int end) {
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
            PrimeCounterTask left = new PrimeCounterTask(start, mid);
            PrimeCounterTask right = new PrimeCounterTask(mid, end);
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
}
