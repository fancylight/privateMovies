package com.light.privateMovies.reptile.core.newCore;

import java.util.Map;

public class ConnectionTarget {
    private Map<String, String> cookies;
    private Map<String, String> headers;
    private String url;
    private TaskType taskType;
    private int reConnectionTimes = 5;
    private int deep;
    private boolean isPoison;

    public ConnectionTarget(int deep) {
        this.deep = deep;
    }

    public void setReConnectionTimes(int reConnectionTimes) {
        this.reConnectionTimes = reConnectionTimes;
    }

    public int getDeep() {
        return deep;
    }

    public void setDeep(int deep) {
        this.deep = deep;
    }

    public Map<String, String> getCookies() {
        return cookies;
    }

    public void setCookies(Map<String, String> cookies) {
        this.cookies = cookies;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public TaskType getTaskType() {
        return taskType;
    }

    public void setTaskType(TaskType taskType) {
        this.taskType = taskType;
    }

    public int getReConnectionTimes() {
        return reConnectionTimes--;
    }

    public boolean isPoison() {
        return isPoison;
    }

    public void setPoison(boolean poison) {
        isPoison = poison;
    }
}
