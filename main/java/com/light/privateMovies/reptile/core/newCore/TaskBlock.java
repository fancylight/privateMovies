package com.light.privateMovies.reptile.core.newCore;

import com.light.privateMovies.reptile.core.newCore.data.AbstractDataResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.BlockingQueue;

public class TaskBlock implements Runnable {
    //连接线程消耗,由任务处理线程创建
    private BlockingQueue<ConnectionTarget> connectionTargets;
    //任务目标,由连接线程创建
    private BlockingQueue<TaskTarget> taskTargets;
    private boolean isTer;
    private AbstractDataResult abstarctDataResult;
    private FrameReptile frameReptile;
    private Logger log = LogManager.getLogger(TaskBlock.class);

    public TaskBlock(BlockingQueue<ConnectionTarget> connectionTargets, BlockingQueue<TaskTarget> taskTargets, FrameReptile frameReptile, AbstractDataResult abstarctDataResult) {
        this.connectionTargets = connectionTargets;
        this.taskTargets = taskTargets;
        this.frameReptile = frameReptile;
        this.abstarctDataResult = abstarctDataResult;
    }

    @Override
    public void run() {
        try {
            while (!isTer) {
                var task = taskTargets.take();
                if (task.isPoison()) {
                    log.warn("毒药退出线程" + Thread.currentThread().getName());
                    break;
                }
                var re = task.getResponse();
                //根据url以及类型进行分配处理
                abstarctDataResult.TaskDo(task.getDeep(), re);
            }
        } catch (InterruptedException e) {
            log.warn(Thread.currentThread().getName() + "中断");
        }

    }
}
