import time

time.time()  #返回当前的时间戳（实时更新）
t=time.localtime()  #返回当前时区的时间，以时间类型（类似于元组）的形式返回年月日等

#结构化时间与时间戳
time.localtime(14567217486)  #将时间戳转化为本地结构化时间
time.mktime('结构化时间') #上面的逆向操作。转化为时间戳

#字符串时间
time.strftime("%Y-%m-%d %X",'字符串时间') #将结构化时间转化为字符串时间。第一个参数为字符串显示的格式
# 小写m代表月，大写M代表分钟。%X代表说时间段，可以直接接收到时间的小时分钟秒等
time.strptime('时间字符串','%Y-%m-%d')   #将字符串时间转化为结构化时间。第二个参数的格式需要和时间字符串的结构完全一致

time.asctime('默认当前时间')  #转化为日期，固定一个常规字符串时间格式。参数为结构化时间
time.ctime('默认当前时间戳')   #同上，但将时间戳转化为固定字符串时间格式

time.sleep(1) #延迟执行，参数为秒

#返回形式time.struct_time(tm_year=2018, tm_mon=5, tm_mday=3, tm_hour=16, tm_min=15, tm_sec=5, tm_wday=3, tm_yday=123, tm_isdst=0)
t.tm_year  #取出年份
time.gmtime()  #格林尼治时间

import datetime  #方法更多更复杂
datetime.datetime.now()  #显示当前时间，更正常一些
