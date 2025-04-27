

/**
 * String 类
 * 字符串的字符使用Unicode字符编码，一个字符占两个字节
 * String  s1 = new String();
 * String  s2 = new String(String original);
 * String  s3 = new String(char[] a);
 * String  s4 =  new String(char[] a,int startIndex,int count)
 *
 * String是一个final类，代表不可变的字符序列
 * String str  = “abc”;与String str1 = new String(“abc”);的区别？
 * 答：String类型的值通过字符串常量池进行存储，直接引号赋值，栈地址直接指向常量池地址，通过new新建则在堆中设置空间，然后再指向常量池
 *
 * String str1  = “abc”；str1=str1+“123”. 意味着修改了常量池中存储的地址，存储新值，原来的值因无引用被回收
 * 字符串常用方法：
 * public int length()
 * public char charAt(int index)
 * public boolean equals(Object anObject)
 * public int compareTo(String anotherString)
 * public int indexOf(String s)  返回s字符串首次出现位置，若无则返回-1
 * public int indexOf(String s ,int startpoint)   从startpoint开始位置
 * public int lastIndexOf(String s)
 * public int lastIndexOf(String s ,int startpoint)
 * public boolean startsWith(String prefix)
 * public boolean endsWith(String suffix)
 * public boolean regionMatches(int firstStart,String other,int otherStart ,int length) 从firstStart开始的子串，与另一个字符串从otherStart开始，长度为length的子串是否相同
 * 字符串对象修改：
 * public String substring(int startpoint)
 * public String substring(int start,int end)
 * pubic String replace(char oldChar,char newChar)
 * public String replaceAll(String old,String new)
 * public String trim()
 * public String concat(String str)
 * public String[] split(String regex)
 *
 *
 * 互相转换：
 * 1、Byte、Short、Long、Float、Double等：通过相应包装类,如Integer.parseInt(String s)的方式解析
 * 2、转换为字符串：String.valueOf(value)的方式转换
 * 3、字符数组转换为字符串：new String(char[]) 和 new String(char[]，int offset，int length)
 * 4、字符串转换为字符数组：public char[] toCharArray()；public void getChars(int srcBegin, int srcEnd, char[] dst, int dstBegin)
 * 4、字节数组转换为字符串：new String(byte[])； new String(byte[]，int offset，int length)
 * 5、字符串转换为字节数组：public byte[] getBytes() ；public byte[] getBytes(String charsetName) （使用编码）
 *
 */
//------------------------------------------------------------------------------------
/**
 * StringBuffer类：可变的字符序列，也是String容器
 * 很多方法与String相同，但StingBuffer是可变长度的；
 * 有三个构造方法：
 * 1．StringBuffer()初始容量为16的字符串缓冲区
 * 2．StringBuffer(int size)构造指定容量的字符串缓冲区
 * 3．StringBuffer(String str)将内容初始化为指定字符串内容
 * 常用方法:
 * StringBuffer append(String s),   StringBuffer append(int n) ,
 * StringBuffer append(Object o) ,  StringBuffer append(char n),
 * StringBuffer append(long n),  StringBuffer append(boolean n),  append默认都是调用String.valueOf方法将不同类型对象转换为string
 * StringBuffer insert(int index, String str)
 * public StringBuffer reverse()
 * StringBuffer delete(int startIndex, int endIndex)
 * public char charAt(int n )
 * public void setCharAt(int n ,char ch)
 * StringBuffer replace( int startIndex ,int endIndex, String str)
 * public int indexOf(String str)
 * public String substring(int start,int end)
 * public int length()
 *
 */
//------------------------------------------------------------------------------------

/**
 * StringBuilder类: 非常类似StringBuffer类，但是更新。
 * 相比StringBuffer，可变字符序列、效率高、线程不安全。StringBuffer是线程安全的（方法都有synchronized）
 *
 */

import org.junit.Test;

import java.util.Calendar;
import java.util.Date;
import java.text.SimpleDateFormat;

/**
 * string使用陷阱（规范）：
 * string s="a"; 创建了一个字符串   s=s+"b";
 * 实际上原来的"a"字符串对象已经丢弃了，现在又产生了一个字符串s+"b"（也就是"ab")。
 * 如果多次执行这些改变串内容的操作，会导致大量副本字符串对象存留在内存中，降低效率。如果这样的操作放到循环中，会极大影响程序的性能。
 */


public class lesson_13_hotclass {
  public static void main(String[] args) {
    StringBuffer buffer = new StringBuffer("我喜欢学习");
    buffer.append("数学");

    StringBuilder sbd=new StringBuilder();
    sbd.append("数学");   //方法无synchronized，多线程操作需要留意。

  }
}

/**
 * 1.java.lang.System类：
 * public static long currentTimeMillis()用来返回当前时间与1970年1月1日0时0分0秒之间以毫秒为单位的时间差。适于计算时间差。
 */

/**
 * java.util.Date类
 * 构造器
 *Date( )使用Date类的无参数构造方法创建的对象可以获取本地当前时间。
 * Date(long date)
 * 常用方法：
 * getTime():返回自 1970 年 1 月 1 日 00:00:00 GMT 以来此 Date 对象表示的毫秒数。
 * toString():把此 Date 对象转换为以下形式的 String： dow mon dd hh:mm:ss zzz yyyy 其中： dow是一周中的某一天 (Sun, Mon, Tue, Wed, Thu, Fri, Sat)，zzz是时间标准。
 *
 * Date类的API不易于国际化，大部分被废弃了
 */

/**
 * java.text.SimpleDateFormat类是一个不与语言环境有关的方式来格式化和解析日期的具体类。
 * 允许进行格式化（日期文本）、解析（文本日期）
 * SimpleDateFormat() ：默认的模式和语言环境创建对象
 * public SimpleDateFormat(String pattern)：该构造方法可以用参数pattern指定的格式创建一个对象，该对象调用：public String format(Date date)方法格式化时间对象date
 *
 * public Date parse(String source)：从给定字符串的开始解析文本，以生成一个日期。
 */

class TestDate{

  @Test
  public void test(){
    Date dt=new Date();  //当前时间
    dt.getTime();

    SimpleDateFormat sdf=new SimpleDateFormat();
    sdf.format(dt);
    sdf.parse("");

    SimpleDateFormat formater2 = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
    formater2.parse("2020年12月31日 20:00:00");
  }
}

/**
 * java.util.Calendar(日历)类
 * Calendar是一个抽象基类，主用用于完成日期字段之间相互操作的功能。
 * 获取Calendar实例的方法:
 * 1、使用Calendar.getInstance()方法
 * 2、调用它的子类GregorianCalendar的构造器。
 */
class TestCalendar{
  @Test
  public void test(){
    Calendar c=Calendar.getInstance();   //获取当前日期实例。
    int day=c.get(Calendar.DAY_OF_MONTH);  //当前日期为当月第几天
    c.add(Calendar.DAY_OF_MONTH,-2);  //原处修改，将实例修改为增减几天的日期
    c.set(Calendar.DAY_OF_MONTH,23);  //直接设置

    SimpleDateFormat formater2 = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
    Date dt=formater2.parse("2020年12月31日 20:00:00");

    c.setTime(dt);  //还可以设置日期。
    Date d1=c.getTime();  //返回日期。

  }
}


/**
 * math类
 * java.lang.Math提供了一系列静态方法用于科学计算；其方法的参数和返回值类型一般为double型。
 * abs     绝对值
 * acos,asin,atan,cos,sin,tan  三角函数
 * sqrt     平方根
 * pow(double a,doble b)     a的b次幂
 * log    自然对数
 * exp    e为底指数
 * max(double a,double b)
 * min(double a,double b)
 * random()      返回0.0到1.0的随机数
 * long round(double a)     double型数据a转换为long型（四舍五入）
 * toDegrees(double angrad)     弧度—>角度
 * toRadians(double angdeg)     角度—>弧度
 *
 *
 */

/**
 * BigInteger类：Integer类作为int的包装类，能存储的最大整型值为2^31−1，BigInteger类的数字范围较Integer类的数字范围要大得多，可以支持任意精度的整数。
 * 构造器
 * BigInteger(String val)
 * 常用方法
 * public BigInteger abs()
 * public BigInteger add(BigInteger val)
 * public BigInteger subtract(BigInteger val)
 * public BigInteger multiply(BigInteger val)
 * public BigInteger divide(BigInteger val)
 * public BigInteger remainder(BigInteger val)
 * public BigInteger pow(int exponent)
 * public BigInteger[] divideAndRemainder(BigInteger val)
 */

/**
 * BigDecimal类：
 * 一般的Float类和Double类可以用来做科学计算或工程计算，但在商业计算中，要求数字精度比较高，故用到java.math.BigDecimal类。BigDecimal类支持任何精度的定点数
 * 构造器
 * public BigDecimal(double val)
 * public BigDecimal(String val)
 * 常用方法
 * public BigDecimal add(BigDecimal augend)
 * public BigDecimal subtract(BigDecimal subtrahend)
 * public BigDecimal multiply(BigDecimal multiplicand)
 * public BigDecimal divide(BigDecimal divisor, int scale, int roundingMode)
 *
 */
