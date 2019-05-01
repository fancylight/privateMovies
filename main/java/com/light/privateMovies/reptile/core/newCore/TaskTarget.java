package com.light.privateMovies.reptile.core.newCore;

import org.jsoup.Connection;

public class TaskTarget {
    private Connection.Response response;
    private TaskType taskType;//类型标记
    private String url;//url标记
    private int deep;//表示该任务的深度
    private boolean isPoison;//表示毒药
    public TaskTarget(Connection.Response response, TaskType taskType, String url) {
        this.response = response;
        this.taskType = taskType;
        this.url = url;
    }

    public TaskTarget(Connection.Response response, TaskType taskType, String url, int deep) {
        this.response = response;
        this.taskType = taskType;
        this.url = url;
        this.deep = deep;
    }

    public TaskTarget() {
    }

    public TaskType getTaskType() {
        return taskType;
    }

    public String getUrl() {
        return url;
    }

    public Connection.Response getResponse() {
        return response;
    }

    public int getDeep() {
        return deep;
    }

    public boolean isPoison() {
        return isPoison;
    }

    public void setPoison(boolean poison) {
        isPoison = poison;
    }
}
