package com.light.privateMovies.service;

import com.light.privateMovies.dao.ModuleDao;
import com.light.privateMovies.dao.MovieDao;
import com.light.privateMovies.pojo.Movie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
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

    public void moduleMoviesChache() {
        movieHashMap = movieDao.getAll().stream().collect(Collectors.groupingBy(Movie::getMovieName));
        hasBuf = true;
        var modules = moduleDao.getAll();
        modules.stream().forEach(m -> {
            ArrayList<Movie> list = new ArrayList<>();
            var movies = getMoviesByMoudleAll(m.getModuleName());
            moduleMovies.put(m.getModuleName(), list);
            var target = movies.stream().filter(movie -> {
                if (movie.getLocalPath().contains(m.getModuleName()) && new File(movie.getLocalPath()).exists())
                    return true;
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
    public List<Movie> getMoviesByMoudleAll(String moudlePath) {
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
        if (hasBuf)
            m = movieHashMap.get(name).get(0);
        else
            movieDao.getMovieByMovieName(name);
        return m != null ? m.getLocalPath() : "";
    }

    /**
     * 查看本地存在的模块电影
     *
     * @param moudlePath
     * @return
     */
    public List<Movie> getMoviesByMoudle(String moudlePath) {
//        if (!hasBuf) {
//            movieHashMap = movieDao.getAll().stream().collect(Collectors.groupingBy(Movie::getMovieName));
//            hasBuf = true;
//        }

        return moduleMovies.get(moudlePath).stream().collect(Collectors.toList());
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
}
