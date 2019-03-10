package com.light.privateMovies.service;

import com.light.privateMovies.dao.ModuleDao;
import com.light.privateMovies.dao.MovieDao;
import com.light.privateMovies.init.SubDeal;
import com.light.privateMovies.pojo.Movie;
import com.light.privateMovies.reptile.core.ReptileUtil;
import com.light.privateMovies.util.FileUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Transactional
public class MovieService {
    private MovieDao movieDao;
    private ModuleDao moduleDao;
    private boolean hasBuf = false;
    //缓冲模块对应本地实际包含的电影
    private HashMap<String, List<Movie>> moduleMovies = new HashMap<>();
    private SubDeal subDeal;
    //分p信息
    private HashMap<String, List<String>> partMovies = new HashMap<>();
    //电影名-电影
    private Map<String, List<Movie>> movieHashMap = new HashMap<>();

    public HashMap<String, List<String>> getPartMovies() {
        return partMovies;
    }


    public void setPartMovies(HashMap<String, List<String>> partMovies) {
        this.partMovies = partMovies;
    }

    @Autowired
    public void setMovieDao(MovieDao movieDao) {
        this.movieDao = movieDao;
    }

    @Autowired
    public void setModuleDao(ModuleDao moduleDao) {
        this.moduleDao = moduleDao;
    }


    @Autowired
    public void setSubDeal(SubDeal subDeal) {
        this.subDeal = subDeal;
    }

    public Map<String, List<Movie>> getMovieHashMap() {
        return movieHashMap;
    }

    //返回字幕位置,但是不包括盘符,否则400
    public List<String> getSubs(String movieName) {
        var list = subDeal.getSubPath(movieName);
        return list == null ? null : list.stream().map(t -> {
            String path = t.replaceAll("\\\\", "/");
            return path.substring(path.indexOf("/"));
            //todo:因此资源中srt实际都是vtt 类型,因此就返回vtt类型,即使是srt也返回vtt
        }).filter(t -> {
            String type = ReptileUtil.getType(t);
            return type.equals("vtt") || type.equals("srt");
        }).map(t -> t.substring(0, t.lastIndexOf(".") + 1) + "vtt").collect(Collectors.toList());
    }

    public void addMovieToCache(Movie movie) {
        if (movieHashMap != null) {
            movieHashMap.put(movie.getMovieName(), Stream.of(movie).collect(Collectors.toList()));
        }

    }

    /**
     * 该函数表示缓冲,被initService调用
     */
    public void moduleMoviesCache() {
        //todo:由于我打算将无码部分的不放进数据库中,因此,在取完数据中真正存在的信息后,
        //手动添加无码部分,因此将获取movies和拼接模块-movies分离了
//        movieHashMap = movieDao.getAll().stream().collect(Collectors.groupingBy(Movie::getMovieName));
//        hasBuf = true;
        var modules = moduleDao.getAll();
        modules.stream().forEach(m -> {
            ArrayList<Movie> list = new ArrayList<>();
            //获取指定模块路径中的数据
            var movies = getMoviesByModuleAll(FileUtil.getPathPart(m.getLocalPath(), 1));
            moduleMovies.put(m.getModuleName(), list);
            //判断该电影是任然存在
            var target = movies.stream().filter(movie -> {
                if (new File(movie.getLocalPath()).exists()) {
                    return true;
                }
                return false;
            }).collect(Collectors.toList());
            list.addAll(target);
        });
    }

    /**
     * 不经过查看本地
     *
     * @param moudlePath
     * @return
     */
    public List<Movie> getMoviesByModuleAll(String moudlePath) {
        return movieHashMap.values().stream().flatMap(t -> t.stream()).filter(t -> t.getLocalPath().contains(moudlePath)).collect(Collectors.toList());
    }

    /**
     * 获取电影实际位置
     * //电影名称要么是 xxx 要么是xxxparty
     *
     * @param name
     * @return 如果为null说明电影不存在
     */
    public String getRealPathByName(String name) {
        boolean isPart = false;
        var para = name;
        if (name.contains("part")) {
            name = name.substring(0, name.indexOf("part"));
            isPart = true;
        }
        Movie m = null;
        if (hasBuf) {
            m = movieHashMap.containsKey(name) ? movieHashMap.get(name).get(0) : null;
        } else
            m = movieDao.getMovieByMovieName(name);
        if (m != null) {
            if (!isPart)
                return m.getLocalPath();
            else {
                return m.getLocalPath() + "/../" + para;
            }
        } else
            return "";
    }

    /**
     * 查看本地存在的模块电影
     *
     * @param modulePath
     * @return
     */
    public List<Movie> getMoviesByModule(String modulePath) {
//        if (!hasBuf) {
//            movieHashMap = movieDao.getAll().stream().collect(Collectors.groupingBy(Movie::getMovieName));
//            hasBuf = true;
//        }

        return moduleMovies.get(modulePath).stream().collect(Collectors.toList());
    }

    public Movie getMovieByName(String movieName) {
        if (!hasBuf) {
            return movieDao.getMovieByMovieName(movieName);
        }
        return movieHashMap.get(movieName).get(0);
    }

    //缓存并获取所有movie list
    public List<Movie> getAllMovies() {
        if (!hasBuf) {
            movieHashMap = movieDao.getAll().stream().collect(Collectors.groupingBy(Movie::getMovieName));
            hasBuf = true;
        }
        return movieHashMap.values().stream().flatMap(t -> t.stream()).collect(Collectors.toList());
    }

    /**
     * 删除电影
     *
     * @param movieName
     * @return
     */
    public String deleteMovie(String movieName) {
        var m = movieHashMap.get(movieName).get(0);
        var module = getModuleName(m);
        if (!module.equals("")) {
            var list = moduleMovies.get(module).stream().filter(t -> !t.getMovieName().equals(m.getMovieName())).collect(Collectors.toList());
            moduleMovies.put(module, list);
            movieHashMap.remove(movieName);
            //调用数据库并且删除本地
            movieDao.delete(m, moduleMovies.keySet());
        }
        return movieName;
    }

    /**
     * //todo:这里的处理都是我没有在movie中添加module id字段造成的
     * 获取模块名称
     *
     * @param movie
     * @return
     */
    private String getModuleName(Movie movie) {
        if (movie != null)
            return movie.getLocalPath().split("/")[1];
        return "";
    }

    public void update(Movie movie) {
        movieDao.update(movie);
    }

    /**
     * 遍历所有电影,删选符合信息的电影
     *
     * @param keyWord
     * @return
     */
    public List<Movie> getMoviesByKeyWord(String keyWord) {
        return getAllMovies().stream().filter(movie -> movie.getMovieName().contains(keyWord)
                || movie.getMovieTypes().stream().anyMatch(type -> type.getMovieType().contains(keyWord))
                || movie.getActors().stream().anyMatch(actor -> actor.getActor_name().contains(keyWord))).collect(Collectors.toList());
    }

    public List<String> getPartList(String movieName) {
        return this.partMovies.get(movieName);
    }
}
