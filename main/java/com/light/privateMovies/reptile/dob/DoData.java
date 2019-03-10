package com.light.privateMovies.reptile.dob;

import com.light.privateMovies.reptile.annotation.Step;
import com.light.privateMovies.reptile.ja.Result;

/**
 * 豆瓣获取数据
 */
@Step(url = "https://movie.douban.com")
public class DoData {
    private Result result;

    public Result getResult() {
        return result;
    }
}
