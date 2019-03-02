package com.light.privateMovies.reptile.steps;

import com.light.privateMovies.reptile.core.Reptile;
import com.light.privateMovies.reptile.core.StepMethod;
import com.light.privateMovies.util.FileUtil;
import org.jsoup.Connection;

import java.util.List;
import java.util.Map;

public class PicDown extends StepMethod {
    public PicDown(String path, String host, String pro, Map<String, String> header, Map<String, String> cookies) {
        super(path, host, pro, header, cookies);
    }

    public PicDown(String path) {
        super(path);
    }

    private String downLoadPath; //图片下载的位置
    private String name;
    private String type;  //如actor ,cover,detail
    private byte[] buf;

    public String getDownLoadPath() {
        return downLoadPath;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setDownLoadPath(String downLoadPath) {
        this.downLoadPath = downLoadPath;
    }

    public void addDownLoadPath(String part) {
        this.downLoadPath += "/" + part;
    }

    public byte[] getBuf() {
        return buf;
    }

    public void setBuf(byte[] buf) {
        this.buf = buf;
    }

    @Override
    public void deal(Connection.Response response, List<StepMethod> methods, int deep, Reptile reptile) {
        super.deal(response, methods, deep, reptile);
        beforeDown(response, methods, deep, reptile);
        addDownLoadPath(this.getName() + "." + this.getType());
        if (buf == null)
            FileUtil.wirteToDest(FileUtil.getInBytes(response.bodyStream()), downLoadPath);
        else
            FileUtil.wirteToDest(buf, downLoadPath);
        afterDown(response, methods, deep, reptile);
    }

    protected void beforeDown(Connection.Response response, List<StepMethod> methods, int deep, Reptile reptile) {

    }

    protected void afterDown(Connection.Response response, List<StepMethod> methods, int deep, Reptile reptile) {

    }

}
