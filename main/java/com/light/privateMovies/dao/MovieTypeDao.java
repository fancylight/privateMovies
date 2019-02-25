package com.light.privateMovies.dao;

import com.light.privateMovies.dao.base.LightBaseDao;
import com.light.privateMovies.pojo.MovieType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate5.HibernateTemplate;
import org.springframework.stereotype.Repository;


@Repository
public class MovieTypeDao extends LightBaseDao<MovieType> {
    public MovieTypeDao() {
        this.clazz = MovieType.class;
    }
    @Autowired
    public void setHibernateTemplate(HibernateTemplate hibernateTemplate) {
        this.hibernateTemplate = hibernateTemplate;
    }
    public MovieType getTypeByName(String name){
        var type=new MovieType();
        type.setMovieType(name);
        var list= getListByExample(type);
        if(list.size()!=0)
            return list.get(0);
        type=null;
        return type;
    }
}
