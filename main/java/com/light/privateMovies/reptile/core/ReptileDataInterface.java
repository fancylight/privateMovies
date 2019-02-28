package com.light.privateMovies.reptile.core;


/**
 * 爬虫接口
 */
public interface ReptileDataInterface {
    void init();
    void getRe(String url,int deep);

}
