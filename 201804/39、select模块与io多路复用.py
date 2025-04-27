import select
import socket

server=socket.socket(socket.AF_INET,socket.SOCK_STREAM)
server.bind(('127.0.0.1',19999))
server.listen(5)
inp=[server,]
while True:
    print('start')
    r,w,e=select.select(inp,[],[],5)  #参数分别为输入列表、输出列表、错误列表,只监听5秒钟，超过往下执行
    #输入列表为select进行监听的对象列表，任何一个对象连接就以列表形式返回

    #select触发方式：水平触发。即只要检测到连接状态，就会一直触发输出，并非需要变动才能触发。
    #但是，如果接收连接后，就不再触发（已经不在监听池列表里）
    print(r)
    for i in r:  #r为有活动的socket对象列表
        if i == server:
            conn,addr=i.accept()   #i接收连接后，就不再触发select返回
            print(conn)
            print('你好')
            inp.append(conn)
        else:
            data=i.recv(1024).decode('utf8')
            print(data)

    print(r)
    print('end------')

#优点：可以同时监听多个socket连接对象（也可以监控conn连接）。
#缺点：select只能监听1024个链接。如果要超过需要使用epoll模块