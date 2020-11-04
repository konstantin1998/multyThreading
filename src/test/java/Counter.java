public class Counter {
    private int n = 0;

    public synchronized void inc() {
        n++;
    }

    public int getN() {
        return n;
    }
}
