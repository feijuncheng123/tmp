import h2o

from h2o.automl import H2OAutoML
h2o.init(ip="10.45.47.69",port=54323)
import pandas as pd
import numpy as np
import os


path="/home/oracle/tmp_import/h2odata/testdata/classfy.csv"

print(os.path.isfile(path))
df = h2o.import_file(path)
df.describe()
x = df.columns
y="label"
x.remove(y)
df.cbind()

aml = H2OAutoML(max_models = 10, seed = 1,exclude_algos="XGBoost")
aml.train(x = x, y = y, training_frame =df)

lb = aml.leaderboard

