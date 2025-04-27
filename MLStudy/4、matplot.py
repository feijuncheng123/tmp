import matplotlib.pyplot as plt
import pandas
import calendar

plt.subplots_adjust(left=None, bottom=None, right=None, top=None, wspace=None, hspace=None)
#用于调整绘图区域与边框的间距。wspace和hspace用于控制宽度和高度的百分比，可以用作subplot之间的间距

data=pandas.read_csv(r"E:\python\Pro1\untitled\数据挖掘与机器学习\data\UNRATE.csv",
                     header=0,engine='python')
data["DATE"]=pandas.to_datetime(data["DATE"])
print(data.head())

plt.plot(data["DATE"],data["VALUE"])
#以x,y轴的形式传入。没有创建对象
plt.xticks(label=calendar.month_name[1:13],rotation=30,) #设置x轴标签（间隔，角度，标签名等），rotation指倾斜30度
plt.xlabel('Month') #横轴名称
plt.title("图形标题")
plt.show()  #显示图形

fig=plt.figure()  #创建一个画图区间
ax1=fig.add_subplot(2,2,1)  #在画图空间上划分2X2的画图空间，在第一个块中创建子图
ax2=fig.add_subplot(2,2,2)
ax3=fig.add_subplot(2,2,4)  #在第四个块中创建子图

fig2=plt.figure(figsize=[3,6])  #创建一个宽3，高6的画图区域
ax4=fig2.add_subplot(2,1,1) #在画图区域中创建2X1个画图空间，在第一个空间中创建子图
ax4.plot() #开始画图

#也可以：
fig3=plt.figure()
ax5=fig3.add_subplot(1,1)  #为绘图区添加图形块
ax5.plot(data["DATE"],data["VALUE"],label="label1",linestyle='--',linewidth=10) #linestyle为线型,linewidth为粗细
ax5.plot(data["DATE1"],data["VALUE1"],label="label2",color='g')  #在同一个图内画两条折线,设定每条线的标签
ax5.legend(loc='best')  #显示每条曲线的标签框（图例），loc是放置位置，如右边，右上角等

#画柱形图
data2=pandas.read_csv(r"E:\python\Pro1\untitled\数据挖掘与机器学习\data\fandango_scores.csv",
                      engine='python',header=0)
fig,ax=plt.subplots(2,3)
#创建一个2X3的绘图区域，返回绘图区fig，以及以数组形式返回ax每个绘图块
ax.bar('x轴的位置序列（每个柱距离原点距离）','每条柱的高度序列',0.5)
#0.5是指每条柱子的宽度
ax.set_xtickts()
#set_xtickts告诉matplotlib要将刻度放在数据范围中的哪些位置，默认情况下，这些位置也就是刻度标签
ax.set_xticktlabel()
#通过set_xticklabels将任何其他的值用作标签

ax.tick_params()  #设置刻度的参数，如锯齿的显示，刻度线的显示等

#画条形图（横向柱状图）
ax.barh("每个条形的位置",'每个条形的长度',0.5)

#画散点图
ax.scatter('x轴坐标','y轴坐标')
ax.set_xlabel()  #设置x轴标签

#画直方图（连续型柱状图）
ax.hist('连续性的值',range=["值的区间"],bins=10)
#bins指将值划分为10组显示，每个组的频率；range指只画出范围内的值的图形

#画箱型图
a=['a','b','c']
ax.boxplot(data2[a].values)  #在一个绘图块中，绘制a中每个序列的箱型图
ax.set_xticktlabels(a,rotation=90)


ax.text('高度值','宽度值','text') #在绘图区宽高的区域写上文字