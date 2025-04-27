import  numpy as np

from tpot import TPOTClassifier
import pandas as pd
pd.set_option('display.max_columns', 50)
pd.set_option('display.max_rows', 100)
data=pd.read_csv("100条.txt",engine="python")
data.drop(columns=["uer_id",' grp_type',' brand'],inplace=True)
# data.replace(r'^\s*$', np.nan,regex=True,inplace=True)
# print(data)

df_obj = data.select_dtypes(['object'])
df_obj[df_obj.columns] = df_obj.apply(lambda x: x.str.strip())
data.replace(' ', np.nan,inplace=True)
print(data)
data[" is_login_active"]=data[" is_login_active"].astype("float64")
data[" price"]=data[" price"].astype("float64")
print(data.shape)
print(data.dtypes)
target=data.iloc[:,0]
feature=data.iloc[:,1:]

np.isnan(feature)

m=TPOTClassifier(generations=5, population_size=50, verbosity=2,n_jobs=4)
tpot = TPOTClassifier(generations=5, population_size=50, verbosity=2,n_jobs=4)

tpot.fit(feature, target)
x=tpot.fitted_pipeline_.get_params()
print(x)
tpot.export('tpot_iris_pipeline.py')


