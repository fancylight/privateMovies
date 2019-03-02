package com.light.privateMovies.dao;

import com.light.privateMovies.dao.base.LightBaseDao;
import com.light.privateMovies.pojo.Actor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate5.HibernateTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;


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
        return l.size() != 0 ? l.get(0) : null;
    }

    @Override
    public void setListData(List<Actor> listData) {
        //存在去除
        var li = listData.stream().filter(t -> {

                    var re = !isExist("actor_name", t.getActor_name());
                    //将除去的部分添加id
                    if (!re) {
                       var tt=getActorByName(t.getActor_name());
                       //TODO:这里要么实现一个工具类,要么实现深拷贝,即重写clone函数
                        t.setActor(tt); //这里使用一个简单的方式
                    }
                    //保留不存在的
                    return re;
                }
        ).collect(Collectors.toList());

        super.setListData(li);
    }
}
