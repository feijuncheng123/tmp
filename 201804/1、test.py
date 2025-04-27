a=1
b=0
l1=[]
l2=[]
while a < 100:
    if a%2 == 1:
        l1.append(a)
    elif a%2 == 0 :l2.append(a)
    b += a
    a += 1
print("100以内奇数：",l1)
print("100以内偶数：",l2)
print(b)