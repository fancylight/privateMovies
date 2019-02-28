package com.light.privateMovies.dao.base;

import org.hibernate.query.Query;
import org.springframework.orm.hibernate5.HibernateCallback;
import org.springframework.orm.hibernate5.HibernateTemplate;
import org.springframework.stereotype.Repository;

import javax.persistence.Id;
import javax.persistence.Table;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


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
     * 根据key value 查询指定表数据是否存在
     *
     * @param key
     * @param value
     * @return
     */
    public boolean isExist(String key, String value) {
        List l = getList(key, value);
        return l.size() != 0;
    }

    private List getList(String key, String value) {
        String tableName = clazz.getAnnotation(Table.class).name();
        String sql = "select * from " + tableName + " where " + key + " = '" + value + "'";
        return hibernateTemplate.execute(session -> session.createSQLQuery(sql).list());
    }
    //通过某字段返回list
    public List<T> getListByKeyValue(String key, String value) {
        List l = getList(key, value);
        return l;
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

    /**
     * HQL语句 查询如 "SELECT movieName,localPath FROM com.xxx.Movie" column和table都是指的是对象不是数据表
     * 查询对象的部分字段
     *
     * @param columnName 查询的对象属性
     * @return 返回包含T对象的list
     */
    public List<T> getPartData(String[] columnName) {
        //获取
        String tableName = "";
        tableName = clazz.getName();

        String finalTableName = tableName;//实际上这是个final,如果此时给该值赋值,则内部类异常

        return hibernateTemplate.execute((HibernateCallback<List<T>>) session -> {
            String hql = "select ";
            for (String col : columnName) {
                hql += col + ",";
            }
            hql = hql.substring(0, hql.lastIndexOf(","));
            hql += " from " + finalTableName;
            Query qu = session.createQuery(hql);
            List<Object[]> l = qu.list(); //返回List<>,其中每个都是包含字段值的Object[]
            //构造器
            Constructor<T> constructor = null;
            try {
                constructor = clazz.getDeclaredConstructor();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
            Constructor<T> finalConstructor = constructor;
            //赋值部分
            List<Field> fs = new ArrayList<>();
            for (String col : columnName) {
                try {
                    var f = clazz.getDeclaredField(col);
                    f.setAccessible(true);
                    fs.add(f);
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                }
            }

            return l.stream().map(t -> {
                T t1 = null;
                try {
                    t1 = finalConstructor.newInstance();
                    for (int index = 0; index < fs.size(); index++) {

                        fs.get(index).set(t1, t[index]);
                    }
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
                return t1;
            }).collect(Collectors.toList());
        });
    }
}
