import socket
#也称为套接字
#socket将TCP/ip协议隐藏在所提供的接口后面。通过调用接口，无需了解TCP/ip协议实现，就可以直接通过socket进行通信
import struct
#TCP服务器端
server=socket.socket(socket.AF_INET,socket.SOCK_STREAM)
#创建服务端socket对象.第一个参数表示socket基于网络通信，第二个参数表示通信基于TCP协议（流）。

server.setsockopt(socket.SOL_SOCKET,socket.SO_REUSEADDR,1)
#该配置主要防止过于频繁启动服务导致端口被占用的错误。（配置代表可以重用端口）
server.bind(('127.0.0.1',18898))
#以元组形式绑定ip地址及端口号.ip用字符串，127.0.0.1表示本机，8888代表端口号

server.listen(5)  #半连接池。等待的链接相当于打开网站一直进不去，在等待相应。
#服务器进入监听状态，5代表可以等待通话的链接数
#监听成功则相当于向客户端发送第二次握手信息（第一次握手信息为客服端发送连接请求）。
# server.setblocking(False)  #设置阻塞IO为关闭状态
#IO阻塞被关闭后，server.accept将不再等待链接，而是直接往下执行，但由于接收不到信息而报错。因此需要和try连用
s = struct.Struct('!QBB')
while True:
    conn,addr=server.accept()  #服务器在此处等待通信。
    # addr地址为接收的客户端的通讯ip和端口，以元组形式。conn是套接字对象
    #接收通信，一个链接会以元组返回两个信息：链接编号，和对方地址
    #成功则相当于第三次握手成功，客户端回应了第二次握手信息。链接建立成功
    while True:
        try:
            msg=conn.recv(1024)  #无论客户端还是服务器端，每接收到信息都会回复一条接收到信息的回执，确保数据不丢失
            #使用创建的链接接收信息，1024指一次可以接收1024字节的信息.接收到的消息为二进制代码
            #1024仅仅指从自己的缓冲区中取出1024字节，并非直接从网络取
            # x=msg.decode('utf8')
            # print(x,len(x)) #需要进行解码
            a=s.unpack(msg)
            print(a)
            send_msg=input("请输入对话：")
            # if not send_msg:continue      #空值无法发送。会卡住（并非空格）
            conn.send(send_msg.encode('utf8'))  #发送消息。字符串需要编码为二进制才能发送
            print('已发送')
            #conn.sendall()
        except Exception:
            print("下一个链接")
            break
    conn.close()  #链接断开

server.close()  #服务器关闭
'''
s=server
#服务端套接字函数
s.bind()    #绑定(主机,端口号)到套接字
s.listen()  #开始TCP监听
s.accept()  #被动接受TCP客户的连接,(阻塞式)等待连接的到来

#客户端套接字函数
s.connect()     #主动初始化TCP服务器连接
s.connect_ex()  #connect()函数的扩展版本,出错时返回出错码,而不是抛出异常

#公共用途的套接字函数
s.recv()            #接收TCP数据
s.send()            #发送TCP数据(send在待发送数据量大于己端缓存区剩余空间时,数据丢失,不会发完)
s.sendall()         #发送完整的TCP数据(本质就是循环调用send,sendall在待发送数据量大于己端缓存区剩余空间时,数据不丢失,循环调用send直到发完)
s.recvfrom()        #接收UDP数据
s.sendto()          #发送UDP数据
s.getpeername()     #连接到当前套接字的远端的地址
s.getsockname()     #当前套接字的地址
s.getsockopt()      #返回指定套接字的参数
s.setsockopt()      #设置指定套接字的参数
s.close()           #关闭套接字

#面向锁的套接字方法
s.setblocking()     #设置套接字的阻塞与非阻塞模式
s.settimeout()      #设置阻塞套接字操作的超时时间
s.gettimeout()      #得到阻塞套接字操作的超时时间

#面向文件的套接字的函数
s.fileno()          #套接字的文件描述符
s.makefile()        #创建一个与该套接字相关的文件
'''