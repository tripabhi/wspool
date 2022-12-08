package bench.runner;

import bench.control.MergeSortTask;
import bench.control.PrimeCounterTask;
import bench.tasks.MergeSort;
import bench.tasks.PrimeCounter;
import jdk.jshell.spi.ExecutionControl;
import pool.WSPool;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;

public class BenchmarkRunner {

    private static final String MERGE_SORT = "MergeSort";
    private static final String FIBONACCI = "Fibonacci";
    private static final String PRIME_COUNTER = "PrimeCounter";

    public void run(String task, int nThreads) {
        switch (task) {
            case MERGE_SORT -> runMergeSortBench(nThreads);
            case PRIME_COUNTER -> runPrimeCounterBench(nThreads);
            default -> throw new IllegalArgumentException("Cannot identify \"task\"");
        }
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

    public void runMergeSortBench(int nThreads) {
        WSPool pool = new WSPool(nThreads);
        int N = 1000000;
        int[] array = new Random().ints(N).toArray();

        MergeSort task = new MergeSort(array, 0, array.length);
        long start = System.currentTimeMillis();
        pool.submit(task);
        task.get();
        System.out.println("T " + (System.currentTimeMillis() - start));
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
        System.out.println("CT " + (System.currentTimeMillis() - start));

        forkJoinPool.shutdown();
    }

    public void runPrimeCounterBench(int nThreads) {
        WSPool pool = new WSPool(nThreads);
        int low = 10, high = 1000000;
        PrimeCounter task = new PrimeCounter(low, high);

        long start = System.currentTimeMillis();
        pool.submit(task);
        task.get();
        System.out.println("T " + (System.currentTimeMillis() - start));

        try {
            pool.shutdown();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }


        PrimeCounterTask controlTask = new PrimeCounterTask(low, high);
        ForkJoinPool forkJoinPool = (ForkJoinPool) Executors.newWorkStealingPool(nThreads);

        start = System.currentTimeMillis();
        forkJoinPool.invoke(controlTask);
        System.out.println("CT " + (System.currentTimeMillis() - start));

        forkJoinPool.shutdown();
    }

}
