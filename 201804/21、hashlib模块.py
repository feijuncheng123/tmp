import hashlib
#加密。或者生成唯一码

m5=hashlib.md5("ad".encode('gbk'))
#创建md5对象.添加字符串进行加密混合，降低被破解概率
#添加字符串是添加在加密文本的前方。编码模式不会改变密文
#md5算法不可逆，只能生成密文，不能根据密文解密成字符。算法固定，同一字符串密文相同
m5.update('min'.encode('utf8'))
print(m5.hexdigest())  #生成密文

hashlib.sha256()  #用法与md5完全一样，只换了名字

import uuid
a=uuid.UUID()