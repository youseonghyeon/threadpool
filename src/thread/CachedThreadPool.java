package thread;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class CachedThreadPool {
    private final BlockingQueue<WorkerThread> idleThreads;
    private final AtomicBoolean isStopped = new AtomicBoolean(false);

    public CachedThreadPool() {
        idleThreads = new LinkedBlockingQueue<>();
    }

    public void execute(Runnable task) {
        if (isStopped.get()) {
            throw new IllegalStateException("Thread pool is stopped");
        }

        WorkerThread worker = idleThreads.poll();
        if (worker == null) {
            worker = new WorkerThread(idleThreads);
            worker.start();
        }
        worker.assignTask(task);
    }

    public void shutdown() {
        isStopped.set(true);
        for (WorkerThread worker : idleThreads) {
            worker.stopWorker();
        }
    }
}
