import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.ElementType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Properties;

/**
 * Object类中提供了public final Class getClass()方法。是反射的源头
 * 该方法返回值的类型是一个Class类。指针指向运行时类
 *
 */

/**
 * Class 类：
 * （1）Class本身也是一个类，只能由系统建立对象
 * （2）一个类在 JVM 中只会有一个Class实例
 * （3）一个运行时类只会加载一次
 * （4）一个Class对象对应的是一个加载到JVM中的一个.class文件
 * （5）每个类的实例都会记得自己是由哪个 Class 实例所生成
 * （6）通过Class可以完整地得到一个类中的完整结构
 *
 * 获取Class类对象的四种方式：
 * 1、前提：若已知具体的类，通过类的class属性获取，该方法
 *                     最为安全可靠，程序性能最高
 *    实例：Class clazz = Person.class;
 * 2、前提：已知某个类的实例，调用该实例的getClass()方法
 *                      获取Class对象
 *
 *    实例： Person p=new Person();
 *          Class clazz = p.getClass();
 *          或者Class clazz = “String”.getClass();//字符串Class
 * 3、前提：已知一个类的全类名，且该类在类路径下，可通过
 *        Class类的静态方法forName()获取，可能抛出ClassNotFoundException
 *    实例：Class clazz = Class.forName(“java.lang.String”);
 * 4、其他方式(不做要求)
 *    ClassLoader cl = this.getClass().getClassLoader();
 *    Class clazz4 = cl.loadClass(“类的全类名”);
 */
class TestReflect{

  /**
   * 反射样例：可以调用和修改私有方法和属性
   * 疑问1：通过直接new的方式或反射的方式都可以调用公共的结构，开发中到底用那个？
   *     //建议：直接new的方式。
   *     //什么时候会使用：反射的方式。 反射的特征：动态性
   * 疑问2：反射机制与面向对象中的封装性是不是矛盾的？如何看待两个技术？
   *     //不矛盾：封装是建议调用相应方法；反射是解决能否调用的问题
   * @throws IllegalAccessException
   * @throws InstantiationException
   * @throws NoSuchFieldException
   * @throws NoSuchMethodException
   * @throws InvocationTargetException
   */
  @Test
  public void test2() throws IllegalAccessException, InstantiationException, NoSuchFieldException, NoSuchMethodException, InvocationTargetException {
    /*
      1、通过获取的Class类创建实例：调用Class对象的newInstance()方法
      要求：
          1）类必须有一个无参数的构造器。
          2）类的构造器的访问权限需要足够。
      2、如果构造器有参数，可以通过getDeclaredConstructor(Class … parameterTypes)取得本类的指定形参类型的构造器

      在javabean中要求提供一个public的空参构造器。原因：
        1.便于通过反射，创建运行时类的对象
        2.便于子类继承此运行时类时，默认调用super()时，保证父类有此构造器
     */
    Class<MyReflect> mrClass=MyReflect.class;   //获取运行时类
    MyReflect mr=mrClass.newInstance();   //通过运行时类创建新实例

    Constructor cons = mrClass.getConstructor(String.class,int.class);   //获取构造器
    Object obj = cons.newInstance("Tom", 12);
    MyReflect p = (MyReflect) obj;

    Field f=mrClass.getDeclaredField("name");   //获取运行时类的属性。注意：如果声明为public可以直接通过getField()方法获取，如果定义为private则需要通过getDeclaredField方法获取，该方法只要申明就能获取到
    f.setAccessible(true); //对于申明为private的属性需要调用setAccessible设为true，否则无法修改值。
    f.set(mr,"abc");   //需要指定修改的示例。

    Method m1=mrClass.getMethod("setName",String.class);  //传入参数需要使用类设定，如果有多个参数，则定义多个类别的类，直接用逗号分割开。
    m1.invoke(mr,"abc");
  }


  /**
   * 类加载器
   * @throws IOException
   * @throws ClassNotFoundException
   * @throws NoSuchMethodException
   * @throws IllegalAccessException
   * @throws InvocationTargetException
   * @throws InstantiationException
   */
  @Test
  public void test0() throws IOException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
    MyReflect mr=new MyReflect();
    Class clazz= mr.getClass();  //实际就是一个指针，指向运行时的类

    //使用classloader加载配置文件
    ClassLoader loader=this.getClass().getClassLoader();
    InputStream in=loader.getResourceAsStream("com/package/property.prop");  //从具体的包里面加载文件。
    Properties p=new Properties();
    p.load(in);
    String name=p.getProperty("name");

    //1.获取一个系统类加载器
    ClassLoader classloader = ClassLoader.getSystemClassLoader();
    System.out.println(classloader);
    //2.获取系统类加载器的父类加载器，即扩展类加载器
    classloader = classloader.getParent();
    System.out.println(classloader);
    //3.获取扩展类加载器的父类加载器，即引导类加载器
    classloader = classloader.getParent();
    System.out.println(classloader);
    //4.测试当前类由哪个类加载器进行加载
    classloader = Class.forName("exer2.ClassloaderDemo").getClassLoader();
    System.out.println(classloader);

    //1.根据全类名获取对应的Class对象
    String name1 = "com.java.MyReflect";
    Class clazz1 = Class.forName(name);
    //2.调用指定参数结构的构造器，生成Constructor的实例
    Constructor<MyReflect> con = clazz1.getConstructor(String.class,Integer.class);
    //3.通过Constructor的实例创建对应类的对象，并初始化类属性
    MyReflect p2 = con.newInstance("Peter",20);
    System.out.println(p2);

  }


  /**
   * 以下都是Class类实例
   */
  @Test
  public void test1(){
    Class c1 = Object.class;
    Class c2 = Comparable.class;
    Class c3 = String[].class;
    Class c4 = int[][].class;
    Class c5 = ElementType.class;
    Class c6 = Override.class;
    Class c7 = int.class;
    Class c8 = void.class;
    Class c9 = Class.class;

    int[] a = new int[10];
    int[] b = new int[100];
    Class c10 = a.getClass();
    Class c11 = b.getClass();
    // 只要数组的元素类型与维度一样，就是同一个Class
    System.out.println(c10 == c11);
  }
}





class MyReflect{
  private String name;
  private int age;

  public MyReflect() {
  }

  public MyReflect(String name, int age) {
    this.name = name;
    this.age = age;
  }

  public String getName() {
    return name;
  }

  public int getAge() {
    return age;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setAge(int age) {
    this.age = age;
  }

  @Override
  public String toString() {
    return "MyReflect{" +
        "name='" + name + '\'' +
        ", age=" + age +
        '}';
  }
}



public class lesson_14_reflect_1 {
}
