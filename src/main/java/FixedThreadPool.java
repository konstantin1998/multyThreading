import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;

public class FixedThreadPool implements ThreadPool{
    public static void main(String[] args) {
        FixedThreadPool fixedThreadPool = new FixedThreadPool(2);

        fixedThreadPool.start();
        for (int i = 0; i < 10; ++i) {
            fixedThreadPool.execute(() -> {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignore) {
                }
                System.out.println("Thread executed");
            });
        }
//        try {
//            Thread.sleep(5000);
//        } catch (InterruptedException ignore) {
//        }
//        fixedThreadPool.stop();
    }
    private final int maxThreadCount;
    private final Deque<Runnable> queue;
    private final ArrayList<Thread> threads;

    FixedThreadPool(int maxThreadCount) {
        this.maxThreadCount = maxThreadCount;
        queue = new ArrayDeque<>();
        threads = new ArrayList<>();
    }

    public void stop() {
        for (Thread thread : threads) {
            thread.interrupt();
        }
    }

    @Override
    public void start() {
        for (int i = 0; i < maxThreadCount; i++) {
            Thread thread = new Thread(() -> {
                while(!Thread.currentThread().isInterrupted()) {

                    Runnable task;
                    synchronized (queue) {

                        while (queue.isEmpty()) {
                            try {
                                queue.wait();
                            } catch (InterruptedException e) {
                                System.out.println("interrupted");
                            }
                        }
                        task = queue.pop();
                    }
                    task.run();
                }
            });
            threads.add(thread);
            thread.start();
        }
    }

    @Override
    public void execute(Runnable runnable) {
        synchronized (queue) {
            queue.push(runnable);
            queue.notify();
        }
    }
}
