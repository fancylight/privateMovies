package com.light.privateMovies.reptile;


import com.light.privateMovies.module.TypeDeal;
import com.light.privateMovies.pojo.Actor;
import com.light.privateMovies.pojo.Movie;
import com.light.privateMovies.pojo.MovieDetail;
import com.light.privateMovies.reptile.annotation.Step;
import com.light.privateMovies.util.FileUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Connection;
import org.jsoup.nodes.Element;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ArzonData {

    private static Logger logger = LogManager.getLogger(ArzonData.class);
    private String localTargetPath; //表示目标应该存放的位置
    private String localTargetName; //表示目标初始位置
    //
    private Map<String, String> other = new HashMap();
    private Map<String, String> actorLink = new HashMap();

    private Map<String, byte[]> detailData = new HashMap<>();
    private byte[] coverData;
    private Map<String, byte[]> actorData = new HashMap<>();
    //TODO:此处这个可以抽象出来   ArzonData这样的结构抽象成泛型base类,泛型为最终数据
    private Result result; //表示返回数据

    public ArzonData(String localTargetName) {
        this.localTargetName = localTargetName;
        localTargetName = ReptileUtil.dealDouble(localTargetName);
        this.localTargetPath = localTargetName.substring(0, localTargetName.lastIndexOf("/") + 1);
    }

    public String getLocalTargetPath() {
        return localTargetPath;
    }

    //不处理直接添加
    public void addNewPath(String path) {
        this.localTargetPath += path;
    }

    public void setLocalTargetPath(String localTargetPath) {
        this.localTargetPath = localTargetPath;
    }

    public String getLocalTargetName() {
        return localTargetName;
    }

    public void setLocalTargetName(String localTargetName) {
        this.localTargetName = localTargetName;
    }

    /**
     * 设置信息
     *
     * @param res
     * @param last
     * @param now
     */
    public void getInfoByProMethod(Connection.Response res, StepMethod last, StepMethod now) {
        String referer = res.url().toString();
        now.setHeader(last.getHeader());
        now.setCookies(last.getCookies());
        now.setHost(last.getHost());
        now.setPro(last.getPro());
        now.setHeader("Referer", referer);
    }

    @Step(url = "/index.php?action=adult_customer_agecheck&agecheck=1&redirect=https://www.arzon.jp/itemlist.html", target = "获取正确的cookies")
    class Arzon0 extends StepMethod {

        public Arzon0(String path, String host, String pro, Map<String, String> header, Map<String, String> cookies) {
            super(path, host, pro, header, cookies);
        }

        @Override
        public int afterDoc(Connection.Response res, Reptile reptile, int deep, List<StepMethod> methods) {
            var next = methods.get(deep + 1);
            if (next != null) {
                next.setCookies(res.cookies());
            }
            return super.afterDoc(res, reptile, deep, methods);
        }

        /**
         * 4.当结束时,执行method0.end()
         */
        @Override
        public void end() {
            //整理数据
            String type = localTargetName.substring(localTargetName.lastIndexOf(".") + 1);
            new File(localTargetName).renameTo(new File(localTargetPath + "../" + other.get("code") + "." + type));
            logger.info("处理结束");
            //1.创建电影对象
            Movie movie = new Movie();
            String codeName = other.get("code");
            LocalDate releaseTime = LocalDate.parse(other.get("releaseTime"));
            String title = other.get("title");
            String desc = other.get("desc");
            String lenth = other.get("length");
            movie.setMovieName(codeName);
            movie.setReleaseTime(releaseTime);
            movie.setTitle(title);
            movie.setDesc(desc);
            movie.setLength(Integer.parseInt(lenth));
            movie.setCover(coverData);
            //使用hibernate,如果存在表关联,则需要在各对象间储存时持有正确的关系
            //2.创建detail对象
            var detailList = detailData.entrySet().stream().map(t -> {
                MovieDetail detail = new MovieDetail(t.getValue(), movie);
                return detail;
            }).collect(Collectors.toList());
            //3.创建actor对象
            var actorList = actorData.entrySet().stream().map(t -> {
                Actor actor = new Actor();
                actor.setActor_name(t.getKey());
                actor.setActor_pic(t.getValue());
                return actor;
            }).collect(Collectors.toList());
            //最终数据
            result = new Result(actorList, movie, detailList);
        }
    }

    @Step(url = "https://www.arzon.jp/itemlist.html?t=&m=all&s=&q=?", target = "访问搜索页,获取指定电影")
    class Arzon1 extends StepMethod {

        public Arzon1(String path, String host, String pro, Map<String, String> header, Map<String, String> cookies) {
            super(path, host, pro, header, cookies);
        }

        @Override
        public void deal(Connection.Response response, List<StepMethod> methods, int deep, Reptile reptile) {
            super.deal(response, methods, deep, reptile);
            var now = methods.get(deep);
            try {
                var document = response.parse();
                Element element = document.select(".pictlist a").first();
                if (element != null) {
                    String a = element.attr("href");
                    methods.add(new Arzion2(a.replace("//", "/"), reptile.getHost(), reptile.getPro(), now.getHeader(), now.getCookies()));
                } else {
                    //不存在该电影,写入日志
                    logger.warn("该影片不存在");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 数据:
     * 1.title 存放到数据库,并且创建
     *
     * @see #localTargetPath= localTargetPath/actor-actor.../title目录作为新的根目录
     * 2.actor 创建多个picDown,演员图片直接存放至数据库
     * 3.detail 存放到数据库数据库,并
     * @see #localTargetPath =localTargetPath/detial/ 存放新数据
     * 4.当结束时,执行method0.end()
     */
    @Step(url = "https://www.arzon.jp/?.html", target = "指定电影详情页")
    class Arzion2 extends StepMethod {

        public Arzion2(String path, String host, String pro, Map<String, String> header, Map<String, String> cookies) {
            super(path, host, pro, header, cookies);
        }

        @Override
        public void deal(Connection.Response response, List<StepMethod> methods, int deep, Reptile reptile) {

            super.deal(response, methods, deep, reptile);
            var now = methods.get(deep);
            String referer = response.url().toString();
            try {
                var document = response.parse();
                var cover = document.select(".item_detail a").first();
                var coverLink = cover.attr("href");
                if (coverLink == "") {
                    System.out.println("不存在封面");
                } else {
                    //封面图片
                    var picDown = new picDown(coverLink, "", now.getPro(), now.getHeader(), now.getCookies());
                    picDown.setHeader("Referer", referer);
                    picDown.setPicType(PicType.COVER);
                    //测试点
                    methods.add(picDown);
                }

                //详情页图片
                var das = document.select(".detail_img a");
                for (Element element : das) {
                    var detailPicLink = element.attr("href");
                    var detailDown = new picDown(detailPicLink, "", now.getPro(), now.getHeader(), now.getCookies());
                    detailDown.setHeader("Referer", referer);
                    detailDown.setPicType(PicType.DETAIL);
                    detailDown.setPicName(detailPicLink.substring(coverLink.lastIndexOf("/") + 1));
                    //测试点
                    methods.add(detailDown);
                }
                //处理信息
                var tds = document.select(".item_register td");
                for (int index = 0; index < tds.size(); index++) {
                    var key = tds.get(index).text();
                    if (key.equals("AV女優：")) {
                        var a = tds.get(++index).select("a");
                        if (a.size() != 0) {
                            //演员图片
                            for (Element ea : a) {
                                //TODO: 进入了ArzonData流程就说明该电影在数据库中不存在,那么至少在localPath目录下要创建关于该电影的 /cover /actor /detail数据 这里要处理的是从那么获取数据
                                //1.爬虫数据
                                //2.本地数据
                                actorLink.put(ea.text(), ea.attr("href"));
                                var actorDown = new ActorMethod(ea.attr("href"));
                                getInfoByProMethod(response, now, actorDown);
                                actorDown.setActorName(ea.text() + ".jpg");
                                //测试点
                                methods.add(actorDown);
                            }
                        }
                    } else if (key.equals("品番：")) {
                        other.put("code", TypeDeal.getACode(tds.get(++index).text()));
                    } else if (key.equals("発売日：")) {
                        String time = tds.get(++index).text();
                        String rex = "[0-9]{1,4}/[0-9]{1,2}/[0-9]{1,2}";
                        Pattern rexP = Pattern.compile(rex);
                        Matcher matcher = rexP.matcher(time);
                        if (matcher.find()) {
                            String[] re = matcher.group().split("/");
                            other.put("releaseTime", LocalDate.of(Integer.parseInt(re[0]), Integer.parseInt(re[1]), Integer.parseInt(re[2])).toString());
                        }
                    } else if (key.equals("収録時間：")) {
                        String time = tds.get(++index).text();
                        String reg = "[0-9]{1,3}";
                        Pattern rexp = Pattern.compile(reg);
                        var match = rexp.matcher(time);
                        if (match.find()) {
                            time = match.group();
                        } else
                            time = "0";
                        other.put("length", time);
                    }
                }
                //标题
                var title = document.select(".detail_title_new2 h1").first().text();
                title = title.replaceAll(".", "");
                other.put("title", title);
                //介绍
                String desc = document.select(".item_text").first().text();
                other.put("desc", desc);
                //根据演员创建新目录
                actorLink.keySet().stream().forEach(t -> {
                    addNewPath(t + "-");
                });
                addNewPath("/detail/");
                ReptileUtil.createDir(localTargetPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Step(url = "https://img.arzon.jp/image/??", target = "图片下载页面")
    class picDown extends StepMethod {
        private PicType picType;
        private String picName;

        public picDown(String path, String host, String pro, Map<String, String> header, Map<String, String> cookies) {
            super(path, host, pro, header, cookies);
        }

        public picDown(String path) {
            super(path);
        }

        public String getPicName() {
            return picName;
        }

        public void setPicName(String picName) {
            this.picName = picName;
        }

        public PicType getPicType() {
            return picType;
        }

        public void setPicType(PicType picType) {
            this.picType = picType;
        }


        /**
         * @param response
         * @param methods
         * @param deep
         * @param reptile
         */
        @Override
        public void deal(Connection.Response response, List<StepMethod> methods, int deep, Reptile reptile) {
            super.deal(response, methods, deep, reptile);
            String path = localTargetPath;
            var buf = FileUtil.getInBytes(response.bodyStream());
            if (picType == PicType.COVER) {
                path += "../cover/";
                this.setPicName(other.get("code") + ".jpg");
                coverData = buf;
            } else if (picType == PicType.ACTOR) {
                path = localTargetPath + "../actor/";
                actorData.put(this.getPicName(), buf);
            } else
                detailData.put(this.getPicName(), buf);
            ReptileUtil.createDir(path);
            path += getPicName();
            logger.warn(path + "保存路径" + picType);
            FileUtil.wirteToDest(buf, path);
        }
    }

    //
    class ActorMethod extends StepMethod {
        private String actorName;

        public ActorMethod(String path, String host, String pro, Map<String, String> header, Map<String, String> cookies, String actorName) {
            super(path, host, pro, header, cookies);
            this.actorName = actorName;
        }

        public ActorMethod(String actorLink) {
            super(actorLink);
        }

        public void setActorName(String actorName) {
            this.actorName = actorName;
        }

        @Override
        public void deal(Connection.Response response, List<StepMethod> methods, int deep, Reptile reptile) {
            super.deal(response, methods, deep, reptile);
            try {
                var docment = response.parse();
                var actorLink = docment.select(".p_list1 .img img").attr("src");
                var actorDown = new picDown(actorLink);
                actorDown.setPicName(this.actorName);
                actorDown.setPicType(PicType.ACTOR);
                getInfoByProMethod(response, methods.get(deep), actorDown);
                methods.add(actorDown);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    //'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8',

    /**
     * 'Connection': "keep-alive",
     * 'Cache-Control': 'max-age=0',
     * 'Upgrade-Insecure-Requests': 1,
     * 'Accept-Encodin':'gzip, deflate, br',
     * 'Accept-Language':'zh-CN,zh;q=0.9',
     * 'Cookie':cook,
     * 'User-Agent':'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/71.0.3578.98 Safari/537.36',
     * 'Referer':''
     * <p>
     * 这里的日天问题是jsoup将重定向自己执行了,导致我这里的逻辑不正确,日了狗了
     */
    public Result getReFromArzon() {
//        String path = "/index.php?action=adult_customer_agecheck&agecheck=1&redirect=https://www.arzon.jp/itemlist.html";
        String path = "/index.php?action=adult_customer_agecheck&agecheck=1&redirect=https://www.arzon.jp/itemlist.html";
        String host = "www.arzon.jp";
        String pro = "https";
        var arzon0 = new Arzon0(path, host, pro, new HashMap<>(), new HashMap<>());
        arzon0.setHeader("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
        arzon0.setHeader("connection", "keep-alive");
        arzon0.setHeader("upgrade-insecure-requests", "1");
        arzon0.setHeader("accept-encoding", "gzip, deflate, br");
        arzon0.setHeader("accept-language", "zh-CN,zh;q=0.9");
        arzon0.setHeader("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:64.0) Gecko/20100101 Firefox/64.0");
        arzon0.setHeader("host", arzon0.getHost());
        String target = ReptileUtil.pathToCode(this.localTargetName);
        var arzon1 = new Arzon1("/itemlist.html?t=all&m=all&s=&q=" + target, host, pro, arzon0.getHeader(), arzon0.getCookies());
        Reptile re = new Reptile(Stream.of(arzon0, arzon1).collect(Collectors.toList()));
        re.init();
        return result;
    }
}
