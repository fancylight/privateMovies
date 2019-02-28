package com.light.privateMovies.web.pojo;

public class ActorData {
    private String picPath;
    private String name;

    public ActorData(String picPath, String name) {
        this.picPath = picPath;
        this.name = name;
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
}
