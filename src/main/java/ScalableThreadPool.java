import java.util.ArrayDeque;
import java.util.Deque;

public class ScalableThreadPool implements ThreadPool{
    private final int max;
    private final int min;
    private final ThreadCountWatcher threadCount;
    private final Deque<Runnable> taskQueue;
    private final Deque<Thread> threadQueue;

    public static void main(String[] args) {
        ScalableThreadPool threadPool = new ScalableThreadPool(2, 5);

        for (int i = 0; i < 10; ++i) {
            threadPool.execute(() -> {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignore) {
                }
                System.out.println("Thread executed");
            });
        }
        threadPool.start();
    }

    ScalableThreadPool(int minThreadCount, int maxThreadCount) {
        this.max = maxThreadCount;
        this.min = minThreadCount;
        threadCount = new ThreadCountWatcher();
        taskQueue = new ArrayDeque<>();
        threadQueue = new ArrayDeque<>();
    }

    public void stop() {
        for (Thread thread : threadQueue) {
            thread.interrupt();
        }
    }

    private Thread getStandardThread() {

        return new Thread(() -> {
            System.out.println(Thread.currentThread().getName() + " standart");
            while(true) {
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
    }

    private Thread getAuxiliaryThread() {

        return new Thread(() -> {
            System.out.println(Thread.currentThread().getName() + " auxiliary");
            while(true) {
                Runnable task;
                synchronized (taskQueue) {
                    if (taskQueue.isEmpty()) {
                        synchronized (threadCount) {
                            threadCount.decreaseValue();
                        }
                        return;
                    }
                    task = taskQueue.pop();
                    taskQueue.notifyAll();
                }
                task.run();
            }
        });
    }

    @Override
    public void start() {
        for (int i = 0; i < min; i++) {
            Thread thread = getStandardThread();
            threadQueue.add(thread);
            synchronized (threadCount) {
                threadCount.increaseValue();
            }
            thread.start();
            //если тут не подождать, то очередь задач всегда обрабатывается максимальным количеством потоков
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        while (true) {
            synchronized (taskQueue) {
                while (taskQueue.isEmpty()) {
                    try {
                        taskQueue.wait();
                    } catch (InterruptedException ignored) {
                    }
                }

                int threadNumber;
                synchronized (threadCount) {
                    threadNumber = threadCount.getValue();
                }

                if (threadNumber < max) {
                    Thread thread = getAuxiliaryThread();
                    synchronized (threadCount) {
                        threadCount.increaseValue();
                    }
                    thread.start();
                }
            }
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
