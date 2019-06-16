package com.light.privateMovies.reptile.newRepMovie;

import com.light.privateMovies.pojo.Movie;
import com.light.privateMovies.reptile.core.ReptileUtil;
import com.light.privateMovies.reptile.core.newCore.data.AbstractDataResult;
import com.light.privateMovies.reptile.core.newCore.data.StepTask;
import com.light.privateMovies.reptile.ja.Result;
import com.light.privateMovies.util.FileUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Connection;

import java.io.File;
import java.io.IOException;

public class Europe extends AbstractDataResult<Result> {
    private byte[] buf;
    private String path;
    private String movieName;
    private String rootPath;
    private Logger logger = LogManager.getLogger(Europe.class);
    private boolean state = true;

    public Europe(String movieName, String path, String rootPath) {
        //固定url,可以作为配置文件
        String url = "https://www.hotmovies.com/search.php?words=" + movieName + "&hdnNavigationType=3&searchtype_value=";
        var con = createConnection(url, 0);
        con.getHeaders().put("host", "www.hotmovies.com");
        addNewStepTask(new IndexTask());
        this.target = con;
        this.path = path;
        this.rootPath = rootPath;
    }

    @Override
    public Result getData() {
        if (!state)
            return null;
        Movie movie = new Movie();
        var re = new Result(null, movie, null);
        new File(rootPath + "/" + movieName).mkdir();
        String type = ReptileUtil.getType(path);
        String newPath = rootPath + "/" + movieName + "/" + movieName + "." + type;
        new File(path).renameTo(new File(newPath));
        movie.setMovieName(movieName);
        movie.setCover(buf);
        return re;
    }

    //主页获取
    class IndexTask implements StepTask {

        @Override
        public void deal(Connection.Response response) {
            try {
                var doc = response.parse();
                var a = doc.select(".movie_list .title a").first();
                if (a == null) {
                    logger.info("不存在" + response.url());
                    stop();
                    return;
                }
                movieName = a.text();
                var newLink = a.attr("href");
                System.out.println("匹配的" + movieName);
                //创建新的链接
                var con = createConnection(newLink, 1);
                addNewStepTask(new MainPic());
                addNewConnection(con);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //进入并下载
    class MainPic implements StepTask {

        @Override
        public void deal(Connection.Response response) {
            try {
                var doc = response.parse();
                var cover = doc.select(".large_cover img").first();
                var newLink = cover.attr("src");
                addNewStepTask(new picDown());
                addNewConnection(createConnection(newLink, 2));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //下载图片
    class picDown implements StepTask {

        @Override
        public void deal(Connection.Response response) {
            buf = FileUtil.getInBytes(response.bodyStream());
            stop();
        }
    }
}
