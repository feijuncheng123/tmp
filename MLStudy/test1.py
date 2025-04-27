from sklearn.linear_model import LinearRegression
from tpot import TPOTClassifier
from sklearn.datasets import load_iris
from sklearn.model_selection import train_test_split


iris = load_iris()
X_train, X_test, y_train, y_test = train_test_split(iris.data, iris.target,
                                                    train_size=0.75, test_size=0.25)

tpot = TPOTClassifier(generations=5, population_size=50, verbosity=2,n_jobs=4)
tpot.fit(X_train, y_train)
x=tpot.fitted_pipeline_.get_params()
print(x)
tpot.export('tpot_iris_pipeline.py')



from sklearn.pipeline import Pipeline
from sklearn.preprocessing import Normalizer, PolynomialFeatures
import numpy as np

norm = Normalizer()
poly = PolynomialFeatures(2, include_bias=False)
lr = LinearRegression()
pipeline = Pipeline([('norm', norm),('poly',poly),('lr', lr)])
pipeline.fit(X_train, y_train)

feature_names = pipeline.named_steps['poly'].get_feature_names()

support = pipeline.named_steps['norm'].support_

x=np.array(feature_names)[support]
print(x)



