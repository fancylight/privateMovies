package com.light.privateMovies.dao;

import com.light.privateMovies.dao.base.LightBaseDao;
import com.light.privateMovies.pojo.MovieDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate5.HibernateTemplate;
import org.springframework.stereotype.Repository;

/**
 * 关于该部分,关于详细数据添加时必须有电影id,否则不能添加
 */
@Repository
public class MovieDetailDao extends LightBaseDao<MovieDetail> {
    public MovieDetailDao() {
        this.clazz = MovieDetail.class;
    }

    @Autowired
    public void setHibernateTemplate(HibernateTemplate hibernateTemplate) {
        this.hibernateTemplate = hibernateTemplate;
    }
}
