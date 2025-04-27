
//单例模式：
//采取一定的方法保证在整个的软件系统中，对某个类只能存在一个对象实例，并且该类只提供一个取得其对象实例的方法
//首先必须将类的构造方法的访问权限设置为private，无法通过new创建对象
//只能调用该类的某个静态方法以返回类内部创建的对象
//通过==判断是否同一对象

import java.util.zip.DeflaterOutputStream;

//饿汉式单例：一加载就创建。
public class lesson_6_design_pattern {
  private lesson_6_design_pattern(){}

  private static lesson_6_design_pattern singleton=new lesson_6_design_pattern();

  public static lesson_6_design_pattern getSingleton(){return singleton; }
}

//懒汉式单例:用的时候才创建，多线程中可能存在线程安全问题
class lesson_6_design_pattern_01 {
  private lesson_6_design_pattern_01(){}

  private static lesson_6_design_pattern_01 singleton=null;

  public static lesson_6_design_pattern_01 getSingleton(){
    if(singleton==null){
      singleton=new lesson_6_design_pattern_01();
    }
    return singleton;
  }
}


//main方法：程序入口
//也可以通过调用执行
//class不一定需要public修饰


//初始化代码块:
//代码块如果需要修饰，则只能用static进行修饰
//每创建一个对象就会执行一次
//属性初始化可以位于代码块之后，显式初始化值和代码块的赋值按各自先后顺序覆盖

//静态代码块：
//使用static修饰整个代码块
class InitCode{
  private String name="weffwe";
  private int age;
  public static String nation="china";

  //初始化代码块。可以输出语句
  {
    System.out.println("初始化代码块");
    name="abc";
    age=12;
    sex="abc";
  }
  //可以有多个初始化代码块。按前后顺序执行。
  {
    System.out.println("初始化代码块2");
    sex="man";
  }

  //会早于非静态代码块执行，同样的，多个静态代码块按照顺序执行。只会加载一次
  static {
    System.out.println("静态代码块");
    nation="中国";
  }


  //代码块执行早于构造器
  private InitCode() {
    name="123";
    System.out.println("构造器");
  }

  public String sex="woman";  //最终会输出woman
}


//final关键字
//final修饰的类不能被继承，如String类、System类、StringBuffer类
//final标记的方法不能被子类重写。如Object类中的getClass()
//final标记的变量(成员变量或局部变量)即称为常量,不能使用默认初始化，必须显式的赋值。名称大写，且只能被赋值一次。必须在声明的同时或在每个构造方法中或代码块中显式赋值，然后才能使用
//必须在创建实例前赋值。
final class A{
  public final double PI=3.1415926;
  public final Double e;
  public final int zero;
  private final int ten;
  {
    PI=1.4;   //重复赋值不行
    e=2.718281828459045;
  }

  public A(){
    zero=0;
  }

  public void setTen(int i){ten=i;}  //在方法中赋值也不行

  public int addOne(final int x) {
    return ++x;}   //x为final变量，赋值后不能被修改

}

