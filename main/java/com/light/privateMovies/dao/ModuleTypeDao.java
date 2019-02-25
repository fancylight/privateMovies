package com.light.privateMovies.dao;

import com.light.privateMovies.dao.base.LightBaseDao;
import com.light.privateMovies.pojo.ModuleType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate5.HibernateTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ModuleTypeDao extends LightBaseDao<ModuleType> {
    public ModuleTypeDao() {
        this.clazz = ModuleType.class;
    }

    @Autowired
    public void setHibernateTemplate(HibernateTemplate hibernateTemplate) {
        this.hibernateTemplate=hibernateTemplate;
    }

    public ModuleType getTypeByName(String moduleTypeName) {
        var m = new ModuleType();
        m.setTypeName(moduleTypeName);
        var l = getListByExample(m);
        return l.size() == 0 ? l.get(0) : null;
    }

    //
}
