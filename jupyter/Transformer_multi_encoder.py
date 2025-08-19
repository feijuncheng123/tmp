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

# Transformer 模型（简化，只用 Encoder）
class TransformerTimeSeries(nn.Module):
    def __init__(self, input_size=7, hidden_size=64, num_layers=2, num_heads=4, dropout=0.1):
        super().__init__()
        self.input_embedding = nn.Linear(input_size, hidden_size)
        self.pos_encoder = PositionalEncoding(hidden_size)
        encoder_layer = nn.TransformerEncoderLayer(d_model=hidden_size, nhead=num_heads, dropout=dropout)
        self.encoder = nn.TransformerEncoder(encoder_layer, num_layers=num_layers)
        self.fc_out = nn.Linear(hidden_size, input_size)  # 输出 7 个特征
    
    def forward(self, src):
        # src: [batch_size, 2016, 7]
        src = self.input_embedding(src)  # [batch_size, 2016, hidden_size]
        src = self.pos_encoder(src.transpose(0, 1)).transpose(0, 1)  # 添加位置编码
        output = self.encoder(src.transpose(0, 1))  # [2016, batch_size, hidden_size]
        output = output.transpose(0, 1)  # [batch_size, 2016, hidden_size]
        return self.fc_out(output[:, -288:, :])  # 取最后 288 步 [batch_size, 288, 7]

# 数据准备
def create_samples(data, window_size=2016, target_size=288):
    inputs, targets = [], []
    for i in range(0, len(data) - window_size - target_size + 1):
        inputs.append(data[i:i+window_size])
        targets.append(data[i+window_size:i+window_size+target_size])
    return torch.tensor(inputs, dtype=torch.float32), torch.tensor(targets, dtype=torch.float32)

data = np.random.randn(2304, 7)
inputs, targets = create_samples(data)

# 训练
model = TransformerTimeSeries()
optimizer = torch.optim.Adam(model.parameters(), lr=0.001)
criterion = nn.MSELoss()

batch_inputs = inputs[0:32]  # [32, 2016, 7]
batch_targets = targets[0:32]  # [32, 288, 7]
optimizer.zero_grad()
pred = model(batch_inputs)  # [32, 288, 7]
loss = criterion(pred, batch_targets)
loss.backward()
optimizer.step()
print(f"Loss: {loss.item():.4f}")
