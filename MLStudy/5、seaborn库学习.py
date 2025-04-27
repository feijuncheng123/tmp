
import numpy as np
import seaborn as sns
import matplotlib
import matplotlib.pyplot as plt

#seaborn库主要提供了一些主题模板。可以直接调用

def sinplot(arg):
    x=np.linspace(0,14,100)
    for y in range(1,3):
        plt.plot(x,np.sin(x+y*0.5)*(7-y)*arg)
    plt.xlabel("x")
    plt.ylabel("y")
    plt.show()

# sns.set()  #设置默认模板后，图形配色等会发生变化
# sns.set_style("whitegrid")  #提供了集中主题风格供选择
sns.violinplot()
sns.despine(left=True,offset=-100)  #无参数默认去除全部坐标轴线.offset值图形偏离坐标轴的距离，用于设置横纵坐标轴的交点等
sns.set_context("talk",font_scale=4.5)  #画图大小，用于打印等.font_scale为字体大小
sinplot(1)

#也可以
with sns.axes_style("whitegrid"):  #不同风格画图
    plt.plot()


#颜色设置
plt.plot(['data1'],['data2'],sns.xkcd_rgb['pale red'])
#xkcd_rgb指定大概的颜色名称，然后sns库进行匹配配色

#连续型画板
sns.palplot(sns.color_palette("Blues")) #由浅入深
sns.palplot(sns.color_palette("Blues_r"))  #加上_r，由深入浅

#色调连续性变化画板
sns.palplot(sns.color_palette("cubehelix",8))
#cubehelix 指色调
sns.palplot(sns.cubehelix_palette(8,start=0.5,rot=0.75))
#可以指定一个起始值和区间值
sns.palplot(sns.light_palette("green")) #颜色较浅
sns.palplot(sns.dark_palette("green")) #颜色较深

sns.palplot(sns.light_palette("green",reverse=True))  #颜色较浅,但由深入浅







