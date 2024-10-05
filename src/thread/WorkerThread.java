package thread;


import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

class WorkerThread extends Thread {
    private final BlockingQueue<WorkerThread> idleThreads;
    private final BlockingQueue<Runnable> taskQueue;
    private boolean isStopped = false;

    public WorkerThread(BlockingQueue<WorkerThread> idleThreads) {
        this.idleThreads = idleThreads;
        this.taskQueue = new LinkedBlockingQueue<>(1);
    }

    public void assignTask(Runnable task) {
        try {
            taskQueue.put(task);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void run() {
        while (!isStopped) {
            try {
                Runnable task = taskQueue.poll(60, TimeUnit.SECONDS);
                if (task != null) {
                    task.run();
                    idleThreads.offer(this);
                } else {
                    stopWorker();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public void stopWorker() {
        isStopped = true;
        this.interrupt();
    }
}
