package com.light.privateMovies.web.pojo;

/**
 * 匹配 movies.html页面
 */
public class MoviesData {
    private String picPath;
    private String name;
    private String filePath;
    private long time;
    public MoviesData(String picPath, String name, String filePath,long time) {
        this.picPath = picPath;
        this.name = name;
        this.filePath = filePath;
        this.time=time;
    }

    public String getPicPath() {
        return picPath;
    }

    public void setPicPath(String picPath) {
        this.picPath = picPath;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFileath(String filePath) {
        this.filePath = filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
