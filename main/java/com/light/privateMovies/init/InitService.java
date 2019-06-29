package com.light.privateMovies.init;


import com.light.privateMovies.pojo.ModuleEntry;
import com.light.privateMovies.pojo.Movie;
import com.light.privateMovies.reptile.ja.ArzonData;
import com.light.privateMovies.reptile.ja.ConstantPath;
import com.light.privateMovies.reptile.core.ReptileUtil;
import com.light.privateMovies.reptile.ja.JavData;
import com.light.privateMovies.reptile.ja.Result;
import com.light.privateMovies.service.ModuleService;
import com.light.privateMovies.service.MovieService;
import com.light.privateMovies.service.ReService;
import com.light.privateMovies.util.fileTargetDeal.AbstractFileDeal;
import com.light.privateMovies.util.FileUtil;
import com.light.privateMovies.util.fileTargetDeal.RenameAFileDealChain;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

public class InitService {
    Logger logger = LogManager.getLogger(InitService.class);

    private String[] target;
    private ModuleService moduleService;
    private ReService reService;
    private MovieService movieService;
    private HashMap<String, String> partMovies;//分p部分
    private List<Movie> unMovies;//无数据部分

    @Autowired
    public void setModuleService(ModuleService moduleService) {
        this.moduleService = moduleService;
    }

    @Autowired
    public void setReService(ReService reService) {
        this.reService = reService;
    }

    @Autowired
    public void setMovieService(MovieService movieService) {
        this.movieService = movieService;
    }

    /**
     * 根据模块表去处理不同模块对应本地文件
     * <p>
     * 1.递归遍历处理目标
     * 2.根据module_type 对不同目标获取不同数据
     */
    //init方法
    void scanInitByModule() {
        //获取到模块指定的所有目标
        logger.info("初始化");
        long t0 = System.currentTimeMillis();
        var list = moduleService.getAllModule();
        target = new String[]{"mp4", "mkv", "avi", "wmk", "wmv"};
        //先执行一次缓冲
        logger.info("初始部分缓冲");
        movieService.getAllMovies();
        scanLocal(list);
        logger.info("前台缓冲");
        movieService.movieModuleScanAndDeal(target);
        //重新缓冲
        movieService.setHasBuf(false);
        //处理电影模块分类
        movieService.getAllMovies();
        //添加无码部分
        unMovies.stream().forEach(movie -> movieService.addMovieToCache(movie));
        //执行前台缓冲
        movieService.moduleMoviesCache();
        //处理分p部分
        partListDeal();
        //手动释放
        target = null;
        partMovies = null;
        logger.info("初始化时间:" + (System.currentTimeMillis() - t0) + "ms");
    }

    private void partListDeal() {
        if (partMovies == null) {
            logger.warn("不存在分p");
            return;
        } else {
            partMovies.keySet().stream().forEach(partMovie -> {
                String name = ReptileUtil.pathToName(partMovie);
                name = name.substring(0, name.indexOf("part"));
                //找到匹配的电影
                String finalName = name;
                movieService.getAllMovies().stream().anyMatch(movie -> {
                    if (movie.getMovieName().equals(finalName)) {
                        //移动
                        String src = partMovies.get(partMovie);
                        String dest = movie.getLocalPath() + "/../" + partMovie;
                        new File(src).renameTo(new File(dest));
                        logger.warn("存在分p移动:" + src + "-------->" + dest);
                        var list = movieService.getPartMovies().get(movie.getMovieName());
                        if (list == null) {
                            List<String> l = new ArrayList<>();
                            l.add(partMovie);
                            movieService.getPartMovies().put(movie.getMovieName(), l);
                        } else
                            movieService.getPartMovies().get(movie.getMovieName()).add(partMovie);
                        return true;
                    }
                    return false;
                });
            });
        }
    }

    /**
     * //[0]  扫描每个目标,匹配目标
     * //[1]  若目标在数据库中存在,则不处理
     * //[2]  若目标在数据库中不存在,则调用爬虫获取数据,并且正确数据存档到数据库
     * //       1.封面,desc-->movie_table   2.detail->detail_table
     * //TODO:存在内部类调用
     *
     * @param entries
     */
    public void scanLocal(List<ModuleEntry> entries) {
        Map<String, List<Movie>> targetLocal = reService.getMovieAndPath();
        RenameAFileDealChain renameAFileDealChain = new RenameAFileDealChain();
        entries.forEach(t -> {
            String path = t.getLocalPath();
            String type = t.getModuleType().getTypeName();
            FileUtil.scanDir(new AbstractFileDeal() {
                @Override
                public void deal(File file, String[] targetType, String parentPath) {
                    String filePath = file.getPath();

                    if (ReptileUtil.filterTarget(ReptileUtil.getType(filePath), target)) {
                        if (!partMovie(file, type))
                            targetDeal(file, targetType, parentPath, type, renameAFileDealChain, targetLocal);
                    }

                }
            }, path, target, "");
        });
        //收集task完毕执行线程
        CountDownLatch latch = new CountDownLatch(aInitTasks.size());
        aInitTasks.parallelStream().forEach(t -> {
            t.task();
            latch.countDown();
        });
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param file
     * @return <tt>true</tt>表示该文件应该被跳过后续类型处理
     */
    private boolean partMovie(File file, String type) {
        String name = file.getName(); //name是带有.type后缀的
        //todo:可以改成配置形式,也就是过滤后缀为xxx的文件,并在文件收集之后做进一步处理
        String part = name.contains("part") ? name.substring(name.indexOf("part")) : "";
        if (part.equals(""))
            return false;
        else {
            if (partMovies == null)
                partMovies = new HashMap<>();
            partMovies.put(file.getName(), file.getPath());
            return true;
        }
    }

    /**
     * 处理匹配成功的文件
     *
     * @param file                 文件名
     * @param targetType           匹配类型
     * @param parentPath           父路径
     * @param type                 文件类型
     * @param renameAFileDealChain 重命名器
     * @param targetLocal          本地路径
     */
    private void targetDeal(File file, String[] targetType, String parentPath, String type, RenameAFileDealChain renameAFileDealChain, Map<String, List<Movie>> targetLocal) {
        String filePath;
        if (type.equals("av")) {
            //调用重命名
            filePath = (String) renameAFileDealChain.deal(file, targetType, parentPath);
            var code = ReptileUtil.getACode(filePath);
            if (!code.equals("")) {
                //1.不存在
                if (!targetLocal.keySet().contains(code)) {
                    //获取arzon数据
                    aInitTasks.add(new AInitTask(code, filePath));
                }//存在
                else {
                    //查看是否要执行
                    if (ReptileUtil.ifNeedLocal(ReptileUtil.fileToPath(filePath))) {
                        dealIfExist(file, parentPath, filePath, code);
                    }//todo:收集完数据应该对数据库中movie做一次检测,如果对应localPath不存在数据,则标记为本地移除,这要给movie加一个字段
                    else {
                        //存在数据并且不需要重新创建本地,判断是否本地文件如进行过转码操作, .mkw->mp4,会造成localPath改变
                        var movie = movieService.getMovieByName(code);
                        try {
                            String abPath = new File(movie.getLocalPath()).getCanonicalPath();
                            String path = file.getCanonicalPath();
                            if (!abPath.equals(path)) {
                                //更新路径
                                logger.info("可能转码,更新路径:" + filePath);
                                String newPath = parentPath + "/" + ConstantPath.DETAIL + "/../" + file.getName();
                                newPath = newPath.replaceAll("\\\\", "/");
                                movie.setLocalPath(newPath);
                                reService.updateMoviePath(movie);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            } else {
                logger.warn(filePath + "不是一个番号");
            }
            //假设从豆瓣拿取数据
        } else if (type.equals("电影")) {

        } else if (type.equals("无码") || type.equals("动画") || type.equals("欧美")) {
            //去除该文件的所有空格 以及除了最后一个.

            if (unMovies != null) {
                String name = file.getName();
                unMovies.add(Movie.CreateNMovie(file));
            } else {
                unMovies = new ArrayList<>();
                unMovies.add(Movie.CreateNMovie(file));
            }
        }
    }


    /**
     * 处理当目标已经存在于数据库中
     *
     * @param file
     * @param parentPath
     * @param filePath
     * @param code
     */
    private void dealIfExist(File file, String parentPath, String filePath, String code) {
        logger.error(filePath + "已经存在数据,进行本地转移操作");
        Movie movie = reService.getMovieByName(code);
        //创建三个目录
        Set<String> actors = movie.getActors().stream().map(t -> t.getActor_name()).collect(Collectors.toList()).stream().collect(HashSet::new, HashSet::add, HashSet::addAll);
        String actorPath = ReptileUtil.createActorDir(actors);
        String moviePath = ReptileUtil.createTitleCodeDir(movie.getMovieName(), movie.getTitle());
        String p = ReptileUtil.dealDouble(parentPath) + "/" + actorPath + "/" + moviePath;
        String aP = p + "/" + ConstantPath.ACTOR;
        String dP = p + "/" + ConstantPath.DETAIL;
        String cP = p + "/" + ConstantPath.COVER;
        ReptileUtil.createDir(aP);
        ReptileUtil.createDir(dP);
        ReptileUtil.createDir(cP);
        //复制detail
        movie.getMovieDetails().stream().forEach(f -> FileUtil.wirteToDest(f.getDetailPic(), dP + "/" + f.getName() + ".jpg"));
        //复制cover
        FileUtil.wirteToDest(movie.getCover(), cP + "/" + movie.getMovieName() + ".jpg");
        //复制actor
        movie.getActors().stream().forEach(a -> FileUtil.wirteToDest(a.getActor_pic(), aP + "/" + a.getActor_name() + ".jpg"));
        String newName = dP + "/../" + file.getName();
        //移动
        file.renameTo(new File(newName));
        movie.setLocalPath(newName);
        reService.updateMoviePath(movie);
    }

    private List<AInitTask> aInitTasks = new ArrayList<>();

    /**
     * 爬取AV数据的任务队列
     */
    class AInitTask {
        private String code;
        private String filePath;

        public AInitTask(String code, String filePath) {
            this.code = code;
            this.filePath = filePath;
        }

        public void task() {
            //todo:该网站还有一些bug,不如说搜索jbs-008 他会有两个结果第一个为NJBS-008,第二个为jbs-008
            var re = new ArzonData(filePath).getReFromArzon();
            if (re != null) {
                //获取jav数据
                logger.error("在aron处理了" + re.getMovie().getLocalPath());
                insertData(re);
            } else {
//                logger.error("aron不存在" + code + "此影片不处理");
                logger.error("aron不存在" + code + "jav处理");
                //到jav获取数据
                var result = new JavData(code, filePath).getResult();
                if (result != null) {
                    insertData(result);
                } else
                    logger.warn(code + "在aron和jav都没有");
            }
        }

        private void insertData(Result re) {
            //fixme: 此处有未处理的逻辑,如果此处返回的类型为空,也就是说jav网站没有获取到数据,此处会造成空指针异常,这里异常事物我没有做处理
            var types = new JavData(re.getMovie().getMovieName()).getType();
            reService.addTypeList(types);
            re.getMovie().setCreateTime(LocalDateTime.now());
            re.getMovie().setMovieTypes(types);
            reService.saveReptileData(re);
        }
    }

    /**
     * 获取正常电影数据
     */
    class MInitTask extends AInitTask {

        public MInitTask(String code, String filePath) {
            super(code, filePath);
        }

        @Override
        public void task() {
        }
    }

}
