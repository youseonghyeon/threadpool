package thread;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class CachedThreadPool {
    private final BlockingQueue<Runnable> taskQueue;
    private final BlockingQueue<WorkerThread> idleThreads;
    private final AtomicBoolean isStopped = new AtomicBoolean(false);
    private final AtomicInteger currentThreadCount = new AtomicInteger(0);
    private final int maxThreads;

    public CachedThreadPool(int maxThreads) {
        this.taskQueue = new LinkedBlockingQueue<>();
        this.idleThreads = new LinkedBlockingQueue<>();
        this.maxThreads = maxThreads;
    }

    public void execute(Runnable task) {
        if (isStopped.get()) {
            throw new IllegalStateException("Thread pool is stopped");
        }

        WorkerThread worker = idleThreads.poll();

        if (worker == null && currentThreadCount.get() < maxThreads) {
            worker = new WorkerThread(taskQueue, idleThreads);
            worker.start();
            currentThreadCount.incrementAndGet();
        }

        // 작업을 큐에 추가
        taskQueue.offer(task);
    }

    public void shutdown() {
        isStopped.set(true);
        for (WorkerThread worker : idleThreads) {
            worker.stopWorker();
        }
    }
}
