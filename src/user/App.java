package user;

import thread.MyThreadPoolService;

public class App {

    public static void main(String[] args) throws InterruptedException {
        MyThreadPoolService pool = new MyThreadPoolService(1, 100);

        pool.execute(() -> {
            extracted();
        });

        pool.execute(() -> {
            extracted();
        });
        pool.execute(() -> {
            extracted();
        });
        pool.execute(() -> {
            extracted();
        });
        pool.execute(() -> {
            extracted();
        });
        pool.execute(() -> {
            extracted();
        });
        pool.execute(() -> {
            extracted();
        });
        Thread.sleep(1000);

        pool.execute(() -> {
            extracted();
        });
        pool.execute(() -> {
            extracted();
        });


        Thread.sleep(1000);

        pool.execute(() -> {
            extracted();
        });
        pool.execute(() -> {
            extracted();
        });
        pool.execute(() -> {
            extracted();
        });
        pool.execute(() -> {
            extracted();
        });



        try {
            Thread.sleep(70000); // 70초 대기
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        pool.shutdown();
    }

    private static void extracted() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Task is finished " + Thread.currentThread().getName());
    }
}
