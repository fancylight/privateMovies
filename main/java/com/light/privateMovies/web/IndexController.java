package com.light.privateMovies.web;

import com.light.privateMovies.pojo.ModuleEntry;
import com.light.privateMovies.pojo.Movie;
import com.light.privateMovies.reptile.ja.ConstantPath;
import com.light.privateMovies.service.ModuleService;
import com.light.privateMovies.service.MovieService;
import com.light.privateMovies.util.FileUtil;
import com.light.privateMovies.web.pojo.ActorData;
import com.light.privateMovies.web.pojo.ModuleData;
import com.light.privateMovies.web.pojo.MovieData;
import com.light.privateMovies.web.pojo.MoviesData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.net.URLDecoder;
import java.time.ZoneOffset;
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

    /**
     * 返回所有
     *
     * @param moduleName
     * @return
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
     * 返回电影通过电影名
     *
     * @param movieName
     * @return
     */
    @RequestMapping("/getMovie")
    public MovieData getMovieByName(@RequestParam(name = "movieName") String movieName) {
        Movie m = movieService.getMovieByName(movieName);
        var actors = m.getActors().stream().map(t -> {
            ActorData actorData = new ActorData(getPicPath(m, ConstantPath.ACTOR, t.getActor_name()), t.getActor_name());
            return actorData;
        }).collect(Collectors.toList());
        MovieData movieData = new MovieData(getPicPath(m, ConstantPath.COVER, m.getMovieName()), getMoviePath(m), getSrtPath(m), actors, m.getMovieName());
        return movieData;
    }

    @RequestMapping("showMovies")
    public String playMovie(@RequestParam(name = "movieName") String movieName) {
        String name=movieName.substring(movieName.lastIndexOf("/")+1);
        name=name.substring(0,name.lastIndexOf("."));
        var m=movieService.getMovieByName(name);
        if(m==null)
            return "不存在电影";
        String realPath=m.getLocalPath();
        try {
            Runtime.getRuntime().exec(Constant.LOCALPLAYER+" "+realPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "success";
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
