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
        
        self.fc_out = nn.Linear(hidden_size, input_size)
    
    def forward(self, src, tgt=None, inference=False):
        # src: [batch_size, 2016, 7]
        src = self.input_embedding(src)  # [batch_size, 2016, hidden_size]
        src = self.pos_encoder(src.transpose(0, 1)).transpose(0, 1)
        encoder_out = self.encoder(src.transpose(0, 1)).transpose(0, 1)  # [batch_size, 2016, hidden_size]
        
        if inference:
            # 推理模式：逐步生成
            outputs = []
            tgt = torch.zeros(src.size(0), 1, src.size(2)).to(src.device)  # 初始输入 [batch_size, 1, 7]
            for t in range(288):
                tgt_embed = self.tgt_embedding(tgt)  # [batch_size, t+1, hidden_size]
                tgt_embed = self.pos_encoder(tgt_embed.transpose(0, 1)).transpose(0, 1)
                tgt_mask = nn.Transformer.generate_square_subsequent_mask(tgt.size(1)).to(src.device)
                decoder_out = self.decoder(tgt_embed.transpose(0, 1), encoder_out.transpose(0, 1), tgt_mask).transpose(0, 1)
                out = self.fc_out(decoder_out[:, -1, :])  # 取最后一步 [batch_size, 7]
                outputs.append(out.unsqueeze(1))
                tgt = torch.cat([tgt, out.unsqueeze(1)], dim=1)  # 更新 tgt
            return torch.cat(outputs, dim=1)  # [batch_size, 288, 7]
        else:
            # 训练模式：teacher forcing
            tgt = self.tgt_embedding(tgt)  # [batch_size, 288, hidden_size]
            tgt = self.pos_encoder(tgt.transpose(0, 1)).transpose(0, 1)
            tgt_mask = nn.Transformer.generate_square_subsequent_mask(tgt.size(1)).to(src.device)
            print(f"Decoder input first step shape: {tgt[:, 0, :].shape}")  # [batch_size, hidden_size]
            decoder_out = self.decoder(tgt.transpose(0, 1), encoder_out.transpose(0, 1), tgt_mask).transpose(0, 1)
            return self.fc_out(decoder_out)

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
model = TransformerSeq2Seq()
optimizer = torch.optim.Adam(model.parameters(), lr=0.001)
criterion = nn.MSELoss()

batch_inputs = inputs[0:32]  # [32, 2016, 7]
batch_targets = targets[0:32]  # [32, 288, 7]
optimizer.zero_grad()
pred = model(batch_inputs, batch_targets)  # 训练模式
loss = criterion(pred, batch_targets)
loss.backward()
optimizer.step()
print(f"Loss: {loss.item():.4f}")

# 推理示例
pred_inference = model(batch_inputs, inference=True)  # 推理模式
print(f"Inference output shape: {pred_inference.shape}")
