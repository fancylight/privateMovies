package com.light.privateMovies.web;

import com.light.privateMovies.pojo.ModuleEntry;
import com.light.privateMovies.pojo.Movie;
import com.light.privateMovies.reptile.ja.ConstantPath;
import com.light.privateMovies.service.ModuleService;
import com.light.privateMovies.service.MovieService;
import com.light.privateMovies.util.FileUtil;
import com.light.privateMovies.web.pojo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLDecoder;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class IndexController {
    private ModuleService moduleService;
    private MovieService movieService;

    @Autowired
    public void setModuleService(ModuleService moduleService) {
        this.moduleService = moduleService;
    }

    @Autowired
    public void setMovieService(MovieService movieService) {
        this.movieService = movieService;
    }


    /**
     * //TODO:看看jackson源码如何转换json的
     * 首页 获取所有模块相关信息
     *
     * @param moduleName
     * @return
     * @see org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
     */
    @RequestMapping(value = "/modulesData/{moduleName}", method = {RequestMethod.GET})
    public List<ModuleData> getMovieDate(@PathVariable String moduleName) {
        //FIXME: 此处返回json由于hibernate的问题导致了无限递归的问题
        //1.将ModuleType 忽视 使用@JsonIgnore或者,此处置null
        //如果我们还是需要ModuleType信息那么就不能如此简单的处理,比如说前台需要的是含有module并且具有type信息的情况
        //2.也可以通过手动去除该list中每个module中type对于module的引用
        //3.研究jackson源码 看看还有什么处理方式
        //todo:我觉得应该不要返回ModuleEntry列表,而是自己创建一个,更加有控制性,比如说加入一个moduleCoverPath
        List<ModuleEntry> list = null;
        if (moduleName.equals("all"))
            list = moduleService.getAllModule();
        else
            list = moduleService.getModuleByName(moduleName);
        list.stream().forEach(t -> {
            if (t.getModuleType() != null) t.getModuleType().setModuleEntries(null);
        });
        //转换
        var l = list.stream().map(t -> {
            var ml = movieService.getMoviesByMoudle(t.getModuleName());
            Movie movie = null;
            if (ml.size() != 0)
                movie = ml.get(0);
            var md = new ModuleData(getPicPath(movie, ConstantPath.COVER, movie.getMovieName()), t.getModuleName(), t.getModuleName());
            return md;
        }).collect(Collectors.toList());
        return l;
    }

    private String getPicPath(Movie movie, String type, String targetName) {
        return "/pic/" + movie.getMovieName() + "/" + type + "/" + targetName + ".jpg";
    }

    /**
     * 获取图片
     *
     * @param movieName
     * @param type
     * @param request
     * @return
     */
    @RequestMapping("/pic/{movieName}/{type}/*.jpg")
    public byte[] getPic(@PathVariable String movieName, @PathVariable String type, HttpServletRequest request) {
        String url = request.getRequestURL().toString();
        String target = url.substring(url.lastIndexOf("/") + 1);
        try {
            target = URLDecoder.decode(target, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        //根据电影名称找到实际路径
        String real = movieService.getRealPathByName(movieName);
        byte[] bytes = null;
        if (real.equals(""))
            return bytes;
        else {
            real = real.substring(0, real.lastIndexOf("/"));
            String path = real + "/" + type + "/" + target;
            try {
                bytes = FileUtil.getInBytes(new FileInputStream(new File(path)));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            return bytes;
        }
    }

    private int maxBytes = 64 * 1024;

    //H5 VIDEO请求 为 Content-Range: bytes=79036416-
    @RequestMapping("/movie/{movieName}/{target}")
    public byte[] playMovieOnH5(@PathVariable(name = "movieName") String movieName, HttpServletResponse response, @RequestHeader(name = "range")String range,HttpServletRequest request) {
        String real = movieService.getRealPathByName(movieName);
        if (real.equals(""))
            return null;
        long length = new File(real).length();
        String url=request.getRequestURL().toString();
        String target=url.substring(url.lastIndexOf("/")+1);
        String type = target.substring(target.lastIndexOf(".") + 1);
        //1.处理start-end
        range = range.replace("bytes=", "");
        long[] dataRange = new long[2];
        String[] realRange = range.split("-");
        dataRange[0] = Long.parseLong(realRange[0]);
        if (realRange.length == 2) {
            dataRange[1] = Long.parseLong(realRange[1]);
        } else {
            dataRange[1] = length - 1;
        }

        byte[] buf = new byte[maxBytes];
        //2.根据距离进行读
        try {
            var ra = new RandomAccessFile(new File(real), "r");
            ra.seek(dataRange[0]);
            int readLen = ra.read(buf); //返回读取的字节数
            var out = response.getOutputStream();
            response.setHeader("Content-Length", "" + readLen);
            response.setHeader("Content-Type", "video/" + type);
            response.setHeader("Accept-Ranges", "bytes");
            response.setHeader("Content-Range", "bytes " + dataRange[0] + "-" + dataRange[1] + "/" + length);
            response.setStatus(206);
            out.write(buf, 0, readLen);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @RequestMapping("/srt/{srtName}")
    public byte[] getSub(@PathVariable(name = "srtName") String strName, HttpServletRequest request) {
        return null;
    }

    /**
     * 返回指定模块的所有电影
     *
     * @param moduleName 模块名
     * @return 电影s
     */
    @RequestMapping("/getMovies")
    public List<MoviesData> getMoviesByModuleName(@RequestParam(name = "moduleName") String moduleName) {
        var list = movieService.getMoviesByMoudle(moduleName);
        return list.stream().map(t -> {
            MoviesData data = new MoviesData(getPicPath(t, ConstantPath.COVER, t.getMovieName()), t.getMovieName(), getMoviePath(t), t.getCreateTime().toEpochSecond(ZoneOffset.UTC));
            return data;
        }).collect(Collectors.toList());
    }

    /**
     * @param movieName 电影名
     * @return 电影信息
     */
    @RequestMapping("/getMovie")
    public MovieData getMovieByName(@RequestParam(name = "movieName") String movieName) {
        Movie m = movieService.getMovieByName(movieName);
//        var actors = m.getActors().stream().map(t -> {
//            ActorData actorData = new ActorData(getPicPath(m, ConstantPath.ACTOR, t.getActor_name()), t.getActor_name());
//            return actorData;
//        }).collect(Collectors.toList());
//        MovieData movieData = new MovieData(getPicPath(m, ConstantPath.COVER, m.getMovieName()), getMoviePath(m), getSrtPath(m), actors, m.getMovieName());
        return getMovieDataByMovie(m);
    }

    @RequestMapping("/showMovies")
    public String playMovie(@RequestParam(name = "movieName") String movieName) {
        String name = movieName.substring(movieName.lastIndexOf("/") + 1);
        name = name.substring(0, name.lastIndexOf("."));
        var m = movieService.getMovieByName(name);
        if (m == null)
            return "不存在电影";
        String realPath = m.getLocalPath();
        try {
            Runtime.getRuntime().exec(Constant.LOCALPLAYER + " " + realPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "success";
    }

    /**
     * 通过特定类型返回电影列表
     *
     * @param type
     * @return
     */
    @RequestMapping("/getMoviesByType")
    public List<MovieData> getMovieByActorName(@RequestParam(name = "type") String type, @RequestParam(name = "data") String data) {
        List<Movie> movies = new ArrayList<>();
        if (type.equals("actor"))
            movies = movieService.getAllMovies().stream().filter(t -> t.getActors().stream().anyMatch(t2 -> t2.getActor_name().equals(data))).collect(Collectors.toList());
        else if (type.equals("type"))
            movies = movieService.getAllMovies().stream().filter(t -> t.getMovieTypes().stream().anyMatch(t2 -> t2.getMovieType().contains(data))).collect(Collectors.toList());
        return movies.stream().map(m -> getMovieDataByMovie(m)).collect(Collectors.toList());
    }

    /**
     * 创建movieData数据
     *
     * @param movie
     * @return
     */
    private MovieData getMovieDataByMovie(Movie movie) {
        var m = new MovieData(getPicPath(movie, ConstantPath.COVER, movie.getMovieName()), getMoviePath(movie), getSrtPath(movie), getActorDataByMovie(movie), movie.getMovieName(), getTypeDataBy(movie));
        return m;
    }

    /**
     * 创建ActorData数据
     *
     * @param movie
     * @return
     */
    private List<ActorData> getActorDataByMovie(Movie movie) {
        return movie.getActors().stream().map(actor -> new ActorData(getPicPath(movie, ConstantPath.ACTOR, actor.getActor_name()), actor.getActor_name())).collect(Collectors.toList());
    }

    /**
     * 创建typeData
     */
    public TypeData getTypeDataBy(Movie movie) {
        return new TypeData(movie.getMovieTypes().stream().map(t -> t.getMovieType()).collect(Collectors.toList()));
    }

    //todo:返回字幕,前台路径为 /srt/{movieName},后台判断有哪些字幕都发送过去
    private String getSrtPath(Movie m) {
        return "/srt/" + m.getMovieName();
    }

    //todo:返回电影前台路径为 /movie/{movieName.type}
    private String getMoviePath(Movie t) {
        var local = t.getLocalPath();
        String typeName = local.substring(local.lastIndexOf("/") + 1);
        return "/movie/" + t.getMovieName() + "/" + typeName;
    }


}
