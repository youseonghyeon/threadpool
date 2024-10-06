package thread;

import java.util.concurrent.BlockingQueue;

class WorkerThread extends Thread {
    // taskQueue는 작업을 담는 큐입니다. 부모 클래스인 MyThreadPoolService에서 초기화하며 공유합니다.
    private final BlockingQueue<Runnable> taskQueue;
    // isStopped는 스레드가 종료되었는지 여부를 나타내는 플래그입니다.
    private boolean isStopped = false;
    // lastStartTime은 마지막으로 작업을 수행한 시간을 나타냅니다.
    private long lastStartTime;

    protected WorkerThread(BlockingQueue<Runnable> taskQueue) {
        this.taskQueue = taskQueue;
    }

    @Override
    public void run() {
        while (!isStopped) {
            try {
                lastStartTime = System.currentTimeMillis();
                // 블로킹 큐에서 작업을 실행하므로 따로 time sleep을 사용하지 않습니다.
                taskQueue.take().run();
            } catch (InterruptedException e) {
                System.out.println("[WARN] Thread interrupted " + Thread.currentThread().getName());
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    protected long getLastStartTime() {
        return lastStartTime;
    }

    protected void stopWorker() {
        isStopped = true;
        this.interrupt(); // 인터럽트를 통해 종료
    }
}
