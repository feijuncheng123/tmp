import java.util.Scanner;

public class lesson_2_control {
  public static void main(String[] args) {
    //条件语句：if()else()
    int tag=0;
    if(tag<10){
      System.out.println("小于10");
    } else if(tag<15){
      System.out.println();
    } else {System.out.println();}

    //从键盘输入：
    //Scanner是一个监控输入的类，System.in表示监控系统的输入。
    Scanner sc=new Scanner(System.in);
    //next()方法表示从Scanner里面获取下一个值，为字符串
    String s1=sc.next();
    System.out.println(s1);

    //分支语句：switch case break
    //case 后面为常量，不能接条件判断；
    //必须每一分支后使用break，确保满足条件从该分支跳出。如果略去break，会导致满足条件的分支后面的其他分支也同时执行。最后一个可以省略，但依然建议加上。
    //switch(表达式)中表达式的返回值必须是下述几种类型之一：byte，short，char，int，枚举，String
    int input = sc.nextInt();
    switch (input) {
      case 10:
        System.out.println("等于10");
        break;
      case 100:
        System.out.println("等于100");
        break;
      case 1000:
        System.out.println("等于1000");
        break;
      default:
        System.out.println("default");
        break;
    }

    //for循环语句：for（申明语句；条件语句；自增语句）  自增语句在循环体结束时才执行
    //int i=0为变量申明语句，不能重复申明；在整个循环中只会执行一次；
    //++i和i++都会导致i自增1，并没有差异。但如果使用赋值如n<10;n=i++; 则会有差异；
    //每次循环开始时不会执行i++，循环结束时才会执行i++操作
    for(int i=0; i<10; i++){
      System.out.println(i);  //从0开始打印，而不是从1开始
    }

    //while循环语句：可以和for循环互换
    int i=0;
    while (i<100){
      System.out.println(i);
      i++;
    }

    //do ... while ...循环:使用较少
    int i1=0;
    do {
      System.out.println(i);
      i1++;
    } while (i1<100);

    //break:可以在for循环前添加标签（相当于变量名），然后使用continue或者break控制哪一层循环。
    label1:for(int i2=0; i2<10; i2++){
      label2:for(int i3=0; i3<5; i3++) {
        if(i2*i3==20){continue label1;}
        else if(i2*i3==30){break label2;}
      }
    }

  }
}
