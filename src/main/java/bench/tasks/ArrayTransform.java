package bench.tasks;

import pool.RAction;

public class ArrayTransform extends RAction {

    private final double[] array;
    private final int scale;
    private final int start;
    private final int end;
    private final int threshold = 1000;

    public ArrayTransform(double[] array, int scale, int start, int end) {
        this.array = array;
        this.scale = scale;
        this.start = start;
        this.end = end;
    }

    @Override
    protected void compute() {
        if(end - start < threshold) {
            scaleDirectly();
        } else {
            int mid = start + ((end - start) >> 1);

            ArrayTransform left = new ArrayTransform(array, scale, start, mid);
            ArrayTransform right = new ArrayTransform(array, scale, mid, end);

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
