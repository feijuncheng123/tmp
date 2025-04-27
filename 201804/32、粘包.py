#粘包是指发送的数据没有一次性发送完毕，导致执行后续指令后，还在传送前期数据。导致数据混乱
#粘包的原因是：套接字从缓冲区收发数据，由于数据量大，无法一次性发送完毕导致。导致每次传送都只能依次发送，而与请求数据不相符
#udp协议不存在粘包问题
#tcp发送空字符串时，对方无法收到，会导致阻塞卡住。而udp发送空，对面就收到空
#tcp为优化网络，较小数据一次性打包发送，如果多次send较小数据则对方可能一次性收到，而发送较大数据，则分多次才能收到
#UDP面向消息发送，每次发送的消息都打上一个消息头，而对方根据该消息头一次性接收消息。


import socket

server=socket.socket(socket.AF_INET,socket.SOCK_STREAM)
server.bind(('127.0.0.1',8888))
server.listen(5)
conn,addr=server.accept()
#conn.send()
#可以通过循环接收（要提前发送数据长度）
#send一次最多能发送8kb
#recv接收最好不要超过8096个字节
conn.sendall('data')
#sendall方法，底层循环send方法，将数据发送完毕



import struct
#将不同数据类型的数据打包成固定长度的字节
#当传递诸如int、char之类的基本数据的时候，需要有一种机制将某些特定的结构体类型打包成二进制流的字符串然后再网络传输。
#接收端也应该可以通过某种机制进行解包还原出原始的结构体数据
struct.pack('fmt','data')  #打包
struct.unpack('fmt','data') #解包
values = (1, 'abc', 2.7)
s = struct.Struct('I3sf')
#I 表示int，3s表示三个字符长度的字符串，f 表示 float
s.format  #s的打包格式
s.size  #s包的长度

packed_data = s.pack(*values)
unpacked_data = s.unpack(packed_data)
