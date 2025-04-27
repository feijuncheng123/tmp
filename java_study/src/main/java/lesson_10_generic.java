
//泛型
//为什么要有泛型:
//1. 解决元素存储的安全性问题(类型不安全)
//2. 解决获取数据元素时，需要类型强转的问题

//Java泛型可以保证如果程序在编译时没有发出警告，运行时就不会产生ClassCastException异常。同时，代码更加简洁、健壮。
//把一个集合中的内容限制为一个特定的数据类型，这就是generics背后的核心思想。

//泛型的几个重要使用
//1.在集合中使用泛型
//2.自定义泛型类
//3.泛型方法
//4.泛型接口

import com.sun.istack.internal.NotNull;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class lesson_10_generic {

  @Test
  public void myTest(){
    ArrayList<Integer> l=new ArrayList<>();
    l.add(123);
  }
}

//自定义泛型类:实例化时不指定类型，则默认为object类型
//静态方法中不能使用类的泛型。
//不能在catch中使用泛型（catch小括号内）
//从泛型类派生子类，泛型类型需具体化
class Queue<K>{
  private String  name;
  private long id;
  private List<K> kList=new ArrayList<>();

  public void put(K k){
    this.kList.add(k);
  }

  //泛型方法：
  public <E> E getGenMothed(E e){
    return e;
  }

  public <E> List<E> copyToCollect(E[] eArray, @NotNull List<E> li){
    li.addAll(Arrays.asList(eArray));
    return li;
  }

  public  <K> List<K> copyToCollect01(K[] eArray, @NotNull List<K> li){
    li.addAll(Arrays.asList(eArray));
    return li;
  }

  //调用示例
  public static void test01(){
    Queue<Integer> q=new Queue<>();
    String[] s=new String[10];

    Integer[] i=new Integer[10];
    ArrayList<String> l=new ArrayList<String>();

    q.copyToCollect(s,l);

  }

}


//泛型与继承的关系问题：
//如果B是A的一个子类型（子类或者子接口），而G是具有泛型声明的类或接口，G<B>并不是G<A>的子类型。因为G<A>更宽泛，有多个子类的情况下，都可以往里添加。
//比如：String是Object的子类，但是List<String >并不是List<Object>的子类。
//通配符：问号？
//通配符的泛型是其他所有泛型的父类，可以全部往里放
//不允许往通配符的集合类中写入（可读，可赋值），例外写入null
class Test01<E>{
  @Test
  public void test(){
    List<?> l1=null;
    List<Integer> li=new ArrayList<>();
    List<String> ls=new ArrayList<>();
    l1=li;
    l1=ls;
  }

  public void test2(List<? extends E> list){ }   //表示通配符的类型必须是E的子类
  public void test3(List<? super E> list){ }   //表示通配符的类型必须是E的父类
}


//枚举
//枚举类对象的属性不应允许被改动, 所以应该使用 private final 修饰
//自定义枚举示例：final关键词修饰的属性为常量，不允许被赋值。
class Season{
  private final String seasonName;
  private final String seasonDesc;

  private Season(String seasonName, String seasonDesc) {
    this.seasonName = seasonName;
    this.seasonDesc = seasonDesc;
  }

  public static final Season SPRING=new Season("春季","万物复苏");  //常量
  public static final Season SUMMER=new Season("夏季","阳光普照");  //常量

  public String getSeasonName() {
    return seasonName;
  }

  public String getSeasonDesc() {
    return seasonDesc;
  }

  @Override
  public String toString() {
    return "Season{" +
        "seasonName='" + seasonName + '\'' +
        ", seasonDesc='" + seasonDesc + '\'' +
        '}';
  }

  @Test
  public static void test(){
    System.out.println(Season.SPRING.toString());
  }

}

//内置枚举类：
//注意：多个枚举之间用逗号分割。
//枚举的case必须放到最上方。
//传入参数必须定义构造器。
//使用 enum 定义的枚举类默认继承了 java.lang.Enum 类
//构造器只能使用 private 访问控制符
enum Season1{
  SPRING("春季","万物复苏"),
  SUMMER("夏季","阳光普照"),
  AUTUMN("夏季","阳光普照"){   //可以在不同枚举中定义相应方法，或者同一个方法，但不同输出
    public void show(){
      System.out.println("");
    }
  };


  private final String seasonName;
  private final String seasonDesc;

  Season1(String seasonName, String seasonDesc) {
    this.seasonName = seasonName;
    this.seasonDesc = seasonDesc;
  }

  public String getSeasonName() {
    return seasonName;
  }

  public String getSeasonDesc() {
    return seasonDesc;
  }

  @Test
  public static void test(){
    Season1[] s=Season1.values();  //可以把全部枚举以数组形式返回
    Season1 s1=Season1.valueOf("SPRING"); //类似于反射，通过枚举的字符串名获取相应枚举。如果无法匹配上，将报错
  }
}


//注解Annotation
//JDK内置的基本注解类型（3个）
//自定义注解类型
//对注解进行注解（4个）
//利用反射获取注解信息（在反射部分涉及）

//通过使用 Annotation, 程序员可以在不改变原有逻辑的情况下, 在源文件中嵌入一些补充信息.
//Annotation 可以像修饰符一样被使用, 可用于修饰包,类, 构造器, 方法, 成员变量, 参数, 局部变量的声明


//3个基本注解：
//@Override: 限定重写父类方法, 该注释只能用于方法
//@Deprecated: 用于表示某个程序元素(类, 方法等)已过时
//@SuppressWarnings: 抑制编译器警告

//自定义注解:用的非常少
//定义新的 Annotation 类型使用 @interface 关键字
//Annotation 的成员变量在 Annotation 定义中以无参数方法的形式来声明. 其方法名和返回值定义了该成员的名字和类型.
@interface MyAnnotation{
  String name() default "atguigu";
}



