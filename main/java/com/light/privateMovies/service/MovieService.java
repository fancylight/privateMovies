package com.light.privateMovies.service;

import com.light.privateMovies.dao.MovieDao;
import com.light.privateMovies.pojo.Movie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class MovieService {
    private MovieDao movieDao;

    @Autowired
    public void setMovieDao(MovieDao movieDao) {
        this.movieDao = movieDao;
    }

    private Map<String, List<Movie>> movieHashMap = new HashMap<>();

    private boolean hasBuf = false;

    /**
     * 获取电影实际位置
     *
     * @param name
     * @return 如果为null说明电影不存在
     */
    public String getRealPathByName(String name) {
        Movie m = movieDao.getMovieByMovieName(name);
        return m != null ? m.getLocalPath() : "";
    }

    /**
     * 获取根路径为 moudlePath的电影
     *
     * @param moudlePath
     * @return
     */
    public List<Movie> getMoviesByMoudle(String moudlePath) {
        if (!hasBuf) {
            movieHashMap = movieDao.getAll().stream().collect(Collectors.groupingBy(Movie::getMovieName));
            hasBuf = true;
        }
        return movieHashMap.values().stream().flatMap(t -> t.stream()).filter(t -> t.getLocalPath().contains(moudlePath)).collect(Collectors.toList());
    }

    public Movie getMovieByName(String movieName) {
        if (!hasBuf) {
            return movieDao.getMovieByMovieName(movieName);
        }
        return movieHashMap.get(movieName).get(0);
    }
}
