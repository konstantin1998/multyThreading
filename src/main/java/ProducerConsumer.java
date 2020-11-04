import java.util.ArrayDeque;
import java.util.Deque;

public class ProducerConsumer {
    private final static int min = 3;
    private final static int max = 5;
    private final static Deque<Runnable> taskQueue = new ArrayDeque<Runnable>();
    private final static Deque<Thread> threadQueue = new ArrayDeque<Thread>();

    private static class ProducerThread extends Thread {

        private Runnable getTask() {
            while (true) {
                synchronized (taskQueue) {
                    while (taskQueue.isEmpty()) {
                        try {
                            taskQueue.wait();
                        } catch (InterruptedException ignore) {
                        }
                    }
                    Runnable task = taskQueue.removeFirst();
                    taskQueue.notifyAll();
                    return task;
                }
            }
        }

        public void run() {
            while(true) {

                while(true) {
                    synchronized (threadQueue) {
                        while (threadQueue.size() >= max) {
                            try {
                                threadQueue.wait();
                            } catch (InterruptedException ignore) {

                            }
                        }

                        Runnable task;
                        if (threadQueue.size() < min) {
                            task = () -> {
                                while (!Thread.currentThread().isInterrupted()) {
                                    Runnable runnable;
                                    synchronized (taskQueue) {
                                        while (taskQueue.isEmpty()) {
                                            try {
                                                taskQueue.wait();
                                            } catch (InterruptedException ignored) {

                                            }
                                        }
                                        runnable = taskQueue.pop();
                                        taskQueue.notifyAll();
                                    }
                                    runnable.run();
                                }
                            };
                        } else {
                            task = getTask();
                        }

                        threadQueue.push(new Thread(task));
                        threadQueue.notifyAll();
                        break;
                    }
                }

            }
        }
    }

    private static class Consumer extends Thread {
        public void run() {
            while(true) {
                Thread thread = getThread();
                thread.start();
            }
        }

        private Thread getThread() {
            while (true) {
                synchronized (threadQueue) {
                    while (threadQueue.size() <= min) {
                        try {
                            threadQueue.wait();
                        } catch (InterruptedException ignore) {
                        }
                    }
                    Thread thread = threadQueue.pop();
                    threadQueue.notifyAll();
                    return thread;
                }
            }
        }


    }

    public static void main(String[] args) {
        new ProducerThread().start();
        new Consumer().start();
    }
}
