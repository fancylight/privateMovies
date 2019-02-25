package com.light.privateMovies.dao.base;

import org.springframework.orm.hibernate5.HibernateTemplate;
import org.springframework.stereotype.Repository;

import javax.persistence.Id;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.List;

@Repository
public class LightBaseDao<T> implements BaseDao<T> {
    protected Class<T> clazz;
    protected HibernateTemplate hibernateTemplate;

    @Override
    public Serializable add(T t) {
        return hibernateTemplate.save(t);
    }

    @Override
    public T get(Serializable id) {
        return hibernateTemplate.get(clazz, id);
    }

    @Override
    public void delete(T t) {
        hibernateTemplate.delete(t);
    }

    @Override
    public void update(T t) {
        hibernateTemplate.update(t);
    }

    @Override
    public List<T> getAll() {
        return hibernateTemplate.loadAll(clazz);
    }

    /**
     * 使用此函数,若带有主键则返回一个对象
     *
     * @param t
     * @return
     */
    protected List<T> getListByExample(T t) {
        return hibernateTemplate.findByExample(t);
    }

    /**
     * 设置list,并且list中每个对象设置主键值
     * 实际上这个事情hibernate已经做了
     *
     * @param list
     */
    @Deprecated
    private void setList(List<T> list) {
        if (list.size() == 0)
            return;
        var fs = list.get(0).getClass().getDeclaredFields();
        Field targettField = null;
        for (var f : fs) {
            var ans = f.getAnnotations();
            for (var an : ans) {
                if (an.annotationType().getTypeName().equals(Id.class.getTypeName())) {
                    targettField = f;//找到被标记为id的field
                }
            }
        }
        for (T t : list) {
            Serializable id = add(t);
            try {
                targettField.set(t, id);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    public void setListData(List<T> listData) {
        listData.stream().forEach(t -> add(t));
    }
}
