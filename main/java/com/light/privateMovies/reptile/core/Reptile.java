package com.light.privateMovies.reptile.core;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.*;

/**
 * 1.该架构有很多改进的地方
 * 2.由于jdk本身的问题,总是采用本地dns解析,因此即使通过sock5代理,dns污染还是存在,因此解决方式:
 * https://www.ultratools.com/tools/ipWhoisLookupResult
 * 通过外网查询ip,保存在hosts文件中
 * 3.有趣的问题是如果该网站使用了cloudflare:
 * cloudflare工作原理
 * <p>
 * <p>
 * client  <--------->    cloudflare  <-------------> server
 * a.首先我们访问的url被解析成的ip实际是cloudflare,而cloudflare则知道真实server的ip,当然了cloudflare也许还有一些目标服务器的缓冲,
 * 以减少服务器的压力;
 * b.举个例子
 * 104.24.20.64 www.javbus.com 是我查到的ip,这个ip的确对应的是www.javbus.com,但是如果直接通过浏览器访问104.24.20.64,则会导致
 * Error 1003,原因在于此时请求头host:104.24.20.64,远端的cloudflare会拒绝请求.
 * 修改方式就是将host:www.javbus.com
 * c.使用jsoup直接访问 www.javbus.com 并且提前修改hosts文件就能正确访问
 */
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
    Logger logger = LogManager.getLogger(Reptile.class);
    //设置失败重调次数
    public void setTimeOutRe(int timeOutRe) {
        this.timeOutRe = timeOutRe;
        nowRe = timeOutRe;
    }

    public void setWait(int wait) {
        this.wait = wait;
    }


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

    /**
     * 关于实现多线程和阻塞队列的方式
     * 1.此类属于消费者和生产者,即从url队列获取连接信息,进行连接,并且创建doc队列
     * 2.methodStep的子类也就属于消费者和生产者,从doc拿取数据进行分析,并且新增连接队列
     * 3.如何停止消费死循环
     * @param url
     * @param deep
     */
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
                //增加一个可以提前跳出的途径
                int x = 0;
                if (methods.get(deep).getEnd())
                    x = methods.get(deep).end();
                if (x == -1)
                    return;
            } else {
                //跳过此次处理
                nowDeep++;
            }

            if (nowDeep >= methods.size()) {
                methods.get(0).end();
            } else {
                getRe(getUrl(methods.get(nowDeep)), nowDeep);

            }
        } catch (IOException e) {
            //FIXME:此处一一个bug如果出现了socket超时,应该重新进行调用
            e.printStackTrace();
            logger.warn("发生socket异常" + e.getMessage());
            if (nowRe-- > 0) {
                logger.warn("进行重调" + url + "次数:" + nowRe);
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
