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

# 自定义 Encoder 层（突出残差连接）
class CustomEncoderLayer(nn.Module):
    def __init__(self, d_model, nhead, dim_feedforward=256, dropout=0.1):
        super().__init__()
        self.self_attn = nn.MultiheadAttention(d_model, nhead, dropout=dropout)
        self.linear1 = nn.Linear(d_model, dim_feedforward)
        self.dropout = nn.Dropout(dropout)
        self.linear2 = nn.Linear(dim_feedforward, d_model)
        self.norm1 = nn.LayerNorm(d_model)
        self.norm2 = nn.LayerNorm(d_model)
        self.dropout1 = nn.Dropout(dropout)
        self.dropout2 = nn.Dropout(dropout)
    
    def forward(self, src):
        # Self-Attention + 残差连接
        attn_output, _ = self.self_attn(src, src, src)  # [seq_len, batch_size, d_model]
        src = self.norm1(src + self.dropout1(attn_output))  # 残差连接
        print(f"After Self-Attention residual: {src.shape}")
        
        # FFN + 残差连接
        ffn_output = self.linear2(self.dropout(torch.relu(self.linear1(src))))
        src = self.norm2(src + self.dropout2(ffn_output))  # 残差连接
        print(f"After FFN residual: {src.shape}")
        return src

# Transformer 模型
class TransformerTimeSeries(nn.Module):
    def __init__(self, input_size=7, hidden_size=64, num_layers=2, num_heads=4):
        super().__init__()
        self.input_embedding = nn.Linear(input_size, hidden_size)
        self.pos_encoder = PositionalEncoding(hidden_size)
        self.encoder_layers = nn.ModuleList([CustomEncoderLayer(hidden_size, num_heads) for _ in range(num_layers)])
        self.fc_out = nn.Linear(hidden_size, input_size)
    
    def forward(self, src):
        src = self.input_embedding(src)  # [batch_size, 2016, hidden_size]
        src = self.pos_encoder(src.transpose(0, 1)).transpose(0, 1)
        for layer in self.encoder_layers:
            src = layer(src.transpose(0, 1)).transpose(0, 1)  # [batch_size, seq_len, hidden_size]
        return self.fc_out(src[:, -288:, :])  # [batch_size, 288, 7]

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
