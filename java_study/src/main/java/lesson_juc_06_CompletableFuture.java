import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class lesson_juc_06_CompletableFuture {
    public static void main(String[] args) throws InterruptedException, ExecutionException {

        /**
         * 1、Futrue:表示一个异步任务的引用,需要客户端不断阻塞等待或 者不断轮询才能知道任务是否完成,最终还是需要同步操作。
         * 主要缺点：
         * （1）不支持手动完成，无法通知主线程执行情况，不能主动取消，需要一直轮询
         * （2）不支持进一步的非阻塞调用，get 方法会一直阻塞到任务完成，不支持回调函数
         * （3）不支持链式调用，即一个Future结果无法继续传递给下一个Future进行处理
         * （4）不支持多个 Future 合并。即多个Future执行完毕后，无法回调同一处理
         * （5）不支持异常处理。没有异常处理的api，出问题难定位。
         *
         * 2、CompletableFuture： 实现了 Future, CompletionStage 接口，CompletionStage 接口才是异步编程 的接口抽象
         *
         *
         */

        CompletableFuture<String> future = new CompletableFuture<>();

        new Thread(() -> {
            try {
                System.out.println(Thread.currentThread().getName() + "子线程开始干活"); //子线程睡 5 秒
                Thread.sleep(5000);
                //在子线程中完成主线程
                future.complete("success");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, "A").start();

        //主线程调用 get 方法阻塞
        System.out.println("主线程调用 get 方法获取结果为: " + future.get());
        System.out.println("主线程完成,阻塞结束!!!!!!");

        //无返回值
        CompletableFuture<Void> future01 = CompletableFuture.runAsync(() -> {
            try {
                System.out.println("子线程启动干活");
                Thread.sleep(5000);
                System.out.println("子线程完成");
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        //主线程阻塞
        future01.get();
        System.out.println("主线程结束");


        //运行一个有返回值的异步任务
        CompletableFuture<String> completableFuture2 = CompletableFuture.supplyAsync(() -> {
            try {
                System.out.println("子线程开始任务");
                Thread.sleep(5000);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return "子线程完成了!";
        });
        //主线程阻塞
        completableFuture2.whenComplete((t, u) -> {
            System.out.println("------t=" + t);  //t为返回值
            System.out.println("------u=" + u);  //u为异常
        }).get();


        //当一个线程依赖另一个线程时，可以使用 thenApply 方法来把这两个线程串行化。
        AtomicReference<AtomicInteger> num = new AtomicReference<>(new AtomicInteger());
        CompletableFuture<Integer> completableFuture3 = CompletableFuture.supplyAsync(() -> {
            try {
                System.out.println("加 10 任务开始");
                num.get().addAndGet(10);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return num;
        }).thenApply(integer -> {
            return num.get().get() * num.get().get();
        });

        //thenAccept 消费处理结果, 接收任务的处理结果，并消费处理，无返回结果。
        CompletableFuture.supplyAsync(() -> {
            try {
                System.out.println("加 10 任务开始");
                num.updateAndGet(v -> v + 10);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return num;
        }).thenApply(integer -> {
            return num.get() * num.get();
        }).thenAccept(new Consumer<Integer>() {
            @Override
            public void accept(Integer integer) {
                System.out.println("子线程全部处理完成,最后调用了 accept,结果为:" + integer);
            }
        });


        //异常处理,出现异常时触发
        CompletableFuture<Integer> completableFuture4 = CompletableFuture.supplyAsync(() -> {
            int i = 1 / 0;
            System.out.println("加 10 任务开始");
            num += 10;
            return num;
        }).exceptionally(ex -> {
            System.out.println(ex.getMessage());
            return -1;
        });


        //handle 类似于 thenAccept/thenRun 方法,是最后一步的处理调用,但是同时可以处理异常
        CompletableFuture<Integer> completableFuture5 = CompletableFuture.supplyAsync(() -> {
            System.out.println("加 10 任务开始");
            num += 10;
            return num;
        }).handle((i, ex) -> {
            System.out.println("进入 handle 方法");
            if (ex != null) {
                System.out.println("发生了异常,内容为:" + ex.getMessage());
                return -1;
            } else {
                System.out.println("正常完成,内容为: " + i);
                return i;
            }
        });


        //thenCompose 合并两个有依赖关系的 CompletableFutures 的执行结果
        //第一步加 10
        CompletableFuture<Integer> completableFuture6 = CompletableFuture.supplyAsync(() -> {
            System.out.println("加 10 任务开始");
            num += 10;
            return num;
        });
//合并
        CompletableFuture<Integer> completableFuture7 = future.thenCompose(i ->
                //再来一个
                CompletableFuture CompletableFuture.supplyAsync(() -> {
            return i + 1;
        }));


        //thenCombine 合并两个没有依赖关系的 CompletableFutures 任务
        CompletableFuture<Integer> job1 = CompletableFuture.supplyAsync(() -> {
            System.out.println("加 10 任务开始");
            num += 10;
            return num;
        });
        CompletableFuture<Integer> job2 = CompletableFuture.supplyAsync(() -> {
            System.out.println("乘以 10 任务开始");
            num = num * 10;
            return num;
        });
//合并两个结果
        CompletableFuture<Object> future05 = job1.thenCombine(job2, new BiFunction<Integer, Integer, List<Integer>>() {
            @Override
            public List<Integer> apply(Integer a, Integer b) {
                List<Integer> list = new ArrayList<>();
                list.add(a);
                list.add(b);
                return list;
            }
        });


        //allOf: 一系列独立的 future 任务，等其所有的任务执行完后做一些事情
        List<CompletableFuture> list = new ArrayList<>();
        CompletableFuture<Integer> job1 = CompletableFuture.supplyAsync(() -> {
            System.out.println("加 10 任务开始");
            num += 10;
            return num;
        });
        list.add(job1);
        CompletableFuture<Integer> job2 = CompletableFuture.supplyAsync(() -> {
            System.out.println("乘以 10 任务开始");
            num = num * 10;
            return num;
        });
        list.add(job2);
        CompletableFuture<Integer> job3 = CompletableFuture.supplyAsync(() -> {
            System.out.println("减以 10 任务开始");
            num = num * 10;
            return num;
        });
        list.add(job3);
        CompletableFuture<Integer> job4 = CompletableFuture.supplyAsync(() -> {
            System.out.println("除以 10 任务开始");
            num = num * 10;
            return num;
        });
        list.add(job4); //多任务合并 List<Integer> collect =
        list.stream().map(CompletableFuture<Integer>::join).collect(Collectors.toList());
        System.out.println(collect);



        //anyOf: 只要在多个 future 里面有一个返回，整个任务就可以结束，而不需要等到每一个 future 结束
        CompletableFuture<Integer>[] futures = new CompletableFuture[4]; CompletableFuture<Integer> job1 = CompletableFuture.supplyAsync(() -> {
            try{
                Thread.sleep(5000); System.out.println("加 10 任务开始");
                num += 10;
                return num; }catch (Exception e){
                return 0; }
        });
        futures[0] = job1;
        CompletableFuture<Integer> job2 = CompletableFuture.supplyAsync(() -> {
            try{
                Thread.sleep(2000); System.out.println("乘以 10 任务开始"); num = num * 10;
                return num;
            }catch (Exception e){ return 1;
            } });
        futures[1] = job2;
        CompletableFuture<Integer> job3 = CompletableFuture.supplyAsync(() -> { try{
            Thread.sleep(3000); System.out.println("减以 10 任务开始"); num = num * 10;
            return num;
        }catch (Exception e){ return 2;
        } });
        futures[2] = job3;
        CompletableFuture<Integer> job4 = CompletableFuture.supplyAsync(() -> { try{
            Thread.sleep(4000); System.out.println("除以 10 任务开始");
            num = num * 10;
            return num; }catch (Exception e){
            return 3; }
        });
        futures[3] = job4;
        CompletableFuture<Object> future = CompletableFuture.anyOf(futures); System.out.println(future.get());
    }

}
}

