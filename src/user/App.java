package user;

import thread.MyThreadPoolService;

public class App {

    public static void main(String[] args) throws InterruptedException {
        MyThreadPoolService pool = new MyThreadPoolService(10, true);

        for (int i = 0; i < 1000; i++) {
            int taskId = i;
            pool.execute(() -> {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                System.out.println("Task " + taskId + " is finished " + Thread.currentThread().getName());
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
