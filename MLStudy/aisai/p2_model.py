import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
import seaborn as sns
from collections import Counter

data=pd.read_csv(r"E:\python\Pro1\untitled\MLStudy\data\aisai\data.csv",header=0)

data.apply(lambda x:x.value_count())
data.nunique()
data.groupby("")

plt.figure(figsize=(12,10), dpi= 80)

sns.heatmap(data.corr(), xticklabels=data.corr().columns, yticklabels=data.corr().columns, cmap='RdYlGn', center=0, annot=True,fmt='.1f')

plt.title('Correlogram of features', fontsize=22)
plt.xticks(fontsize=12)
plt.yticks(fontsize=12)
plt.show()

from sklearn.feature_selection import c

sns.pairplot(df, kind="scatter", hue="species", plot_kws=dict(s=80, edgecolor="white", linewidth=2.5))

pd.crosstab()


data.fillna()
test=data.groupby(["SUB_NO","CALL_MONTH","STATUS"]).apply(lambda col:col.sum()).reset_index(drop=True)
data.pivot_table(values='SUB_NO', rows='STATUS', cols='FLAG', aggfunc=lambda x: len(x.unique()))


data.to_csv("model_data.csv",header=True,index=False)
