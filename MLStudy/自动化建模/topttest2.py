from tpot import TPOTRegressor
from tpot import TPOTClassifier
from sklearn.datasets import load_boston
from sklearn.model_selection import train_test_split
import pandas as pd
from sklearn.preprocessing import LabelEncoder
from sklearn.metrics.classification import precision_score,recall_score,accuracy_score,f1_score

# pd.set_option('display.max_columns', 50)
# pd.options.display.float_format = '{:20,.2f}'.format

lb=LabelEncoder()
data=pd.read_csv(r"E:\python\Pro1\untitled\MLStudy\自动化建模\modeldata.csv")
data.columns

object_cols=data.select_dtypes(["object"])
for col in object_cols:
    data[col]=lb.fit_transform(data[col])


train_data=data.iloc[:,0:-1]
target_data=data.loc[:,"flag"]
# print(train_data.head())

X_train, X_test, y_train, y_test = train_test_split(train_data, target_data,
                                                    train_size=0.75, test_size=0.25)

tpot = TPOTClassifier(generations=5, population_size=50, verbosity=2,scoring="f1",n_jobs=10)
tpot.fit(X_train, y_train)
print(tpot.score(X_test, y_test))
p=tpot.predict(X_test)
print(precision_score(y_test,p))
print(recall_score(y_test,p))
print(accuracy_score(y_test,p))



# tpot.export('tpot_boston_pipeline.py')

exctracted_best_model = tpot.fitted_pipeline_.steps[-1][1]

exctracted_best_model.fit(X_train,y_train)


from sklearn.neighbors import KNeighborsClassifier

m=KNeighborsClassifier(algorithm='auto', leaf_size=30, metric='minkowski',
                     metric_params=None, n_jobs=None, n_neighbors=5, p=2,
                     weights='distance')

m.fit(X_train, y_train)



from sklearn.kernel_approximation import RBFSampler


from sklearn.linear_model import LogisticRegression
from sklearn.ensemble import GradientBoostingClassifier


LogisticRegression(C=25.0, class_weight=None, dual=False, fit_intercept=True,
                   intercept_scaling=1, l1_ratio=None, max_iter=100,
                   multi_class='warn', n_jobs=None, penalty='l1',
                   random_state=42, solver='warn', tol=0.0001, verbose=0,
                   warm_start=False)


from  lightgbm import LGBMClassifier

a=LGBMClassifier()

from numpy import genfromtxt, savetxt
