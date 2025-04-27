
#pip freeze 查看当前环境安装得所有包和版本
# pip freeze > requirements.txt  可以将当前环境中的所有包依赖导入到文件中
# pip install -r requirements.txt 可以一次性安装文件中的依赖包

#结合virtualenv使用

# 设置虚拟环境：
# python -m venv --system-site-packages  dev-env  #在当前目录下设置一个新的dev-env虚拟环境。pycharm通过设置创建。system-site-packages参数为继承全局环境
# 在虚拟环境下安装ipython内核，供jupyter调用
# ipython kernel install --user --name=dev-env
#在jupyter启动后，在脚本的内核kernel中选择相应内核