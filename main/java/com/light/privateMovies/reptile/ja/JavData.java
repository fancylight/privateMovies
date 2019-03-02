package com.light.privateMovies.reptile.ja;

import com.light.privateMovies.pojo.Actor;
import com.light.privateMovies.pojo.Movie;
import com.light.privateMovies.pojo.MovieDetail;
import com.light.privateMovies.pojo.MovieType;
import com.light.privateMovies.reptile.annotation.Step;
import com.light.privateMovies.reptile.core.Reptile;
import com.light.privateMovies.reptile.core.ReptileUtil;
import com.light.privateMovies.reptile.core.StepMethod;
import com.light.privateMovies.reptile.steps.PicDown;
import com.light.privateMovies.util.FileUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Connection;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 目前来看仅仅打算获取类别这一项
 */
@Step(url = "https://www.javbus.com/")
public class JavData {
    private String code;
    private List<MovieType> types = new ArrayList<>();
    private Logger logger = LogManager.getLogger(JavData.class);
    private Result result;
    private Map<String, String> other = new HashMap<>();
    // x:/xx/acotrs/code-title/
    private String targetPath;
    private String targetName;
    //获取数据的容器
    private byte[] cover;
    private HashMap<String, byte[]> actor = new HashMap();
    private HashMap<String, byte[]> detail = new HashMap();

    public void setCode(String code) {
        this.code = code;
    }

    //-----------------------------type信息--------------------------------
    class JavStep extends StepMethod {
        public JavStep(String path, String host, String pro, Map<String, String> header, Map<String, String> cookies) {
            super(path, host, pro, header, cookies);
        }

        public JavStep(String path) {
            super(path);
        }

        @Override
        public void deal(Connection.Response response, List<StepMethod> methods, int deep, Reptile reptile) {
            super.deal(response, methods, deep, reptile);
            try {
                var doc = response.parse();
                var title = doc.title();
                if (title.equals("404 Page Not Found! - JavBus")) {
                    logger.warn("java页面不存在" + code);
                }
                var ps = doc.select(".header");
                String typeArr[] = null;
                for (var ele : ps) {
                    if (ele.text().equals("類別:"))
                        typeArr = ele.nextElementSibling().text().split(" ");
                }
                for (String type : typeArr)
                    types.add(new MovieType(type));

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public int afterDoc(Connection.Response res, Reptile reptile, int deep, List<StepMethod> methods) {
            setEnding(true);
            return super.afterDoc(res, reptile, deep, methods);

        }

        @Override
        public int end() {
           return -1;
        }
    }

    //-----------------------------组装result信息--------------------------------
    class JavDatas extends StepMethod {

        public JavDatas(String path, String host, String pro, Map<String, String> header, Map<String, String> cookies) {
            super(path, host, pro, header, cookies);
        }

        public JavDatas(String path) {
            super(path);
        }

        @Override
        public void deal(Connection.Response response, List<StepMethod> methods, int deep, Reptile reptile) {
            super.deal(response, methods, deep, reptile);
            try {
                var doc = response.parse();
                var title = doc.title();
                if (title.equals("404 Page Not Found! - JavBus")) {
                    logger.warn("java页面不存在" + code);
                    return;
                }
                //1.信息
                var headers = doc.select(".header");
                for (var element : headers) {
//                    if(element.text().equals("識別碼")){
//                        String code=ReptileUtil.getACode(element.text());
//                        other.put("code",code);
//                    }else if
                    String key = element.text();
                    switch (key) {
                        case "識別碼:": {
                            String code = ReptileUtil.getACode(element.nextElementSibling().text());
                            other.put("code", code);
                            break;
                        }
                        case "發行日期:": {
                            String t = element.parent().textNodes().get(0).text().replaceAll(" ", "");
                            other.put("releaseTime", LocalDate.parse(t, DateTimeFormatter.ofPattern("yyyy-MM-dd")).toString());
                            break;
                        }
                        case "長度:": {
                            String t = element.parent().textNodes().get(0).text();
                            other.put("length", ReptileUtil.getLengthTime(t));
                            break;
                        }
                        case "類別:": {
                            break;
                        }
                        case "演員": {
                            var targetP = element.parent().nextElementSibling().nextElementSibling();
                            Set<String> sets = new HashSet<>();
                            if(targetP!=null){
                                var actors = targetP.select(".genre a");
                                for (var el : actors) {
                                    String path = new URL(el.attr("href")).getPath();
                                    var p = new ActorsStep(path, this.getHost(), this.getPro(), this.getHeader(), this.getCookies());
                                    methods.add(p);
                                    sets.add(el.text());
                                }
                            }

                            //文件夹
                            String actorsDir = ReptileUtil.createActorDir(sets);
                            addTargetPath(actorsDir);

                            break;
                        }
                    }
                }
                //2.封面
                var coverImg = doc.select(".bigImage img");
                if (coverImg != null) {
                    String coverLink = coverImg.attr("src");
                    String titleInfo = coverImg.attr("title");
                    var url = new URL(coverLink);
                    String path = url.getPath();
                    String host = url.getHost();
                    String pro = url.getProtocol();
                    String type = ReptileUtil.getType(path);
                    //标题
                    other.put("title", titleInfo);
                    addTargetPath("/" + ReptileUtil.createTitleCodeDir(ReptileUtil.getACode(code), titleInfo));
                    ReptileUtil.createDir(targetPath);
                    ReptileUtil.createDir(targetPath + "/" + ConstantPath.ACTOR);
                    ReptileUtil.createDir(targetPath + "/" + ConstantPath.DETAIL);
                    ReptileUtil.createDir(targetPath + "/" + ConstantPath.COVER);
                    var picDown = new PicDown(path, host, pro, this.getHeader(), this.getCookies()) {
                        @Override
                        protected void beforeDown(Connection.Response response, List<StepMethod> methods, int deep, Reptile reptile) {
                            this.setBuf(FileUtil.getInBytes(response.bodyStream()));
                            cover=this.getBuf();
                            addDownLoadPath(ConstantPath.COVER);
                        }
                    };

                    picDown.setDownLoadPath(targetPath);
                    picDown.setName(ReptileUtil.getACode(code));
                    picDown.setType(type);
                    methods.add(picDown);
                }
                //3.详情页
                var des = doc.select("#sample-waterfall .sample-box");
                for (var ele : des) {
                    String link = ele.attr("href");
                    URL url = new URL(link);
                    String path = url.getPath();
                    String host = url.getHost();
                    String pro = url.getProtocol();
                    String type = ReptileUtil.getType(path);
                    String name = ReptileUtil.pathToName(path);
                    var picDown = new PicDown(path, host, pro, this.getHeader(), this.getCookies()) {
                        @Override
                        protected void beforeDown(Connection.Response response, List<StepMethod> methods, int deep, Reptile reptile) {
                            this.setBuf(FileUtil.getInBytes(response.bodyStream()));
                            detail.put(name, this.getBuf());
                            addDownLoadPath(ConstantPath.DETAIL);
                        }
                    };
                    picDown.setDownLoadPath(targetPath);
                    picDown.setName(name);
                    picDown.setType(type);
                    methods.add(picDown);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public int end() {
           result= ArzonData.Assemble(targetName,targetPath+"/detail/",other,actor,cover,detail);
            return  -1;
        }
    }

    //----------------------------------actor信息------------------------------------
    class ActorsStep extends StepMethod {

        public ActorsStep(String path, String host, String pro, Map<String, String> header, Map<String, String> cookies) {
            super(path, host, pro, header, cookies);
        }

        public ActorsStep(String path) {
            super(path);
        }

        @Override
        public void deal(Connection.Response response, List<StepMethod> methods, int deep, Reptile reptile) {
            super.deal(response, methods, deep, reptile);
            try {
                var doc = response.parse();
                var div = doc.select(".photo-frame").first();
                if (div != null) {
                    var img = div.select("img");
                    var url = new URL(img.attr("src"));
                    var name = img.attr("title");
                    String path =  url.getPath();
                    String host = url.getHost();
                    String pro = url.getProtocol();
                    var picDown = new PicDown(path, host, pro, this.getHeader(), this.getCookies()) {
                        @Override
                        protected void beforeDown(Connection.Response response, List<StepMethod> methods, int deep, Reptile reptile) {
                            this.setBuf(response.bodyAsBytes());
                            actor.put(name,this.getBuf());
                            addDownLoadPath(ConstantPath.ACTOR);
                        }
                    };
                    String type = ReptileUtil.getType(path);
                    picDown.setDownLoadPath(targetPath);
                    picDown.setName(name);
                    picDown.setType(type);
                    methods.add(picDown);
                } else {
                    logger.warn("不存在" + response.url());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String getCode() {
        return code;
    }

    public List<MovieType> getTypes() {
        return types;
    }

    public void setTypes(List<MovieType> types) {
        this.types = types;
    }

    public Logger getLogger() {
        return logger;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    public void setResult(Result result) {
        this.result = result;
    }

    public Map<String, String> getOther() {
        return other;
    }

    public void setOther(Map<String, String> other) {
        this.other = other;
    }

    public String getTargetPath() {
        return targetPath;
    }

    public void setTargetPath(String targetPath) {
        this.targetPath = targetPath;
    }

    public String getTargetName() {
        return targetName;
    }

    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }

    public void addTargetPath(String part) {
        this.targetPath += part;
    }

    //该网站属于rest风格, 例如:https://www.javbus.com/CESD-721 就是目标页面,此时有个问题要给code中间加一个-
    public JavData(String code) {
        //
        var m = Pattern.compile("^[a-z|A-Z]{3,5}").matcher(code);
        String codePart = "";
        if (m.find())
            codePart = m.group();
        var m2 = Pattern.compile("[0-9]{3,4}$").matcher(code);
        String number = "";
        if (m2.find())
            number = m2.group();
        this.code = codePart + "-" + number;
    }

    public JavData(String code, String targetPath) {
        this(code);
        this.targetName = targetPath;
        targetName = ReptileUtil.dealDouble(targetName);
        this.targetPath = targetName.substring(0, targetName.lastIndexOf("/") + 1);
    }

    /**
     * 获取type信息
     *
     * @return
     */
    public List<MovieType> getType() {
        var step = new JavStep("/" + code, "www.javbus.com", "https", ReptileUtil.getBrowerParas(), new HashMap<>());
        new Reptile(Collections.singletonList(step)).init();
        return types;
    }

    /**
     * 获取result
     *
     * @return
     */
    public Result getResult() {
        var step = new JavDatas("/" + code, "www.javbus.com", "https", ReptileUtil.getBrowerParas(), new HashMap<>());
        new Reptile(Stream.of(step).collect(Collectors.toList())).init();
        return result;
    }
}
