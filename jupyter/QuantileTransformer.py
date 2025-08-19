import torch
import torch.nn as nn
import numpy as np
import math

# 位置编码
class PositionalEncoding(nn.Module):
    def __init__(self, d_model, max_len=5000):
        super().__init__()
        pe = torch.zeros(max_len, d_model)
        position = torch.arange(0, max_len, dtype=torch.float).unsqueeze(1)
        div_term = torch.exp(torch.arange(0, d_model, 2).float() * (-math.log(10000.0) / d_model))
        pe[:, 0::2] = torch.sin(position * div_term)
        pe[:, 1::2] = torch.cos(position * div_term)
        pe = pe.unsqueeze(0).transpose(0, 1)
        self.register_buffer('pe', pe)
    
    def forward(self, x):
        x = x + self.pe[:x.size(0), :]
        return x

# Quantile Transformer 模型
class QuantileTransformer(nn.Module):
    def __init__(self, input_size=7, hidden_size=64, num_layers=2, num_heads=4, num_quantiles=3):
        super().__init__()
        self.num_quantiles = num_quantiles
        self.quantiles = [0.05, 0.5, 0.95]  # 下界、中值、上界
        self.input_embedding = nn.Linear(input_size, hidden_size)
        self.pos_encoder = PositionalEncoding(hidden_size)
        encoder_layer = nn.TransformerEncoderLayer(d_model=hidden_size, nhead=num_heads)
        self.encoder = nn.TransformerEncoder(encoder_layer, num_layers=num_layers)
        self.fc_out = nn.Linear(hidden_size, input_size * num_quantiles)  # 输出 input_size * num_quantiles
    
    def forward(self, src, forecast_len=288):
        src = self.input_embedding(src)  # [batch_size, src_len, hidden_size]
        src = self.pos_encoder(src.transpose(0, 1)).transpose(0, 1)
        output = self.encoder(src.transpose(0, 1)).transpose(0, 1)
        output = self.fc_out(output[:, -forecast_len:, :])  # [batch_size, forecast_len, input_size * num_quantiles]
        return output.view(output.size(0), forecast_len, -1, self.num_quantiles)  # [batch_size, 288, 7, 3]

# Quantile Loss
def quantile_loss(pred, target, quantiles):
    losses = []
    for i, q in enumerate(quantiles):
        errors = target - pred[..., i]
        losses.append(torch.max((q - 1) * errors, q * errors).mean())
    return sum(losses) / len(quantiles)

# 模拟数据
def create_samples(data, window_size=2016, target_size=288):
    inputs, targets = [], []
    for i in range(0, len(data) - window_size - target_size + 1):
        inputs.append(data[i:i+window_size])
        targets.append(data[i+window_size:i+window_size+target_size])
    return torch.tensor(inputs, dtype=torch.float32), torch.tensor(targets, dtype=torch.float32)

data = np.random.randn(2304, 7)
inputs, targets = create_samples(data)

# 模型和训练
model = QuantileTransformer()
optimizer = torch.optim.Adam(model.parameters(), lr=0.001)

batch_inputs = inputs[0:32]  # [32, 2016, 7]
batch_targets = targets[0:32]  # [32, 288, 7]
optimizer.zero_grad()
pred = model(batch_inputs)  # [32, 288, 7, 3]
loss = quantile_loss(pred, batch_targets.unsqueeze(-1), model.quantiles)  # 加 unsqueeze 匹配形状
loss.backward()
optimizer.step()
print(f"Loss: {loss.item():.4f}")

# 推理示例：预测区间
# pred[:, :, :, 0] 是下界，pred[:, :, :, 1] 是中值，pred[:, :, :, 2] 是上界
