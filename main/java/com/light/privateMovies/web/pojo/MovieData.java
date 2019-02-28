package com.light.privateMovies.web.pojo;

import java.util.List;

public class MovieData {
    private String picPath;
    private String filePath;
    private String srt;
    private List<ActorData> actor;
    private String name;

    public MovieData(String picPath, String filePath, String srt, List<ActorData> actor, String name) {
        this.picPath = picPath;
        this.filePath = filePath;
        this.srt = srt;
        this.actor = actor;
        this.name = name;
    }

    public String getPicPath() {
        return picPath;
    }

    public void setPicPath(String picPath) {
        this.picPath = picPath;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getSrt() {
        return srt;
    }

    public void setSrt(String srt) {
        this.srt = srt;
    }

    public List<ActorData> getActor() {
        return actor;
    }

    public void setActor(List<ActorData> actor) {
        this.actor = actor;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
