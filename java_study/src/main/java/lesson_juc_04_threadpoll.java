import java.util.concurrent.*;

public class lesson_juc_04_threadpoll {
    public static void main(String[] args) {

        /**
         * 1、线程池优点：
         * （1）避免了在处理短时间任务时创建与销毁线程的代价。线程池不仅能够保证内核的充分利用，还能防止过分调度。
         *
         * 2、线程池的种类与创建：
         *（1）newCachedThreadPool(常用)：创建一个可缓存线程池，如果线程池长度超过处理需要，可灵活回收空闲线程，若无可回收，则新建线程.
         *   特点：
         *     • 线程池中数量没有固定（遇强则强），可达到最大值(Interger. MAX_VALUE)
         *     • 线程池中的线程可进行缓存重复利用和回收(回收默认时间为 1 分钟)
         *     • 当线程池中，没有可用线程，会重新创建一个线程
         *
         * (2)newFixedThreadPool(常用):可重用固定线程数的线程池，以共享的无界队列方式来运行这些线程
         *  特点：
         *      • 线程池中的线程处于一定的量，可以很好的控制线程的并发量
         *      • 线程可以重复被使用，在显示关闭之前，都将一直存在
         *      • 超出一定量的线程被提交时候需在队列中等待
         *
         * (3)newSingleThreadExecutor(常用)
         *  特点：
         *      • 线程池中最多执行 1 个线程，之后提交的线程活动将会排在队列中以此执行
         *
         * （4）newScheduleThreadPool(了解)：线程池支持定时以及周期性执行任务，创建一个 corePoolSize 为传入参数，最大线程数为整形的最大数的线程池**
         *  特点：
         *      • 线程池中具有指定数量的线程，即便是空线程也将保留
         *      • 可定时或者延迟执行线程活动
         *
         * （5）newWorkStealingPool：
         *      jdk1.8 提供的线程池，底层使用的是 ForkJoinPool 实现，创建一个拥有多个任务队列的线程池，可以减少连接数，创建当前可用 cpu 核数的线程来并行执行任务
         *
         *
         * 3、自定义线程池：通过传入ThreadPoolExecutor类7个重要参数实现：
         *  • corePoolSize 线程池的核心线程数
         *  • maximumPoolSize 能容纳的最大线程数
         *  • keepAliveTime 空闲线程存活时间
         *  • unit 存活的时间单位
         *  • workQueue 存放提交但未执行任务的队列
         *  • threadFactory 创建线程的工厂类
         *  • handler 等待队列满后的拒绝策略
         *
         * （1）流程：当提交任务数大于 corePoolSize 的时候，会优先将任务放到 workQueue 阻塞队列中。当阻塞队列饱和后，会扩充线程池中线程数，
         *      直到达到maximumPoolSize 最大线程数配置。此时，再多余的任务，则会触发线程池 的拒绝策略
         *
         * （2）拒绝策略（重点）：
         *     一、CallerRunsPolicy：被拒绝后由提交者线程执行
         *     二、AbortPolicy：丢弃任务，并抛出拒绝执行 RejectedExecutionException 异常信息。线程池默认的拒绝策略。必须处理好抛出的异常，否则会打断当前的执行流程，影响后续的任务执行。
         *     三、DiscardPolicy: 直接丢弃，其他啥都没有
         *     四、DiscardOldestPolicy: 当触发拒绝策略，只要线程池没有关闭的话，丢弃阻塞队列 workQueue 中最老的一个任务，并将新任务加入
         *
         * 4、阿里巴巴规范：不允许使用Executors.new的方式来创建线程池，比如使用ThreadPoolExecutor自定义创建。
         * 原因：
         *  （1） FixedThreadPool 和 SingleThreadExecutor 底层都是用LinkedBlockingQueue实现的，这个队列最大长度为Integer.MAX_VALUE，可能会堆积大量请求，从而导致 OOM
         *  （2） newCachedThreadPool和newScheduleThreadPool允许创建的最大线程数量为Integer.MAX_VALUE，会创建大量线程，从而导致OOM
         *
         */


        /**
         * corePoolSize 线程池的核心线程数
         * maximumPoolSize 能容纳的最大线程数
         * keepAliveTime 空闲线程存活时间
         * unit 存活的时间单位
         * workQueue 存放提交但未执行任务的队列 * threadFactory 创建线程的工厂类:可以省略 * handler 等待队列满后的拒绝策略:可以省略 */
        ThreadPoolExecutor newCachedThreadPool =(ThreadPoolExecutor) Executors.newCachedThreadPool();
        newCachedThreadPool.getActiveCount();

        /**
         * corePoolSize 线程池的核心线程数
         * maximumPoolSize 能容纳的最大线程数
         * keepAliveTime 空闲线程存活时间
         * unit 存活的时间单位
         * workQueue 存放提交但未执行任务的队列 * threadFactory 创建线程的工厂类:可以省略 * handler 等待队列满后的拒绝策略:可以省略 */
        ExecutorService newFixedThreadPool = Executors.newFixedThreadPool(10);


        /**
         * corePoolSize 线程池的核心线程数
         * maximumPoolSize 能容纳的最大线程数
         * keepAliveTime 空闲线程存活时间
         * unit 存活的时间单位
         * workQueue 存放提交但未执行任务的队列 * threadFactory 创建线程的工厂类:可以省略 * handler 等待队列满后的拒绝策略:可以省略
         */
        ExecutorService newSingleThreadExecutor = Executors.newSingleThreadExecutor();


        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(10);


        /**
         * parallelism:并行级别，通常默认为 JVM 可用的处理器个数
         * factory:用于创建 ForkJoinPool 中使用的线程。
         * handler:用于处理工作线程未处理的异常，默认为 null
         * asyncMode:用于控制 WorkQueue 的工作模式:队列---反队列
         */
        ExecutorService newWorkStealingPool = Executors.newWorkStealingPool();


        /**
         * 自定义线程池（必须）
         */
        ThreadPoolExecutor myThreadPoolExecutor = new ThreadPoolExecutor(
                5,
                10,
                1,
                TimeUnit.MINUTES,
                new ArrayBlockingQueue<>(3),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.AbortPolicy()
        );




    }
}
