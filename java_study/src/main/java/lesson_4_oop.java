
//import：
//java.lang包下的类或者接口无需导入，已经默认导入；
//批量导入一个包下的类或者接口可以用*号表示类。如:import java.util.*
//*号只能导入当前包下的类或接口，无法导入子包下的类或接口，子包需要单独导入
//import语句出现在package语句之后、类定义之前
//import语句不是必需的，可坚持在类里使用其它类的全名（完整路径）。
//import后可以使用static导入类中的静态属性


import static java.lang.System.out;

//常用包：
//java.lang----包含一些Java语言的核心类，如String、Math、Integer、System和Thread，提供常用功能。
//java.net----包含执行与网络相关的操作的类和接口。
//java.io ----包含能提供多种输入/输出功能的类。
//java.util----包含一些实用工具类，如定义系统特性、接口的集合框架类、使用与日期日历相关的函数。
//java.text----包含了一些java格式化相关的类
//java.sql----包含了java进行JDBC数据库编程的相关类/接口
//java.awt----包含了构成抽象窗口工具集（abstract window toolkits）的多个类，这些类被用来构建和管理应用程序的图形用户界面(GUI)。
//java.applet----包含applet运行所需的一些类。


public class lesson_4_oop {
  public static void main(String[] args) {
    out.println("");
    //实例化
    Person p=new Person("",18);
    p.age=20;
    p.name="ads";

  }

}

class Person{
  //申明属性.默认为相应类型的默认值。申明格式：修饰符 类型 属性名=初值 ;
  //成员变量和局部变量的主要区别：成员变量存放于堆内存中，可以不显式赋值，实例化时会自动赋予默认值；而局部变量必须显式的赋值，存放于栈内存中。
  //成员变量权限修饰符：public、private、protected、缺省
  public String name;
  public int age;

  //方法申明：

  public Person(String name, int age) {
    this.name = name;
    this.age = age;
  }
  //语法格式：
  //  修饰符 返回值类型 方法名(参数列表) {
  //    方法体语句；
  //  }

  //方法内不能再定义方法，但可以调用其他方法

  //返回值：
  //void：表示该方法无返回值；不能出现return语句
  //有返回值需要申明返回值类型。同时这类方法一定有return语句；return之后不能再申明语句


  //权限修饰符同成员变量。
  public String getName(){
    return name;
  }

  public void setName(String n){
    name=n;
  }

  public String desc(){
    System.out.println("name:" + name);
    if(age>=18){return "大于18岁"; } else {return  "小于18岁";}
  }

  //方法重载（overload）
  //要求：必须在同一个类中。方法名必须相同; 方法的参数列表不同（类型不同或者个数不同）
  //只要满足条件，方法可以返回不同的值类型
  //参数列表完全一致，但方法返回值不同，不构成重载
  public void addAge(int a){age += a;}
  public void addAge(int a,int b){age =  a;}

  //参数个数可变,args默认为一个数组。和传入int[] args不构成重载，默认是一样的
  //可变个数参数只能放在最后，不能放在固定形参之前。
  public void addage(int ... args){
    //for循环，遍历数组中元素
    for (int arg : args) {
      age += arg;
    }
  }
  public static void addage(char a){
    Person p=new Person("",0);
    p.age += a;
  }

  //不构成重载，报错
//  public void addage(int[] args){
//    for (int arg : args) {
//      age += arg;
//    }
//  }

  public int getAge() {
    return age;
  }
}

//工厂类
class Factory{
  //通过方法重载创建类实例
  public Person createPerson(){
    return new Person("",0);
  }
  public Person createPerson(String name,int age){
    Person p=new Person("",0);
    p.name=name;
    p.age=age;
    return p;
  }

  //可以传入匿名对象：desc(new Person())。匿名的对象由于没有赋值给变量名引用（只有堆空间无栈空间），仅局部使用后就废弃；
  public void desc(Person person){
    System.out.println(person.name);
  }

}

//参数传递机制：值传递机制
//对于基本数据类型：由于存放于栈空间中，将值传入方法中时，不会影响传入变量的原始值；（传入实际值）
//对于引用变量：可能会对传入的对象进行更改，原始对象的内部可能发生改变。（传入栈空间的内存地址引用。类似于python中的list，原处修改。需要注意）
//分清不同作用域下同名局部变量的作用范围。
class MathTool{
  public int sum(int x,int y){return x+y;}

  public MyType swap(MyType mt){
    int temp=mt.a;
    mt.a=mt.b;
    mt.b=temp;
    return mt;
  }

}

class MyType{
  public int a;
  public int b;
}

//封装与隐藏：对某些值进行限制，不能随便修改赋值，或者类型限制等，需要隐藏一些信息然后加以限制
//权限修饰符（修饰方法或者属性）：
//private：仅类内部可访问，其他任何地方都不行，子类都不行。（最严）
//缺省： 类内部或者同一包下可访问。
//protected: 相比缺省，在子类中也可访问。
//public：任何地方可访问

//static方法访问非static方法，编译不通过。

//类修饰符：仅能使用public或者缺省。
//public类任何地方可访问；
//缺省：仅同一包内部可访问

//构造器
//如果不显式的申明构造器，会默认的提供一个空参的构造器。
//一旦申明构造器，则默认的就不会提供；
//不声明返回值类型。（与声明为void不同）
//不能被static、final、synchronized、abstract、native修饰，不能有return语句返回值
//父类的构造器不可被子类继承

class Animal{

  //类属性赋值顺序：1、初始化默认值；2、显式初始化赋值；3、构造器给属性初始化赋值；4、通过调用方法赋值；
  private String name;
  private int legs;
  private String color;

  //构造器1：如果仅申明带参数的构造器，那么new实例时，小括号内必须传入参数；
  //注意：不申明返回类型。
  public Animal(String c){
    color=c;
  }
  //构造器2：可以申明不同的构造器。构造器之间构成重载；
  public Animal(){
    color="black";
  }

  public void setLegs(int l){
    if(l>0 && l%2==0){
      legs=l;
    }else {System.out.println("数据输入有误！");}
  }

  public int getLegs(){
    return legs;
  }
}

//this关键词：相当于python中的self，指代实例化后的当前实例。
//主要用来修饰属性和方法，防止混淆；
//提高阅读性。
//可以用来修饰构造器
//使用场景：当需要调用当前实例对象时。
class computer{
  private String cpu;
  private int memory;
  private String disk;

  //如果不适用this，构造方法的形参名不能直接用cpu。否则会无法给属性赋值。
  public computer(String cpu){
    this.cpu=cpu;
  }

  //修饰构造器
  public computer(String cpu,int memory){
    this(cpu);   //必须写在首行，先行实例化。
    this.memory=memory;
  }

}

//javaBean：一种Java语言写成的可重用组件，
// 满足以下条件就可以作为javaBean：
//类是公共的
//有一个无参的公共的构造器
//有属性，且有对应的get、set方法

