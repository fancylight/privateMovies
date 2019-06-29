package com.light.privateMovies.service;

import com.google.gson.JsonObject;
import com.light.privateMovies.constant.MovieFileConstant;
import com.light.privateMovies.dao.ModuleDao;
import com.light.privateMovies.dao.MovieDao;
import com.light.privateMovies.init.SubDeal;
import com.light.privateMovies.pojo.Movie;
import com.light.privateMovies.reptile.core.ReptileUtil;
import com.light.privateMovies.util.FileUtil;
import com.light.privateMovies.util.JsonUtil;
import com.light.privateMovies.util.fileTargetDeal.AbstractFileDeal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Transactional
public class MovieService {
    private MovieDao movieDao;
    private ModuleDao moduleDao;
    private ModuleService moduleService;
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

    public ModuleService getModuleService() {
        return moduleService;
    }

    @Autowired
    public void setModuleService(ModuleService moduleService) {
        this.moduleService = moduleService;
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

    public void setHasBuf(boolean hasBuf) {
        this.hasBuf = hasBuf;
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
        moduleMovies.put(MovieFileConstant.FAVORITE, new ArrayList<>());
        modules.stream().forEach(m -> {
            ArrayList<Movie> list = new ArrayList<>();
            //获取指定模块路径中的数据
            String module = FileUtil.getPathPart(m.getLocalPath(), 0) + "/" + FileUtil.getPathPart(m.getLocalPath(), 1);
            var movies = getMoviesByModuleAll(module);
            moduleMovies.put(m.getModuleName(), list);
            //判断该电影是任然存在
            var target = movies.stream().filter(movie -> {
                if (new File(movie.getLocalPath()).exists()) {
                    //给电影添加上模块名
                    movie.setModuleTypeName(m.getModuleType().getTypeName());
                    movie.setModuleName(m.getModuleName());
                    if (movie.isFavorite())
                        moduleMovies.get(MovieFileConstant.FAVORITE).add(movie);
                    if (!movie.getModuleTypeName().equals("av"))
                        readExtraInfo(movie);
                    return true;
                }
                return false;
            }).collect(Collectors.toList());
            list.addAll(target);
        });
    }

    private void readExtraInfo(Movie movie) {
        String jsonPath = movie.getLocalPath() + "/../" + movie.getMovieName() + MovieFileConstant.EXTRA_INFO_JSON_SUFFIX;
        var json = new File(jsonPath);
        var jsonObject = JsonUtil.jsonFromFile(json);
        if (jsonObject == null) {
            return;
        }
        boolean isFavorite = jsonObject.get(MovieFileConstant.FAVORITE).getAsBoolean();
        movie.setFavorite(isFavorite);
        if (isFavorite)
            moduleMovies.get(MovieFileConstant.FAVORITE).add(movie);
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
        Movie m = getMovieByName(name);
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

    //TODO:由于window文件不区分大小写,因此导致我这里有个bug,要对电影名按照大小写查询一次
    public Movie getMovieByName(String movieName) {
        if (!hasBuf) {
            return movieDao.getMovieByMovieName(movieName);
        }
        List<Movie> movies;
        movies = movieHashMap.get(movieName);
        if (movies == null)
            movies = movieHashMap.get(movieName.toUpperCase());
        if (movies == null)
            movies = movieHashMap.get(movieName.toLowerCase());
        return movies == null ? null : movies.get(0);
    }

    /**
     * 获取数据库中电影
     *
     * @return
     */
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
        var module = m.getModuleName();
        if (!module.equals("")) {
            var list = moduleMovies.get(module).stream().filter(t -> !t.getMovieName().equals(m.getMovieName())).collect(Collectors.toList());
            moduleMovies.put(module, list);
            movieHashMap.remove(movieName);
            //调用数据库并且删除本地
            var modules = moduleDao.getModuleByName(module);
            var mm = modules.get(0);
            boolean simple = m.getModuleTypeName().equals("av") ? false : true;
            movieDao.delete(m, mm.getLocalPath(), simple);
        }
        return movieName;
    }

    /**
     * 该函数弃用
     * 获取模块名称
     *
     * @param movie
     * @return
     */
    @Deprecated
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

    /**
     * 按照模块路径扫描电影,若硬盘存在电影,并在数据库中也存在,则改变数据库中movie#module
     */
    public void movieModuleScanAndDeal(String[] target) {
        var moduleEntries = moduleService.getAllModule();
        moduleEntries.stream().filter(m -> {
            String typeName = m.getModuleType().getTypeName();
            return typeName.equals("av") ? true : false;

        }).forEach(moduleEntry -> {
            FileUtil.scanDir(new AbstractFileDeal() {
                @Override
                public void deal(File file, String[] targetType, String parentPath) {
                    String filePath = file.getPath();
                    if (ReptileUtil.filterTarget(ReptileUtil.getType(filePath), target)) {
                        String diskName = FileUtil.getFileName(file.getName());
                        Movie movie = movieDao.getMovieByMovieName(diskName);
                        if (movie != null) {
                            movie.setModuleEntry(moduleEntry);
                            movieDao.update(movie);
                        }
                    }
                }
            }, moduleEntry.getLocalPath(), target, "");
        });
    }

    /**
     * 设置favorite
     * 1.数据库中存放的类型通过设置数据库就可以
     * 2.没有存放在数据库中的则在文件路径额外添加一个extraInfo.json文件
     *
     * @param name
     * @return
     */
    public boolean setFavorite(String name) {
        var movie = getMovieByName(name);
        if (movie != null) {
            boolean now = !movie.isFavorite();
            movie.setFavorite(now);
            if (movie.getModuleTypeName().equals("av"))
                movieDao.update(movie);
            else
                writeFavoriteJson(movie, now);
            if (!moduleMovies.get(MovieFileConstant.FAVORITE).contains(movie))
                moduleMovies.get(MovieFileConstant.FAVORITE).add(movie);
            if (now == false) {
                moduleMovies.get(MovieFileConstant.FAVORITE).remove(movie);
            }
            return true;
        }
        return false;
    }

    private void writeFavoriteJson(Movie movie, boolean isFavorite) {
        var json = movie.getLocalPath() + "/../" + movie.getMovieName() + MovieFileConstant.EXTRA_INFO_JSON_SUFFIX;
        var file = new File(json);
        if (!file.exists()) {
            try {
                file.createNewFile();
                var jsonObject = new JsonObject();
                jsonObject.addProperty(MovieFileConstant.FAVORITE, isFavorite);
                JsonUtil.write(file, jsonObject);
                return;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        JsonObject jsonObject = JsonUtil.jsonFromFile(file);
        jsonObject.addProperty(MovieFileConstant.FAVORITE, isFavorite);
        JsonUtil.write(file, jsonObject);
    }

    public void setFavorite(Movie movie, boolean isFavorite) {
        if (movie != null) {
            movie.setFavorite(isFavorite);
            movieDao.update(movie);
        }
        moduleMovies.get(MovieFileConstant.FAVORITE).remove(movie);
    }
}
