import torch

# Tensors 类似于 NumPy 的 ndarrays ，同时 Tensors 可以使用 GPU 进行计算。
torch.empty(5, 3)  # 输出5行3列的Tensors
torch.zeros(5, 3, dtype=torch.long)  # 输出5行3列全为0的Tensors
a = torch.tensor([5.5, 3])  # 直接应用到数据

a = a.new_ones(5, 3, dtype=torch.double)  # 创建一个tensor基于已经存在的 tensor。该方法实际与原tensor无关

x = torch.randn_like(a, dtype=torch.float)  # 创建一个相同维度的随机tensor
x.size()  # 输出torch.Size([5, 3])。是一个数组

y = torch.rand(5, 3)
print(x + y)  # 相同维度tensor相加
print(torch.add(x, y))  # 也可以

result = torch.empty(5, 3)
torch.add(x, y, out=result)  # 指定输出的tensor，结果存入该tensor中
print(result)

y.add_(x)  # in-place相加，y变化为最新值

'''注意: 任何使张量会发生变化的操作都有一个后缀 _ '''
'''tensor可以使用标准的 NumPy 类似的索引操作'''
print(x[:, 1])

x = torch.randn(4, 4)
y = x.view(16)  # view操作改变形状，改为1维的16个元素的操作。改变后维度必须与原矩阵元素个数匹配
z1 = x.view(-1, 8)  # -1表示当前维度不设定，满足其他维度的元素个数要求。该处为(2,8)大小的矩阵
z2 = x.view(8, -1)  # 该处为(8,2)大小的矩阵

x = torch.randn(1)
print(x.item())  # 单元素tensor能够通过item直接取值

###################################################################################################
# torch.Tensor是核心类。如果将某个tensor属性 .requires_grad 设置为 True，则会开始跟踪针对 该tensor 的所有操作
# 完成计算后，可以调用 .backward() 来自动计算所有梯度。该张量的梯度将累积到 .grad 属性中。

# 可调用 .detach()，它将其与计算历史记录分离，并防止将来的计算被跟踪。
# 还可使用with torch.no_grad(): 包装代码停止跟踪历史记录（和使用内存）。在评估模型时无需求梯度
# 每个张量都有一个 .grad_fn 属性保存着创建了该张量的 Function 的引用

# 求取导数可以调用.backward()方法。如果Tensor是标量（即包含一个元素），则不需要指定任何参数；但是如果它有更多元素，则需要指定一个gradient参数来指定张量的形状。
x = torch.ones(2, 2, requires_grad=True)
y = x + 2
print(x.requires_grad)
print(y.grad_fn)

z = y * y * 3
out = z.mean()
out.backward()
print(x.grad)


