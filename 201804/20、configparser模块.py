import configparser as cfp
#处理配置文件，如mysql配置文件my.ini

config=cfp.ConfigParser(allow_no_value=True)  #创建对象。相当于空字典.allow_no_value表示允许使用空值（注释）
#创建配置文件
config1 = configparser.ConfigParser({"; comment": None}, allow_no_value=True)
#在default块内添加注释

config['default']={'name':'feijuncheng','age':18,'height0':170}
config['hometown']={}
config['hometown']['province']='yunnan'
with open('my.ini','w') as f:
    config.write(f)    #将对象写入到文件中。（与文件操作的参数不同。注意）

#增删改查
config=cfp.ConfigParser()
config.read("my.ini")  #读取文件，无需用变量接收

config.sections()  #以列表形式返回配置文件的各个模块名
'hometown' in config  #判断是否在配置文件内
config['default']['name']  #取值

 #default特殊意义：遍历其他模块时，也会将default的值遍历出来
 config.options('块名') #取出块下面所有的元素名，以列表形式返回
 config.items('块名')  #取出块下面的元素键值对，组成元组以列表形式返回
 config.get("块名",'元素名') #取块内元素的值

config.add_section("块名")  #新增块
config.set('块名','key','value')  #像快内添加或者修改元素
config.remove_section('块名')  #把整个块删除
config.remove_option('块名','key') #删除块下面的键值对


