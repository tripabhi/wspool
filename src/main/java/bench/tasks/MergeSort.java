package bench.tasks;

import pool.RAction;
import pool.WSPool;

import java.util.Arrays;
import java.util.Random;

public class MergeSort extends RAction {

    private final int[] array;
    private final int left;
    private final int right;

    public MergeSort(int[] array, int left, int right) {
        this.array = array;
        this.left = left;
        this.right = right;
    }

    @Override
    protected void compute() {
        if(this.right - this.left > 16) {
            int mid = this.left + ((this.right - this.left) >> 1);
            MergeSort left = new MergeSort(array, this.left, mid);
            MergeSort right = new MergeSort(array, mid, this.right);
            invokeAll(left, right);
            merge(mid);
        } else {
            Arrays.sort(array, left, right);
        }
    }

    private void merge(int mid) {
        int[] copy = new int[this.right - this.left];

        System.arraycopy(array, this.left, copy, 0, copy.length);

        int cLow = 0;
        int cHigh = this.right - this.left;
        int cMid = mid - this.left;

        for (int i = this.left, p = cLow, q = cMid; i < this.right; i++) {
            if (q >= cHigh || (p < cMid && copy[p] < copy[q])) {
                array[i] = copy[p++];
            } else {
                array[i] = copy[q++];
            }
        }
    }

    public int[] getArray() {
        return this.array;
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

    public static void main(String[] args) throws InterruptedException {
        WSPool pool = new WSPool(4);
        int n = 100000;
        int[] arr = new Random().ints(n).toArray();
        MergeSort m = new MergeSort(arr, 0 , arr.length);
        pool.start();
        long start = System.currentTimeMillis();
        pool.submit(m);
        System.out.println("Task submitted");



        m.get();
        System.out.println("Time taken " + (System.currentTimeMillis() - start));
        System.out.println("Sorted Array !");
        System.out.println("Is sorted ? " + isSorted(m.getArray()));

        pool.shutdown();
    }
}
