public class ThreadCountWatcher {
    private int threadCount;

    ThreadCountWatcher() {
        threadCount = 0;
    }

    public void increaseValue() {
        threadCount ++;
    }

    public void decreaseValue() {
        threadCount --;
    }

    public int getValue() {
        return threadCount;
    }
}
