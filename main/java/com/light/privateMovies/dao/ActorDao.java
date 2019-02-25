package com.light.privateMovies.dao;

import com.light.privateMovies.dao.base.LightBaseDao;
import com.light.privateMovies.pojo.Actor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate5.HibernateTemplate;
import org.springframework.stereotype.Repository;


@Repository
public class ActorDao extends LightBaseDao<Actor> {

    public ActorDao() {
        this.clazz = Actor.class;
    }

    @Autowired
    public void setHibernateTemplate(HibernateTemplate hibernateTemplate) {
        this.hibernateTemplate = hibernateTemplate;
    }

    public Actor getActorByName(String name) {
        var actor = new Actor();
        actor.setActor_name(name);
        var l = getListByExample(actor);
        return l.size() == 0 ? l.get(0) : null;
    }
}
