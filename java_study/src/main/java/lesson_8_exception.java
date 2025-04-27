
//异常处理：语法错误和逻辑错误不叫异常
//Java程序在执行过程中所发生的异常事件可分为两类：
//Error:  Java虚拟机无法解决的严重问题。如：JVM系统内部错误、资源耗尽等严重情况。一般不编写针对性的代码进行处理
//Exception: 其它因编程错误或偶然的外在因素导致的一般性问题，可以使用针对性的代码进行处理。例如：空指针访问、试图读取不存在的文件、网络连接中断等

//所有异常或者Error都继承自java.lang.Throwable
//分类：编译时异常、运行时异常（除继承自RuntimeException的其他异常全部称为编译时异常）
//常见运行时异常：
//ArrayIndexOutOfBoundsException：数组下标越界；
//ArithmeticException：算术异常，比如除0；
//ClassCastException：类型转换异常。
//NullPointerException：空指针。栈内存中存储地址为空，但访问该变量

import javafx.beans.binding.ObjectExpression;
import org.junit.Test;

import java.io.*;

//编译时异常：
//java.io.FileNotFoundException：编译时无论文件有没有都会报错，必须对文件不存在的情况进行处理
public class lesson_8_exception {
  public static void main(String[] args) {
    String s="123";
    s=null;
    System.out.println(s.length());
  }

  @Test
  public void ioTest() throws IOException {
    FileInputStream f=new FileInputStream(new File("hellow.wxt"));  //必须添加异常处理语句
    f.close();
  }
}


//异常处理机制：
//Java提供的是异常处理的抓抛模型
//执行过程中如出现异常，会生成一个异常类对象，该异常对象将被提交给Java运行时系统，这个过程称为抛出(throw)异常。
//一旦抛出异常类对象，程序就终止执行。异常类对象会抛出给方法调用者
//java提供了两种方式处理抛出异常：
//方式一：try{}catch{}方式：printStackTrace可以打印出全部信息；getMessage()仅能输出错误的消息，而无法追踪。
//可以有多个catch语句


//方式二：显式的申明抛出异常，表名该方法不处理异常，而是由调用者处理
//异常可以逐层向上抛
class CatchException{
  FileInputStream f;

  void test01() throws FileNotFoundException,IOException {
    //显式的抛出异常。如果该方法报错，则抛出IOException，由调用者处理.如果可能出现不同错误，且需要做不同处理，可以定义多个抛出异常。
    //如果统一处理，则可以只保留父类
    //需要在调用者中定义异常处理方法

    try{
      f=new FileInputStream(new File("hellow.wxt"));
      Double a= 100.0/0;
    } catch (ArithmeticException e){
      e.printStackTrace();
      String es=e.getMessage();
    }  finally {                        //finally是可选的,但一般在需要关闭资源时使用。finally会无视try中的return语句，在try中return执行前会先执行finally中语句。如果finally中出现return则会跳过try中的return
      System.out.println("");
      f.close();
    }
  }

  void test2() throws FileNotFoundException,IOException {  //同样需要定义异常
    test01();
  }

  public int compareTo(Object o){
    if(this==o){return 0;}
    else throw new RuntimeException("");   //可以手动抛出异常
  }

}

//自定义异常:
//1、继承Exception或其他现有的异常类
//2、提供一个序列号，标识唯一的异常
//3、提供重载的构造器，提供异常信息
class MyException extends Exception {  //编译时异常
  static final long serialVersionUID = -3345676879124229238L;
  public MyException(){}
  public MyException(String msg){super(msg);}

}

class MyException01 extends RuntimeException {  //运行时异常

}