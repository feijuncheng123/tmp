

//抽象类：
//将一个父类设计得非常抽象，以至于它没有具体的实例，这样的类叫做抽象类。
//抽象类不可以被实例化
//抽象类有构造器（只要是类就有构造器）
//不能用abstract修饰属性、私有方法、构造器、静态方法、final的方法。
//如果子类没有完全实现抽象方法，则子类也是抽象类

//用abstract不能修饰属性，属性不构成多态，子类无法覆盖父类属性
//不能修饰static方法，原因是static方法在类加载时一次性加载，可以直接通过类名调用，而abstract没有方法体，无法执行
public class lesson_7_abstract_interface {
}

abstract class beings{
  protected String form;
  protected String size;
  protected String material;

  protected beings(String form,String size,String material){
    this.form=form;
    this.size=size;
    this.material=material;
  }

  //abstract用来修饰方法表示抽象方法，表示需要子类实现。
  //含有抽象方法的类必须被声明为抽象类。
  protected abstract void move();

  public static void main(String[] args) {

  }
}


class stone extends beings {
  public stone(String form, String size, String material) {
    super(form, size, material);
  }

  public stone(){
    super("a","12","c");
  }

  public void move(){ }

}

//模板方法设计模式(TemplateMethod)
//抽象类作为多个子类的通用模板，子类在抽象类的基础上进行扩展、改造，但子类总体上会保留抽象类的行为方式。
//1、当功能内部一部分实现是确定，一部分实现是不确定的。这时可以把不确定的部分暴露出去，让子类去实现
//2、父类提供了多个子类的通用方法，并把一个或多个方法留给其子类实现，就是一种模板模式。


//接口：抽象方法和常量值的定义的集合。只能有常量和抽象方法。不能包含一般方法，无构造器
//有时必须从几个类中派生出一个子类，继承它们所有的属性和方法。但是，Java不支持多重继承。接口可以得到多重继承的效果
//从本质上讲，接口是一种特殊的抽象类。只包含常量和方法的定义，而没有变量和方法的实现。
//一个类可以实现多个接口，接口也可以继承其它接口
//接口采用多继承机制.但接口只能继承接口，不能继承类
//接口支持多态，可以作为类型参数
interface Place{
  //只能包含常量
  public static final String NATION="china";
  //尽管可以省略public static final几个关键词，但接口中默认还是使用了这几个关键词。省略后外部同样可以调用，但不能赋值，也不能改写
  Double PI=3.14159;

  public abstract String province();
  //同样的，方法默认携带public abstract ，可以省略。
  String city();
}

interface Sex{
  String setSex();
}

//接口可以多继承
interface body extends Place,Sex {
  String RACE="黄";
  int setHeight();
  String setName();
}


class chinese extends beings implements Place,body  {

  protected chinese(String form, String size, String material) {
    super(form, size, material);
  }

  @Override
  public String province() {
    return null;
  }

  @Override
  public String city() {
    return null;
  }

  @Override
  protected void move() {

  }

  @Override
  public int setHeight() {
    return 0;
  }

  @Override
  public String setName() {
    return null;
  }

  @Override
  public String setSex() {
    return null;
  }
}

//接口的主要用途就是被实现类实现。（面向接口编程）
//工厂方法设计模式：
//通过方法生产实例，不同参数生产不同实例。但一般生产的实例都有共同父类（接口）。
//不同实例都有共同的父类方法，都可以作为父类类型。
interface CarType{
  int speed();
  String carType();
  void run();
}

class Car implements CarType{

  @Override
  public int speed() {
    return 120;
  }

  @Override
  public String carType() {
    return "轿车";
  }

  @Override
  public void run() {
    System.out.println("轿车上路！");
  }
}

class Bus implements CarType {

  @Override
  public int speed() {
    return 80;
  }

  @Override
  public String carType() {
    return "大巴";
  }

  @Override
  public void run() {
    System.out.println("大巴上路！");
  }
}

class CarFactory {
  private CarType car;
  public CarFactory(String name){
     car = (name.equals("car")) ? new Car() : new Bus();
  }

  CarType getCar(){return car;}

  public static void main(String[] args) {
    CarFactory c=new CarFactory("bus");
    c.getCar().run();
  }
}


//代理模式：
//一个类的实例（被代理）通过另外一个类进行实例化，并管理。（静态代理）


//内部类：比较少用
//成员内部类：和方法属性并列。又分为static成员内部类和非static成员内部类
//static成员内部类
//局部内部类：放到了成员方法中。
//
class InnerClass{
  String name;
  String b;

  public InnerClass(String a, String b) {
    this.name = a;
    this.b = b;
  }

  private void run(){}

  //成员内部类（非静态）
  public class IC{
    String name=InnerClass.this.name;  //如果出现重名，调用外部类的属性的方式
    String type=b;
    public void testRun(){run();}   //可以直接调用外部类的方法属性
  }

  //成员内部类（静态）不能直接调用外部类方法
  public static class SIC{

  }

  //局部内部类，非常少用。一般用在方法需要返回一个接口或者父类的对象
  //也可以返回一个实现接口的匿名类
  public Sex createClass(){
    class MethodClass implements Sex{
      @Override
      public String setSex() {
        return null;
      }
    }
    return new MethodClass();
  }

  //匿名类
  public Sex createClass01(){
    return new Sex(){
      @Override
      public String setSex() {
        return null;
      }
    };
  }
}

//创建内部类实例
class InnerClassTest{
  public static void main(String[] args) {
    //静态内部类.通过类名直接实例化
    InnerClass.SIC i=new InnerClass.SIC();

    //非静态实例化
    InnerClass i1=new InnerClass("a","b");
    InnerClass.IC ii1=i1.new IC();

  }
}

