package com.light.privateMovies.dao;

import com.light.privateMovies.dao.base.LightBaseDao;
import com.light.privateMovies.pojo.Movie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate5.HibernateTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class MovieDao extends LightBaseDao<Movie> {
    public MovieDao() {
        this.clazz = Movie.class;
    }

    @Autowired
    public void setHibernateTemplate(HibernateTemplate hibernateTemplate) {
        this.hibernateTemplate = hibernateTemplate;
    }

    public Movie getMovieByMovieName(String name) {
        var m = new Movie();
        m.setMovieName(name);
        var list = getListByExample(m);
        if (list.size() > 0)
            return list.get(0);
        m = null;
        return m;
    }
}
