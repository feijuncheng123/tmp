import sun.misc.Launcher;

import java.net.URL;

public class lesson_16_classloader {
    public static void main(String[] args) {

        //jvm对class文件实行按需加载模式，且采用双亲委派模式加载：子类加载器收到加载请求，会首先逐级向上请求父类加载。父类加载成功则可，不成功才再往下加载
        //实质是核心包具有加载优先权。这种保护核心api的机制成为沙箱安全机制

        //引导类加载器bootstrapclass
        URL[] urLs = Launcher.getBootstrapClassPath().getURLs();  //引导类加载器为c++编写，但可以获取到加载的目录
        for(URL url : urLs){
            System.out.println(url.getPath());
        }


        //应用（系统）类加载器、扩展类加载器
        ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();  //应用类加载器
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader(); //当前线程上下文类加载器，也是应用类加载器
        //扩展类加载器主要加载ext目录下的文件
        ClassLoader parent = systemClassLoader.getParent();  //就是扩展类加载器



        //自定义加载类：
        //1、jar包隔离，特别是中间件开发中隔离jar包避免冲突；2、修改类加载方式；3、扩展加载源；4、防止源码泄露
        //实现方式：继承抽象类java.lang.ClassLoader，改写findClass方法（自定义逻辑放该方法中）
        //没有特殊需求可以直接继承URLClassLoader，可以避免重写findClass获取文件字节流


        //两个类是否为同一个类的判定条件：1、类名和路径完全一致；2、两个类的加载器必须相同
    }
}
