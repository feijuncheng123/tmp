import org.junit.Test;

import java.io.ObjectInputStream;

/**
 * 进程(process)是程序的一次执行过程，或是正在运行的一个程序. 动态过程：有它自身的产生、存在和消亡的过程。程序是静态的，进程是动态的
 * 线程(thread)，进程可进一步细化为线程，是一个程序内部的一条执行路径。
 * 若一个程序可同一时间执行多个线程，就是支持多线程的
 * 每个Java程序都有一个隐含的主线程： main 方法
 *
 * 线程间共享对象，执行时会在每个线程中都有个缓存，线程修改后，线程缓存需要将值写入主存之中。数据同步有消耗
 *
 */


public class lesson_12_thread {
  public static void main(String[] args) {
    ThreadTest tt=new ThreadTest();

    //3、子线程启动，一个子线程（实例）只能启动一次。非阻塞
    tt.setName("thread");   //都可以设置name
    tt.start();

    tt.setPriority(Thread.MAX_PRIORITY);  //设置队列优先级，也可以通过整数设定。最高优先级默认为10.优先级并代表一定先执行，但执行权概率更高

    //主线程
    Thread.currentThread().yield();  //强行释放当前线程的执行权。然后继续互相争抢线程执行权。
    System.out.println("main");


    for(int i=0;i<100;i++){
        System.out.println(Thread.currentThread().getName() + ": "+ i);
        if(i==20){
          try{
            Thread.sleep(100);  //当前线程睡眠100ms
            tt.join();  //在A线程中调用B线程的join方法，表示暂停A线程，由B线程加入后执行完毕，在执行A线程
          }
          catch (InterruptedException e){
            e.printStackTrace();
          }
        }
    }


    System.out.println(tt.isAlive());  //子线程是否还存活


  }
}

//1、创建一个继承自Thread的类
class ThreadTest extends Thread {

  //2、重写run方法，即在子线程中希望执行的代码
  public void run(){
    Thread.currentThread().setName("thread");
    System.out.println(Thread.currentThread().getName());  //获取子线程的名字
  }

}


/**
 * 实现多线程的第二种方式：实现Runnable接口。。该方式更好
 * 优点：
 * 1、避免了单继承的局限（继承了Thread就不能继承其他类了）
 * 2、多个线程可以共享同一个接口实现类的对象，非常适合多个相同线程来处理同一份资源(集成thread的类本身就是一个thread，无法再传给其他thread处理。但Runnable的实例可以同时传入到多个thread中)
 */
class thread2 implements Runnable {
  int x=100;   //传入多个线程时保持共享。多线程同时操作一个对象
  @Override
  public void run() {
    while ((x--)>0){
      System.out.println(Thread.currentThread().getName()+":"+x);
    }
  }
}

class startThread2 {
  @Test
  public void test(){
    thread2 t2=new thread2();    //共享对象

    Thread thread0=new Thread(t2);   //将实现了Runnable接口的实例传入到Thread构造器中。
    Thread thread1=new Thread(t2);   //将同一对象放入Thread中，其中的变量是共享的，比如x。
    Thread thread2=new Thread(t2);
    thread0.start();
    thread1.start();
    thread2.start();
  }
}


/**
 * 线程的生命周期
 * Thread.State枚举表示了线程的几种状态
 * 新建 -> 就绪  -> 运行 -> 阻塞(sleep\wait\join) -> 死亡
 * 线程的同步机制：
 * 1、同步代码块（锁）：synchronized (同步监视器）{//需要被同步的代码块}
 * 哪个线程获取到同步监视器，该线程就执行被同步代码(抢锁)
 * 2、同步方法：
 */

class thread3 implements Runnable {
  int x=100;   //传入多个线程时保持共享。多线程同时操作一个对象
  final Object obj=new Object();  //所有对象都可以作为锁。注意：必须保证同一个锁对象是所有线程共享的。（共用一把锁）

  @Override
  public void run() {
      while (x>0){
        synchronized (obj){ try {
          //obj也可以直接换为this，也是可以的。（实例只造了一个）。synchronized不能写在while外面，因为一个锁内全部循环，等于单线程。
          // 在while循环中使用共享变量判断也是错误的。因为不同线程已经获取到该共享变量
          // 使用共享变量都需详细分析。特别是线程什么时候获取到该变量。（获取变量的入口）
          Thread.sleep(100);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        System.out.println(Thread.currentThread().getName()+":" + x--);
      }
    } }

  //同步方法：
  //同步方法的锁默认为当前对象this
  private synchronized void show(){   //申明为synchronized表示该方法为同步方法。
    if(x>0){
      try { Thread.sleep(100); }
      catch (InterruptedException e) { e.printStackTrace(); }
      System.out.println(Thread.currentThread().getName()+":" + x--);
  } }
}

/**
 * 互斥锁
 * 关键字synchronized 来与对象的互斥锁联系.当某个对象用synchronized修饰时，表明该对象在任一时刻只能由一个线程访问。
 * 同步的局限性：导致程序的执行效率要降低
 * 同步方法（非静态的）的锁为this。
 * 同步方法（静态的）的锁为当前类本身。
 */
//懒汉式单例确保线程安全
class singleton{
  singleton instance=null;
  private singleton(){};
  public singleton getInstance(){
    if(instance == null){    //外面再套一层，可以提高效率。
      synchronized (singleton.class){
        if(instance == null){
          instance=new singleton();
        } }}
    return instance;
  }
}

/**
 * 死锁问题
 * 不同的线程分别占用对方需要的同步资源不放弃
 * 互相占用对方继续执行需要的锁。
 */

class TestDeadLock {
  static StringBuffer s1 = new StringBuffer();
  static StringBuffer s2 = new StringBuffer();

  public static void main(String[] args) {
    new Thread(() -> {
      synchronized (s1) {
        s2.append("A");
        synchronized (s2) {
          s2.append("B");
          System.out.print(s1);
          System.out.print(s2);
        }}}).start();

    new Thread() {
      public void run() {
        synchronized (s2) {
          s2.append("C");
          synchronized (s1) {
            s1.append("D");
            System.out.print(s2);
            System.out.print(s1);
          }}}}.start();
  }}

/**
 * 线程通信：object中wait() 与 notify() 和 notifyAll()方法
 * wait()：令当前线程挂起并放弃CPU，使别的线程可访问并修改共享资源，而当前线程排队等候再次对资源的访问
 * notify()：唤醒正在排队等待同步资源的线程中优先级最高的线程结束等待
 * notifyAll ()：唤醒正在排队等待资源的所有线程结束等待.
 * 注意：
 * Java.lang.Object提供的这三个方法只有在synchronized方法或synchronized代码块中才能使用，否则会报java.lang.IllegalMonitorStateException异常
 *
 * wait() 方法:使当前线程进入等待（某对象）状态 ，直到另一线程对该对象发出 notify (或notifyAll) 为止
 * 调用方法的必要条件：当前线程必须具有对该对象的监控权（加锁）
 * 调用此方法后，当前线程将释放对象监控权 ，然后进入等待
 * 在当前线程被notify后，要重新获得监控权，然后从！断点！处继续代码的执行。
  */

class Communication implements Runnable{
  int i = 1;
  public void run() {
    while (true) {
      synchronized (this) {
        notify();    //当前线程获得锁的执行权。释放的另外的线程也进不来
        if (i <= 100) {
          System.out.println(Thread.currentThread().getName() + ":" + i++);
        }
        else   break;
        try { wait();}   //当前线程等待，另外的激活的线程接着执行
        catch (InterruptedException e) { e.printStackTrace();}
      }
    }
  }
}



