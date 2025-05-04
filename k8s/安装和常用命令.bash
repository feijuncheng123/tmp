sed -ri 's/.*swap.*/#&/' /etc/fstab



cat>/etc/sysctl.d/k8s.conf <<EOF
 net.bridge.bridge-nf-call-ip6tables = 1
 net.bridge.bridge-nf-call-iptables = 1
EOF

yum install ntpdate

ntpdate time.windows.com

#所有节点安装Docker/kubeadm/kubelet
sudo wget https://mirrors.aliyun.com/docker-ce/linux/centos/docker-ce.repo -O /etc/yum.repos.d/docker-ce.repo
#yum -y install docker-ce-18.06.1.ce-3.el7
sudo yum install docker-ce-19.03.9-3.el7
sudo systemctl enable docker && systemctl start docker
docker --version
sudo sudo bash -c "cat > /etc/docker/daemon.json" << EOF
{
"registry-mirrors":["https://b9pmyelo.mirror.aliyuncs.com"]
}
EOF

sudo bash -c "cat > /etc/yum.repos.d/kubernetes.repo" << EOF
[kubernetes]
name=Kubernetes
baseurl=https://mirrors.aliyun.com/kubernetes/yum/repos/kubernetes-el7-x86_64
enabled=1
gpgcheck=0
repo_gpgcheck=0
gpgkey=https://mirrors.aliyun.com/kubernetes/yum/doc/yum-key.gpg https://mirrors.aliyun.com/kubernetes/yum/doc/rpm-package-key.gpg
EOF
sudo yum install -y kubelet kubeadm kubectl
sudo systemctl enable kubelet

#master执行
sudo kubeadm init \
--apiserver-advertise-address=192.168.0.3 \
--image-repository registry.aliyuncs.com/google_containers \
--kubernetes-version v1.28.2 \
--service-cidr=10.96.0.0/12 \
--pod-network-cidr=10.244.0.0/16

mkdir -p $HOME/.kube
cp -i /etc/kubernetes/admin.conf $HOME/.kube/config
chown $(id-u):$(id-g) $HOME/.kube/config
kubectl get nodes

kubectl apply –f https://raw.githubusercontent.com/coreos/flannel/master/Documentation/kube-flannel.yml
kubectl get pods -n kube-system #查看正在运行的组件


#在master执行一键生成子节点join命令
kubeadm token create --print-join-command  
 
kubeadm token list #查看token
#如果token过期，则：
kubeadm token create --ttl 0  #使用此命令创建一个永不过期的token

#子节点执行
kubeadm join 192.168.0.3:6443 --token fzay9o.6jqnt4vpivkqb8tq \
--discovery-token-ca-cert-hash sha256:2794f5f747f9c572bc02883d981ff38781228f95429123f1ea6397634ec4369a
#如果报container的错，按照问题案例1中设置container

#测试
kubectl create deployment nginx --image=nginx
kubectl expose deployment nginx --port=80 --type=NodePort
kubectl get pod,svc  #80:32131/TCP 。32131是对外暴露的端口
#访问地址：http://NodeIP:32131.使用任意一个node节点ip加上32131端口都能访问nginx主页
kubectl delete deployment nginx #删除部署

kubectl get namespace #获取命名空间。不同命名空间互相隔离。类似数据库
#命名空间只能用于 Namespaced 资源（如 Pod、Service、Deployment），不能作用于非 namespaced 资源（如 Node、PersistentVolume）。

#进入容器
kubectl exec -it my-pod -c container2 -- bash


#查看日志：
journalctl -f -u kubelet.service  #-f为实时查看

#查看pods状态
kubectl get pods
#查看详细pod情况：
kubectl describe pod xxx
#查看节点情况：
kubectl get nodes #（-o wide）加wide更详细
#查看详细node情况
kubectl describe node xxx



#yaml编排文件生成（一般不用手写）
#1、全新生成 ,然后在此基础上修改：
kubectl create deployment web --image=nginx -o yaml --dry-run > myl.yaml #dry-run尝试运行，但不执行
#2、已部署项目，使用get导出：
kubectl get deploy nginx -o=yaml > nginx.yaml

sed -i 's#sandbox_image = "/google_containers/pause:3.9"#sandbox_image = "registry.aliyuncs.com/google_containers/pause:3.9"#g' /etc/containerd/config.toml

#污点taint：是node的属性而不是pod的
#有三个值：
#1、NoSchelule: 该node一定不被调度
#2、preferNoSchelule: 尽量不被调度
#3、NoExecute： 不会调度，并且还会驱逐该node已有的pod
#为节点添加污点：
kubectl taint node [nodename] key=value:NoSchelule #(taint的三个值)
kubectl taint node node1 env=dev:NoSchelule  #为节点打上污点


# 使用标签 'unhealthy' 和值 'true' 更新 Pod 'foo'
kubectl label pods foo unhealthy=true

# 使用标签 'status' 和值 'unhealthy' 更新 Pod 'foo'，覆盖所有现有值
kubectl label --overwrite pods foo status=unhealthy

# 更新命名空间中的所有 Pod
kubectl label pods --all status=unhealthy

# 更新由 "pod.json" 中的 type 和 name 标识的 Pod
kubectl label -f pod.json status=unhealthy

# 仅在资源版本为 1 且未更改时更新 Pod 'foo'
kubectl label pods foo status=unhealthy --resource-version=1

#ConfigMap:非加密配置
kubectl create configmap flink-config --from-file=flink-config.yaml # 从flink的配置文件中生成configmap
kubectl describe configmap flink-config
#ConfigMap需要挂载


#helm添加仓库：
helm repo add stable http://mirror.azure.cn/kubernetes/charts/
helm repo list #查看
helm repo update #更新
helm repo remove http://mirror.azure.cn/kubernetes/charts/ #删除


#helm快速部署
helm search repo mysql  #搜索镜像
helm install stable/mysql  #安装搜索出来的image
helm install status mysql #查看安装状态
kubectl edit svc nginx #vim模式编辑yaml文件：type: ClusterIP修改为type: NodePort就可以对外暴露端口

helm list -A #列出全部已安装charts
helm history ingress-nginx -n ingress-nginx #列出具体charts操作历史

helm show values repo/charts #列出特定repo下的charts可配置的值。
helm get values charts -n namespace #列出已安装的charts可配置的值。

#helm更新参数：
#必须提供charts路径（或者repo/charts形式），否则很麻烦
helm get values ingress-nginx -n ingress-nginx --all > my-values.yaml  #先导出现有配置进行修改
helm upgrade ingress-nginx repo/ingress-nginx \  #此处repo/ingress-nginx也可以使用本地chart路径或者tgz包
  -n ingress-nginx \
  -f my-values.yaml #使用修改后配置进行升级


#自定义charts
helm create myCharts  #创建一个叫myCharts的charts模板，本地目录文件夹
cd myCharts #进入myCharts
#其中：
#charts文件夹：为空，一般不用管
#Charts.yaml文件记录chart基本信息、属性配置等
#templates文件夹：包含pod的yaml文件，可以删掉，编写自己的yaml放进去
#values.yaml：记录全局变量等内容
cd templates
rm -rf ./*
kubectl create deployment flink-app --image=flink:1.20 -o yaml --dry-run  >> deployment.yaml 
kubectl expose deployment flink-app --port=8081 --target-port=8081 --type=NodePort --dry-run -o yaml >> service.yaml

#docker建立本地仓库：
docker pull registry 
docker run -d -p 5000:5000  -v /root/my-repo/:/var/lib/registry --privileged=true registry  #开启私有仓库

#使用dockerfile构建镜像
docker build -f /path/to/Dockerfile -t myimage:latest ./
docker tag  flink-on-k8s-test:v1  192.168.0.3:5000/flink-on-k8s-test:v1  #重命名容器
docker push localhost:5000/flink-on-k8s-test:v1  #推送到私有库

helm install flink-app ./flink-app  #安装自定义charts
helm upgrade flink-app ./flink-app #升级应用

#helm动态部署
#values.yaml定义变量，然后在yaml文件中捕获变量形成动态部署
#在templates中的yaml文件中使用 {{ .values.image}} 获取相应变量. {{ .Release.Name}}可以获取发行版名称

#持久化存储：
yum install nfs-utils  #需要在k8s 的nodes服务器上也安装nfs-utils
#在nfs服务器上启动nfs
systemctl start nfs

#设置挂载路径
vim /etc/exports #设置挂载路径
#/data/nfs *(rw,no_root_squash)
#挂载路径/data/nfs需创建
#创建nfs.yaml文件进行映射

#使用pv和pvc进行映射
#直接使用nfs需要指定node ip会对外暴露，是缺点
#使用pv和pvc分别映射解决：pv定义ip和相应路径和容量，用于管理。pvc应用中通过yaml定义匹配方式。


#ingress
#NodePort缺陷：可以从所有node的端口访问，导致每个端口在k8s集群中只能使用一次
#实际使用中是使用域名+端口的形式
#ingress作为服务统一入口，使用service关联一组pod：一个service下可能有多个pod。然后ingress作为入口，通过service发现pod
#需要安装匹配置ingress-controller
helm upgrade --install ingress-nginx ingress-nginx \
  --repo https://kubernetes.github.io/ingress-nginx \
  --namespace ingress-nginx --create-namespace \
  --set controller.replicaCount=3 \
  --set controller.service.type=LoadBalancer \
  --set controller.hostnetwork=true
#注意：ingress不是互斥关系，一般都需要同时配置。service暴露pod（通常是 ClusterIP 类型），ingress把对域名的请求转发到该service上
kubectl get ingressclass #获取已部署的ingress-controller名称

#服务中的ingress.yaml文件中，host域名必须配置映射到ingress-controller运行的node节点ip中，否则无法访问
#ingress-controller可以多副本部署，这样分布在不同node上，就可以进行负载均衡
kubectl describe deployment ingress-nginx-controller -n ingress-nginx #查看ingress-nginx-controller详细信息，包括副本数

#在部署的应用chart中添加ingress.yaml文件配置host和映射的服务和端口

kubectl logs mypod #log的输出日志