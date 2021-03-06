package com.light.privateMovies.reptile.ja;


import com.light.privateMovies.module.TypeDeal;
import com.light.privateMovies.pojo.Actor;
import com.light.privateMovies.pojo.Movie;
import com.light.privateMovies.pojo.MovieDetail;
import com.light.privateMovies.reptile.core.Reptile;
import com.light.privateMovies.reptile.core.ReptileUtil;
import com.light.privateMovies.reptile.annotation.Step;
import com.light.privateMovies.reptile.core.StepMethod;
import com.light.privateMovies.util.FileUtil;
import com.light.privateMovies.web.Constant;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Connection;
import org.jsoup.nodes.Element;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 最终获取 演员 电影 详情页信息
 */
@Step(url = "https://www.arzon.jp/")
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
         * 4.当结束时,因该执行每个method的end函数
         */
        @Override
        public int end() {
            result = Assemble(localTargetName, localTargetPath, other, actorData, coverData, detailData);
            return -1;
        }
    }

    /**
     * @param localTargetName 表示原目标
     * @param localTargetPath
     * @param other           新的基地址为 .../detail/
     * @param actorData       演员信息
     * @param coverData       封面信息
     * @param detailData      详情图
     * @return
     */
    public static Result Assemble(String localTargetName, String localTargetPath, Map<String, String> other, Map<String, byte[]> actorData,
                                  byte[] coverData, Map<String, byte[]> detailData) {
        //整理数据
        String type = localTargetName.substring(localTargetName.lastIndexOf(".") + 1);
        String newName = localTargetPath + "../" + other.get("code") + "." + type;
        new File(localTargetName).renameTo(new File(newName));
        logger.info("处理结束");
        //todo: 提前终止就不会执行这里
        //1.创建actor对象
        var actorList = actorData.entrySet().stream().map(t -> {
            Actor actor = new Actor();
            actor.setActor_name(ReptileUtil.pathToName(t.getKey()));
            actor.setActor_pic(t.getValue());
            return actor;
        }).collect(Collectors.toList());
        //2.创建电影对象
        Movie movie = new Movie();
        String codeName = other.get("code") != null ? other.get("code") : "";
        String re = other.get("releaseTime") != null ? other.get("releaseTime") : "";
        LocalDate releaseTime = re.equals("") ? null : LocalDate.parse(re);
        String title = other.get("title") != null ? other.get("title") : "";
        String desc = other.get("desc") != null ? other.get("desc") : other.get("desc");
        String lenth = other.get("length") != null ? other.get("length") : "";
        movie.setMovieName(codeName);
        movie.setReleaseTime(releaseTime);
        movie.setTitle(title);
        movie.setDesc(desc);
        movie.setLength(Integer.parseInt(lenth));
        movie.setCover(coverData);
        movie.setActors(actorList);
        movie.setLocalPath(newName);
        //使用hibernate,如果存在表关联,则需要在各对象间储存时持有正确的关系
        //3.创建detail对象
        var detailList = detailData.entrySet().stream().map(t -> {
            MovieDetail detail = new MovieDetail(t.getValue(), movie, ReptileUtil.pathToName(t.getKey()));
            return detail;
        }).collect(Collectors.toList());

        //最终数据
        return new Result(actorList, movie, detailList);
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
                    logger.warn("该影片不存在" + this.getPath());
                    setEnding(true);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public int end() {
            return -1;
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
                                //TODO: 进入了ArzonData流程就说明该电影在数据库中不存在,但是对应的演员可能存在,应该跳过此次下载,但是不处理也可以

                                //1.爬虫数据
                                actorLink.put(ea.text(), ea.attr("href"));
                                var actorDown = new ActorMethod(ea.attr("href"));
                                getInfoByProMethod(response, now, actorDown);
                                actorDown.setActorName(ea.text() + ".jpg");
                                //测试点
                                methods.add(actorDown);
                            }
                        }
                    } else if (key.equals("品番：")) {
                        //fixme:该网站有一些品番：	TKIPX-129  廃盤   TKTABP-720  廃盤  这种情况,导致最终番号显示不正确
                        other.put("code", ReptileUtil.getACode(tds.get(++index).text()));
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
                        time = ReptileUtil.getLengthTime(time);
                        other.put("length", time);
                    }
                }
                //标题
                String title = "";
                var titleElemnt = document.select(".detail_title_new2 h1").first();
                if (titleElemnt != null) {
                    title = titleElemnt.text();
                    title = title.replaceAll("\\.", "");
                    title = title.replaceAll("　", ""); //这里日文网站的空格,utf8编码为 e3 08 08 而不是asc的20
                }

                other.put("title", title);
                //介绍
                var descElement = document.select(".item_text").first();
                String desc = "";
                if (descElement != null)
                    desc = descElement.text();
                other.put("desc", desc);
                //根据演员创建新目录
                addNewPath(ReptileUtil.createActorDir(actorLink.keySet()) + "/");
                //作品名
                addNewPath(ReptileUtil.createTitleCodeDir(other.get("code"), title) + "/");
                addNewPath("detail/");
                ReptileUtil.createDir(localTargetPath);
                //创建一个演员目录,防止有些没有演员信息的情况导致前边逻辑错误
                ReptileUtil.createDir(localTargetPath + "../" + ConstantPath.ACTOR + "/");
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
            //TODO:看看能不能将picDown修改为独立的,供以后使用
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
     * 'ConnectionTarget': "keep-alive",
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
        String target = ReptileUtil.pathToName(this.localTargetName);
        var arzon1 = new Arzon1("/itemlist.html?t=all&m=all&s=&q=" + target, host, pro, arzon0.getHeader(), arzon0.getCookies());
        Reptile re = new Reptile(Stream.of(arzon0, arzon1).collect(Collectors.toList()));
        re.init();
        return result;
    }
}
