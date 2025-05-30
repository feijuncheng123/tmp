#梯度提升树（GBDT）

# GBDT中的树都是回归树，不是分类树。回归树用于预测实数值，如明天的温度、用户的年龄、网页的相关程度等，但改造后也可以用于分类。
# 回归树总体流程类似于分类树的，但树的计算原理不一样：
# 1、回归树在每个节点（不一定是叶子节点）都会得一个预测值，预测值等于属于这个节点的所有目标属性的平均值。
# 2、回归树分枝时穷举每一个属性的每个阈值找最好的分割点，
# 但衡量最好的标准不再是最大熵，而是最小化均方差--即∑（真实值-预测值）^2/n。即该节点下的值集中度最高。
# 3、分枝直到每个叶子节点上目标值都唯一（这太难）或者达到预设的终止条件（如叶子个数上限），
# 若最终叶子节点上值不唯一，则以该节点上所有值做为该叶子节点的预测值。


#GBDT流程：
# 1、训练一颗弱分类器，即单棵树。
# 2、得到第一棵树的对每个样本的预测残差（即真实值-预测值）。
# 3、将每个样本在第一棵树的残差，作为第二棵树的训练目标属性。训练后再次预测，


