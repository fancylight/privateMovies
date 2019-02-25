package com.light.privateMovies.reptile;


/**
 * 爬虫接口
 */
public interface ReptileDataInterface {
    void init();
    void getRe(String url,int deep);

}
