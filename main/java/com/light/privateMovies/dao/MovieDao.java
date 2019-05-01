package com.light.privateMovies.dao;

import com.light.privateMovies.dao.base.LightBaseDao;
import com.light.privateMovies.pojo.Movie;

import com.light.privateMovies.util.FileUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate5.HibernateTemplate;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class MovieDao extends LightBaseDao<Movie> {
    public MovieDao() {
        this.clazz = Movie.class;
    }

    @Autowired
    public void setHibernateTemplate(HibernateTemplate hibernateTemplate) {
        this.hibernateTemplate = hibernateTemplate;
    }

    /**
     * @param name 电影名
     * @return 电影
     */
    public Movie getMovieByMovieName(String name) {
        var m = new Movie();
        m.setMovieName(name);
        var list = getListByExample(m);
        if (list.size() > 0)
            return list.get(0);
        m = null;
        return m;
    }

    /**
     * 仅仅获取电影名和path
     *
     * @return
     */
    public Map<String, List<Movie>> getNameAndPath() {
        var list = getPartData(new String[]{"movieName", "localPath"});
        return list.stream().collect(Collectors.groupingBy(Movie::getMovieName));
    }

    //todo:删除操作要分情况,典型的文件夹应该是   模块/演员/作品名/作品,要排除不是这种情况
    //解决方法1:将暴露在模块下的电影创建一个同名文件夹
    public void delete(Movie movie, String modulePath) {
        super.delete(movie);
        //删除本地文件
        try {
            String targetPath=new File(movie.getLocalPath()).getCanonicalPath();
            String dirPath=new File(targetPath+"/..").getCanonicalPath();
            FileUtil.deleteDir(dirPath, targetPath, modulePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
