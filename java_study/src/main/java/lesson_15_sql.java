
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;


public class lesson_15_sql {
  public static void main(String[] args) throws ClassNotFoundException, SQLException {
    Properties properties=new Properties();


    //加载注册驱动程序
    Class.forName("sun.jdbc.odbc.jdbcOdbcDriver");

    //取得对数据库的连接
    Connection conn=DriverManager.getConnection("jdbc:hive2://mnbdp-bms-nn2.mini1.cn:10000/default", properties);

    Driver  driver=DriverManager.getDriver("");
  }
}
