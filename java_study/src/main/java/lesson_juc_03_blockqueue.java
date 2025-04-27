
import java.util.concurrent.ArrayBlockingQueue;


public class lesson_juc_03_blockqueue {
    public static void main(String[] args) {

        /**
         * 1、阻塞队列特点：
         * （1）当队列是空的，从队列中获取元素的操作将会被阻塞
         * （2）当队列是满的，从队列中添加元素的操作将会被阻塞
         * 优点：不需要阻塞或者唤醒线程，通过阻塞队列可以实现线程间通信
         *
         * 2、BlockingQueue核心方法：
         * （1）为空或者为满时抛出异常操作：
         *      add(e):插入,正常返回true
         *      remove()：移除
         *      element():检查元素
         *  (2)抛出特殊值操作：
         *      offer(e):插入成功返回true，失败返回false
         *      poll():成功返回元素值，失败返回null
         *      peek():元素检查
         *  (3)阻塞操作：
         *      put(e)
         *      take()
         *  (4)超时操作：
         *      offer(e,timeout)
         *      poll(timeout)
         *
         * 3、常见的 BlockingQueue
         *  （1）ArrayBlockingQueue(常用)：生产者和消费者共用锁对象，无法真正同时并行；插入和删除元素不会产生或销毁任何额外的实例对象（LinkedBlockingQueue则会），gc性能更高，大批量数据处理更适合
         *  （2）LinkedBlockingQueue（常用）：生产者和消费者分别采用独立锁，适用高并发，但插入和销毁会额外生成或销毁node对象，gc性能慢
         *  （3）DelayQueue：指定元素的延迟时间，到了才能获取该元素
         *  （4）PriorityBlockingQueue：基于优先级的阻塞队列（通过Compator对象指定优先级），但不阻塞生产者，生产快于消费可能会耗尽内存
         *  （5）SynchronousQueue：无缓冲队列，生产即消费，无法存储数据，可声明公平锁或者非公平锁（抢占式）
         *  （6）LinkedTransferQueue：消费者预占式队列，即空队列时消费者生成一个null对象占位，生产者发现null直接填充唤醒线程执行。
         *  （7）LinkedBlockingDeque：双向阻塞队列，可以从队列的两端插入和移除元素。
         *
         */

        ArrayBlockingQueue<String> strings = new ArrayBlockingQueue<>(10);  //常用阻塞队列


    }
}
