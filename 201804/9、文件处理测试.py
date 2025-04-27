f9=open(r"E:\python_study\untitled\1.txt","r+")

f9.seek(10)
print(f9.tell())
f9.truncate(5)
f9.close()