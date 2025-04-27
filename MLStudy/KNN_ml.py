import numpy as np
import operator

def creatdataset():
    group=np.array([[1,1.1],[1,1],[0,0],[0,0.1]])
    labels = ['A','A','B','B']
    return group,labels

def classify(inX,dataSet,labels,k):
    datasetSize=dataSet.shape[0]
    diffMat=np.tile(inX,(datasetSize,1))-dataSet
    sqdiffMat=diffMat**2
    sqDistances=sqdiffMat.sum(axis=1)
    distances=sqDistances**0.5
    sortedDistIndicies=distances.argsort()
    classcount={}
    for i in range(k):
        voteIlable=labels[sortedDistIndicies[i]]
        classcount[voteIlable] = classcount.get(voteIlable,0) + 1
        sortedclasscount = sorted(classcount.items(),key=operator.itemgetter(1),reverse=True)
        return sortedclasscount[0][0]


group,labels = creatdataset()
output = classify([1.1,1.2],group,labels,3)
print(output)
