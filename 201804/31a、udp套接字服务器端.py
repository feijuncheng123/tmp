import socket

udp_server=socket.socket(socket.AF_INET,socket.SOCK_DGRAM)
#UDP协议套接字，第二个参数指数据报式套接字
#UDP协议无需监听。而需要在发送的包中添加上目标地址
#UDP不可靠。可能会导致数据丢失。（数据量大时，单次发送，无论对方有无接收完毕，都按次发送数据）

udp_server.bind(('127.0.0.1',8001))
while True:
    data,client_ip=udp_server.recvfrom(2048)  #接收到的数据为元组.第二个元素为发送端地址
    print(data)
    udp_server.sendto("server".encode('utf8'),client_ip)
    help(udp_server.sendall)

udp_server.close()