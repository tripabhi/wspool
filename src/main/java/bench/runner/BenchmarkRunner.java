package bench.runner;

import bench.control.ArrayTransformTask;
import bench.control.FibonacciTask;
import bench.control.MergeSortTask;
import bench.control.PrimeCounterTask;
import bench.tasks.ArrayTransform;
import bench.tasks.Fibonacci;
import bench.tasks.MergeSort;
import bench.tasks.PrimeCounter;
import pool.WSPool;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;

public class BenchmarkRunner {

    private static final String MERGE_SORT = "MergeSort";
    private static final String FIBONACCI = "Fibonacci";
    private static final String PRIME_COUNTER = "PrimeCounter";
    private static final String ARRAY_TRANSFORM = "ArrayTransform";

    public long[] run(String task, int nThreads) {
        return switch (task) {
            case MERGE_SORT -> runMergeSortBench(nThreads);
            case FIBONACCI -> runFibonacciBench(nThreads);
            case PRIME_COUNTER -> runPrimeCounterBench(nThreads);
            case ARRAY_TRANSFORM -> runArrayTransform(nThreads);
            default -> throw new IllegalArgumentException("Cannot identify \"task\"");
        };
    }

    public static boolean isSorted(int[] arr) {
        boolean sorted = true;
        for(int i = 1; i < arr.length; i++) {
            if(arr[i] < arr[i - 1]) {
                sorted = false;
                break;
            }
        }
        return sorted;
    }

    public long[] runMergeSortBench(int nThreads) {
        long[] time = new long[2];
        WSPool pool = new WSPool(nThreads);
        int N = 1000000;
        int[] array = new Random().ints(N).toArray();
        pool.start();

        MergeSort task = new MergeSort(array, 0, array.length);
        long start = System.currentTimeMillis();
        pool.submit(task);
        task.get();
        time[0] = System.currentTimeMillis() - start;
        assert (isSorted(task.getArray()));
        try {
            pool.shutdown();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        // Running against Java's Fork Join Pool implementation
        array = new Random().ints(N).toArray();
        MergeSortTask controlTask = new MergeSortTask(array, 0, array.length);
        ForkJoinPool forkJoinPool = (ForkJoinPool) Executors.newWorkStealingPool(nThreads);

        start = System.currentTimeMillis();
        forkJoinPool.invoke(controlTask);
        time[1] = System.currentTimeMillis() - start;

        forkJoinPool.shutdown();

        return time;
    }

    public long[] runPrimeCounterBench(int nThreads) {
        long[] time = new long[2];
        WSPool pool = new WSPool(nThreads);
        int low = 10, high = 1000000;
        pool.start();
        PrimeCounter task = new PrimeCounter(low, high);

        long start = System.currentTimeMillis();
        pool.submit(task);
        task.get();
        time[0] = System.currentTimeMillis() - start;

        try {
            pool.shutdown();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }


        PrimeCounterTask controlTask = new PrimeCounterTask(low, high);
        ForkJoinPool forkJoinPool = (ForkJoinPool) Executors.newWorkStealingPool(nThreads);

        start = System.currentTimeMillis();
        forkJoinPool.invoke(controlTask);
        time[1] = System.currentTimeMillis() - start;

        forkJoinPool.shutdown();

        return time;
    }


    public static double[] getDoubleArray(int n) {
        double[] arr = new double[n];
        Random r = new Random();
        for(int i = 0; i < n; i++) {
            arr[i] = r.nextDouble();
        }
        return arr;
    }

    public long[] runArrayTransform(int nThreads) {
        long[] time = new long[2];
        WSPool pool = new WSPool(nThreads);
        int N = 10000000;
        int scale = 10;
        pool.start();
        double[] array = getDoubleArray(N);
        ArrayTransform task = new ArrayTransform(array, scale, 0, array.length);

        long start = System.currentTimeMillis();
        pool.submit(task);
        task.get();
        time[0] = System.currentTimeMillis() - start;

        try {
            pool.shutdown();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        array = getDoubleArray(N);
        ArrayTransformTask controlTask = new ArrayTransformTask(array, scale, 0, array.length);
        ForkJoinPool forkJoinPool = (ForkJoinPool) Executors.newWorkStealingPool(nThreads);

        start = System.currentTimeMillis();
        forkJoinPool.invoke(controlTask);
        time[1] = System.currentTimeMillis() - start;

        forkJoinPool.shutdown();

        return time;
    }

    public long[] runFibonacciBench(int nThreads) {
        long[] time = new long[2];
        WSPool pool = new WSPool(nThreads);
        int N = 25;
        pool.start();
        Fibonacci task = new Fibonacci(N);

        long start = System.currentTimeMillis();
        pool.submit(task);
        task.get();
        time[0] = System.currentTimeMillis() - start;

        try {
            pool.shutdown();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }


        FibonacciTask controlTask = new FibonacciTask(N);
        ForkJoinPool forkJoinPool = (ForkJoinPool) Executors.newWorkStealingPool(nThreads);

        start = System.currentTimeMillis();
        forkJoinPool.invoke(controlTask);
        time[1] = System.currentTimeMillis() - start;

        forkJoinPool.shutdown();

        return time;
    }
}
