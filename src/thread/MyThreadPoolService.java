package thread;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class MyThreadPoolService {
    private final BlockingQueue<Runnable> taskQueue;
    private final CopyOnWriteArrayList<WorkerThread> activeThreadList;
    private final AtomicBoolean isStopped = new AtomicBoolean(false);
    private final int maxThreads;

    public MyThreadPoolService(int maxThreads) {
        this(maxThreads, false);
    }

    public MyThreadPoolService(int maxThreads, boolean useWatchDog) {
        this.taskQueue = new LinkedBlockingQueue<>();
        this.activeThreadList = new CopyOnWriteArrayList<>();
        this.maxThreads = maxThreads;
        if (useWatchDog) {
            startWatchDogThread();
        }
    }

    public void execute(Runnable task) {
        if (isStopped.get()) {
            throw new IllegalStateException("Thread pool is stopped");
        }
        if (activeThreadList.size() < maxThreads) {
            generateWorkerThread();
        }
        taskQueue.offer(task);
    }

    public void shutdown() {
        isStopped.set(true);
        for (WorkerThread worker : activeThreadList) {
            worker.stopWorker();
        }
    }

    private void generateWorkerThread() {
        WorkerThread worker = new WorkerThread(taskQueue);
        activeThreadList.add(worker);
        worker.start();
    }

    private void startWatchDogThread() {
        new Thread(() -> {
            while (!isStopped.get()) {
                activeThreadList.removeIf(workerThread -> !workerThread.isAlive());

                if (!taskQueue.isEmpty() && activeThreadList.size() < maxThreads) {
                    generateWorkerThread();
                    continue;
                }

                sleep(1000);
            }
        }).start();
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
