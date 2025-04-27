
#参数详见：https://github.com/dmlc/xgboost/blob/master/doc/parameter.rst

params={'booster':'gbtree', #默认gbtree，可以为gbtree，gblinear或者dart。gbtree和dart为树模型，gblinear则基于线性函数
    'objective': 'multi:softmax',
        # reg:logistic ： 逻辑回归，输出概率
        # binary:logitraw ：二分类逻辑回归，逻辑变换前输出分数。输出的结果为wTx
        # binary:logistic
        # binary:hinge
        # multi:softmax ：多分类
        # multi:softprob  多分类，但输出向量，为概率
    'num_class': 5,
    'eval_metric': 'auc',
        #rmse: 均方根误差
        #mae: 平均绝对值误差
        #logloss: negative log-likelihood
        #error: 二分类错误率，=#(wrong cases)/#(all cases)。对于预测，预测值大于0.5被认为是正类，其它归为负类
        #error@t : 同error，只是阈值可以通过t来设置
        #merror: 多分类错误率，计算公式为(wrong cases)/(all cases)
        #mlogloss: 多分类log损失
        #auc: 曲线下的面积
        #aucpr：pr曲线下面积
        #ndcg: Normalized Discounted Cumulative Gain
        #map: Mean Average Precision
        #
    'max_depth':10,#[default=6]，设为0表示无限制（tree_method需要设为hist），range: [0,∞]
    'lambda':10,#权重的L2正则化项。默认为1，越高越保守
    'alpha':2,#权重的L1正则化项.
    'subsample':0.75, #每次迭代采用的样本比例
    'colsample_bytree':0.75,#特征采样比例。每构建一棵树采样一次。
    'min_child_weight':2, #相当于叶子节点的最小样本数，小于该数则不分裂。default=1，range: [0,∞]
    'eta': 0.025, #default=0.3,也可以设为learning_rate。步长收缩。range: [0,1]
    'gamma':0.01,#default=0, alias: min_split_loss.在叶子节点上进一步切分所需的最小损失减少。越大越保守，但也可能欠拟合。range: [0,∞]
    'max_delta_step':0,#默认0,限制每棵树权重改变的最大步长,一般无需改变。但样本极不平衡时，这个参数对逻辑回归优化器是很有帮助的
    'scale_pos_weight':0,#控制正负例样本权重的平衡，对样本极端不平衡时十分有用。通常可以将其设置为负样本的数目与正样本数目的比值。
    'grow_policy':"depthwise",#控制节点增加的参数。depthwise（默认）为深度优先，尽可能往靠近根部处分裂。lossguide为loss优先，尽可能往降低loss值处分裂
    'max_leaves':0,#仅在grow_policy为lossguide设置，添加的最大节点数
    'max_bin':256,#仅在tree_method为hist时设置，连续变量的最大分桶数。增加该参数可以提高精准度，但计算量增加
    'num_parallel_tree':5,#default=1,每次迭代时并行构建树的数量，用于构建随机森林。
    'tree_method':"auto",
        #树的构建方法，支持auto, exact, approx, hist, gpu_hist五个选项。
        # auto自动选择最快的。小数据集则使用exact（精确）构建
        # 非常大数据集则使用approx（近似）构建，使用该方法会提示
        # approx为使用分位数和梯度直方图的近似算法
        # hist为快速直方图优化的近似贪婪算法，进行了性能改进
        #gpu_hist：gpu实现hist算法
    'seed':0,
    'nthread':8,
    'verbosity':3, #0(silent), 1 (warning), 2 (info), 3 (debug)
    'disable_default_eval_metric':0 #Flag to disable default metric. Set to >0 to disable.
        }


