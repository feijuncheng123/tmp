

/**
 * 1、在多线程中，共享数据分为主存和缓存，每个线程操作自己的缓存，然后同步到主存中。同步由于有消耗，导致线程间出现内存可见性问题
 * 2、可以通过同步锁刷新数据，获取锁时会刷新主存数据。但是加锁会导致性能很低
 *
 *
 * volatile关键字：多线程操作共享数据，可以保证共享数据是内存可见的（直接操作主存数据）
 * volatile相较于synchronized是轻量级的同步策略。
 * 1、synchronized有互斥性，volatile不具备互斥性，所有线程都可操作，但是在主存中操作
 * 2、volatile不保证变量的原子性（不可分割）。
 * 比如i++操作，分为了读取然后修改的操作，volatile不保证两步操作是不可分拆的，多线程下同样会出问题。但synchronized修饰后两步操作合并不能分拆。
 *
 * java.util.concurrent.atomic包下提供了大量原子变量
 * 1、原子变量自身具有volatile特性
 * 2、使用了CAS算法保证了原子性
 *
 * CAS算法：
 * CAS (Compare-And-Swap) 是一种硬件对并发的支持，针对多处理器操作而设计的处理器中的一种特殊指令，用于管理对共享数据的并发访问
 * CAS 是一种无锁的非阻塞算法的实现
 * CAS 包含了3 个操作数：
 * 1、需要读写的内存值V。就是原始值
 * 2、进行比较的值A。相当于更新后的最新值，比如多线程在主存修改时，其他线程修改后的最新值。
 * 3、拟写入的新值B。比较和写入是同时发生的
 * 当且仅当V 的值等于A 时，CAS 通过原子方式用新值B 来更新V 的值，否则不会执行任何操作
 *
 * CAS相比synchronized效率高，原因是CAS不会导致线程阻塞，为进入线程会立即进行下一次的操作
 */

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.ConcurrentHashMap;

public class java_juc {
  public static void main(String[] args) {
      TestVolatile tv=new TestVolatile();
      new Thread(tv).start();

      while (true){
        if(tv.isFlag()){               //如果不添加volatile，由于while true速度特别快，导致主线程中tv无法更新，而一直循环
          System.out.println("flag is true");
          break;
        }
      }

  }
}

class TestVolatile implements Runnable{
  private volatile boolean flag=false;   //volatile修饰变量，使该变量直接在主存中操作，保证内存可见
  private AtomicBoolean atomFlag=new AtomicBoolean(false);

  public void setAtomFlag(boolean atomFlag) {
    this.atomFlag.getAndSet(atomFlag);
  }

  public boolean getAtomFlag() {
    return atomFlag.get();
  }

  @Override
  public void run() {
    try {
      Thread.sleep(100);
    } catch (InterruptedException ignored) { }

    flag=true;
    this.atomFlag.getAndSet(true);
    System.out.println("flag:" + isFlag());
  }

  public boolean isFlag() {
    return flag;
  }

  public void setFlag(boolean flag) {
    this.flag = flag;
  }

}


/**
 * ConcurrentHashMap 锁分段
 * HashMap线程不安全，Hashtable线程安全，但是多线程增删值时效率特别慢（独占锁）
 * ConcurrentHashMap提供了锁分段：即将HashMap拆分成不同的段（链表），不同的段分别提供不同的锁，这样多线程操作时分别操作不同段，效率提供很多
 * 在jdk1.8后替换为CAS算法了
 *
 */

