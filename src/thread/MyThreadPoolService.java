package thread;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class MyThreadPoolService {
    // minThreads와 maxThreads는 최소 스레드 개수와 최대 스레드 개수를 의미합니다.
    private final int minThreads;
    private final int maxThreads;
    // taskQueue는 작업을 담는 큐입니다.
    private final BlockingQueue<Runnable> taskQueue;
    // activeThreadList는 현재 활성화된 스레드를 관리하는 리스트입니다.
    private final List<WorkerThread> activeThreadList;
    // isStopped는 스레드 풀이 종료되었는지 여부를 나타내는 플래그입니다.
    private final AtomicBoolean isStopped = new AtomicBoolean(false);

    // 스레드의 타임아웃 시간을 나타내는 상수입니다.
    private final int threadTimeout = 10000;

    /**
     * MyThreadPoolService 생성자입니다.
     * 생성자 실행 시 WatchDog 쓰레드가 시작됩니다.
     */
    public MyThreadPoolService(int minThreads, int maxThreads) {
        if (minThreads < 1 || maxThreads < 1 || minThreads > maxThreads) {
            throw new IllegalArgumentException("Invalid thread pool size");
        }
        this.minThreads = minThreads;
        this.maxThreads = maxThreads;
        this.taskQueue = new LinkedBlockingQueue<>();
        this.activeThreadList = Collections.synchronizedList(new ArrayList<>());
        startWatchDogThread();
    }

    /**
     * 스레드 풀에 작업을 추가합니다.
     * 스레드 풀이 종료된 경우 IllegalStateException을 발생시킵니다.
     */
    public void execute(Runnable task) {
        if (isStopped.get()) {
            throw new IllegalStateException("Thread pool is stopped");
        }
        if (!taskQueue.isEmpty() && activeThreadList.size() < maxThreads) {
            generateWorkerThread();
        }
        taskQueue.offer(task);
    }

    /**
     * 스레드 풀을 종료합니다.
     */
    public void shutdown() {
        isStopped.set(true);
        for (WorkerThread worker : activeThreadList) {
            worker.stopWorker();
        }
    }

    /**
     * 새로운 쓰레드를 생성하고 activeThreadList에 추가합니다.
     */
    private void generateWorkerThread() {
        WorkerThread worker = new WorkerThread(taskQueue);
        activeThreadList.add(worker);
        worker.start();
    }

    /**
     * WatchDog 쓰레드를 시작합니다.
     */
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

    /**
     * 의도하지 않게 종료된 스레드를 activeThreadList 에서 제거합니다.
     */
    private void removeDeadThreads() {
        activeThreadList.removeIf(workerThread -> !workerThread.isAlive());
    }

    /**
     * 타임아웃된 스레드를 한개씩 activeThreadList에서 제거합니다.
     */
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

    /**
     * 스레드가 타임아웃되었는지 확인합니다.
     */
    private boolean timeOver(WorkerThread workerThread) {
        long lastStartTime = workerThread.getLastStartTime();
        return System.currentTimeMillis() - lastStartTime > threadTimeout;
    }


    /**
     * 최소 스레드 개수를 유지합니다.
     * <p>
     *     taskQueue에 작업이 있으면서, 쓰레드가 모두 종료된 경우 작업을 실행하지 못하는데 이를 방지합니다.
     * </p>
     */
    private void maintainMinThreads() {
        if (activeThreadList.size() < minThreads) {
            int currentThreadSize = activeThreadList.size();
            for (int i = currentThreadSize; i < minThreads; i++) {
                generateWorkerThread();
            }
        }
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
