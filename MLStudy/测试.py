from sklearn import tree
import pandas as pd
from sklearn.model_selection import train_test_split
import graphviz
import pydotplus
import numpy as np

train_field=['affairs',"gender",'age','yearsmarried','children','religiousness','education','occupation','rating']

data=pd.read_csv("datafile.txt",header=None,names=train_field)

target=train.iloc[:,0]
feature=train.iloc[:,1:]



train_data,test_data,train_target,test_targe=train_test_split(data,test_size=0.2,random_state=42)



model=tree.DecisionTreeClassifier(max_depth=4)
m=model.fit(trainfeature,traintarget)

dot_data = tree.export_graphviz(m,
                                out_file=None,
                                feature_names=train_field[1:],
                                class_names=np.unique(traintarget),
                                filled=True,
                                rounded=True,
                                special_characters=True,
                                precision=0
                                )

graph = graphviz.Source(dot_data,encoding="utf8")

dot_data=dot_data.replace("helvetica","SIMKAI")
graph1=pydotplus.graph_from_dot_data(dot_data)
graph1.write("tree.png",format='png')
