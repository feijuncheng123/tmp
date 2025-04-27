import pandas as pd
import seaborn as sns
import matplotlib.pyplot as plt
plt.rcParams['font.sans-serif']=['SimHei'] #用来正常显示中文标签
plt.rcParams['axes.unicode_minus']=False #用来正常显示负号

df=pd.read_csv("")
modeldata=df

plt.figure(figsize=(10,6), dpi= 80)
sns.kdeplot(modeldata.loc[df['STATUS'] == 0, "CALLING_NUMS"], shade=True, color="g", label="正常号码", alpha=.7)
sns.kdeplot(modeldata.loc[df['STATUS'] == 1, "CALLING_NUMS"], shade=True, color="deeppink", label="欺诈号码", alpha=.7)
plt.title('主叫通话号码数分布图', fontsize=22)
plt.ticklabel_format(style='plain', axis='x')
plt.legend()
plt.show()


# oneid
from pyspark.sql import SparkSession
spark=SparkSession.builder().getOrCreate()
data=spark.sql("select * from zc_pro1778_role4.dwa_oneid_sample_data_tmp")
pyModelData = data.toPandas()

cols=["cnt_all_voice","per_all_voice","all_voice_time","per_all_voice_time","cnt_busy_workday_voice","per_busy_workday_voice","busy_workday_voice_time","per_busy_workday_voice_time","cnt_play_workday_voice","per_play_workday_voice","play_workday_voice_time","per_playe_workday_voice_time","cnt_off_workday_voice","per_off_workday_voice","cnt_workday_voice","per_workday_voice","workday_voice_time","per_workday_voice_time","cnt_weekend_voice","per_weekend_voice","weekend_voice_time","per_weekend_voice_time","cnt_workday_days","per_workday_days","cnt_weekend_days","per_weekend_days","long_voice_days","long_voice_days_rank","social_coincide_nums","social_coincide_nums_rank","enb_coincide_nums","enb_coincide_nums_rank","day_time_enb_nums","day_time_enb_nums_rank","day_time_top_enb_days","night_time_enb_nums","night_time_enb_nums_rank","night_time_top_enb_days","weekend_enb_nums","weekend_enb_nums_rank","weekend_top_enb_days"]
cols_names=["总通话次数","总通话次数占比","总通话时长","总通话时长占比","忙时通话次数","忙时通话次数占比","忙时通话时长","忙时通话时长占比","闲时通话次数","闲时通话次数占比","闲时通话时长","闲时通话时长占比","下班时段通话次数","下班时段通话次数占比","工作日通话次数","工作日通话次数占比","工作日通话时长","工作日通话时长占比","非工作日通话次数","非工作日通话次数占比","非工作日通话时长","非工作日通话时长占比","工作日通话天数","工作日通话天数占比","非工作日通话天数","非工作日通话天数占比","最长连续通话天数","最长连续通话天数排名","交往圈重合数","交往圈重合数排名","总的基站重合数","总的基站重合数排名","白天时段基站重合数","白天时段基站重合数排名","白天时段常用基站TOP1重合天数","夜晚时段基站重合数","夜晚时段基站重合数排名","夜晚时段常用基站TOP1重合天数","非工作日基站重合数","非工作日基站重合数排名","非工作日常用基站TOP1重合天数"]

cols_map=dict(zip(cols,cols_names))

fig = plt.figure(figsize=[15, 20])
for i in range(len(cols)):
    col_name=cols[i]
    ax = fig.add_subplot(7, 3, i + 1)
    sns.kdeplot(pyModelData.loc[pyModelData['label'] == 0, "cnt_all_voice"], shade=True, color="g",label="not_family_num", alpha=.7,ax=ax)
    sns.kdeplot(pyModelData.loc[pyModelData['label'] == 1, "cnt_all_voice"], shade=True, color="deeppink",label="family_num", alpha=.7,ax=ax)
    ax.set_title(col_name, fontsize=12)
fig.tight_layout(h_pad=1)
fig.show()



plt.figure(figsize=(10,6), dpi= 80)
sns.kdeplot(pyModelData.loc[pyModelData['label'] == 0, "cnt_all_voice"], shade=True, color="g", label="not_family_num", alpha=.7)
sns.kdeplot(pyModelData.loc[pyModelData['label'] == 1, "cnt_all_voice"], shade=True, color="deeppink", label="family_num", alpha=.7)
plt.title('age_gap distribute', fontsize=22)
plt.ticklabel_format(style='plain', axis='x')
plt.legend()
plt.show()


