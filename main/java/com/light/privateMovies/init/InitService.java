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
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

@Service
public class InitService {
    Logger logger = LogManager.getLogger(InitService.class);

    private String[] target;
    private ModuleService moduleService;
    private ReService reService;
    private MovieService movieService;

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
        var list = moduleService.getAllModule();
        target = new String[]{"mp4", "mkv", "avi", "wmk", "wmv"};
        scanLocal(list);
        //执行前台缓冲
        logger.info("前台缓冲");
        movieService.moduleMoviesChache();
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
                    //调用重命名
                    if (ReptileUtil.filterTarget(ReptileUtil.getType(filePath), target))
                        if (type.equals("av")) {
                            //todo:处理字幕文件
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
                                        logger.error(filePath + "已经存在数据,进行本地转移操作");
                                        Movie movie = reService.getMovieByName(code);
                                        //创建三个目录
                                        Set<String> actors = movie.getActors().stream().map(t -> t.getActor_name()).collect(Collectors.toList()).stream().collect(HashSet::new, HashSet::add, HashSet::addAll);
                                        String actorPath = ReptileUtil.createActorDir(actors);
                                        String moviePath = ReptileUtil.createTitleCodeDir(movie.getMovieName(), movie.getTitle());
                                        String p = parentPath + "/" + actorPath + "/" + moviePath;
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
                                    }//todo:收集完数据应该对数据库中movie做一次检测,如果对应localPath不存在数据,则标记为本地移除,这要给movie加一个字段
                                    else {
                                        //数据库存在,并且本地正确
                                    }
                                }
                            } else {
                                logger.warn(filePath + "不是一个番号");
                            }
                        } else if (type.equals("movie")) {

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

    private List<AInitTask> aInitTasks = new ArrayList<>();

    class AInitTask {
        private String code;
        private String filePath;

        public AInitTask(String code, String filePath) {
            this.code = code;
            this.filePath = filePath;
        }

        public void task() {
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
            var types = new JavData(re.getMovie().getMovieName()).getType();
            reService.addTypeList(types);
            re.getMovie().setCreateTime(LocalDateTime.now());
            re.getMovie().setMovieTypes(types);
            reService.saveReptileData(re);
        }
    }
}
