package com.light.privateMovies.reptile.core;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Connection;

import java.util.List;
import java.util.Map;

public abstract class StepMethod {
    private String path;
    private String host;
    private String pro;
    private Map<String, String> header;
    private Map<String, String> cookies;
    private static Logger logger = LogManager.getLogger(StepMethod.class);
    private boolean ending; //表示是否应该执行

    public boolean isEnding() {
        return ending;
    }

    public void setEnding(boolean ending) {
        this.ending = ending;
    }

    public Map<String, String> getCookies() {
        return cookies;
    }

    public void setCookies(Map<String, String> cookies) {
        this.cookies = cookies;
    }

    public StepMethod(String path, String host, String pro, Map<String, String> header, Map<String, String> cookies) {
        this.path = path;
        this.host = host;
        this.pro = pro;
        this.header = header;
        this.cookies = cookies;
    }

    public StepMethod(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPro() {
        return pro;
    }

    public void setPro(String pro) {
        this.pro = pro;
    }

    public Map<String, String> getHeader() {
        return header;
    }

    public void setHeader(Map<String, String> header) {
        this.header = header;
    }

    public void setHeader(String key, String value) {
        header.put(key, value);
    }

    public void setCooke(String key, String value) {
        cookies.put(key, value);
    }

    public void beforeDoc(Connection.Response res, Reptile reptile) {
        logger.info("访问" + res.url().toString());
//        System.out.println("本次信息头");
//        System.out.println("cook");
//        res.cookies().entrySet().stream().forEach(t -> System.out.println(t.getKey() + ":" + t.getValue()));
//        res.headers().entrySet().stream().forEach(t -> System.out.println(t.getKey() + ":" + t.getValue()));
    }

    public int afterDoc(Connection.Response res, Reptile reptile, int deep, List<StepMethod> methods) {
        logger.info("结束" + res.url().toString());
        return ++deep;
    }

    /**
     * 该部分由子类实现
     *
     * @param response
     * @param methods
     * @param deep
     * @param reptile
     */
    public void deal(Connection.Response response, List<StepMethod> methods, int deep, Reptile reptile) {
        logger.info("解析" + response.url().toString());
    }

    //TODO:修改该函数的调用时机,使其符合end的意义,即当reptile框架处理完所有请求,即deep=size()时,依次调用end()

    /**
     * 如果返回-1代表终止整个过程
     * @return
     */
    public int end() {
        return 0;
    }

    public boolean getEnd() {
        return isEnding();
    }
}
