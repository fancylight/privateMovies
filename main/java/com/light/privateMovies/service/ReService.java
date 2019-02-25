package com.light.privateMovies.service;


import com.light.privateMovies.dao.*;
import com.light.privateMovies.reptile.Result;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * 处理关系
 */
@Service
public class ReService {
    Logger logger = LogManager.getLogger(ReService.class);
    private ActorDao actorDao;
    private MovieDao movieDao;
    private MovieTypeDao movieTypeDao;
    private ModuleTypeDao moduleTypeDao;
    private ModuleDao moduleDao;
    private MovieDetailDao movieDetailDao;

    @Autowired
    public void setActorDao(ActorDao actorDao) {
        this.actorDao = actorDao;
    }

    @Autowired
    public void setMovieDao(MovieDao movieDao) {
        this.movieDao = movieDao;
    }

    @Autowired
    public void setMovieTypeDao(MovieTypeDao movieTypeDao) {
        this.movieTypeDao = movieTypeDao;
    }

    @Autowired
    public void setModuleTypeDao(ModuleTypeDao moduleTypeDao) {
        this.moduleTypeDao = moduleTypeDao;
    }

    @Autowired
    public void setModuleDao(ModuleDao moduleDao) {
        this.moduleDao = moduleDao;
    }

    @Autowired
    public void setMovieDetailDao(MovieDetailDao movieDetailDao) {
        this.movieDetailDao = movieDetailDao;
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
        movieDao.add(result.getMovie());
        movieDetailDao.setListData(result.getMovieDetail());
    }
}
