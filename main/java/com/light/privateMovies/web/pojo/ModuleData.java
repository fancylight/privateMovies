package com.light.privateMovies.web.pojo;

/**
 * 匹配首页
 */
public class ModuleData {
    private String picPath;
    private String name;
    private String dataPath;

    public ModuleData(String picPath, String name, String dataPath) {
        this.picPath = picPath;
        this.name = name;
        this.dataPath = dataPath;
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

    public String getDataPath() {
        return dataPath;
    }

    public void setDataPath(String dataPath) {
        this.dataPath = dataPath;
    }
}
