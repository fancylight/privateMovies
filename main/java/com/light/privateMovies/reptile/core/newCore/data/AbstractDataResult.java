package com.light.privateMovies.reptile.core.newCore.data;

import com.light.privateMovies.reptile.core.newCore.ConnectionTarget;
import com.light.privateMovies.reptile.core.newCore.TaskTarget;
import org.jsoup.Connection;

import java.util.List;
import java.util.concurrent.BlockingQueue;

/**
 * 调用者还是要通过实现该类来使用
 *
 * @param <E>
 */
public abstract class AbstractDataResult<E> implements DataResult<E> {
    protected List<StepTask> stepTasks;
    protected E data;
    //连接线程消耗,由任务处理线程创建
    private BlockingQueue<ConnectionTarget> connectionTargets;
    //任务目标,由连接线程创建
    private BlockingQueue<TaskTarget> taskTargets;
    private int conCount;
    private int taskCount;
    private boolean hasStop;


    @Override
    public void TaskDo(int deep, Connection.Response response) {
        StepTask task = null;
        if ((task = stepTasks.get(deep)) != null)
            task.deal(response);
    }

    @Override
    public E getData() {
        return data;
    }
    //通过毒药停止消费者-生产者线程
    protected synchronized void stop() {
        if (!hasStop) {
            for (int index = 0; index < conCount; index++) {
                var conPoison = new ConnectionTarget();
                conPoison.setPoison(true);
                connectionTargets.add(conPoison);
            }
            for (int index = 0; index < taskCount; index++) {
                var taskPoison = new TaskTarget();
                taskPoison.setPoison(true);
                taskTargets.add(taskPoison);
            }
        }
        hasStop = true;
    }

    public int getConCount() {
        return conCount;
    }

    public void setConCount(int conCount) {
        this.conCount = conCount;
    }

    public int getTaskCount() {
        return taskCount;
    }

    public void setTaskCount(int taskCount) {
        this.taskCount = taskCount;
    }

    public void setConnectionTargets(BlockingQueue<ConnectionTarget> connectionTargets) {
        this.connectionTargets = connectionTargets;
    }

    public void setTaskTargets(BlockingQueue<TaskTarget> taskTargets) {
        this.taskTargets = taskTargets;
    }
}
