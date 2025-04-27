import os

os.getcwd()  #获取当前工作目录，默认当前文件所在目录
os.chdir('目录')  #更改工作目录
os.makedirs()  #可以创建多层目录
os.removedirs() #删除目录，直到不为空的层级为止
os.rmdir() #删除单级目录，必须为空目录
os.listdir('path')  #列出当前目录下所有文件
os.stat('file.txt')  #输出文件信息，包括创建修改时间等
os.sep #输出系统的路径分隔符
os.linesep #输出系统的行分隔符
os.pathsep  #路径之间的分隔符，如win环境变量的分割符分号;，linux为冒号：
os.system('command') #调用shell（cmd）运行命令。并直接显示.(无需print)
os.path.abspath(__file__)  #输出相对路径或者文件的绝对路径
os.path.split('path') #分割路径为路径及文件名。如果路径最后非文件，也会被截取返回.但最后不能有文件分隔符
os.path.dirname('path') #返回除文件名外的目录。实际为split的第一个元素
os.path.basename('path') #同上，取split的第二个元素
os.path.exists('path') #判断路径是否存在
os.path.isabs('path')  #判断是否是绝对路径
os.path.isfile('path')  #判断文件是否存在
os.path.isdir()  #判断路径是否存在

os.path.join('path1','path2')   #将路径拼接（路径和文件名）
os.path.getatime() #Return the last access time of a file, reported by os.stat()
os.path.getmtime() #Return the last modification time of a file, reported by os.stat()

import sys

sys.path  #获取环境变量（模块搜索路径）
sys.exit()  #退出程序运行
sys.version  #返回python版本
a=sys.argv   #程序运行前，接受传入的值。以列表形式接收
sys.stdout.write('str')   #像屏幕输出相应的字符串
sys.stdout.flush()   #立即刷新


import subprocess as sp

sp.call('dir',shell=True)
#使用shell运行dir命令，无需print会直接打印。不会返回值(加上stdout参数也没用，且不会打印）
string=sp.check_output('dir',shell=True)
#功能同call,但是返回值，返回字节码，需要解码

obj=sp.Popen("dir",shell=True,stdout=sp.PIPE,stdin=sp.PIPE,stderr=sp.PIPE)
#创建popen对象。stdout参数不能少，否则同call。
# stdout=sp.PIPE为输出管道。意为将返回内容输出到管道中，由obj接收
#stderr=sp.PIPE是指如果输入的指令运行错误，将错误码返回到管道中

obj.stdout.read()
#从管道中读取内容，以字节形式。读取后管道变空，无法再次读取

import optparse
#类似于sys.argv方法，在命令行界面中可以接收参数
op=optparse.OptionParser() #创建对象
op.add_option('-h','--host',dest='host')
op.add_option('-u','--user',dest='user')
op.add_option('-p','--password',dest='password')
options,args = op.parse_args()
options.host #取值
#options变量为上面add_option方法接收的各类有名参数，以类似字典的对象返回
#args变量为上面add_option方法以外传递的参数，以列表形式返回。





