package com.light.privateMovies.web.pojo;

import java.util.ArrayList;
import java.util.List;

public class MovieData {
    private String picPath;
    private String filePath;
    private String srt;
    private List<ActorData> actor;
    private String name;
    private TypeData typeData;
    private List<String> subs = new ArrayList<>();
    private List<String> parts;
    public MovieData(String picPath, String filePath, String srt, List<ActorData> actor, String name, TypeData typeData) {
        this.picPath = picPath;
        this.filePath = filePath;
        this.srt = srt;
        this.actor = actor;
        this.name = name;
        this.typeData = typeData;
    }

    public List<String> getParts() {
        return parts;
    }

    public void setParts(List<String> parts) {
        this.parts = parts;
    }

    public List<String> getSubs() {
        return subs;
    }

    public void setSubs(List<String> subs) {
        this.subs = subs;
    }

    public String getPicPath() {
        return picPath;
    }

    public TypeData getTypeData() {
        return typeData;
    }

    public void setTypeData(TypeData typeData) {
        this.typeData = typeData;
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
