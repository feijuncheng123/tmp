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