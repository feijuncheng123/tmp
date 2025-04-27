import numpy as np

class NeuralNetwork():
    def __init__(self):
        np.random.seed(1)  #每一次运行产生的随机数都相等
        self.synaptic_weights=2*np.random.random((3,1))-1

    def __sigmod(self,X):
        return 1/(1+np.exp(-X))

    def predict(self,input):
        return self.__sigmod(np.dot(input,self.synaptic_weights))

    def __sigmoid_derivative(self,y): #sigmoid函数的导数
        return y*(1-y)

    def train(self,train_set_inputs,train_set_outputs,max_step):
        for iteration in range(max_step):
            output=self.predict(train_set_inputs)

            error=train_set_outputs-output  #差异
            y=error*self.__sigmoid_derivative(output)
            print(y)
            adjustment=np.dot(train_set_inputs.T,y)
            # print(adjustment)
            self.synaptic_weights += adjustment


if __name__ == '__main__':
    neural_network=NeuralNetwork()
    print("Random starting synaptic weights:")
    print(neural_network.synaptic_weights)

    train_set_inputs=np.array([[0,0,1],[1,1,1],[1,0,1],[0,1,1]])
    train_set_outputs=np.array([[0,1,1,0]]).T
    # print(train_set_outputs)
    neural_network.train(train_set_inputs,train_set_outputs,10000)

    print('New synaptic weights after training:')
    print(neural_network.synaptic_weights)

    result=neural_network.predict(np.array([0,1,0]))
    print(result)