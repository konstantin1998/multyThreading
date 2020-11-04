import java.util.ArrayDeque;
import java.util.Deque;

public class ScalableThreadPool implements ThreadPool{
    private final int max;
    private final int min;
    private final Deque<Runnable> taskQueue;
    private final Deque<Thread> threadQueue;

    ScalableThreadPool(int minThreadCount, int maxThreadCount) {
        this.max = maxThreadCount;
        this.min = minThreadCount;
        taskQueue = new ArrayDeque<>();
        threadQueue = new ArrayDeque<>();
    }

    public void stop() {
        for (Thread thread : threadQueue) {
            thread.interrupt();
        }
    }

    @Override
    public void start() {
        for (int i = 0; i < min; i++) {
            Thread thread = new Thread(() -> {
                while(!Thread.currentThread().isInterrupted()) {
                    Runnable task;
                    synchronized (taskQueue) {
                        while (taskQueue.isEmpty()) {
                            try {
                                taskQueue.wait();
                            } catch (InterruptedException ignored) {

                            }
                        }
                        task = taskQueue.pop();
                        taskQueue.notifyAll();
                    }
                    task.run();
                }
            });
            threadQueue.add(thread);
            thread.start();
        }
    }

    @Override
    public void execute(Runnable runnable) {
        synchronized (taskQueue) {
            taskQueue.push(runnable);
            taskQueue.notifyAll();
        }
    }
}
