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

# Transformer 模型
class TransformerSeq2Seq(nn.Module):
    def __init__(self, input_size=7, hidden_size=64, num_layers=2, num_heads=4, dropout=0.1):
        super().__init__()
        self.input_embedding = nn.Linear(input_size, hidden_size)
        self.tgt_embedding = nn.Linear(input_size, hidden_size)
        self.pos_encoder = PositionalEncoding(hidden_size)
        
        encoder_layer = nn.TransformerEncoderLayer(d_model=hidden_size, nhead=num_heads, dropout=dropout)
        decoder_layer = nn.TransformerDecoderLayer(d_model=hidden_size, nhead=num_heads, dropout=dropout)
        self.encoder = nn.TransformerEncoder(encoder_layer, num_layers=num_layers)
        self.decoder = nn.TransformerDecoder(decoder_layer, num_layers=num_layers)
        
        self.fc_out = nn.Linear(hidden_size, input_size)  # 输出 7 个特征
    
    def forward(self, src, tgt):
        # src: [batch_size, src_seq_len=2016, 7]
        # tgt: [batch_size, tgt_seq_len=288, 7] (训练时用 teacher forcing)
        src = self.input_embedding(src)  # [batch_size, 2016, hidden_size]
        src = self.pos_encoder(src.transpose(0, 1)).transpose(0, 1)
        encoder_out = self.encoder(src.transpose(0, 1)).transpose(0, 1)  # [batch_size, 2016, hidden_size]
        
        tgt = self.tgt_embedding(tgt)  # [batch_size, 288, hidden_size]
        tgt = self.pos_encoder(tgt.transpose(0, 1)).transpose(0, 1)
        
        # Decoder：Encoder-Decoder Attention 在这里发生
        tgt_mask = nn.Transformer.generate_square_subsequent_mask(tgt.size(1)).to(src.device)  # Mask 防止看未来
        decoder_out = self.decoder(tgt.transpose(0, 1), encoder_out.transpose(0, 1), tgt_mask=tgt_mask).transpose(0, 1)  # [batch_size, 288, hidden_size]
        
        out = self.fc_out(decoder_out)  # [batch_size, 288, 7]
        return out

# 数据准备
def create_samples(data, window_size=2016, target_size=288):
    inputs, targets = [], []
    for i in range(0, len(data) - window_size - target_size + 1):
        inputs.append(data[i:i+window_size])  # [2016, 7]
        targets.append(data[i+window_size:i+window_size+target_size])  # [288, 7]
    return torch.tensor(inputs, dtype=torch.float32), torch.tensor(targets, dtype=torch.float32)

data = np.random.randn(2304, 7)
inputs, targets = create_samples(data)

# 训练
model = TransformerSeq2Seq()
optimizer = torch.optim.Adam(model.parameters(), lr=0.001)
criterion = nn.MSELoss()

batch_inputs = inputs[0:32]  # [32, 2016, 7]
batch_targets = targets[0:32]  # [32, 288, 7]
optimizer.zero_grad()
pred = model(batch_inputs, batch_targets)  # 用 teacher forcing
loss = criterion(pred, batch_targets)
loss.backward()
optimizer.step()
print(f"Loss: {loss.item():.4f}")
