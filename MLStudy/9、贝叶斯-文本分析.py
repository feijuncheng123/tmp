#停用词：大量出现，但对分析无用的词，比如的、一次、一片、那些、各种符号等等
#词频统计（TF）：先踢除停用词。=某个词的词频/总词频数，归一化
#逆文档频率统计（idf）：=log(总文档数/（包含该词的文档数+1）)
#逆文档频率表示：在特定的总文档数下，如10000个文档，出现某个词的文档数越少，则该词的特殊性越高

#TF-idf：词频TF*逆文档频率idf。表示某个词的词频越高，该词在总文档中出现的文档数越少，则越特殊
#主要用于文章的关键词提取

#文本相似度分析
import jieba
import pandas as pd
import numpy
df_news = pd.read_table('./data/val.txt',names=['category','theme','URL','content'],encoding='utf-8')
df_news = df_news.dropna()
df_news.head()

content = df_news.content.values.tolist() #将content列转化为列表
content_S = []
for line in content:
    current_segment = jieba.lcut(line) #对新闻进行分词
    if len(current_segment) > 1 and current_segment != '\r\n': #换行符
        content_S.append(current_segment)

#content_S为嵌套列表，每一个新闻被分词成列表添加进去。
df_content=pd.DataFrame({'content_S':content_S})
df_content.head() #每一行为一个列表组成

#加载停用词表
stopwords=pd.read_csv("stopwords.txt",index_col=False,sep="\t",quoting=3,names=['stopword'], encoding='utf-8')
stopwords.head(20)

#删除停用词
def drop_stopwords(contents, stopwords):
    contents_clean = []
    all_words = []
    for line in contents:
        line_clean = []
        for word in line:
            if word in stopwords:
                continue
            line_clean.append(word)
            all_words.append(str(word))
        contents_clean.append(line_clean)
    return contents_clean, all_words
    # print (contents_clean)


contents = df_content.content_S.values.tolist()
stopwords = stopwords.stopword.values.tolist()
contents_clean, all_words = drop_stopwords(contents, stopwords)

# df_content.content_S.isin(stopwords.stopword)
# df_content=df_content[~df_content.content_S.isin(stopwords.stopword)]
# df_content.head()

#删除停用词后的词表（当前文档内）
df_content=pd.DataFrame({'contents_clean':contents_clean})
df_content.head()

#全部文档的全部词表
df_all_words=pd.DataFrame({'all_words':all_words})
df_all_words.head()

#全部词的词频
words_count=df_all_words.groupby(by=['all_words'])['all_words'].agg({"count":numpy.size})
words_count=words_count.reset_index().sort_values(by=["count"],ascending=False)
words_count.head()

#词云图：很好！
from wordcloud import WordCloud
import matplotlib.pyplot as plt
# %matplotlib inline IPython内嵌绘图用，只支持命令行，不支持pycharm
import matplotlib
matplotlib.rcParams['figure.figsize'] = (10.0, 5.0)

wordcloud=WordCloud(font_path="./data/simhei.ttf",background_color="white",max_font_size=80)
word_frequence = {x[0]:x[1] for x in words_count.head(100).values}
wordcloud=wordcloud.fit_words(word_frequence)
plt.imshow(wordcloud)


import jieba.analyse
index = 2400
print (df_news['content'][index])
content_S_str = "".join(content_S[index])
print ("  ".join(jieba.analyse.extract_tags(content_S_str, topK=5, withWeight=False)))
#extract_tags为使用TF-idf提取文档中的关键词，topK为选取几个关键词


#LDA建模：主题模型，类似于聚类，需要指定要区分的主题数
from gensim import corpora, models, similarities
import gensim  #主要用于自然语言处理的库
#http://radimrehurek.com/gensim/

#做映射，相当于词袋，将每个词进行编码映射
dictionary = corpora.Dictionary(contents_clean)
corpus = [dictionary.doc2bow(sentence) for sentence in contents_clean]

lda = gensim.models.ldamodel.LdaModel(corpus=corpus, id2word=dictionary, num_topics=20)
#类似Kmeans自己指定K值
#一号分类结果：第一类主题的最关键5个主题词
print(lda.print_topic(1, topn=5))

#分别打印主题词
for topic in lda.print_topics(num_topics=20, num_words=5):
    print (topic[1])



'''-------------贝叶斯分类---------------------------'''

df_train=pd.DataFrame({'contents_clean':contents_clean,'label':df_news['category']})
df_train.tail()
df_train.label.unique()

#pandas可以对标签进行映射分类
label_mapping = {"汽车": 1, "财经": 2, "科技": 3, "健康": 4, "体育":5, "教育": 6,"文化": 7,"军事": 8,"娱乐": 9,"时尚": 0}
df_train['label'] = df_train['label'].map(label_mapping)
df_train.head()

from sklearn.model_selection import train_test_split

x_train, x_test, y_train, y_test = train_test_split(df_train['contents_clean'].values, df_train['label'].values, random_state=1)

#用空格连接每个词作为下面CountVectorizer的输入数据
words = []
for line_index in range(len(x_train)):
    try:
        #x_train[line_index][word_index] = str(x_train[line_index][word_index])
        words.append(' '.join(x_train[line_index]))
    except:
        print (line_index)


#需要将词转化为向量
from sklearn.feature_extraction.text import CountVectorizer #向量构造器
#举例
texts=["dog cat fish","dog cat cat","fish bird", 'bird'] #需要用空格分词
cv = CountVectorizer()
#可以添加ngram_range=(1,4)参数，表示对词进行组合，构成组合词计算，但增加复杂度，稀疏
cv_fit=cv.fit_transform(texts)  #将texts转化为向量了

print(cv.get_feature_names())  #打印语料库里面不重复的单词
print(cv_fit.toarray())  #每个不重复代词在每个文档中的次数向量，位置和上面不重复单词对应
print(cv_fit.toarray().sum(axis=0)) #每个单词总的出现次数

#创建一个向量构造器示例
vec = CountVectorizer(analyzer='word', max_features=4000,  lowercase = False)
vec.fit(words)


#用贝叶斯模型训练
from sklearn.naive_bayes import MultinomialNB
classifier = MultinomialNB()
classifier.fit(vec.transform(words), y_train)  #传入向量和标签

#对测试集做同样的处理
test_words = []
for line_index in range(len(x_test)):
    try:
        #x_train[line_index][word_index] = str(x_train[line_index][word_index])
        test_words.append(' '.join(x_test[line_index]))
    except:
         print(line_index)

#计算测试集的准确率
classifier.score(vec.transform(test_words), y_test)


#Tfidf特征提取：也是构造词向量
from sklearn.feature_extraction.text import TfidfVectorizer
vectorizer = TfidfVectorizer(analyzer='word', max_features=4000,  lowercase = False)
vectorizer.fit(words)


