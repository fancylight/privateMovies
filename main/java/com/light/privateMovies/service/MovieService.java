package com.light.privateMovies.service;

import com.light.privateMovies.dao.ModuleDao;
import com.light.privateMovies.dao.MovieDao;
import com.light.privateMovies.init.SubDeal;
import com.light.privateMovies.pojo.Movie;
import com.light.privateMovies.reptile.core.ReptileUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class MovieService {
    private MovieDao movieDao;
    private ModuleDao moduleDao;

    @Autowired
    public void setMovieDao(MovieDao movieDao) {
        this.movieDao = movieDao;
    }

    @Autowired
    public void setModuleDao(ModuleDao moduleDao) {
        this.moduleDao = moduleDao;
    }

    private Map<String, List<Movie>> movieHashMap = new HashMap<>();

    private boolean hasBuf = false;
    //缓冲模块对应本地实际包含的电影
    private HashMap<String, List<Movie>> moduleMovies = new HashMap<>();

    private SubDeal subDeal;

    @Autowired
    public void setSubDeal(SubDeal subDeal) {
        this.subDeal = subDeal;
    }

    //返回字幕位置,但是不包括盘符,否则400
    public List<String> getSubs(String movieName) {
        var list = subDeal.getSubPath(movieName);
        return list == null ? null : list.stream().map(t -> {
            String path = t.replaceAll("\\\\", "/");
            return path.substring(path.indexOf("/"));
            //todo:因此资源中srt实际都是vtt 类型,因此就返回vtt类型,即使是srt也返回vtt
        }).filter(t -> {
            String type = ReptileUtil.getType(t);
            return type.equals("vtt") || type.equals("srt");
        }).map(t -> t.substring(0, t.lastIndexOf(".") + 1) + "vtt").collect(Collectors.toList());
    }

    /**
     * 该函数表示缓冲,被initService调用
     */
    public void moduleMoviesCache() {
        movieHashMap = movieDao.getAll().stream().collect(Collectors.groupingBy(Movie::getMovieName));
        hasBuf = true;
        var modules = moduleDao.getAll();
        modules.stream().forEach(m -> {
            ArrayList<Movie> list = new ArrayList<>();
            var movies = getMoviesByModuleAll(m.getModuleName());
            moduleMovies.put(m.getModuleName(), list);
            var target = movies.stream().filter(movie -> {
                if (movie.getLocalPath().contains(m.getModuleName()) && new File(movie.getLocalPath()).exists()) {
                    return true;
                }
                return false;
            }).collect(Collectors.toList());
            list.addAll(target);
        });
    }

    /**
     * 不经过查看本地
     *
     * @param moudlePath
     * @return
     */
    public List<Movie> getMoviesByModuleAll(String moudlePath) {
        return movieHashMap.values().stream().flatMap(t -> t.stream()).filter(t -> t.getLocalPath().contains(moudlePath)).collect(Collectors.toList());
    }

    /**
     * 获取电影实际位置
     *
     * @param name
     * @return 如果为null说明电影不存在
     */
    public String getRealPathByName(String name) {
        Movie m = null;
        if (hasBuf) {
            m = movieHashMap.containsKey(name) ? movieHashMap.get(name).get(0) : null;
        } else
            movieDao.getMovieByMovieName(name);
        return m != null ? m.getLocalPath() : "";
    }

    /**
     * 查看本地存在的模块电影
     *
     * @param modulePath
     * @return
     */
    public List<Movie> getMoviesByModule(String modulePath) {
//        if (!hasBuf) {
//            movieHashMap = movieDao.getAll().stream().collect(Collectors.groupingBy(Movie::getMovieName));
//            hasBuf = true;
//        }

        return moduleMovies.get(modulePath).stream().collect(Collectors.toList());
    }

    public Movie getMovieByName(String movieName) {
        if (!hasBuf) {
            return movieDao.getMovieByMovieName(movieName);
        }
        return movieHashMap.get(movieName).get(0);
    }

    //缓存并获取所有movie list
    public List<Movie> getAllMovies() {
        if (!hasBuf) {
            movieHashMap = movieDao.getAll().stream().collect(Collectors.groupingBy(Movie::getMovieName));
            hasBuf = true;
        }
        return movieHashMap.values().stream().flatMap(t -> t.stream()).collect(Collectors.toList());
    }

    public String deleteMovie(String movieName) {
        var m = movieHashMap.get(movieName).get(0);
        var module = getModuleName(m);
        if (!module.equals("")) {
            var list = moduleMovies.get(module).stream().filter(t -> !t.getMovieName().equals(m.getMovieName())).collect(Collectors.toList());
            moduleMovies.put(module, list);
            movieHashMap.remove(movieName);
            //调用数据库并且删除本地
            movieDao.delete(m, moduleMovies.keySet());
        }
        return movieName;
    }

    private String getModuleName(Movie movie) {
        if (movie != null)
            return movie.getLocalPath().split("/")[1];
        return "";
    }

    public void updata(Movie movie) {
        movieDao.update(movie);
    }

}
