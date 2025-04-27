#正向概率：知道总体结构，求事件的发生概率
#逆向概率：根据观测值出现概率推测总体结构

#贝叶斯公式：
#p(A|B)=p(B|A)*p(A)/p(B)
#b发生情况下a发生的概率，等于a发生情况下b发生的概率（可理解为a、b同时发生)，除于b发生的概率

#以检测垃圾邮件为例：
#1、求N个词频发生的情况下一封邮件是否为垃圾邮件
#2、可以转化为垃圾邮件发生情况下这些词频发生的概率。
#即：P(junk_mail|(word1,word2,word3,...))=P((word1,word2,word3,...)|junk_mail)*P(junk_mail)/P((word1,word2,word3,...))
#3、由于分母每个词频的总体发生概率对分类无影响（无论是否垃圾邮件值都一样），因此可以不用计算
#4、分子中P(junk_mail)垃圾邮件概率可以用历史数据计算得出,也成为先验概率
#5、P((word1,word2,word3,...)|junk_mail)垃圾邮件发生时词频的发生概率可以转化为朴素贝叶斯计算：
#6、朴素贝叶斯认为每个特征是独立发生的，互不影响。
# 因此P((word1,word2,word3,...)|junk_mail)=P(word1|junk_mail)*P(word2|junk_mail)*....

#案例：拼写检查器，即检测一个单词被拼写错误后，最有可能的正确的词是哪一个
# 求解：argmaxc P(c|w) -> argmaxc P(w|c) P(c) / P(w)
#  P(c), 文章中出现一个正确拼写词 c 的概率, 也就是说, 在英语文章中, c 出现的概率有多大
#  P(w|c), 在用户想键入 c 的情况下敲成 w 的概率. 因为这个是代表用户会以多大的概率把 c 敲错成 w
#  argmaxc, 用来枚举所有可能的 c 并且选取概率最大的

import re,collections
from collections import Counter

def words(text): return re.findall('[a-z]+', text.lower())
#将所有词查找出来，转化为小写，去掉特殊字符


def train(features):
    model = collections.defaultdict(lambda: 1)
    for f in features:
        model[f] += 1
    return model
#features为传入words()函数的结果，对每个词的词频进行汇总加1的操作。其实用counter对象一次性搞定

NWORDS = train(words(open('big.txt').read()))

alphabet = 'abcdefghijklmnopqrstuvwxyz'

def edits1(word):
    n = len(word)
    return set([word[0:i]+word[i+1:] for i in range(n)] +                     # deletion
               [word[0:i]+word[i+1]+word[i]+word[i+2:] for i in range(n-1)] + # transposition
               [word[0:i]+c+word[i+1:] for i in range(n) for c in alphabet] + # alteration
               [word[0:i]+c+word[i:] for i in range(n+1) for c in alphabet])  # insertion
#返回每个单词挪一个位或者换一个字母等方式的列表


def known_edits2(word):
    return set(e2 for e1 in edits1(word) for e2 in edits1(e1) if e2 in NWORDS)
#返回每个单词挪2个位或者换2个字母等方式的列表，且换后的单词是正确单词的列表


def known(words): return set(w for w in words if w in NWORDS)

#如果known(set)非空, candidate 就会选取这个集合, 而不继续计算后面的
def correct(word):
    candidates = known([word]) or known(edits1(word)) or known_edits2(word) or [word]
    return max(candidates, key=lambda w: NWORDS[w])
#known([word])为单词本身就是正确的。
#known(edits1(word))为单词挪一个位或者换个字母后是正确的
#known_edits2(word)为单词挪2个位或者换2个字母后是正确的
#以上都不是则返回单词本生
#由于candidates是个set序列，因此用max返回最可能出现的正确单词。lambda表达式为计算NWORDS字母表中出现频率最高的单词，据此返回

