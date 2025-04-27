import configparser as cfk

a=cfk.ConfigParser()
a.read('my.ini')
x=a.options('hometown')
print(x)