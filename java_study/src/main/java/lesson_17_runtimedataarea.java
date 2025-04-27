import com.sun.org.apache.bcel.internal.generic.ARETURN;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class lesson_17_runtimedataarea {
    public static void main(String[] args) {

        //一个jvm实例只有一个runtime对象（单例对象）
        //java中每个线程Thread都与本地线程一一映射，本地线程负责将任务调度到cpu执行。本地线程初始化后，就会调用Thread中的run方法


        //1、程序计数器（pc寄存器）：存储将要执行的指令地址，执行引擎从寄存器读取地址获取指令然后执行
        //程序计数器是线程私有的，每个线程都有自己单独的计数器，与线程生命周期一致
        //用于程序控制流，如循环、条件判断等


    }

    public String solve(String IP) {

        List<Integer> integers = Arrays.asList(1, 2, 3, 4);


        if (IP.contains(".")) {
            String[] split = IP.split("\\.");
            if (split.length != 4) return "Neither";
            for (String sp : split) {
                try {
                    int spTmp = Integer.parseInt(sp);
                    if (spTmp > 255 || spTmp < 0) return "Neither";
                    else if (sp.startsWith("0")) return "Neither";
                } catch (Throwable e) {
                    return "Neither";
                }
            }
            return "IPv4";
        } else if (IP.contains(":")) {
            String[] split = IP.split(":");
            String regx = "[A-Fa-f0-9]";
            if (split.length != 6) return "Neither";
            for (String sp : split) {
//                if(sp.length()==0) return "Neither";
                Boolean b = sp.matches(regx) && (sp.length() == 4 || sp.equals("0"));
                if (!b) return "Neither";
            }

            return "IPv6";
        }
        else return "Neither";
    }
}
