import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;


/**
 * String类:代表字符串。Java 程序中的所有字符串字面值(如 "abc" )都作 为此类的实例实现。
 * String是一个final类，代表不可变的字符序列。
 * 字符串是常量，用双引号引起来表示。它们的值在创建之后不能更改。
 * String对象的字符内容是存储在一个字符数组value[]中的。
 *
 * 二、    String:字符串，使用一对""引起来表示。
 *     1.String声明为final的，不可被继承
 *     2.String实现了Serializable接口：表示字符串是支持序列化的。
 *             实现了Comparable接口：表示String可以比较大小
 *     3.String内部定义了final char[] value用于存储字符串数据
 *     4.String:代表不可变的字符序列。简称：不可变性。
 *         体现：1.当对字符串重新赋值时，需要重写指定内存区域赋值，不能使用原有的value进行赋值。
 *              2. 当对现有的字符串进行连接操作时，也需要重新指定内存区域赋值，不能使用原有的value进行赋值。
 *              3. 当调用String的replace()方法修改指定字符或字符串时，也需要重新指定内存区域赋值，不能使用原有的value进行赋值。
 *     5.通过字面量的方式（区别于new）给一个字符串赋值，此时的字符串值声明在字符串常量池中。
 *     6.字符串常量池中是不会存储相同内容的字符串的。
 */
public class lesson_21_string {


    /*
    String的实例化方式：
    方式一：通过字面量定义的方式
    方式二：通过new + 构造器的方式

     面试题：String s = new String("abc");方式创建对象，在内存中创建了几个对象？
            两个: 一个是堆空间中new结构，另一个是char[]对应的常量池中的数据："abc"

     String str = "hello";
       //本质上this.value = new char[0]; String s1 = new String();
    //this.value = original.value;
    String s2 = new String(String original);
    //this.value = Arrays.copyOf(value, value.length);
    String s3 = new String(char[] a);
    String s4 = new String(char[] a,int startIndex,int count);

     */
    @Test
    public void test1(){
        //通过字面量定义的方式：此时的s1和s2的数据javaEE声明在方法区中的字符串常量池中。
        String s1 = "javaEE";
        String s2 = "javaEE";
        //通过new + 构造器的方式:此时的s3和s4保存的地址值，是数据在堆空间中开辟空间以后对应的地址值。
        String s3 = new String("javaEE");
        String s4 = new String("javaEE");

        System.out.println(s1 == s2);//true，比较s1和s2的地址值
        System.out.println(s1 == s3);//false
        System.out.println(s1 == s4);//false
        System.out.println(s3 == s4);//false

        System.out.println("***********************");
        Person p1 = new Person("Tom",12);
        Person p2 = new Person("Tom",12);

        System.out.println(p1.name.equals(p2.name));//true
        System.out.println(p1.name == p2.name);//true

        p1.name = "Jerry";
        System.out.println(p2.name);//Tom

        String s5 = "abc";
        String s6 = s4.replace('a', 'm');
        System.out.println(s5);//abc
        System.out.println(s6);//mbc
    }

    /**
     *     结论：
     *     1.常量与常量的拼接结果在常量池。且常量池中不会存在相同内容的常量。
     *     2.只要其中有一个是变量，结果就在堆中。
     *     3.如果拼接的结果调用intern()方法，返回值就在常量池中
     *      注意：
     *      s1 = s1 + "b"; 或 s1 += "b" 的方式：如果多次执行这些改变串内容的操作，会导致大量副本
     *      字符串对象存留在内存中，降低效率。如果这样的操作放到循环中，会极大影响 程序的性能。
     */
    @Test
    public void test2(){
        String s1 = "javaEE";
        String s2 = "hadoop";

        String s3 = "javaEEhadoop";
        String s4 = "javaEE" + "hadoop";
        String s5 = s1 + "hadoop";
        String s6 = "javaEE" + s2;
        String s7 = s1 + s2;

        final String s9="javaEE";
        String s10=s9 + "hadoop";
        System.out.println(s3 == s10);//true  ,s9经过final修饰后转为常量；

        System.out.println(s3 == s4);//true
        System.out.println(s3 == s5);//false
        System.out.println(s3 == s6);//false
        System.out.println(s3 == s7);//false
        System.out.println(s5 == s6);//false
        System.out.println(s5 == s7);//false
        System.out.println(s6 == s7);//false

        String s8 = s6.intern();//返回值得到的s8使用的常量值中已经存在的“javaEEhadoop”
        System.out.println(s3 == s8);//true
    }


    /**
     * 替换：
     * String replace(char oldChar, char newChar)：返回一个新的字符串，它是通过用 newChar 替换此字符串中出现的所有 oldChar 得到的。
     * String replace(CharSequence target, CharSequence replacement)：使用指定的字面值替换序列替换此字符串所有匹配字面值目标序列的子字符串。
     * String replaceAll(String regex, String replacement)：使用给定的 replacement 替换此字符串所有匹配给定的正则表达式的子字符串。
     * String replaceFirst(String regex, String replacement)：使用给定的 replacement 替换此字符串匹配给定的正则表达式的第一个子字符串。
     * 匹配:
     * boolean matches(String regex)：告知此字符串是否匹配给定的正则表达式。
     * 切片：
     * String[] split(String regex)：根据给定正则表达式的匹配拆分此字符串。
     * String[] split(String regex, int limit)：根据匹配给定的正则表达式来拆分此字符串，最多不超过limit个，如果超过了，剩下的全部都放到最后一个元素中。
     * 判断：
     * boolean endsWith(String suffix)：测试此字符串是否以指定的后缀结束
     * boolean startsWith(String prefix)：测试此字符串是否以指定的前缀开始
     * boolean startsWith(String prefix, int toffset)：测试此字符串从指定索引开始的子字符串是否以指定前缀开始
     * 索引操作：
     * boolean contains(CharSequence s)：当且仅当此字符串包含指定的 char 值序列时，返回 true
     * int indexOf(String str)：返回指定子字符串在此字符串中第一次出现处的索引
     * int indexOf(String str, int fromIndex)：返回指定子字符串在此字符串中第一次出现处的索引，从指定的索引开始
     * int lastIndexOf(String str)：返回指定子字符串在此字符串中最右边出现处的索引
     * int lastIndexOf(String str, int fromIndex)：返回指定子字符串在此字符串中最后一次出现处的索引，从指定的索引开始反向搜索
     *
     * 注：indexOf和lastIndexOf方法如果未找到都是返回-1
     *
     * 字符操作：
     * int length()：返回字符串的长度： return value.length
     * char charAt(int index)： 返回某索引处的字符return value[index]
     * boolean isEmpty()：判断是否是空字符串：return value.length == 0
     * String toLowerCase()：使用默认语言环境，将 String 中的所有字符转换为小写
     * String toUpperCase()：使用默认语言环境，将 String 中的所有字符转换为大写
     * String trim()：返回字符串的副本，忽略前导空白和尾部空白
     * boolean equals(Object obj)：比较字符串的内容是否相同
     * boolean equalsIgnoreCase(String anotherString)：与equals方法类似，忽略大小写
     * String concat(String str)：将指定字符串连接到此字符串的结尾。 等价于用“+”
     * int compareTo(String anotherString)：比较两个字符串的大小
     * String substring(int beginIndex)：返回一个新的字符串，它是此字符串的从beginIndex开始截取到最后的一个子字符串。
     * String substring(int beginIndex, int endIndex) ：返回一个新字符串，它是此字符串从beginIndex开始截取到endIndex(不包含)的一个子字符串。
     */
    public void test3(){
        String s1 = "HelloWorld";
        String s2 = s1.toLowerCase();
        System.out.println(s1);//s1不可变的，仍然为原来的字符串
        System.out.println(s2);//改成小写以后的字符串

        System.out.println(s1.equals(s2));
        System.out.println(s1.equalsIgnoreCase(s2));

        String x= String.valueOf(s1.charAt(0));

        System.out.println(s1.contains("Wo"));


    }


    /**
     * 类型转换：
     * 一、String 与 byte[]之间的转换：
     *     编码：String --> byte[]:调用String的getBytes()
     *     解码：byte[] --> String:调用String的构造器
     *
     *     编码：字符串 -->字节  (看得懂 --->看不懂的二进制数据)
     *     解码：编码的逆过程，字节 --> 字符串 （看不懂的二进制数据 ---> 看得懂）
     *
     *     说明：解码时，要求解码使用的字符集必须与编码时使用的字符集一致，否则会出现乱码。
     *
     * 二、String 与 char[]之间的转换
     *
     *     String --> char[]:调用String的toCharArray()
     *     char[] --> String:调用String的构造器
     *
     * 三、String 与基本数据类型、包装类之间的转换。
     *
     *     String --> 基本数据类型、包装类：调用包装类的静态方法：parseXxx(str)
     *     基本数据类型、包装类 --> String:调用String重载的valueOf(xxx)
     */
    public void test4() throws UnsupportedEncodingException {
        String str1 = "abc123中国";
        byte[] bytes = str1.getBytes();//使用默认的字符集，进行编码。
        System.out.println(Arrays.toString(bytes));

        byte[] gbks = str1.getBytes("gbk");//使用gbk字符集进行编码。
        System.out.println(Arrays.toString(gbks));

        String s = new String(bytes);
        String str4 = new String(gbks, "gbk"); //编码集必须对应，否则乱码


        char[] charArray = str1.toCharArray();
        for (int i = 0; i < charArray.length; i++) {
            System.out.println(charArray[i]);
        }

        char[] arr = new char[]{'h','e','l','l','o'};
        String strC = new String(arr);
        System.out.println(strC);

        String str2 = "123";
//        int num = (int)str1;//错误的
        int num = Integer.parseInt(str2);

        String str3 = String.valueOf(num);//"123"
        String str5 = num + "";

    }


    /**
     *     String、StringBuffer、StringBuilder三者的异同？
     *     String:不可变的字符序列；底层使用char[]存储
     *     StringBuffer:可变的字符序列；底层使用char[]; 存储线程安全的，效率低
     *     StringBuilder:可变的字符序列；jdk5.0新增的；底层使用char[]存储，线程不安全的，效率高,建议使用
     *
     *     源码分析：
     *     String str = new String();//char[] value = new char[0];
     *     String str1 = new String("abc");//char[] value = new char[]{'a','b','c'};
     *
     *     StringBuffer sb1 = new StringBuffer();//char[] value = new char[16];底层创建了一个长度是16的数组。
     *     System.out.println(sb1.length());//
     *     sb1.append('a');//value[0] = 'a';
     *     sb1.append('b');//value[1] = 'b';
     *
     *     StringBuffer sb2 = new StringBuffer("abc");//char[] value = new char["abc".length() + 16];
     *
     *     //问题1. System.out.println(sb2.length());//3   添加一个字符计数加一
     *     //问题2. 扩容问题:如果要添加的数据底层数组盛不下了，那就需要扩容底层的数组。
     *              默认情况下，扩容为原来容量的2倍 + 2，同时将原有数组中的元素复制到新的数组中。
     *
     *             指导意义：开发中建议大家使用：StringBuffer(int capacity) 或 StringBuilder(int capacity)
     *
     *     对比String、StringBuffer、StringBuilder三者的效率：
     *     从高到低排列：StringBuilder > StringBuffer > String
     */

    public void test5(){
        StringBuffer sb1 = new StringBuffer("abc");
        sb1.setCharAt(0,'m');  //修改位置了
        System.out.println(sb1); //mbc

        StringBuffer sb2 = new StringBuffer();
        System.out.println(sb2.length());//0

        StringBuilder builder = new StringBuilder("");

    }


    /**
     *    StringBuffer的常用方法：
     * StringBuffer append(xxx)：提供了很多的append()方法，用于进行字符串拼接
     * StringBuffer delete(int start,int end)：删除指定位置的内容
     * StringBuffer replace(int start, int end, String str)：把[start,end)位置替换为str
     * StringBuffer insert(int offset, xxx)：在指定位置插入xxx
     * StringBuffer reverse() ：把当前字符序列逆转
     * public int indexOf(String str)
     * public String substring(int start,int end):返回一个从start开始到end索引结束的左闭右开区间的子字符串
     * public int length()
     * public char charAt(int n )
     * public void setCharAt(int n ,char ch)
     *
     *         总结：
     *         增：append(xxx)
     *         删：delete(int start,int end)
     *         改：setCharAt(int n ,char ch) / replace(int start, int end, String str)
     *         查：charAt(int n )
     *         插：insert(int offset, xxx)
     *         长度：length();
     *         *遍历：for() + charAt() / toString()
     */

    public void test6(){
        StringBuffer s1 = new StringBuffer("abc");
        s1.append(1);
        s1.append('1');
        System.out.println(s1);
//        s1.delete(2,4);
//        s1.replace(2,4,"hello");
//        s1.insert(2,false);
//        s1.reverse();
        String s2 = s1.substring(1, 3);

    }


}
