import matplotlib
#设置全局字体
matplotlib.rc('xtick', labelsize=16)
matplotlib.rc('ytick', labelsize=16)
font = {'family' : 'normal',
        'weight' : 'bold',
        'size'   : 22}
matplotlib.rc('font', **font)
matplotlib.rcParams.update({'font.size': 22}) #更新字体
import matplotlib.font_manager
import matplotlib.pyplot as plt
plt.rcParams.update({'font.size': 22}) #更新字体，同上一样的


fontlist=[f.name for f in matplotlib.font_manager.fontManager.ttflist]
#打印当前系统中的字体名称

from matplotlib.ticker import FormatStrFormatter
fig, ax = plt.subplots()
ax.yaxis.set_major_formatter(FormatStrFormatter('%.2f'))
#设置轴坐标点的显示样式

ax.tick_params(axis='x', rotation=45)
ax.xaxis.set_tick_params(rotation=45)
#两种方式都可以设置坐标点的显示参数

ax.ticklabel_format(useOffset=False)
#关闭坐标点的自动偏置量，会与上面的各种设置冲突
