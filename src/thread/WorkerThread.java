package thread;


import java.util.concurrent.BlockingQueue;

class WorkerThread extends Thread {
    private final BlockingQueue<Runnable> taskQueue;
    private final BlockingQueue<WorkerThread> idleThreads;
    private boolean isStopped = false;

    public WorkerThread(BlockingQueue<Runnable> taskQueue, BlockingQueue<WorkerThread> idleThreads) {
        this.taskQueue = taskQueue;
        this.idleThreads = idleThreads;
    }

    @Override
    public void run() {
        while (!isStopped) {
            try {
                Runnable task = taskQueue.poll(); // 큐에서 작업을 가져옴

                if (task != null) {
                    task.run();
                }

                // 유휴 상태로 돌아가 대기
                idleThreads.offer(this);

                // 작업이 없으면 대기 상태로 유지
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // 인터럽트 처리
            }
        }
    }

    public void stopWorker() {
        isStopped = true;
        this.interrupt(); // 인터럽트를 통해 종료
    }
}
