# -*- coding: utf-8 -*-
"""
Created on Fri Mar 27 16:51:59 2020

@author: ndq
"""

import pandas as pd
import numpy as np
from sklearn.externals import joblib
import cx_Oracle
from sqlalchemy import create_engine
import datetime
import logging
import traceback
import sqlalchemy
import threading
import json
import time
import argparse
import os

# 利用argparse定义命令行参数
ap = argparse.ArgumentParser()
ap.add_argument("-S", "--start_num", type=int, default=1,
                help="read sql start from this num")
args = vars(ap.parse_args())

# 配置日志信息
logger = logging.getLogger('mylogger')
logger.setLevel(logging.INFO)
p_handler = logging.FileHandler('log/predict.log')
p_handler.setLevel(logging.INFO)
p_handler.setFormatter(logging.Formatter('%(asctime)s - %(levelname)s - %(message)s'))
logger.addHandler(p_handler)


# 计算数据库表数据量
def count_database(table_name):
    db = cx_Oracle.connect('fmsrb', 'smart', '10.180.9.8:1521/rb')
    cursor = db.cursor()
    sql = 'select count(*) from fmsrb.' + table_name
    cursor.execute(sql)
    db.commit()
    rs = cursor.fetchall()
    value = rs[0][0]
    db.close()
    return value


# 分批读取测试数据
def chunsize_read_from_database(start_num, end_num, i=0):
    col_names = [
        'CUST_ID', 'CUST_ACCESS_NET_DT', 'CURRENCY_BAL', 'TOTAL_FEE', 'RECHARGE_TIMES',
        'TOTAL_RECHARGE_AMOUNT', 'MAX_RECHARGE_AMOUNT', 'MIN_RECHARGE_AMOUNT',
        'CALLING_NUM', 'CALLING_DUR', 'CALLING_TIMES', 'MESSAGE_SEND', 'UP_STRAFFIC',
        'IS_BLACKLIST', 'RN'
    ]
    try:
        db = cx_Oracle.connect('fmsrb', 'smart', '10.180.9.8:1521/rb')
        cursor = db.cursor()
        sql = 'SELECT * FROM (SELECT A.*, ROWNUM RN FROM (SELECT * FROM fmsrb.score_info) A WHERE ROWNUM <= %s) WHERE RN >= %s' % (
        end_num, start_num)
        cursor.execute(sql)
        db.commit()
        rs = cursor.fetchall()
        df = pd.DataFrame(rs, columns=col_names)
        db.close()
        return df
    except:
        while i < 5:
            i += 1
            logger.info('Failed and Restart Read Data')
            time.sleep(2)
            chunsize_read_from_database(start_num, end_num, i)
            break
        df = pd.DataFrame(columns=col_names)
        return df


# 读取训练好的模型及相关woe值信息
def load_model():
    LRM = joblib.load('output/LRM_local.pkl')
    res_woe = []
    res_iv = []
    res_dict = joblib.load('output/res_dict.pkl')
    activation_dict = joblib.load('output/activation_dict.pkl')
    with open('output/res_woe.txt', 'r') as f_1:
        lines = f_1.readlines()
        # while '\n' in lines:
        #   lines.remove('\n')
    for line in lines:
        res_woe.append(eval(line))
    with open('output/res_iv.txt', 'r') as f_2:
        lines = f_2.readlines()
    for line in lines:
        res_iv.append(eval(line))
    # 2020.4.1增加读取判断标签数据的文件
    with open('output/quantile_dict.json', 'r') as f:
        quantile_dict = json.load(f)
    return LRM, res_woe, res_iv, res_dict, activation_dict, quantile_dict


# 将数据拆分为黑名单用户和非黑名单用
def cut_df(df):
    df.drop('RN', axis=1, inplace=True)
    df['CUST_ACCESS_NET_DT'] = df['CUST_ACCESS_NET_DT'].astype(float)
    df.drop_duplicates(subset=['CUST_ID'], inplace=True)
    df.set_index('CUST_ID', inplace=True)
    df.fillna(0, inplace=True)
    df[np.isinf(df)] = 0
    df[df < 0] = 0  # 对缺失值和异常值进行填补

    # 拆分黑名单和非黑名单用户，非黑名单用户信用分为0，欺诈用户信用分减半
    df['IS_BLACKLIST'] = df['IS_BLACKLIST'].astype(int)
    black_cust = df.loc[df['IS_BLACKLIST'] == 1]
    normal_cust = df.loc[df['IS_BLACKLIST'] != 1]
    label_list = normal_cust['IS_BLACKLIST']
    normal_cust.drop('IS_BLACKLIST', axis=1, inplace=True)
    black_cust['identity_score'] = 0
    black_cust['credit_history_score'] = 0
    black_cust['consume_score'] = 0
    black_cust['connections_score'] = 0
    black_cust['action_score'] = 0
    black_cust['score'] = 0
    return normal_cust, black_cust, label_list


# 通过数据确定标签
def mask_label(normal_cust, quantile_dict):
    normal_cust.loc[((normal_cust['CUST_ACCESS_NET_DT'] >= quantile_dict['CUST_ACCESS_NET_DT'][0]) |
                     ((normal_cust['CALLING_DUR'] >= quantile_dict['CALLING_DUR'][0]) &
                      (normal_cust['CALLING_TIMES'] >= quantile_dict['CALLING_TIMES'][0]) &
                      (normal_cust['MESSAGE_SEND'] >= quantile_dict['MESSAGE_SEND'][0]) &
                      (normal_cust['UP_STRAFFIC'] >= quantile_dict['UP_STRAFFIC'][0])) |
                     ((normal_cust['TOTAL_FEE'] >= quantile_dict['TOTAL_FEE'][0]) &
                      (normal_cust['RECHARGE_TIMES'] >= quantile_dict['RECHARGE_TIMES'][0]) &
                      (normal_cust['TOTAL_RECHARGE_AMOUNT'] >= quantile_dict['TOTAL_RECHARGE_AMOUNT'][0]) &
                      (normal_cust['MAX_RECHARGE_AMOUNT'] >= quantile_dict['MAX_RECHARGE_AMOUNT'][0]) &
                      (normal_cust['MIN_RECHARGE_AMOUNT'] >= quantile_dict['MIN_RECHARGE_AMOUNT'][0])) |
                     (normal_cust['CALLING_NUM'] >= quantile_dict['CALLING_NUM'][0]) |
                     (normal_cust['CURRENCY_BAL'] >= quantile_dict['CURRENCY_BAL'][0])) &
                    ((normal_cust['CUST_ACCESS_NET_DT'] > quantile_dict['CUST_ACCESS_NET_DT'][1]) &
                     (normal_cust['TOTAL_FEE'] > quantile_dict['TOTAL_FEE'][1]) &
                     (normal_cust['RECHARGE_TIMES'] > quantile_dict['RECHARGE_TIMES'][1]) &
                     (normal_cust['TOTAL_RECHARGE_AMOUNT'] > quantile_dict['TOTAL_RECHARGE_AMOUNT'][1]) &
                     (normal_cust['MAX_RECHARGE_AMOUNT'] > quantile_dict['MAX_RECHARGE_AMOUNT'][1]) &
                     (normal_cust['MIN_RECHARGE_AMOUNT'] > quantile_dict['MIN_RECHARGE_AMOUNT'][1]) &
                     (normal_cust['CALLING_NUM'] > quantile_dict['CALLING_NUM'][1]) &
                     (normal_cust['CALLING_DUR'] > quantile_dict['CALLING_DUR'][1]) &
                     (normal_cust['CALLING_TIMES'] > quantile_dict['CALLING_TIMES'][1]) &
                     (normal_cust['MESSAGE_SEND'] > quantile_dict['MESSAGE_SEND'][1]) &
                     (normal_cust['UP_STRAFFIC'] > quantile_dict['UP_STRAFFIC'][1]) &
                     (normal_cust['CURRENCY_BAL'] > quantile_dict['CURRENCY_BAL'][1])),
                    'CUST_FLAG'] = 1
    normal_cust.loc[((normal_cust['CUST_ACCESS_NET_DT'] <= quantile_dict['CUST_ACCESS_NET_DT'][1]) |
                     ((normal_cust['CALLING_DUR'] <= quantile_dict['CALLING_DUR'][1]) &
                      (normal_cust['CALLING_TIMES'] <= quantile_dict['CALLING_TIMES'][1]) &
                      (normal_cust['MESSAGE_SEND'] <= quantile_dict['MESSAGE_SEND'][1]) &
                      (normal_cust['UP_STRAFFIC'] <= quantile_dict['UP_STRAFFIC'][1])) |
                     ((normal_cust['TOTAL_FEE'] <= quantile_dict['TOTAL_FEE'][1]) &
                      (normal_cust['RECHARGE_TIMES'] <= quantile_dict['RECHARGE_TIMES'][1]) &
                      (normal_cust['TOTAL_RECHARGE_AMOUNT'] <= quantile_dict['TOTAL_RECHARGE_AMOUNT'][1]) &
                      (normal_cust['MAX_RECHARGE_AMOUNT'] <= quantile_dict['MAX_RECHARGE_AMOUNT'][1]) &
                      (normal_cust['MIN_RECHARGE_AMOUNT'] <= quantile_dict['MIN_RECHARGE_AMOUNT'][1])) |
                     (normal_cust['CALLING_NUM'] <= quantile_dict['CALLING_NUM'][1]) |
                     (normal_cust['CURRENCY_BAL'] <= quantile_dict['CURRENCY_BAL'][1])) &
                    ((normal_cust['CUST_ACCESS_NET_DT'] < quantile_dict['CUST_ACCESS_NET_DT'][0]) &
                     (normal_cust['TOTAL_FEE'] < quantile_dict['TOTAL_FEE'][0]) &
                     (normal_cust['RECHARGE_TIMES'] < quantile_dict['RECHARGE_TIMES'][0]) &
                     (normal_cust['TOTAL_RECHARGE_AMOUNT'] < quantile_dict['TOTAL_RECHARGE_AMOUNT'][0]) &
                     (normal_cust['MAX_RECHARGE_AMOUNT'] < quantile_dict['MAX_RECHARGE_AMOUNT'][0]) &
                     (normal_cust['MIN_RECHARGE_AMOUNT'] < quantile_dict['MIN_RECHARGE_AMOUNT'][0]) &
                     (normal_cust['CALLING_NUM'] < quantile_dict['CALLING_NUM'][0]) &
                     (normal_cust['CALLING_DUR'] < quantile_dict['CALLING_DUR'][0]) &
                     (normal_cust['CALLING_TIMES'] < quantile_dict['CALLING_TIMES'][0]) &
                     (normal_cust['MESSAGE_SEND'] < quantile_dict['MESSAGE_SEND'][0]) &
                     (normal_cust['UP_STRAFFIC'] < quantile_dict['UP_STRAFFIC'][0]) &
                     (normal_cust['CURRENCY_BAL'] < quantile_dict['CURRENCY_BAL'][0])),
                    'CUST_FLAG'] = 0
    flag = normal_cust['CUST_FLAG']
    flag.fillna(99, inplace=True)
    normal_cust.drop('CUST_FLAG', axis=1, inplace=True)
    return flag


# 将woe相关信息重新组合为新dict
def woe_processing(res_woe, res_iv, res_dict):
    woe_dict = {}
    key_name_list = [k for k in res_dict.keys()]
    for i in range(len(key_name_list)):
        key_name = key_name_list[i]
        key_dict = {}
        for l in range(len(key_name_list)):
            if key_name in res_woe[l].keys():
                for thresh in range(len(res_dict[key_name])):
                    if type(res_dict[key_name][thresh]) is not str:
                        if str(thresh) in res_woe[l][key_name].keys():
                            key_dict[res_dict[key_name][thresh]] = res_woe[l][key_name][str(thresh)]
                        else:
                            continue
                    else:
                        key_dict[res_dict[key_name][thresh]] = res_woe[l][key_name][res_dict[key_name][thresh]]
            else:
                continue
        woe_dict[key_name] = key_dict
    return woe_dict


# 数据衍生特征，数据值进行转换，转为woe得分
def df_process(normal_cust, LRM, woe_dict, activation_dict):
    # 生成2个衍生特征
    normal_cust.loc[normal_cust.CALLING_DUR != 0, 'fee_dur'] = normal_cust.TOTAL_FEE / (1.0 * normal_cust.CALLING_DUR)
    normal_cust.loc[normal_cust.CALLING_DUR == 0, 'fee_dur'] = 0.0
    # logger.info('fee_dur end at '+ datetime.datetime.now().strftime('%Y-%m-%d %H:%M:%S'))

    normal_cust.loc[normal_cust['CALLING_DUR'] >= activation_dict['CALLING_DUR'], 'CALLING_DUR_FLAG'] = 1
    normal_cust.loc[normal_cust['CALLING_DUR'] < activation_dict['CALLING_DUR'], 'CALLING_DUR_FLAG'] = 0
    normal_cust.loc[normal_cust['CALLING_TIMES'] >= activation_dict['CALLING_TIMES'], 'CALLING_TIMES_FLAG'] = 1
    normal_cust.loc[normal_cust['CALLING_TIMES'] < activation_dict['CALLING_TIMES'], 'CALLING_TIMES_FLAG'] = 0
    normal_cust.loc[normal_cust['MESSAGE_SEND'] >= activation_dict['MESSAGE_SEND'], 'MESSAGE_SEND_FLAG'] = 1
    normal_cust.loc[normal_cust['MESSAGE_SEND'] < activation_dict['MESSAGE_SEND'], 'MESSAGE_SEND_FLAG'] = 0
    normal_cust.loc[normal_cust['UP_STRAFFIC'] >= activation_dict['UP_STRAFFIC'], 'UP_STRAFFIC_FLAG'] = 1
    normal_cust.loc[normal_cust['UP_STRAFFIC'] < activation_dict['UP_STRAFFIC'], 'UP_STRAFFIC_FLAG'] = 0
    normal_cust['activation_sum'] = normal_cust['CALLING_DUR_FLAG'] + normal_cust['CALLING_TIMES_FLAG'] + \
                                    normal_cust['MESSAGE_SEND_FLAG'] + normal_cust['UP_STRAFFIC_FLAG']
    normal_cust.loc[normal_cust['activation_sum'] == 4, 'activation'] = 5
    normal_cust.loc[normal_cust['activation_sum'] == 3, 'activation'] = 4
    normal_cust.loc[normal_cust['activation_sum'] == 2, 'activation'] = 3
    normal_cust.loc[normal_cust['activation_sum'] == 1, 'activation'] = 2
    normal_cust.loc[normal_cust['activation_sum'] == 0, 'activation'] = 1
    normal_cust.drop(['CALLING_DUR_FLAG', 'CALLING_TIMES_FLAG',
                      'MESSAGE_SEND_FLAG', 'UP_STRAFFIC_FLAG', 'activation_sum'],
                     axis=1, inplace=True)
    normal_cust['activation'] = normal_cust['activation'].astype(int)
    normal_cust['activation'] = normal_cust['activation'].astype(str)
    '''
    normal_cust['activation'] = normal_cust.apply(lambda x: activation(x, activation_dict), axis=1)
    normal_cust['activation'] = normal_cust['activation'].astype(str)
    '''
    # logger.info('activation end at '+ datetime.datetime.now().strftime('%Y-%m-%d %H:%M:%S'))

    # 删除模型未使用的特征
    normal_cust.drop(['MIN_RECHARGE_AMOUNT', 'MAX_RECHARGE_AMOUNT',
                      'CALLING_DUR', 'CALLING_TIMES', 'TOTAL_FEE'], axis=1, inplace=True)

    # 将数据转为woe值
    for col in normal_cust.columns.tolist():
        # thresh_col = res_dict[col]
        # woe_col = woe_search(col)
        if normal_cust[col].dtype != 'O':
            thresh_col = [i for i in woe_dict[col].keys()]
            thresh_col.sort()
            for i in range(len(thresh_col)):
                if i == 0:
                    normal_cust.loc[normal_cust[col] <= thresh_col[i].right, col] = woe_dict[col][thresh_col[i]]
                elif i == len(thresh_col) - 1:
                    normal_cust.loc[normal_cust[col] > thresh_col[i].left, col] = woe_dict[col][thresh_col[i]]
                else:
                    normal_cust.loc[
                        (normal_cust[col] > thresh_col[i].left) & (normal_cust[col] <= thresh_col[i].right), col] = \
                    woe_dict[col][thresh_col[i]]
        else:
            thresh_col = [i for i in woe_dict[col].keys()]
            for i in thresh_col:
                normal_cust.loc[normal_cust[col] == i, col] = woe_dict[col][i]
    # logger.info('woe end at '+ datetime.datetime.now().strftime('%Y-%m-%d %H:%M:%S'))

    # 利用woe值计算每个特征得分
    location = 600
    scale = 20 / np.log(2)
    w = LRM.coef_
    b = LRM.intercept_

    def woe_score(woe, w, b, n):
        result = (woe * w + (b / float(n))) * scale + (location / float(n))
        return result

    w_dict = {}
    for i in range(len(normal_cust.columns)):
        w_dict[normal_cust.columns[i]] = w[0][i]
    for i in normal_cust.columns:
        normal_cust[i] = normal_cust[i].apply(
            lambda x: woe_score(x, w_dict[i], b[0], len(normal_cust.columns)))
        # normal_cust[i] = (normal_cust[i].astype(float) * w_dict[i] + (b[0] / float(len(normal_cust.columns)))) * scale +(location / float(len(normal_cust.columns)))
    # logger.info('score end at '+ datetime.datetime.now().strftime('%Y-%m-%d %H:%M:%S'))

    return normal_cust


# 计算最终信用分
def compute_credit_score(df, label_list):
    df = pd.merge(df, pd.DataFrame(label_list), left_index=True, right_index=True)
    df.loc[df.IS_BLACKLIST == 0, 'identity_score'] = np.round(df.CUST_ACCESS_NET_DT * 0.15 * 4, 4)
    df.loc[df.IS_BLACKLIST != 0, 'identity_score'] = np.round(df.CUST_ACCESS_NET_DT * 0.15 * 2, 4)
    df.loc[df.IS_BLACKLIST == 0, 'credit_history_score'] = np.round(df.CURRENCY_BAL * 0.35 * 4, 4)
    df.loc[df.IS_BLACKLIST != 0, 'credit_history_score'] = np.round(df.CURRENCY_BAL * 0.35 * 2, 4)
    df.loc[df.IS_BLACKLIST == 0, 'consume_score'] = np.round(
        (df.RECHARGE_TIMES + df.TOTAL_RECHARGE_AMOUNT + df.fee_dur) * 0.20 * 4, 4)
    df.loc[df.IS_BLACKLIST != 0, 'consume_score'] = np.round(
        (df.RECHARGE_TIMES + df.TOTAL_RECHARGE_AMOUNT + df.fee_dur) * 0.20 * 2, 4)
    df.loc[df.IS_BLACKLIST == 0, 'connections_score'] = np.round(df.CALLING_NUM * 0.05 * 4, 4)
    df.loc[df.IS_BLACKLIST != 0, 'connections_score'] = np.round(df.CALLING_NUM * 0.05 * 2, 4)
    df.loc[df.IS_BLACKLIST == 0, 'action_score'] = np.round(
        (df.UP_STRAFFIC + df.MESSAGE_SEND + df.activation) * 0.25 * 4, 4)
    df.loc[df.IS_BLACKLIST != 0, 'action_score'] = np.round(
        (df.UP_STRAFFIC + df.MESSAGE_SEND + df.activation) * 0.25 * 2, 4)

    df['score'] = df['identity_score'] + df['credit_history_score'] + df['consume_score'] + df['connections_score'] + \
                  df['action_score']
    df['score'] = np.round(df['score'])
    return df


# 将黑白用户合并至一个表格中
def concat_df(normal_cust, black_cust, start_num, end_num, flag):
    normal_cust = normal_cust.reset_index(drop=False)
    black_cust = black_cust.reset_index(drop=False)
    normal_cust = normal_cust[['CUST_ID', 'score', 'identity_score', 'credit_history_score',
                               'consume_score', 'connections_score', 'action_score']]
    black_cust = black_cust[['CUST_ID', 'score', 'identity_score', 'credit_history_score',
                             'consume_score', 'connections_score', 'action_score']]

    # 2020.4.1增加用户标签字段
    normal_cust = pd.merge(normal_cust, pd.DataFrame(flag), left_on='CUST_ID', right_index=True)

    all_df = pd.concat((normal_cust, black_cust))
    now_time = datetime.datetime.now()
    now_time = now_time.strftime('%Y-%m-%d %H:%M:%S')
    all_df['compute_time'] = now_time
    all_df['compute_time'] = pd.to_datetime(all_df['compute_time'])
    all_df.rename(columns={'identity_score': 'identity_character',
                           'compute_time': 'create_time',
                           'credit_history_score': 'credit_history',
                           'consume_score': 'repayment_capacity',
                           'connections_score': 'social_relationship',
                           'action_score': 'behavior_preference'}, inplace=True)
    order_list = ['CUST_ID', 'score', 'create_time', 'identity_character', 'social_relationship',
                  'behavior_preference', 'credit_history', 'repayment_capacity', 'CUST_FLAG']
    all_df = all_df[order_list]
    # 2020.4.1增加cust_id转为列表
    cust_id_list = all_df.CUST_ID.unique().tolist()
    return all_df, cust_id_list


'''
# 创建临时表
def create_tmp_table():
    try:
        db = cx_Oracle.connect('fmsrb','smart','10.180.9.8:1521/rb')
        cursor = db.cursor()
        sql = 'CREATE TABLE fmsrb.CUST_SCORE_INFO_TMP (CUST_ID VARCHAR2(20), SCORE NUMBER, CREATE_TIME DATE, \
    IDENTITY_CHARACTER NUMBER, SOCIAL_RELATIONSHIP NUMBER, BEHAVIOR_PREFERENCE NUMBER, \
    CREDIT_HISTORY NUMBER, REPAYMENT_CAPACITY NUMBER, CUST_FLAG NUMBER)'
        cursor.execute(sql)
        db.commit()
        db.close()
    except:
        db = cx_Oracle.connect('fmsrb','smart','10.180.9.8:1521/rb')
        cursor = db.cursor()
        sql_1 = 'truncate table fmsrb.CUST_SCORE_INFO_TMP'
        cursor.execute(sql_1)
        db.commit()
        db.close()
'''


# 创建临时表
def create_tmp_table():
    db = cx_Oracle.connect('fmsrb', 'smart', '10.180.9.8:1521/rb')
    cursor = db.cursor()
    sql = 'CREATE TABLE fmsrb.CUST_SCORE_INFO_TMP (CUST_ID VARCHAR2(20), SCORE NUMBER, CREATE_TIME DATE, \
IDENTITY_CHARACTER NUMBER, SOCIAL_RELATIONSHIP NUMBER, BEHAVIOR_PREFERENCE NUMBER, \
CREDIT_HISTORY NUMBER, REPAYMENT_CAPACITY NUMBER, CUST_FLAG NUMBER)'
    cursor.execute(sql)
    db.commit()
    db.close()


# 结果传入临时表
def to_database_tmp(df, i=0):
    try:
        df.fillna(-1, inplace=True)
        # df['create_time'] = pd.to_datetime(df['create_time'])
        df['CUST_ID'] = df['CUST_ID'].astype(str)
        col = ', '.join(df.columns.tolist())
        s = ', '.join([':' + str(i) for i in range(1, df.shape[1] + 1)])
        sql = 'insert into {}({}) values({})'.format('fmsrb.CUST_SCORE_INFO_TMP', col, s)
        db = cx_Oracle.connect('fmsrb', 'smart', '10.180.9.8:1521/rb')
        cursor = db.cursor()
        cursor.executemany(sql, df.values.tolist())
        db.commit()
        cursor.close()
        db.close()
        logger.info('Finish To_database from ' + str(start_num))
    except:
        if i < 10:
            i += 1
            logger.info('Failed and Restart To_database')
            time.sleep(3)
            to_database_tmp(df, i)
        else:
            df.to_csv('output/error_cust_id.csv', mode='a', header=False, index=False)
            logger.error('Failed To_database from ' + str(start_num))


# 临时表和用户号码连接
def join_table():
    db = cx_Oracle.connect('fmsrb', 'smart', '10.180.9.8:1521/rb')
    cursor = db.cursor()
    sql_1 = "insert into fmsrb.CUST_SCORE_INFO(CUST_ID, SUB_NO, score, create_time, identity_character, \
social_relationship, behavior_preference, credit_history, repayment_capacity, CUST_FLAG) \
select a.CUST_ID, b.SUB_NO, a.score, a.create_time, a.identity_character, \
a.social_relationship, a.behavior_preference, a.credit_history, a.repayment_capacity, a.CUST_FLAG from \
fmsrb.CUST_SCORE_INFO_TMP a \
left join fmsrb.FMS_SUBSCRIBER_RB b \
on a.CUST_ID=b.CUST_ID"
    cursor.execute(sql_1)
    db.commit()
    cursor.close()
    '''
    sql_2 = 'drop table fmsrb.CUST_SCORE_INFO_TMP'
    cursor.execute(sql_2)
    '''
    db.close()


'''
# 删除临时表
def drop_tmp_table():
    db = cx_Oracle.connect('fmsrb','smart','10.180.9.8:1521/rb')
    cursor = db.cursor()
    sql_2 = 'drop table fmsrb.CUST_SCORE_INFO_TMP'
    cursor.execute(sql_2)
    db.commit()
    db.close()
'''


# 清空临时表
def truncate_tmp_table():
    db = cx_Oracle.connect('fmsrb', 'smart', '10.180.9.8:1521/rb')
    cursor = db.cursor()
    sql_2 = 'truncate table fmsrb.CUST_SCORE_INFO_TMP'
    cursor.execute(sql_2)
    db.commit()
    db.close()


# 运行函数
def run(start_num, end_num, sub_no_df=None):
    df = chunsize_read_from_database(start_num, end_num)
    if df.empty:
        logger.info('Failed Read Data from ' + str(start_num))
        pass
    else:
        logger.info('Finish Read Data from ' + str(start_num))
        normal_cust, black_cust, label_list = cut_df(df)
        logger.info('Finish Cut Data from ' + str(start_num))
        if normal_cust.empty:
            normal_cust = pd.DataFrame(columns=['score', 'identity_score', 'credit_history_score',
                                                'consume_score', 'connections_score', 'action_score'])
            normal_cust.index.name = 'CUST_ID'
            flag = pd.Series(index=['CUST_ID'], name='CUST_FLAG')
            logger.info('Finish Process Data from ' + str(start_num))
            logger.info('Normal Cust is empty from ' + str(start_num))
        else:
            flag = mask_label(normal_cust, quantile_dict)
            logger.info('Finish Tab Flag from ' + str(start_num))
            normal_cust = df_process(
                normal_cust, LRM, woe_dict, activation_dict)
            logger.info('Finish Process Data from ' + str(start_num))
            normal_cust = compute_credit_score(normal_cust, label_list)
            logger.info('Finish Compute Score from ' + str(start_num))
        all_df, cust_id_list = concat_df(normal_cust, black_cust, start_num, end_num, flag)  # 增加cust_id_list
        logger.info('Finish Concat Data from ' + str(start_num))

        to_database_tmp(all_df, i=0)  # 数据插入临时表


# csv导入数据库
def csv_to_oracle(sql, chunk, csv_num, i=0):
    try:
        db = cx_Oracle.connect('fmsrb', 'smart', '10.180.9.8:1521/rb')
        cursor = db.cursor()
        cursor.executemany(sql, chunk.values.tolist())
        db.commit()
        cursor.close()
        db.close()
        logger.info('Finish csv_to_database from line' + str(csv_num))
    except:
        if i < 10:
            # logger.error(traceback.format_exc())
            logger.info('Restart csv_to_database from line' + str(csv_num))
            i += 1
            time.sleep(1)
            csv_to_oracle(sql, chunk, csv_num, i)
        else:
            logger.info('Failed csv_to_database from line' + str(csv_num))
            pass


# 读取没有插入oracle的数据文件，并插入临时表
def read_and_to_oracle():
    chunksize = 10000
    if os.path.exists('output/error_cust_id.csv'):  # 判断文件是否存在
        col_name = ['CUST_ID', 'score', 'create_time', 'identity_character',
                    'social_relationship', 'behavior_preference', 'credit_history',
                    'repayment_capacity', 'CUST_FLAG']
        df = pd.read_csv('output/error_cust_id.csv', names=col_name, chunksize=chunksize)
        logger.info('Finish Read csv')

        csv_num = 1
        for chunk in df:
            chunk.fillna(-1, inplace=True)
            chunk['create_time'] = pd.to_datetime(chunk['create_time'])
            chunk['CUST_ID'] = chunk['CUST_ID'].astype(str)
            col = ', '.join(chunk.columns.tolist())
            s = ', '.join([':' + str(i) for i in range(1, chunk.shape[1] + 1)])
            sql = 'insert into {}({}) values({})'.format('fmsrb.CUST_SCORE_INFO_TMP', col, s)
            csv_to_oracle(sql, chunk, csv_num, i=0)
            csv_num += chunksize
    else:
        pass


if __name__ == '__main__':
    logger.info('======================= Start =======================')
    try:
        value = count_database('score_info')
        if args['start_num'] == 1:
            try:
                create_tmp_table()
                logger.info('Finish Create Tmp Table')
            except:
                logger.info('Tmp Table is existed')
                pass
            tmp_count = count_database('CUST_SCORE_INFO_TMP')
            if tmp_count >= value:
                truncate_tmp_table()  # 清空临时表
                logger.info('Finish Truncate Tmp Table')
            else:
                pass
            if os.path.exists('output/error_cust_id.csv'):  # 判断文件是否存在
                os.remove('output/error_cust_id.csv')
            else:
                pass
        else:
            pass
        chunsize = 10000
        if value % chunsize != 0:
            piece = value // chunsize + 1
        else:
            piece = value // chunsize
        now_piece = (args['start_num']) // chunsize
        LRM, res_woe, res_iv, res_dict, activation_dict, quantile_dict = load_model()
        woe_dict = woe_processing(res_woe, res_iv, res_dict)
        while now_piece < piece:
            start_num = now_piece * chunsize + 1
            end_num = start_num + chunsize - 1
            # python2中多线程不如单线程的运行效率，python3中多线程和单线程运行效率基本一致，
            # 主要由于有GIL，故取消线程操作
            '''
            threads=[]
            for i in range(10):
                num_1 = start_num+chunsize*i
                num_2 = end_num+chunsize*i
                if num_1<=value:
                    t = threading.Thread(target=run,args=(num_1, num_2))
                    threads.append(t)
                else:
                    break
            for i in range(len(threads)):
                threads[i].start()
            for i in range(len(threads)):
                threads[i].join()

            now_piece += len(threads)
            '''

            # 使用单线程
            run(start_num, end_num)
            now_piece += 1

        read_and_to_oracle()  # 本地文件读取导入临时表
        join_table()  # 最终将临时表数据和号码关联
        logger.info('Finish Join Sub_no')
        print('Finish predict')
    except:
        print('perdict failed')
        logger.error(traceback.format_exc())

    logger.info('======================= End =======================')


