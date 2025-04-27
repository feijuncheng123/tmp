
import java.util.concurrent.*;


/**
 * Fork/Join:可以将一个大的任务拆分成多个子任务进行并行处理，最后将子 任务结果合并成最后的计算结果，并进行输出
 * (1)Fork:把一个复杂任务进行分拆，大事化小
 * (2)Join:把分拆任务的结果进行合并
 *
 * 1、ForkJoinTask：首先需要创建一个 ForkJoin 任务：需要继承ForkJoinTask的子类，Fork/Join 框架提供了两个子类
 * （1）RecursiveAction:用于没有返回结果的任务
 * （2）RecursiveTask:用于有返回结果的任务
 *
 * 2、ForkJoinPool:ForkJoinTask 需要通过 ForkJoinPool 来执行
 *
 */
class MyTask extends RecursiveTask<Integer> {

    //拆分差值不能超过10，计算10以内运算
    private static final Integer VALUE = 10;
    private int begin ;//拆分开始值
    private int end;//拆分结束值
    private int result ; //返回结果

    //创建有参数构造
    public MyTask(int begin,int end) {
        this.begin = begin;
        this.end = end;
    }

    //拆分和合并过程
    @Override
    protected Integer compute() {
        //判断相加两个数值是否大于10
        if((end-begin)<=VALUE) {
            //相加操作
            for (int i = begin; i <=end; i++) {
                result = result+i;
            }
        } else {//进一步拆分
            //获取中间值
            int middle = (begin+end)/2;
            //拆分左边
            MyTask task01 = new MyTask(begin,middle);
            //拆分右边
            MyTask task02 = new MyTask(middle+1,end);
            //调用方法拆分
            task01.fork();
            task02.fork();
            //合并结果
            result = task01.join()+task02.join();
        }
        return result;
    }
}

public class lesson_juc_05_forkjoin {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        //创建MyTask对象
        MyTask myTask = new MyTask(0,100);
        //创建分支合并池对象
        ForkJoinPool forkJoinPool = new ForkJoinPool();
        ForkJoinTask<Integer> forkJoinTask = forkJoinPool.submit(myTask);
        //获取最终合并之后结果
        Integer result = forkJoinTask.get();
        System.out.println(result);
        //关闭池对象
        forkJoinPool.shutdown();
    }
}
