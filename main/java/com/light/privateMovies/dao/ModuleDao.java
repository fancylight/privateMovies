package com.light.privateMovies.dao;

import com.light.privateMovies.dao.base.LightBaseDao;
import com.light.privateMovies.pojo.ModuleEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate5.HibernateTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;


//该实体类表示创建个一个电影视图目录,具有名字和类型编号
@Repository
public class ModuleDao extends LightBaseDao<ModuleEntry> {
    public ModuleDao() {
        this.clazz = ModuleEntry.class;
    }

    @Autowired
    public void setHibernateTemplate(HibernateTemplate hibernateTemplate) {
        this.hibernateTemplate = hibernateTemplate;
    }

    public ModuleEntry getAllModuleJoinType() {
        return null;
    }

    public List<ModuleEntry> getModuleByName(String name) {
        var m = new ModuleEntry();
        m.setModuleName(name);
        return getListByExample(m);
    }
}
