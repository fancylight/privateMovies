package com.light.privateMovies.dao;

import com.light.privateMovies.dao.base.LightBaseDao;
import com.light.privateMovies.pojo.Movie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate5.HibernateTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
public class MovieDao extends LightBaseDao<Movie> {
    public MovieDao() {
        this.clazz = Movie.class;
    }

    @Autowired
    public void setHibernateTemplate(HibernateTemplate hibernateTemplate) {
        this.hibernateTemplate = hibernateTemplate;
    }

    /**
     *
     * @param name 电影名
     * @return 电影
     */
    public Movie getMovieByMovieName(String name) {
        var m = new Movie();
        m.setMovieName(name);
        var list = getListByExample(m);
        if (list.size() > 0)
            return list.get(0);
        m = null;
        return m;
    }

    /**
     *  仅仅获取电影名和path
     * @return
     */
    public Map<String, List<Movie>> getNameAndPath() {
        var list = getPartData(new String[]{"movieName", "localPath"});
        return list.stream().collect(Collectors.groupingBy(Movie::getMovieName));
    }
}