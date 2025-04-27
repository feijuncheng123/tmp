import java.util.concurrent.*;

public class lesson_juc_02_callable {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        /**
         * runnable: 无返回值接口
         * callable：有返回值接口
         * FutureTask继承了runnable类，但是可以传入callable对象
         */


        FutureTask<String> stringFutureTask = new FutureTask<>(() -> {
            return "12345";
        });

        new Thread(stringFutureTask,"thread01").start(); //需要放到线程中执行

        stringFutureTask.get();
    }


    /**
     * CountDownLatch线程执行计数器，每次执行减1，等到0时，countDownLatch.wait语句才会向下继续执行
     * @throws InterruptedException
     */
    public void countDownLatchTest() throws InterruptedException {

        CountDownLatch countDownLatch = new CountDownLatch(6);
        for(int i=0;i<6;i++){
            new Thread(()->{
                System.out.println(Thread.currentThread().getName());
                countDownLatch.countDown();
            },String.valueOf(i));
        }

        countDownLatch.wait();
        System.out.println("over!");
    }

    /**
     * CyclicBarrier:当线程执行数达到CyclicBarrier设定的具体数值时，会执行CyclicBarrier中的回调函数，同时CyclicBarrier的await会继续往下执行。
     */
    public void cyclicBarrierTest(){
        CyclicBarrier cyclicBarrier = new CyclicBarrier(8, () -> {
            System.out.println("执行数达到8");
        });


        for(int i=0;i<=7;i++){
            new Thread(()->{
                System.out.println(Thread.currentThread().getName());
                try {
                    cyclicBarrier.await();
                    System.out.println(Thread.currentThread().getName()+"come on!");
                } catch (InterruptedException | BrokenBarrierException e) {
                    e.printStackTrace();
                }
            },String.valueOf(i));
        }

    }


    /**
     * Semaphore:设定令牌，线程抢到令牌才能执行
     */
    public void semaphoreTest(){
        Semaphore semaphore = new Semaphore(5);

        for(int i=0;i<=7;i++){
            new Thread(()->{
                try {
                    semaphore.acquire();
                    System.out.println(Thread.currentThread().getName());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }finally {
                    semaphore.release();
                }

            },String.valueOf(i));
        }


        System.out.println(semaphore.availablePermits());

    }
}
