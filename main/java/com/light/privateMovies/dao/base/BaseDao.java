package com.light.privateMovies.dao.base;

import java.io.Serializable;
import java.util.List;

public interface BaseDao<T> {
    Serializable add(T t);
    T get(Serializable id);
    void delete(T t);
    void update(T t);
    List<T> getAll();
}
