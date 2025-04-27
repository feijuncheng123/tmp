import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class lesson_juc_01_base {
    public static void main(String[] args) {

        ConcurrentLinkedDeque<Object> objects = new ConcurrentLinkedDeque<>();

        /**
         * 1、线程状态：Thread.State: NEW, RUNNABLE,BLOCKED,WAITING,TIMED_WAITING,TERMINATED
         * 2、wait和sleep区别：
         *  （1）sleep 是 Thread 的静态方法，wait 是 Object 的方法，任何对象实例都能调用。
         *  （2）sleep 不会释放锁，它也不需要占用锁。wait 会释放锁，但调用它的前提 是当前线程占有锁(即代码要在 synchronized 中)。
         *   (3)它们都可以被 interrupted 方法中断。
         *  （4） wait必须写在循环中避免虚假唤醒（如果写在if中，在wait语句中等待，唤醒时不会再判断if条件，会直接往下执行，导致条件失效）
         *
         *
         * 3、并发与并行：
         *  （1）并发：强调资源或能力，多个线程可以访问同一个资源
         *  （2）并行：强调动作，同时执行。
         *
         * 4、 用户线程和守护线程
         *  （1）用户线程:平时用到的普通线程,自定义线程
         *  （2）守护线程:运行在后台,是一种特殊的线程,比如垃圾回收
         *  （3）当主线程结束后,用户线程还在运行,JVM 存活
         *  （4）如果没有用户线程,都是守护线程,JVM 结束
         *
         * 5、synchronized关键词：
         *  （1）无法被继承，父类中被修饰，子类继承后如果不被修饰则子类方法不是同步方法，但不影响父类方法
         *  （2）同一个类中多个普通方法，synchronized使用的锁都是当前实例对象。如果同一对象的多个synchronized方法顺序执行，即使不在同一线程中也是顺序执行
         *  （3）多个静态方法，锁是当前class对象，多个synchronized静态方法顺序执行，即使不在同一线程中也是顺序执行
         *  （4）对于synchronized方法块，锁是synchronized（this）{}小括号中配置的对象
         *
         *
         * 6、Lock：更细粒度的锁，但需要手动上锁和释放锁(发生异常也不会自动释放锁）
         *  （1）Lock可以让等待锁的线程响应中断，而synchronized却不行，使用 synchronized 时，等待的线程会一直等待下去，不能够响应中断
         *  （2）通过Lock可以知道有没有成功获取锁，而synchronized却无法办到。
         *  （3）Lock可以提高多个线程进行读操作的效率。
         *  （4）公平锁：效率低，资源在线程间分配较好
         *  （5）非公平锁：效率高，但可能导致单个线程独占
         *
         * 7、可重入锁：
         *  （1）即一代码单位中只需要一把锁，就可以进入其中任何其他锁的内部。即嵌套使用锁，最外层锁可以穿透其中任何其他锁的内容。
         *  （2）即使嵌套锁，也必须加锁和释放锁同时进行，否则会导致因某个锁未释放而产生问题
         *
         * 8、集合线程安全解决方案：
         *  （1）使用vector。（古老性能低）
         *  （2）使用Connections.synchronizedList()方法包装 （也比较古老）
         *  （3）使用juc包中的CopyOnWriteArrayList（推荐但性能差)
         *
         * 9、volatile和synchronized：
         *  （1）volatile关键字解决的是内存可见性的问题，会使得所有对volatile变量的读写都会直接刷到主存，即保证了变量的可见性。这样就能满足一些对变量可见性有要求而对读取顺序没有要求的需求。
         *  （2）synchronized关键字解决的是执行控制的问题，它会阻止其它线程获取当前对象的监控锁，这样就使得当前对象中被synchronized关键字保护的代码块无法被其它线程访问，也就无法并发执行，
         *      synchronized还会创建一个内存屏障，内存屏障指令保证了所有CPU操作结果都会直接刷到主存中
         *  （3）volatile和synchronized的区别
         *      一、volatile本质是在告诉jvm当前变量在寄存器（工作内存）中的值是不确定的，需要从主存中读取； synchronized则是锁定当前变量，只有当前线程可以访问该变量，其他线程被阻塞住。
         *      二、volatile仅能使用在变量级别；synchronized则可以使用在变量、方法、和类级别的
         *      三、volatile仅能实现变量的修改可见性，不能保证原子性；而synchronized则可以保证变量的修改可见性和原子性
         *      四、 volatile不会造成线程的阻塞；synchronized可能会造成线程的阻塞。
         *      五、volatile标记的变量不会被编译器优化；synchronized标记的变量可以被编译器优化
         *
         * 10、读写锁：
         * （1）ReentrantReadWriteLock.readLock() 读锁，是共享锁，所有进程可以同时读取，但无法写入
         * （2）ReentrantReadWriteLock.writeLock() 写锁，是独占锁，只能单个线程写入，其他线程无法写入但可读取
         * （3）实现读时可写（写锁降级）：因写入时可读，因此可以通过先用写入锁，再用读取锁的的嵌套方式把读取锁嵌入写入锁中，这样写入时可读取，也反过来实现了读取时写入
         * （4）读锁在前，则无法执行写锁代码
         *
         */

        Thread.State aNew = Thread.State.NEW;

        CopyOnWriteArrayList<String> strings = new CopyOnWriteArrayList<>();
        strings.add("");

        CopyOnWriteArraySet<String> strings1 = new CopyOnWriteArraySet<>();


        ConcurrentHashMap<String, String> stringStringConcurrentHashMap = new ConcurrentHashMap<>();

    }

    //独占锁
    private final ReentrantLock reentrantLock= new ReentrantLock(true);  //公平锁


    //读写锁
    private final ReentrantReadWriteLock reentrantReadWriteLock = new ReentrantReadWriteLock();


    //Condition用于和lock相结合进行线程间切换工作
    private Condition condition = reentrantLock.newCondition();

    int flag =1;

    /**
     * lock简单示例
     */
    public void lockTest(){
        reentrantLock.lock(); //加锁
        //同步代码
        try{

            while (flag != 1){
                condition.await();   //当前线程等待，释放锁。也必须写在while循环中
            }
            System.out.println("...");


            flag = 2;  //通过修改等待条件，可以定制化的启动下一个具体线程。
            condition.signalAll();  //通知其他线程干活

        }catch (Exception ignored){}
        finally {
            reentrantLock.unlock(); //释放锁
        }


    }
    private int number = 0;

    /**
     * synchronized自动上锁和释放锁
     *
     */
    public synchronized void waitTest() throws InterruptedException {
        while (number != 0) {
            this.wait();     //必须写在while循环中，等待其他线程执行，收到notify或者notifyAll时继续执行。会释放锁，其他线程抢到执行
        }
        //干活
        number++;
        System.out.println(Thread.currentThread().getName()+" :: "+number);
        //通知
        this.notifyAll();   //通知其他线程开始执行
    }


    private final ReentrantLock deadLock01= new ReentrantLock(true);  //公平锁
    private final ReentrantLock deadLock02= new ReentrantLock(true);  //公平锁


    /**
     * 死锁案例
     */
    public void deadLockTest01() throws InterruptedException {

        while (flag != 0) {
            deadLock01.lock();
        }
        Thread.sleep(1000);
        try{
            deadLock02.lock();
            //code

            deadLock02.unlock();
        }finally {
            deadLock01.unlock();
        }


    }


    public void deadLockTest02() throws InterruptedException {
        while (flag != 1) {
            deadLock02.lock();
        }
        Thread.sleep(1000);

        try{
            deadLock01.lock();
            //code
            deadLock01.unlock();

        }finally {
            deadLock02.unlock();
        }


    }


    private volatile HashMap<String,String> hashMap=new HashMap<>();
    public void reentrantReadWriteLockTest(){
        reentrantReadWriteLock.writeLock().lock();
        hashMap.put("key","123");
        reentrantReadWriteLock.writeLock().unlock();


        reentrantReadWriteLock.readLock().lock();
        hashMap.get("");
        reentrantReadWriteLock.readLock().unlock();
    }
}
