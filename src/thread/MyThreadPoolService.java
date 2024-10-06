package thread;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class MyThreadPoolService {
    private final int minThreads;
    private final int maxThreads;
    private final BlockingQueue<Runnable> taskQueue;
    private final List<WorkerThread> activeThreadList;
    private final AtomicBoolean isStopped = new AtomicBoolean(false);

    private final int threadTimeout = 10000;

    public MyThreadPoolService(int minThreads, int maxThreads) {
        this.minThreads = minThreads;
        this.maxThreads = maxThreads;
        this.taskQueue = new LinkedBlockingQueue<>();
        this.activeThreadList = Collections.synchronizedList(new ArrayList<>());
        startWatchDogThread();
    }

    public void execute(Runnable task) {
        if (isStopped.get()) {
            throw new IllegalStateException("Thread pool is stopped");
        }
        if (!taskQueue.isEmpty() && activeThreadList.size() < maxThreads) {
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
                removeDeadThreads();
                removeTimedOutThread();
                maintainMinThreads();
                sleep(5000);
            }
        }).start();
    }

    private void removeDeadThreads() {
        activeThreadList.removeIf(workerThread -> !workerThread.isAlive());
    }

    private void removeTimedOutThread() {
        if (activeThreadList.size() > minThreads) {
            Optional<WorkerThread> targetThread = activeThreadList.stream()
                    .filter(this::timeOver)
                    .findFirst();
            targetThread.ifPresent(workerThread -> {
                workerThread.stopWorker();
                activeThreadList.remove(workerThread);
            });
        }
    }

    private void maintainMinThreads() {
        if (activeThreadList.size() < minThreads) {
            int currentThreadSize = activeThreadList.size();
            for (int i = currentThreadSize; i < minThreads; i++) {
                generateWorkerThread();
            }
        }
    }


    private boolean timeOver(WorkerThread workerThread) {
        long lastStartTime = workerThread.getLastStartTime();
        return System.currentTimeMillis() - lastStartTime > threadTimeout;
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
