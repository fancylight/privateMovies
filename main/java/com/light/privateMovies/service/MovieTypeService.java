package com.light.privateMovies.service;

import com.light.privateMovies.dao.MovieTypeDao;
import com.light.privateMovies.pojo.MovieType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class MovieTypeService {
    private MovieTypeDao movieTypeDao;

    @Autowired
    public void setMovieTypeDao(MovieTypeDao movieTypeDao) {
        this.movieTypeDao = movieTypeDao;
    }

    public MovieType addNoExist(MovieType type) {
       return movieTypeDao.addNoExist(type);
    }
}
