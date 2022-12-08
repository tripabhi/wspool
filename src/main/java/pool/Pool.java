package pool;

public interface Pool {

    FJTask<?> submit(FJTask<?> task);
    boolean shutdown() throws InterruptedException;
}
