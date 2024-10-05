package user;

import thread.CachedThreadPool;

public class App {

    public static void main(String[] args) {
        CachedThreadPool pool = new CachedThreadPool();

        for (int i = 0; i < 1000; i++) {
            int taskId = i;
            pool.execute(() -> {
                System.out.println("Task " + taskId + " is being executed by " + Thread.currentThread().getName());
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        try {
            Thread.sleep(70000); // 70초 대기
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        pool.shutdown();
    }
}
