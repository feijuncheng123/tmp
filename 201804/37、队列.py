import queue
#线程队列
#有三种模式：先进先出、先进后出、按优先级


q=queue.Queue(5)  #创建队列对象.最多存5个数据
#线程队列

q.put(1)  #在队列中放入值。插入值满时继续插入则报错
q.get(block=True)  #向队列中取值。根据模式取值。取值后该值从队列取出.block改为false，则当队列为空时返回错误。为true则阻塞

q1=queue.LifoQueue(5)  #后进先出模式队列
#其他方法同上

q2=queue.PriorityQueue(5)  #优先级队列
q2.put([2,'b'])
q2.put([1,'a'])  #传入列表，第一个值为优先级。数值小的优先级大

q.qsize()  #返回队列当前有多少值
q.empty()  #判断是否为空
q.full() #判断是否满
q.put_nowait(2)  #相当于q.put(block=False),当满时报错
q.get_nowait()   #同上

q.task_done()   #发送该线程任务完成的信号
q.join()  #接收其他任务完成的信号，接收到才会往下执行，否则等待


