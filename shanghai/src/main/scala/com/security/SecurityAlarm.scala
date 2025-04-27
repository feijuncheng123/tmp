
/**
  * 功能：主运行逻辑
  * 作者：费俊程
  * 创建日期：2018.12.21：16.16
  */

package com.security


import org.apache.spark.sql.{DataFrame, Dataset, SaveMode, SparkSession}
import com.typesafe.scalalogging.Logger
import org.apache.log4j.BasicConfigurator
import org.slf4j.LoggerFactory
import java.io.StringWriter
import java.io.PrintWriter
import org.apache.log4j
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs._


object SecurityAlarm {
    val logger=Logger(this.getClass)
    /**
      * arg(0): 运行的模块，共支持6个数据模块和一个all模块，同时传入多个用逗号隔开
      * arg(1): 数据源路径，必须传入日数据、月汇总数据、imei码数据的路径，用逗号隔开
      * arg(2): 数据源保存路径，所有数据保存到一个路径中，仅传入一个路径就行
      * arg(3)：计算流量波动的指标，必须传入一个整数
      * 结果保存：采用了overwrite模式，会覆盖已存在的同名文件
      * @param args: Array[String]
      */
    def main(args: Array[String]): Unit = {
        try {
            log4j.Logger.getLogger("org").setLevel(log4j.Level.ERROR)
//            log4j.Logger.getLogger("main").setLevel(log4j.Level.ERROR)
//            BasicConfigurator.configure()
            assert(args.length == 4,"缺少关键参数，请按顺序传入以下参数：运行模块、资源路径、存储路径、流量波动倍数！")
            logger.info(s"所传入的参数为：运行模式：${args(0)};资源路径：${args(1)}；结果存储路径：${args(2)}；流量波动倍数：${args(3)}")
            assert(args(1).split(",").length==3,"请传入日数据、月数据、imei数据路径")
            val sparksess = SparkSession.builder().appName("securityAlarm").getOrCreate()
            val dailypath=args(1).split(",")(0).trim()
            val monthpath=args(1).split(",")(1).trim()

            val imeipath=args(1).split(",")(2).trim()
            val dailyobj = dailyObj(sparksess, dailypath)
            val monthobj = monthObj(sparksess, monthpath)
            val module=args(0).split(",")
            for(m<-module)
                {logger.info(s"开始执行：$m 部分数据")
                    m.trim().toLowerCase match {
                    case "all" =>
                        allData(dailyobj, monthobj, args(3).toInt, args(2))
                        imeiDataSave(sparksess,imeipath,args(2))
                    case "dailydata" =>
                        dailyDataSave(dailyobj, args(3).toInt, args(2))
                    case "monthdata" =>
                        monthDataSave(monthobj, args(2))
                    case "silencedata" =>
                        useDaysDataSave(monthobj, args(2))
                    case "imeidata" =>
                        imeiDataSave(sparksess, imeipath, args(2))
                    case "modeldata" =>
                        val distancedata=pictureDistanceData(dailyobj)
                        modelDataSave(dailyobj, monthobj,distancedata, args(2))
                    case "picturedata" =>
                        pictureDataSave(dailyobj, monthobj,args(2))
                    }
                    logger.info(s"$m 部分数据已执行完毕！")
                }
            sparksess.close()
            logger.info("执行成功！")
        }
        catch {
            case e: Throwable =>
               val sw = new StringWriter
               e.printStackTrace(new PrintWriter(sw))
               logger.error(sw.toString)
                logger.info("执行失败！")
                sys.exit(1)
        }
    }

    def allData(dailyobj:DailyDataProcess,monthobj:MonthDataProcess,weigh:Int,savepath:String): Unit ={
        logger.info("开始执行日数据计算！")
        dailyDataSave(dailyobj,weigh,savepath)
        logger.info("开始执行月汇总数据计算！")
        monthDataSave(monthobj,savepath)
        logger.info("开始执行使用天数计算！")
        useDaysDataSave(monthobj,savepath)
        logger.info("开始执行建模数据计算！")
        val distancedata=pictureDistanceData(dailyobj)
        modelDataSave(dailyobj,monthobj,distancedata,savepath)
        logger.info("开始执行绘图数据计算！")
        pictureDataSave(dailyobj,monthobj,savepath)
    }

    def dailyDataSave(dailyobj:DailyDataProcess,weigh:Int,savepath:String): Unit ={
        val dailydata=dailyData(dailyobj,weigh)
        logger.info("4.7.5号码日使用情况表（日表）头部数据：")
        dailydata.show(20)
        savaData(dailydata,savepath,"dailydata_4_7_5.csv")
    }

    def monthDataSave(monthobj:MonthDataProcess,savepath:String): Unit ={
        val monthdata=monthData(monthobj)
        logger.info("4.7.6号码月使用情况表（月表）头部数据：")
        monthdata.show(20)
        savaData(monthdata,savepath,"monthdata_4_7_6.csv") //第一、二、四张图片数据在此表
    }

    def useDaysDataSave(monthobj:MonthDataProcess,savepath:String): Unit ={
        val usedays=useDays(monthobj)
        logger.info("4.7.4 短期沉默用户表（月表）头部数据：")
        usedays.show(20)
        savaData(usedays,savepath,"silentusers_4_7_4.csv")  //第三张图片使用在此表
    }

    def modelDataSave(dailyobj:DailyDataProcess,monthobj:MonthDataProcess,distancedata:DataFrame,savepath:String): Unit ={
        distancedata.show(20)
        savaData(distancedata,savepath,"p_distancedata.csv")

        val modeldata=modelData(dailyobj,monthobj,distancedata)
        logger.info("建模特征表头部数据：")
        modeldata.show(20)
        savaData(modeldata,savepath,"modeldata.csv")   //模型数据存于此表中
    }

    def pictureDataSave(dailyobj:DailyDataProcess,monthobj:MonthDataProcess,savepath:String): Unit ={
        val hourfrequence=pictureHourFrequence(monthobj)
        logger.info("绘图用每小时使用次数数据：")
        hourfrequence.show(20)
        savaData(hourfrequence,savepath,"p_hourfrequence.csv")

        val hourupvolume=pictureHourUpVolume(monthobj)
        logger.info("绘图用每小时上行流量数据：")
        hourupvolume.show(20)
        savaData(hourupvolume,savepath,"p_hourupvolume.csv")

        val hourdownvolume=pictureHourDownVolume(monthobj)
        logger.info("绘图用每小时下行流量数据：")
        hourdownvolume.show(20)
        savaData(hourdownvolume,savepath,"p_hourdownvolume.csv")
    }

    def imeiDataSave(sparksess:SparkSession,sourcepath:String,savepath:String): Unit ={
        val obj=new ImeiProcess(sparksess,sourcepath)
        val imeidata=obj.imeiProcess()
        logger.info("4.7.3 终端换机表（月表）头部数据：")
        imeidata.show(20)
        savaData(imeidata,savepath,"imeidata_4_7_3.csv")
    }

    private def useDays(monthobj:MonthDataProcess)=monthobj.useDaysCount()
    private def dailyData(dailyobj:DailyDataProcess,weigh:Int): DataFrame =dailyobj.dailyDataProcee(weigh)
    private def monthData(monthobj:MonthDataProcess): DataFrame =monthobj.monthDataProcee()

    private def modelData(dailyobj:DailyDataProcess,monthobj:MonthDataProcess,distancedata:DataFrame): DataFrame ={
        val meandistance=dailyobj.meanDistance(distancedata)
        val maindata=monthobj.modelData()
        maindata.join(meandistance,Seq("USER_NUMBER"),"left")
    }

    private def pictureDistanceData(dailyobj:DailyDataProcess): DataFrame ={
        logger.info("绘图用稳定度数据：")
        dailyobj.EucliDistance()
    }
    private def pictureHourFrequence(monthobj:MonthDataProcess)=monthobj.hourUseFrequency()
    private def pictureHourUpVolume(monthobj:MonthDataProcess)=monthobj.hourUpVolume()
    private def pictureHourDownVolume(monthobj:MonthDataProcess)=monthobj.hourDownVolume()

    private def dailyObj(sparksess:SparkSession,path:String)=new DailyDataProcess(sparksess,path)
    private def monthObj(sparksess:SparkSession,path:String)=new MonthDataProcess(sparksess,path)
    private def savaData(df:DataFrame,basepath:String,filename:String): Unit ={
        logger.info(s"$filename 正在保存文件！")
        val tmppath=s"$basepath/tmp_$filename"
        val path=s"$basepath/$filename"
        df.coalesce(1)
            .write
            .option("compression", "none")
            .option("timestampFormat", "yyyy-MM-dd HH:mm:ss")
            .mode(SaveMode.Overwrite)
            .csv(path)
//        merge(tmppath,path)
    }

//    private def merge(srcPath: String, dstPath: String): Unit =  {
//        val hadoopConfig = new Configuration()
//        val hdfs = FileSystem.get(hadoopConfig)
//        FileUtil.copyMerge(hdfs, new Path(srcPath), hdfs, new Path(dstPath), true, hadoopConfig, null)
//    }
}
