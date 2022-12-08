package pool;

public abstract class RAction extends FJTask<Void> {

    public RAction() {}

    protected abstract void compute();

    public final Void getResult() {
        return null;
    }

    protected final boolean exec() {
        compute();
        return true;
    }
}
