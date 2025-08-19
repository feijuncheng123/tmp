import torch
import torch.nn as nn
import numpy as np

# 滑动窗口
def create_samples(data, window_size=2016, target_size=288):
    inputs, targets = [], []
    for i in range(0, len(data) - window_size - target_size + 1):
        inputs.append(data[i:i+window_size])  # [2016, 7]
        targets.append(data[i+window_size:i+window_size+target_size])  # [288, 7]
    return torch.tensor(inputs, dtype=torch.float32), torch.tensor(targets, dtype=torch.float32)

data = np.random.randn(2304, 7)
inputs, targets = create_samples(data)

# 注意力层
class Attention(nn.Module):
    def __init__(self, hidden_size):
        super().__init__()
        self.W_a = nn.Linear(hidden_size * 2, hidden_size)
        self.v_a = nn.Parameter(torch.randn(hidden_size))
    
    def forward(self, decoder_hidden, encoder_outputs):
        # decoder_hidden: [batch_size, hidden_size]
        # encoder_outputs: [batch_size, seq_len, hidden_size]
        batch_size, seq_len, hidden_size = encoder_outputs.size()
        print(f"Attention: encoder_outputs shape: {encoder_outputs.shape}")
        print(f"Attention: decoder_hidden shape: {decoder_hidden.shape}")
        decoder_hidden = decoder_hidden.unsqueeze(1).repeat(1, seq_len, 1)  # [batch_size, seq_len, hidden_size]
        combined = torch.cat((decoder_hidden, encoder_outputs), dim=2)  # [batch_size, seq_len, 2*hidden_size]
        energy = torch.tanh(self.W_a(combined))  # [batch_size, seq_len, hidden_size]
        scores = energy @ self.v_a  # [batch_size, seq_len]
        attn_weights = torch.softmax(scores, dim=1)  # [batch_size, seq_len]
        context = attn_weights.unsqueeze(-1) * encoder_outputs  # [batch_size, seq_len, hidden_size]
        context = context.sum(dim=1)  # [batch_size, hidden_size]
        print(f"Attention: context shape: {context.shape}, attn_weights shape: {attn_weights.shape}")
        return context, attn_weights

# Seq2Seq 模型
class Seq2SeqLSTMWithAttention(nn.Module):
    def __init__(self, input_size=7, hidden_size=64, output_size=7, num_layers=1):
        super().__init__()
        self.encoder = nn.LSTM(input_size, hidden_size, num_layers=num_layers, batch_first=True)
        self.decoder = nn.LSTM(input_size, hidden_size, num_layers=num_layers, batch_first=True)
        self.attention = Attention(hidden_size)
        self.fc = nn.Linear(hidden_size * 2, output_size)  # 拼接 context 和 decoder 输出
    
    def forward(self, x, target_len=288):
        # Encoder
        encoder_outputs, (h_n, c_n) = self.encoder(x)  # [batch_size, 2016, hidden_size], h_n: [num_layers, batch_size, hidden_size]
        print(f"Encoder outputs shape: {encoder_outputs.shape}, h_n shape: {h_n.shape}")
        
        # Decoder
        dec_input = torch.zeros(x.size(0), 1, x.size(2)).to(x.device)  # [batch_size, 1, 7]
        outputs = []
        h, c = h_n, c_n
        for t in range(target_len):
            dec_out, (h, c) = self.decoder(dec_input, (h, c))  # [batch_size, 1, hidden_size]
            context, attn_weights = self.attention(h[-1], encoder_outputs)  # 用最后一层隐藏状态
            combined = torch.cat((dec_out.squeeze(1), context), dim=1)  # [batch_size, 2*hidden_size]
            out = self.fc(combined)  # [batch_size, 7]
            outputs.append(out.unsqueeze(1))
            dec_input = out.unsqueeze(1)  # 用预测值作为输入
        return torch.cat(outputs, dim=1)  # [batch_size, 288, 7]

# 训练
model = Seq2SeqLSTMWithAttention()
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
