
import org.junit.Test;


//继承
//只能单继承。每个类只能继承一个父类，但可以多层继承
//子类不能直接访问父类中私有的(private)的成员变量和方法。
//方法重写：重写方法必须和被重写方法具有相同的方法名称、参数列表和返回值类型。
//重写方法不能使用比被重写方法更严格的访问权限
//重写和被重写的方法须同时为static的，或同时为非static的
//子类方法抛出的异常不能大于父类被重写方法的异常
class human{
    private String name;
    private int age;
    protected String weight;
    private String voice;

    public void setVoice(String voice) {
        this.voice = voice;
    }

    public human(String name, int age, String voice, String weight) {
        this.name = name;
        this.age = age;
        this.voice = voice;
        this.weight = weight;
    }

    public human() {
        this.name = "abc";
        this.age = 20;
        this.voice = "man";
        this.weight = "";
    }

    public void talk(String words){
        System.out.println(words);
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    public String getWeight() {
        return weight;
    }
}

//子类
//super关键字：super可用于访问父类中属性、成员方法、构造器
//子类构造器中需要对父类初始化，使用super进行初始化
//子类和父类存在方法属性重名的情况下，需要使用super和this进行修饰。比如id
//重写父类方法后，如果还想使用父类的方法，则需要super显式调用
//注意：super的追溯不仅限于直接父类
//子类中所有的构造器默认都会访问父类中空参数的构造器
//当父类中没有空参数的构造器时，子类的构造器必须通过this(参数列表)或者super(参数列表)语句指定调用本类或者父类中相应的构造器，且必须放在构造器的第一行
//父类有多个构造器，也可以在子类构造器中选择性特征父类构造器初始化。
//设计类时，尽量提供空参构造器
class man extends human  {
    protected String dick;

    public man(String name, int age, String voice, String weight) {
        super(name, age, voice, weight);
    }

    public man() { }   //默认调用了父类空参构造器

    public void setDick(String dick) {
        this.dick = dick;
    }

    //方法重写
    public void talk(String words){

        this.setVoice("man");
        System.out.println(words);
    }
}


//多态：
//方法的重载(overload)和重写(overwrite)
//对象的多态性   ——可以直接应用在抽象类和接口上。
//同一父类，可以有多个子类
//多态前提：类的继承；对父类方法的改写。
//子类可以转化为父类、但父类不能调用子类方法。

//多态的作用：
//子类可以替代父类使用，在传参时，用父类的类型，则所有子类都可以传入
//子类作为父类传参时，如果改写了父类的方法，实际调用改写后的方法。
//注意：属性改写不构成多态。即如果子类作为父类传入，调用的属性默认是父类的属性。（与方法改写不同）
//属性在创建实例是子类父类都会加载到堆内存。而方法不存在加载到内存的问题，直接被子类改写。
class MyTest{
    public static void main(String[] args) {
        human m=new man();  //创建子类但转化为父类
        String n=m.getName();
        man h= (man)m;  //强转为子类

        //判断是否可以强转：
        //instanceof是否是一个类的实例。
        if(!(m instanceof man)){System.out.println("是man的实例");}
    }
}

//Object类和equals
//所有类都是object类的子类。
//equals是object类的方法，所有对象都可以调用。

// ==号判断：
//对于基本数据类型，判断值是否相等，两端数据类型不一定相同。即使类型不相同，值相同也会返回true
//对于引用类型，则是比较变量的地址值是否相等。（实际都是比较栈空间中的存储值）

//内置equals方法比较实际也是使用==比较。
//equals方法只能处理引用类型变量
//string类型值相同相互比较为true，原因是string类改写了equals方法

//==和equals异同：
//==比较栈内存的存储值，对于引用类型实际比较内存地址是否相同
//object类中equals同==判断，但可以改写
//改写equals后，分别调用equals和==判断，会产生不同结果

//toString方法：
//Object类中toString方法返回string字符串，默认返回对象内存地址。
//重写toString方法

//包装类：基本数据类型对应的引用类型。大部分将首字母改为大写就是包装类。除了Integer和char对应的Character
//包装类属于类，而非基本数据类型

//junit测试类:Test注释后，在方法上通过右键点击，弹出菜单后可以执行相应方法。
//注解掉main方法，可以一次性全部执行test
public  class lesson_5_oop_pro{
    Integer a=123;
    int a1= a;  //包装类会自动转为基本数据类型，自动拆箱
    Character b='q';
    Float f=new Float("12.5F") ;
    Float f1=12.5F ;  //自动装箱
    String s=String.valueOf(true);   //值的互相转换
    String s1=new String(true);  //不行
    int i=Integer.parseInt("123");
    String s2="1234";
    int i1=(int)s2;  //两个类型没关系，不能强转

    Boolean b1=new Boolean("true"); //只有传入true字符串时为true，其他字符串都是false
    Boolean b2= Boolean.TRUE;


    public static void main(String[] args) {
        System.out.println("awd");
    }

    @Test
    public void run(){System.out.println("awdwdqwd");}
}


//static修饰符：
//静态的对象，可以用来修饰方法和属性，随着类的加载而加载，在有代码首次访问时实例化。所有实例共用同一副本，而非static不同实例各自拥有一个副本
//可以直接通过类名调用。
//修饰后变量成为类变量。所有实例共用同一个变量，一个实例修改了该变量，则全部实例都会更改。
//静态属性为每一个实例都公用的对象，不必在没一个实例中都存储。（静态域中存储）

//修饰方法：
//用static修饰的方法同样随着类的加载而加载（独一份），也可以通过类名直接调用。所有实例共用该方法
//静态方法内部可以调用其他静态属性或方法，但不能调用非静态的属性和方法。
//非静态方法却可以调用静态的方法或属性；

class Chinese{
    String name;
    int age;
    static String nation="中国";
}


