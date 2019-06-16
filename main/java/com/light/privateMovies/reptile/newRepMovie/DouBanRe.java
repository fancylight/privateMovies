package com.light.privateMovies.reptile.newRepMovie;

import com.light.privateMovies.reptile.core.newCore.ConnectionTarget;
import com.light.privateMovies.reptile.core.newCore.TaskBlock;
import com.light.privateMovies.reptile.core.newCore.data.AbstractDataResult;
import com.light.privateMovies.reptile.core.newCore.data.StepTask;
import com.light.privateMovies.reptile.ja.Result;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Connection;
import org.jsoup.nodes.Element;

import java.io.IOException;

public class DouBanRe extends AbstractDataResult<Result> {
    private String movieName;
    private Logger log = LogManager.getLogger(DouBanRe.class);

    public DouBanRe(ConnectionTarget target, String movieName) {
        super(target);
        this.movieName = movieName;
        addNewStepTask(new IndexMovie());
    }

    //rest1:https://www.douban.com/search?cat=1002&q= 获取电影首页
    class IndexMovie implements StepTask {

        @Override
        public void deal(Connection.Response response) {
            try {
                var doc = response.parse();
                var eles = doc.select("div .title a");
                String nextLink = "";
                for (Element e : eles) {
                    if (e.text().equals(movieName)) {
                        nextLink = e.attr("href");
                        break;
                    }
                }
                if (!nextLink.equals("")) {
                    log.info(movieName);
                    var Con = createConnection(nextLink, 1);
                    addNewStepTask(new realMovie());
                    addNewConnection(Con);
                } else {
                    log.warn("不存在" + movieName);
                    stop();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //表示具体的电影页面
    class realMovie implements StepTask {

        @Override
        public void deal(Connection.Response response) {
            try {
                var doc = response.parse();
            } catch (IOException e) {
                e.printStackTrace();
            }
            stop();
        }
    }
}
