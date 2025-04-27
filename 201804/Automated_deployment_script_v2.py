#!/usr/bin/env python
# -*- coding: utf-8 -*-

import logging
import configparser as cfp
import argparse
import requests
import json
import time
import copy
import threading
import sys
import queue as Queue

logging.basicConfig(level=logging.INFO, datefmt='%Y-%m-%d %H:%M:%S',
                    format='%(lineno)d %(name)s %(asctime)s %(levelname)s %(message)s')


class Myargparse(argparse.ArgumentParser):
    def error(self, message):
        raise KeyError(message)


class Config(object):
    def __init__(self):
        self.cfg = cfp.ConfigParser()
        self.parser=Myargparse()
        self.parser.add_argument('--role',required=True)
        self.parser.add_argument('--token',required=True)
        self.parser.add_argument('--properties',required=True)
        self.parser.add_argument('--timeout',type=int)
        self.parser.add_argument('--config_path',required=True)
        self.parameter=self.__parameters()
        self.path=self.parameter['config_path']
        self.configdata = self.__profile()

    def __profile(self):
        self.cfg.read(self.path)
        cfgcontent=self.cfg.sections()
        if not cfgcontent:
            logging.error('The Profile Path does not exist!')
            sys.exit('The Profile Path does not exist!')
        configdata={}
        for section in cfgcontent:
            sectionitem=self.cfg.items(section)
            configdata[section]=dict(sectionitem)
        return configdata

    def __parameters(self):
        try:
            parameter=self.parser.parse_args()
            parameterdict = vars(parameter)
            if parameterdict["timeout"] is None:
                parameterdict["timeout"] = 2000
                logging.info('The default timeout is set to 2000 seconds!')
            parameterdict['properties']=json.loads(parameterdict['properties'])
            return parameterdict
        except KeyError as k:
            logging.error('Missing required parameters:%s'%(k.message))
            sys.exit('Missing required parameters:%s'%(k.message))
        except Exception as e:
            logging.error(e.message)
            sys.exit(e.message)

    @property
    def Waitingtime(self):
        timeout=self.parameter['timeout']
        if timeout<10: timeout=10
        logging.info('The timeout is %s s'%timeout)
        return timeout

    @property
    def Publishdata(self):
        apidata={}
        for key in self.parameter:
            if key in ['role','token','properties']:
                apidata[key]=self.parameter[key]
        apidata.update(self.configdata["default"])
        return apidata

    @property
    def update_ip(self):
        hostdict=self.configdata['host']
        ip=hostdict['update_ip']
        logging.info("The update_ip address is %s"%ip)
        return ip

    @property
    def gestatus_ip(self):
        hostdict = self.configdata['host']
        ip = hostdict['gestatus_ip']
        logging.info("The gestatus_ip address is %s" % ip)
        return ip

    def Querydata(self,responsedata,debug=True):
        querydatalist=[]
        qdata={}
        qdata["role"]=self.parameter["role"]
        qdata["token"]=self.parameter["token"]
        qdata["operator"]=self.configdata["default"]["operator"]
        qdata["debug"]=debug
        qdata["module_publish_id"]=responsedata["result"]["module_publish_id"]
        submitresult=responsedata["result"]["details"]
        if type(submitresult)== list:
            for detail in submitresult:
                qdata["version"]=detail["version"]
                tmp=copy.deepcopy(qdata)
                querydatalist.append(tmp)
        elif type(submitresult)== dict:
            querydatalist.append(submitresult)
        else:
            logging.error('submitresult type error! error type:%s'%(type(submitresult)))
            sys.exit('submitresult type error! error type:%s'%(type(submitresult)))
        logging.info("Number of versions to be queried is %s"%(len(querydatalist)))
        return querydatalist


class operation_handler(object):
    def __post(self,ip,data):
        try:
            response = requests.request('post', ip, json=data, timeout=5)
            status = response.status_code
            logging.info('The response status_code is %s' % status)
            assert status == 200
            logging.info('response code verification succeeded!')
            responsedata = response.json()
            logging.info('Post response is %s'%responsedata)
            assert type(responsedata) == dict
            logging.info('Response type verification succeeded!')
            ret=int(responsedata.get("ret", 1))
            if not ret:
                logging.info('Data sent successfully!')
                return responsedata
            else:
                raise LookupError
        except AssertionError as A:
            logging.error('Response status_code or type verification failed! Error message: %s'%(A.message))
            sys.exit('Response status_code or type verification failed! Error message: %s'%(A.message))
        except LookupError:
            logging.error('Datasent Error! Error code:%s !; sent data:%s'%(responsedata.get("err_msg", "return None"),data))
            sys.exit('Datasent Error! Error code:%s ! sent data:%s' % (responsedata.get("err_msg", "return None"),data))
        except Exception as e:
            logging.error("err_message:%s;sentdata: %s"%(e.message,data))
            sys.exit("err_message:%s;sentdata: %s"%(e.message,data))

    def Publish(self,ip,submitdata):
        logging.info('Begin to submit data!')
        publishdata=self.__post(ip,submitdata)
        return publishdata


    def Publish_status_query(self,ip,querydata):
        logging.info('Start querying the release status!')
        queryresponse = self.__post(ip,querydata)
        publish_progress=queryresponse["result"]["module_publish"]["publish_progress"]
        logging.info('The current version is published at %s'%publish_progress)
        successcluster=[True]
        if int(publish_progress) == 100:
            logging.info('The current version publish successfully!')
            successcluster.extend(queryresponse["result"]["cluster_publish"])
            return successcluster
        failcluster=[False]
        for cluster in queryresponse["result"]["cluster_publish"]:
            logging.info('Cluster zone:%s publish_progress is %s'%(cluster["zone"],cluster["publish_progress"]))
            if int(cluster["publish_progress"]) < 100:
                failcluster.append(cluster)
        return failcluster



class Mythread(threading.Thread):
    def __init__(self,fun,ip,dt,timeout,DEFAULT_QUERY_INTERVAL):
        threading.Thread.__init__(self)
        self.fun=fun
        self.ip=ip
        self.dt=dt
        self.DEFAULT_QUERY_INTERVAL=DEFAULT_QUERY_INTERVAL
        self.timeout=timeout
        self.queue=Queue.Queue(1)
        self.fail=[]

    def run(self):
        logging.info('The current query version is  %s' % self.dt)
        startpoint=time.time()
        i=1
        try:
            while True:
                logging.info("The current version the %s's query "%i)
                i += 1
                looppoint=time.time()
                value=self.fun(self.ip,self.dt)
                if value[0]:
                    self.sucess=list_convert(value[1:],0)
                    return
                else:
                    endpoint = time.time()
                    if endpoint - startpoint >= self.timeout:
                        self.fail.extend(value[1:])
                        break
                    if endpoint-looppoint>=self.DEFAULT_QUERY_INTERVAL: continue
                    else:time.sleep(self.DEFAULT_QUERY_INTERVAL-endpoint+looppoint)
        except SystemExit as s:
            self.queue.put(s.message)

    @property
    def result(self):
        return self.sucess,self.fail


def list_convert(lis1,key):
    lis2=[]
    for term in lis1:
        tmp = {}
        tmp["ret"] = key
        tmp["zone"] = term["zone"]
        tmp["collie"] = term["collie"]
        tmp["err_msg"] = term["err_msg"]
        tmp["version"] = term["version"]
        tmp["publish_progress"] = term["publish_progress"]
        lis2.append(tmp)
    return lis2


def main(DEFAULT_QUERY_INTERVAL=3):
    output = {}
    try:
        config=Config()
        handler=operation_handler()
        publishresponse=handler.Publish(config.update_ip,config.Publishdata)
        querydata=config.Querydata(publishresponse)
        timeout=config.Waitingtime
        queryip=config.gestatus_ip
        output["details"]=[]
        threadlist=[]
        faillist=[]
        for data in querydata:
            t=Mythread(handler.Publish_status_query,queryip,data,timeout,DEFAULT_QUERY_INTERVAL)
            t.start()
            threadlist.append(t)
        for t in threadlist:
            t.join()
            if not t.queue.empty():
                raise SystemExit(t.queue.get())
            success,fail=t.result
            output["details"].extend(success)
            faillist.extend(fail)
        output["ret"] = 0
        output["err_msg"] = ""
        if faillist:
            logging.error('Error: Query Timeout!')
            failcluster=list_convert(faillist,1)
            logging.error('Failure Cluster inluld:%s' % (failcluster))
            output["details"].extend(failcluster)
        else:
            logging.info("Congratulation! All versions publish successfully!")
        return output
    except SystemExit as s:
        output["ret"] = 1
        output["err_msg"]=s.message
        output["details"]=[]
        logging.error('error code: %s'%s.message)
        return output


if __name__ == '__main__':
    res=main(5)
    print('-------------------------------')
    logging.info('return value:\r\n %s'%res)