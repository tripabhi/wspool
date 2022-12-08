package pool;

public abstract class RTask<V> extends FJTask<V> {

    public RTask() {}

    V result;

    protected abstract V compute();

    public final V getResult() {
        return result;
    }

    protected final boolean exec() {
        result = compute();
        return true;
    }

}
