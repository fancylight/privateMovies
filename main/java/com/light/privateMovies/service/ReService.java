package com.light.privateMovies.service;

import com.light.privateMovies.dao.ActorDao;
import com.light.privateMovies.dao.MovieDao;
import com.light.privateMovies.dao.MovieDetailDao;
import com.light.privateMovies.dao.MovieTypeDao;
import com.light.privateMovies.pojo.Movie;
import com.light.privateMovies.pojo.MovieType;
import com.light.privateMovies.reptile.ja.Result;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional
public class ReService {
    private ActorDao actorDao;
    private MovieDao movieDao;
    private MovieDetailDao movieDetailDao;
    private MovieTypeDao movieTypeDao;
    Logger logger = LogManager.getLogger(ReService.class);

    @Autowired
    public void setActorDao(ActorDao actorDao) {
        this.actorDao = actorDao;
    }

    @Autowired
    public void setMovieDao(MovieDao movieDao) {
        this.movieDao = movieDao;
    }

    @Autowired
    public void setMovieDetailDao(MovieDetailDao movieDetailDao) {
        this.movieDetailDao = movieDetailDao;
    }

    @Autowired
    public void setMovieTypeDao(MovieTypeDao movieTypeDao) {
        this.movieTypeDao = movieTypeDao;
    }

    /**
     * 处理由爬虫获得数据
     * detail--> movie---->actor 这是映射管理的情况
     * 传递过来的数据三者的关系应该是正确的,即 每个detail包含movie对象,每个actor对象包含movie对象
     * 存放顺序:
     * actorList   movie   detailList
     *
     * @param result
     */
    public void saveReptileData(Result result) {
        logger.info("开始存放爬虫数据");
        actorDao.setListData(result.getActor());
        //此处的问题在于当actor存在就不插入,导致了对应的actor不存在id,也就不算是持久化数据,此时插入movie对象,会导致异常
        movieDao.add(result.getMovie());
        movieDetailDao.setListData(result.getMovieDetail());
    }

    /**
     * 获取一些必要的数据
     */
    public Map<String, List<Movie>> getMovieAndPath() {
        return movieDao.getNameAndPath();
    }

    public void updateMoviePath(String newPath, String movieName) {
        Movie movie = new Movie();
        movie.setLocalPath(newPath);
        movie.setMovieName(movieName);
        movieDao.update(movie);
    }

    public void updateMoviePath(Movie movie) {
        movieDao.update(movie);
    }

    public Movie getMovieByName(String movieName) {
        return movieDao.getMovieByMovieName(movieName);
    }

    public void addTypeList(List<MovieType> list) {
        movieTypeDao.setListData(list);
    }
}
