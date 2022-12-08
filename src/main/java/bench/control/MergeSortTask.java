package bench.control;


import java.util.Arrays;
import java.util.concurrent.RecursiveAction;

public class MergeSortTask extends RecursiveAction {

    private final int[] array;
    private final int left;
    private final int right;

    public MergeSortTask(int[] array, int left, int right) {
        this.array = array;
        this.left = left;
        this.right = right;
    }

    @Override
    protected void compute() {
        if(this.right - this.left > 16) {
            int mid = this.left + ((this.right - this.left) >> 1);
            MergeSortTask left = new MergeSortTask(array, this.left, mid);
            MergeSortTask right = new MergeSortTask(array, mid, this.right);
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

    private static boolean isSorted(int[] arr) {
        boolean sorted = true;
        for(int i = 1; i < arr.length; i++) {
            if(arr[i] < arr[i - 1]) {
                sorted = false;
                break;
            }
        }
        return sorted;
    }
}
