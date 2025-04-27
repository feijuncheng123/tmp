import logging
#处理日志文件。

#以下级别逐渐升高
logging.debug("调试信息")
logging.info('运行信息')
logging.warning('警告信息')    #会打印warning级别及以上的信息（error、critical）
logging.error('错误信息')
logging.critical('崩溃信息')

#默认级别warning.
logging.basicConfig(
    level=logging.DEBUG,  #level为调整默认级别
    filename="文件名.log", #filename指定输出的文件
    filemode='w',   #写入模式默认为追加，修改为w则写入，前期文件会被覆盖
    datefmt='%Y-%m-%d %H:%M:%S',   #时间显示方式。可省略，但默认会加上毫秒
    format='%(asctime)s %(lineno)d %(message)s'
    #asctime指字符串生成时间，lineno指行号，message指生成的信息
                    )

#创建对象
logger=logging.getLogger('默认root用户')  #创建logging对象。大对象。可以设置子用户
#创建多个对象时，如果用户名用同一个会导致覆盖，即使接受的变量名不一样
#同时创建根用户和子用户，会导致根用户干扰子用户，导致子用户重复打印

fh=logging.FileHandler('文件名.log')  #创建向文件发送日志的logging对象。子对象。会生成log文件
sh=logging.StreamHandler() #创建向屏幕发送日志的logging对象。子对象

fm=logging.Formatter('%(asctime)s %(lineno)d %(message)s')  #格式对象。孙子对象

fh.setFormatter(fm)   #为logging对象设定输出格式。根据孙子对象设定子对象
sh.setFormatter(fm)   #为logging对象设定输出格式。根据孙子对象设定子对象

logger.addHandler(fh)  #根据子对象设定大对象
logger.addHandler(sh)   #根据子对象设定大对象
logger.setLevel('info')  #设定大对象的级别

logger.debug('debug')   #可以同时在屏幕及文件输出
logger.info()
logger.warning()
logger.error()
logger.critical()
