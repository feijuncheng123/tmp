import socket
import struct

sock = socket.create_connection(('127.0.0.1',18898),30)

values = (4, 43,78)
s = struct.Struct('!QBB')
packed_data = s.pack(*values)
sock.sendall(packed_data)

sock.close()