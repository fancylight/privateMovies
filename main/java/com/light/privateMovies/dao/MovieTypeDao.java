package com.light.privateMovies.dao;

import com.light.privateMovies.dao.base.LightBaseDao;
import com.light.privateMovies.pojo.MovieType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate5.HibernateTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;


@Repository
public class MovieTypeDao extends LightBaseDao<MovieType> {
    public MovieTypeDao() {
        this.clazz = MovieType.class;
    }

    @Autowired
    public void setHibernateTemplate(HibernateTemplate hibernateTemplate) {
        this.hibernateTemplate = hibernateTemplate;
    }

    public MovieType getTypeByName(String name) {
        var type = new MovieType();
        type.setMovieType(name);
        var list = getListByExample(type);
        if (list.size() != 0)
            return list.get(0);
        type = null;
        return type;
    }

    @Override
    public void setListData(List<MovieType> listData) {
        var li = listData.stream().filter(t -> {
            var re = !isExist("type_name", t.getMovieType());
            if (!re) {
                var tt = getTypeByName(t.getMovieType());
                t.setMovieType(tt);
            }
            return re;
        }).collect(Collectors.toList());
        super.setListData(li);
    }

    /**
     * 不存在时插入,
     *
     * @param movieType
     * @return
     */
    public MovieType addNoExist(MovieType movieType) {
        var list = getListByKeyValue("type_name", movieType.getMovieType());
        if (list.size() > 0) {
            Object[] objects = list.get(0);
            var type = new MovieType((String) objects[1]);
            type.setId((Integer) objects[0]);
            return type;
        } else {
            add(movieType);
            return movieType;
        }
    }
}
