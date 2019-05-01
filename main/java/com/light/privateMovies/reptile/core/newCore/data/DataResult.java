package com.light.privateMovies.reptile.core.newCore.data;

import org.jsoup.Connection;

public interface DataResult<E> {
    void TaskDo(int deep, Connection.Response response);
    E getData();
}
