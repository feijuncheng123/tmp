import socket
import struct

client=socket.socket(socket.AF_INET,socket.SOCK_STREAM)  #同服务器端
client.connect(('127.0.0.1',8080))  #链接服务器的ip地址和端口号
while True:
    s=input("发送给服务器：")
    if s=='quit':break
    if not s: continue
    client.sendall(s.encode('gbk'))  #发送消息
    l=client.recv(4)
    length=struct.unpack('i',l)
    i=0
    dt=b''
    while i < length[0]:
        data=client.recv(1024)  #接收消息。字符串需要编码为二进制才能发送
        dt =b''.join([dt,data])
        #dt='%s%s'%(dt,data)
        i += len(data)

    #dt = client.recv(7000)
    print(dt.decode('gbk'))

client.close()