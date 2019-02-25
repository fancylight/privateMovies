package com.light.privateMovies.reptile;

import com.light.privateMovies.dao.ActorDao;
import com.light.privateMovies.dao.MovieDao;
import com.light.privateMovies.dao.MovieDetailDao;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.*;

@Service
public class Reptile implements ReptileDataInterface {

    private String initUrl = "";
    private Proxy proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("127.0.0.1", 1080));
    private List<StepMethod> methods = new ArrayList();
    private String host;
    private Map<String, String> header = new HashMap<>();
    private Map<String, String> cookies = new HashMap<>();
    private String pro;
    private int timeOutRe = 10;//超时重调次数
    private int nowRe = timeOutRe;
    private int wait = 500;//每次请求休眠时间,默认500ms

    //设置失败重调次数
    public void setTimeOutRe(int timeOutRe) {
        this.timeOutRe = timeOutRe;
        nowRe = timeOutRe;
    }

    public void setWait(int wait) {
        this.wait = wait;
    }

    Logger logger = LogManager.getLogger(Reptile.class);

    public Reptile(String initUrl) {
        this.initUrl = initUrl;
    }

    public String getUrl(StepMethod stepMethod) {
        String path = "";

        if (stepMethod.getPath().startsWith("//"))
            path = stepMethod.getPath().replace("//", "");
        else if (stepMethod.getPath().startsWith("/"))
            path = stepMethod.getHost() + stepMethod.getPath();
        return stepMethod.getPro() + "://" + path;
    }

    public String getHost() {
        return host;
    }

    public String getPro() {
        return pro;
    }

    public Reptile(List<StepMethod> methods) {
        this.methods = methods;
    }

    public void setHeader(Map<String, String> header) {
        this.header = header;
    }

    public void setCookies(Map<String, String> cookies) {
        this.cookies = cookies;
    }

    public void setHeader(String key, String vlaue) {
        header.put(key, vlaue);
    }

    public void setCookie(String key, String value) {
        cookies.put(key, value);
    }

    @Override
    public void init() {
        StepMethod m0 = methods.get(0);
        host = m0.getHost();
        header = m0.getHeader();
        cookies = m0.getCookies();
        this.pro = m0.getPro();
        this.host = m0.getHost();
        getRe(getUrl(m0), 0);
    }

    //TODO:修改跑虫架构  修改为while 队列模式,否则当递归过多还导致栈溢出
    public void getRe(String url, int deep) {
        Connection connection = Jsoup.connect(url).proxy(proxy);
        connection.headers(methods.get(deep).getHeader());
        connection.cookies(methods.get(deep).getCookies());
        try {

            var res = connection.method(Connection.Method.GET).ignoreContentType(true).ignoreHttpErrors(true).execute();
            nowRe = timeOutRe;
            methods.get(deep).beforeDoc(res, this);
            int nowDeep = deep;
            if (dealWithCode(res.statusCode(), url)) { //正常请求
                methods.get(deep).deal(res, methods, deep, this);
                nowDeep = methods.get(deep).afterDoc(res, this, deep, methods);
            } else {
                //跳过此次处理
                nowDeep++;
            }
            if (nowDeep >= methods.size()) {
                //TODO 结束函数
                methods.get(0).end();
            } else {
                getRe(getUrl(methods.get(nowDeep)), nowDeep);

            }
        } catch (IOException e) {
            //FIXME:此处一一个bug如果出现了socket超时,应该重新进行调用
            e.printStackTrace();
            logger.warn("发生socket异常" + e.getMessage());
            if (nowRe-- > 0) {
                logger.warn("进行重调");
                getRe(url, deep);//重调
            } else {
                logger.warn("重调超过次数总请求退出");
                return;
            }
        }
    }

    private boolean dealWithCode(int statusCode, String url) {
        if (statusCode == 404) {
            logger.warn("请求目标404,跳过此次请求" + url);
            return false;
        }
        return true;
    }

    public void addRule(StepMethod stepMethod) {
        methods.add(stepMethod);
    }
}
