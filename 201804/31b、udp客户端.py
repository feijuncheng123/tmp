import socket

client=socket.socket(socket.AF_INET,socket.SOCK_DGRAM)
while True:
    client.sendto("client".encode('utf8'),('127.0.0.1',8001))
    data=client.recvfrom(2048)
    print(data)