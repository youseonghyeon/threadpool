package thread;

import java.util.concurrent.BlockingQueue;

class WorkerThread extends Thread {
    private final BlockingQueue<Runnable> taskQueue;
    private boolean isStopped = false;

    public WorkerThread(BlockingQueue<Runnable> taskQueue) {
        this.taskQueue = taskQueue;
    }

    @Override
    public void run() {
        while (!isStopped) {
            try {
                taskQueue.take().run();
            } catch (InterruptedException e) {
                System.out.println("[WARN] Thread interrupted " + Thread.currentThread().getName());
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    public void stopWorker() {
        isStopped = true;
        this.interrupt(); // 인터럽트를 통해 종료
    }
}
