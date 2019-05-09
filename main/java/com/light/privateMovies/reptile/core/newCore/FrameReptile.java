package com.light.privateMovies.reptile.core.newCore;

import com.light.privateMovies.reptile.core.newCore.data.AbstractDataResult;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class FrameReptile {
    private AbstractDataResult abstractDataResult;
    private ExecutorService ConExecutor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    private ExecutorService TaskExecutor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    private boolean hasShutDown = false;

    public FrameReptile(AbstractDataResult abstractDataResult) {
        this.abstractDataResult = abstractDataResult;
        abstractDataResult.setConCount(Runtime.getRuntime().availableProcessors());
        abstractDataResult.setTaskCount(Runtime.getRuntime().availableProcessors());

    }

    /**
     * 根据初始目标进行处理
     * 最终的数据通过传递过来的AbstractDataResult,用户自行获取
     */
    public void start() {
        ConnectionTarget target=abstractDataResult.target;
        int count = Runtime.getRuntime().availableProcessors();
        List<Future> list = new ArrayList<>();
        List<Future> list2 = new ArrayList<>();
        BlockingQueue<ConnectionTarget> connectionTargets = new LinkedBlockingQueue();
        connectionTargets.add(target);
        BlockingQueue<TaskTarget> taskTargets = new LinkedBlockingQueue<>();
        //设置毒药位置
        abstractDataResult.setConnectionTargets(connectionTargets);
        abstractDataResult.setTaskTargets(taskTargets);
        for (int index = 0; index < count; index++) {
            //连接线程
            var con = new ConnectionBlock(connectionTargets, taskTargets);
            list.add(ConExecutor.submit(con));
            //任务线程
            list2.add(TaskExecutor.submit(new TaskBlock(connectionTargets, taskTargets, this,abstractDataResult)));
        }
        //等待任务结束
        for (Future future : list2) {
            try {
                future.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized void stop() {
        if (!hasShutDown) {
            //关闭线程池
            ConExecutor.shutdownNow();
            TaskExecutor.shutdownNow();
        }
    }
}
