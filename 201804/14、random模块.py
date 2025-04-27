import random

random.random() #生成0到1的随机数（包含0）
random.randint(1,10) #生成1-10的随机整数，含1和10
random.randrange(1,10) #同上，但不包含10
random.choice("序列") #从序列中随机选取一个值
random.sample('序列',2) #从序列中随机选取两个值
random.uniform(1,5)  #随机生成1-5之间的浮点数，包含两端
random.shuffle('序列') #打乱序列顺序，让其随机排序。不返回新值
