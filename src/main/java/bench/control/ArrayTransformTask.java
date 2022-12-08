package bench.control;

import java.util.concurrent.RecursiveAction;

public class ArrayTransformTask extends RecursiveAction {
    private final double[] array;
    private final int scale;
    private final int start;
    private final int end;

    public ArrayTransformTask(double[] array, int scale, int start, int end) {
        this.array = array;
        this.scale = scale;
        this.start = start;
        this.end = end;
    }

    @Override
    protected void compute() {
        int threshold = 1000;
        if(end - start < threshold) {
            scaleDirectly();
        } else {
            int mid = start + ((end - start) >> 1);

            ArrayTransformTask left = new ArrayTransformTask(array, scale, start, mid);
            ArrayTransformTask right = new ArrayTransformTask(array, scale, mid, end);

            invokeAll(left, right);
        }
    }


    private void scaleDirectly() {
        for(int i = start; i < end; i++) {
            array[i] = array[i] * scale;
        }
    }

    public double[] getArray() {
        return array;
    }
}
