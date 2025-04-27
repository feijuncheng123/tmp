import org.junit.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Date;

public class lesson_11_io {

  @Test
  public void myIO() throws IOException, ClassNotFoundException {
    /**
     * java.io.File类的使用
     * IO原理及流的分类
     * 文件流(重点)
     * FileInputStream  /  FileOutputStream  /  FileReader  /  FileWriter
     * 缓冲流(重点)
     * BufferedInputStream / BufferedOutputStream /
     * BufferedReader / BufferedWriter
     * 转换流
     * InputStreamReader  /  OutputStreamWriter
     * 标准输入/输出流
     * 打印流（了解）
     * PrintStream  /  PrintWriter
     * 数据流（了解）
     * DataInputStream  /  DataOutputStream
     * 对象流    ----涉及序列化、反序列化 (重点)
     * ObjectInputStream  /  ObjectOutputStream
     * 随机存取文件流
     * RandomAccessFile
     */

    File file=new File("相对路径或绝对路径");
    //1、File对象可以表示一个文件也可以是文件目录。该对象与平台无关
    //2、File类对象的方法仅支持创建、删除、重命名等文件对象操作，涉及文件内容的修改只能通过io流进行完成(File 不能访问文件内容本身)
    //3、File对象可以作为参数传递给流的构造函数
    //File类的常见构造方法：
    //public File(String pathname)以pathname为路径创建File对象，可以是绝对路径或者相对路径，如果是相对路径，则默认的当前路径在系统属性user.dir中存储
    //public File(String parent,String child) 以parent为父路径，child为子路径创建File对象。
    //4、File类常用方法：
    //访问文件名
    file.getName(); //获取文件名，字符串
    file.getPath(); //获取文件路径，字符串
    file.getParent(); //获取上层文件夹路径
    file.getAbsoluteFile(); //获取带路径的文件对象
    file.getAbsolutePath(); //获取绝对路径
    file.renameTo(new File("新文件名")); //重命名文件，要求file原文件一定存在，而新命名文件一定不存在。可以作为剪切功能使用

//    文件检测
    file.exists();
    file.canWrite();
    file.canRead();
    file.isFile();
    file.isDirectory();

//    目录操作相关
    file.mkdir();   //如果上级目录不存在，则不会创建
    file.mkdirs();  //如果上级目录不存在，一同创建
    file.list();
    file.listFiles();

//    文件操作相关
    file.createNewFile();
    file.delete();
    file.deleteOnExit();

//    获取常规文件信息
    file.lastModified();
    file.length();


    /**
     * IO流
     * 用来处理设备之间的数据传输。
     * Java程序中，对于数据的输入/输出操作以”流(stream)” 的方式进行
     * 输入input：读取外部数据（磁盘、光盘等存储设备的数据）到程序（内存）中。
     * 输出output：将程序（内存）数据输出到磁盘、光盘等存储设备中
     * 流的分类:
     * 按操作数据单位: 字节流(8 bit)(InputStream, OutputStream)，字符流(16 bit)(Reader, Writer)
     * 按流向: 输入流，输出流
     * 按角色: 节点流,直接作用于文件(FileInputStream、FileReader,FileOutputStream、FileWriter)，处理流（缓冲流）
     *
     * 由四个基类（InputStream, OutputStream，Reader, Writer）派生出来的子类名称都是以其父类名作为子类名后缀
     */


    /**
     * 抽象基类			节点流（文件流）               缓冲流（处理流的一种,可以提升文件操作的效率）(主要使用，速度快效率高)
     * InputStream		FileInputStream		          BufferedInputStream
     * OutputStream		FileOutputStream		        BufferedOutputStream(flush())
     * Reader			    FileReader（文本）				    BufferedReader(readLine())
     * Writer			    FileWriter（文本）				    BufferedWriter(flush())
     */


    /**
     * InputStream & Reader： 除了处理单位不一样，其他一样。FileReader只能处理文本文件，对于图片视频等内容只能用字节流。文本都建议用FileReader
     * InputStream 和 Reader 是所有输入流的基类。
     * int read()
     * int read(byte[] b)
     * int read(byte[] b, int off, int len)
     */
    File f1=new File("paht");
    FileInputStream fips=new FileInputStream(f1);
    int b=fips.read();   //read读取一个字节。如果读到文件末尾会返回-1，表示读完。
    byte[] bts=new byte[20];
    int c=fips.read(bts);    //一次性读取20个字节，存储到bts数组中。返回读取字节的数量，同样的，读取到结尾会返回-1

    while ((b=fips.read()) != -1){
      System.out.println(b);
    }
    fips.close();   //一定要记得关闭

    FileReader fr=new FileReader(f1);
    char[] ch=new char[20];
    fr.read(ch);

    /**
     * OutputStream & Writer: 除了处理单位不一样，其他一样. FileWriter只能处理文本文件，对于图片视频等内容只能用字节流，文本都建议用FileWriter
     * void write(int b/int c);
     * void write(byte[] b/char[] cbuf);
     * void write(byte[] b/char[] buff, int off, int len);
     * void flush();
     * void close();
     */
    FileOutputStream fop=new FileOutputStream(f1);
    fop.write("abcd".getBytes());     //将字符串写入到文件。文件可以不存在，会自动创建。若存在会将原有文件覆盖。多次写入为append
    fop.close();

    //文本文件
    FileWriter fw = new FileWriter("Test.txt");
    fw.write("abcd");
    fw.close();


    /**
     * 缓冲流：在使用这些流类时，会创建一个内部缓冲区数组.方法和节点流几乎一样
     * BufferedInputStream 和 BufferedOutputStream
     * BufferedReader 和 BufferedWriter
     *缓冲流要“套接”在相应的节点流之上，对读写的数据提供了缓冲的功能，提高了读写的效率，同时增加了一些新的方法
     *对于输出的缓冲流，写出的数据会先在内存中缓存，使用flush()将会使内存中的数据立刻写出
     */

    ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream("1234".getBytes());
    BufferedReader bufferedReader = new BufferedReader(new StringReader("dfffg"));

    BufferedInputStream bis=new BufferedInputStream(new FileInputStream(new File("")));
    BufferedOutputStream bos=new BufferedOutputStream(new FileOutputStream(new File("")));
    bis.read(bts);  //同样的，读取字节存入到
    bos.write(bts);   //无默认换行。
    bos.flush(); //写入时建议都加上
    bis.close();
    bos.close();

    BufferedReader br=new BufferedReader(new FileReader(new File("")));
    BufferedWriter bw=new BufferedWriter(new FileWriter(new File("")));
    String d= br.readLine();

    bw.write(d + "\n");
    bw.newLine(); //也是换行，和"\n"不要重复使用
    br.close();


    /**
     * 转换流：字节流转换为字符流，或者字符流转换为字节流
     * InputStreamReader：将字节流转换为字符流（解码成字符）
     * OutputStreamWriter：将字符流转换为字节流（编码为字节）
     */
    InputStreamReader isr = new InputStreamReader(System.in, StandardCharsets.UTF_8);
    isr.close();

    InputStreamReader isr1 = new InputStreamReader(new FileInputStream(new File("")), StandardCharsets.UTF_8);
    BufferedReader br1=new BufferedReader(isr1); //读入缓冲流

    OutputStreamWriter osw=new OutputStreamWriter(new FileOutputStream(new File("")),StandardCharsets.UTF_8);
    BufferedWriter bw1=new BufferedWriter(osw);  //写入缓冲流

    isr1.close();
    br1.close();
    osw.close();
    bw1.close();


    /**
     * 标准输入输出流
     * System.in和System.out分别代表了系统标准的输入和输出设备。默认输入设备是键盘，输出设备是显示器
     * System.in的类型是InputStream
     * System.out的类型是PrintStream，其是OutputStream的子类FilterOutputStream 的子类
     * 通过System类的setIn，setOut方法对默认设备进行改变。
     * public static void setIn(InputStream in)
     * public static void setOut(PrintStream out)
     */


    /**
     * 打印流
     * PrintStream(字节打印流)和PrintWriter(字符打印流)
     * PrintStream和PrintWriter的输出不会抛出异常
     * PrintStream和PrintWriter有自动flush功能
     * System.out返回的是PrintStream的实例
     */
    FileOutputStream fos = null;
    try {
      fos = new FileOutputStream(new File("D:\\IO\\text.txt"));
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    assert fos != null;
    PrintStream ps = new PrintStream(fos,true); //创建打印输出流,设置为自动刷新模式(写入换行符或字节 '\n' 时都会刷新输出缓冲区)
    System.setOut(ps);  // 把标准输出流(控制台输出)改成文件
    for (int i = 0; i <= 255; i++) {  //输出ASCII字符
      System.out.print((char)i);
      if (i % 50 == 0) {   //每50个数据一行
        System.out.println(); // 换行
      }  }
    ps.close();

    /**
     * 数据流（了解）
     * 数据流有两个类：(用于读取和写出基本数据类型的数据）DataInputStream 和 DataOutputStream
     * DataInputStream中的方法
     *      boolean readBoolean()		byte readByte()
     *      char readChar()			float readFloat()
     *      double readDouble()		short readShort()
     *      long readLong()			int readInt()
     *      String readUTF()                                 void readFully(byte[] b)
     *
     * DataOutputStream中的方法
     * 将上述的方法的read改为相应的write即可。
     */

    DataOutputStream dos = new DataOutputStream(new FileOutputStream("d:\\IOTest\\destData.dat"));
    dos.writeUTF("ab中国");  //写UTF字符串
    dos.writeBoolean(false);  //写入布尔值
    dos.writeLong(1234567890L);  //写入长整数

    /**
     * 对象流（重要）：ObjectInputStream和OjbectOutputSteam
     * 用于存储和读取对象的处理流。它的强大之处就是可以把Java中的对象写入到数据源中，也能把对象从数据源中还原回来。
     * 序列化(Serialize)：用ObjectOutputStream类将一个Java对象写入IO流中。序列化的好处在于可将任何实现了Serializable接口的对象转化为字节数据，使其在保存和传输时可被还原
     * 反序列化(Deserialize)：用ObjectInputStream类从IO流中恢复该Java对象
     *
     * 不能序列化 static和 transient修饰的成员变量
     *
     * 可序列化的对象必须实现两个接口其中之一：Serializable；Externalizable（Serializable子类）.类中的属性类型也必须实现相应的接口
     *
     * 凡是实现Serializable接口的类都有一个表示序列化版本标识符的静态变量：private static final long serialVersionUID;建议显示声明
     *
     */

    ObjectSerial os1=new ObjectSerial(123L,"abcd");
    ObjectSerial os2=new ObjectSerial(456L,"xyz");
    ObjectOutputStream ops=new ObjectOutputStream(new FileOutputStream("obejct.txt"));
    ops.writeObject(os1);
    ops.flush();
    ops.writeObject(os2);
    ops.flush();
    ops.close();


    ObjectInputStream ois=new ObjectInputStream(new FileInputStream("obejct.txt"));
    ObjectSerial os3=(ObjectSerial)ois.readObject();
    ObjectSerial os4=(ObjectSerial)ois.readObject();
    ois.close();

    /**
     * 随机访问流：RandomAccessFile
     * 可以直接跳到文件的任意地方来读、写文件
     * 支持只访问文件的部分内容，可以向已存在的文件后追加内容
     * RandomAccessFile 对象包含一个记录指针，用以标示当前读写处的位置。RandomAccessFile 类对象可以自由移动记录指针：
     * long getFilePointer()：获取文件记录指针的当前位置
     * void seek(long pos)：将文件记录指针定位到 pos 位置
     *
     * 构造器
     * public RandomAccessFile(File file, String mode)
     * public RandomAccessFile(String name, String mode)
     *需要指定一个 mode 参数指定访问模式：
     * r: 以只读方式打开
     * rw：打开以便读取和写入
     * rwd:打开以便读取和写入；同步文件内容的更新
     * rws:打开以便读取和写入；同步文件内容和元数据的更新
     *
     */
    RandomAccessFile raf = new RandomAccessFile("test.txt", "rw");
    raf.seek(5);
    byte [] b = new byte[1024];

    int off = 0;
    int len = 5;
    raf.read(b, off, len);
    raf.getFilePointer();

    String str = new String(b, 0, len);
    System.out.println(str);

    String temp = raf.readLine();
    raf.seek(5);
    raf.write("xyz".getBytes());
    raf.write(temp.getBytes());

    raf.close();


  }
}

class ObjectSerial implements Serializable{
  Long id;
  String address;

  public ObjectSerial(Long id, String address) {
    this.id = id;
    this.address = address;
  }

  @Override
  public String toString() {
    return "ObjectSerial{" +
        "id=" + id +
        ", address='" + address + '\'' +
        '}';
  }
}