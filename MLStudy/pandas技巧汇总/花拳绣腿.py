import pandas as pd

pd.set_option('display.max_columns', 50)  #设定最大行数最大列数
pd.set_option('display.max_rows', 100)
pd.set_option('display.float_format', lambda x: '%.3f' % x)  #设置科学记数法
pd.options.display.float_format = '{:20,.2f}'.format #设置科学记数法


df=pd.read_csv("data.csv")
df_obj = df.select_dtypes(['object'])  #筛选出特定类型的列

df[df_obj.columns] = df_obj.apply(lambda x: x.str.strip())  #对每一列做trip空格操作

df.sample()

import numpy as np
np.linalg.eig()
float("1/2")

from scipy.linalg import eigh
from scipy.linalg import eigvals

eigh(df.to_numpy())

np.real()