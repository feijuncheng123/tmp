import seaborn as sns
import matplotlib.pyplot as plt


df = sns.load_dataset('iris')
df.head()

plt.figure(figsize=(10,8), dpi= 80)
sns.pairplot(df, kind="scatter", hue="species", plot_kws=dict(s=80, edgecolor="white", linewidth=2.5))
plt.show()

import pandas as pd

a=pd.read_csv()


from  sklearn.ensemble import GradientBoostingClassifier
from  sklearn.model_selection import train_test_split
df = sns.load_dataset('iris')
a,b,c,d = train_test_split(df,df[""],test_size=0.3)

gbm1 = GradientBoostingClassifier(n_estimators=50, random_state=10, subsample=0.6, max_depth=7,
                                  min_samples_split=900)

gbm1.fit(a, c)
gbm1.apply()

import requests

r=requests.post(url="request_url",verify=False)
