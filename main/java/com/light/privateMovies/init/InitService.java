package com.light.privateMovies.init;


import com.light.privateMovies.dao.*;
import com.light.privateMovies.pojo.ModuleEntry;
import com.light.privateMovies.pojo.Movie;
import com.light.privateMovies.reptile.ArzonData;
import com.light.privateMovies.reptile.ConstansPath;
import com.light.privateMovies.reptile.ReptileUtil;
import com.light.privateMovies.reptile.Result;
import com.light.privateMovies.service.ModuleService;
import com.light.privateMovies.service.ReService;
import com.light.privateMovies.util.fileTargetDeal.AbstractFileDeal;
import com.light.privateMovies.util.FileUtil;
import com.light.privateMovies.util.fileTargetDeal.RenameAFileDealChain;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class InitService {
    Logger logger = LogManager.getLogger(InitService.class);

    private String[] target;
    private ModuleService moduleService;
    private ReService reService;

    @Autowired
    public void setModuleService(ModuleService moduleService) {
        this.moduleService = moduleService;
    }

    @Autowired
    public void setReService(ReService reService) {
        this.reService = reService;
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
                    if (ReptileUtil.filterTarget(filePath.substring(filePath.lastIndexOf(".") + 1), target))
                        if (type.equals("av")) {
                            filePath = (String) renameAFileDealChain.deal(file, targetType, parentPath);
                            var code = ReptileUtil.getACode(filePath);
                            //1.不存在
                            if (!targetLocal.keySet().contains(code)) {
                                var re = new ArzonData(filePath).getReFromArzon();
                                reService.saveReptileData(re);
                            }//存在
                            else {
                                //查看是否要执行
                                if (ReptileUtil.ifNeedLocal(ReptileUtil.fileToPath(filePath))) {
                                    logger.warn(filePath + "已经存在数据,进行本地转移操作");
                                    Movie movie = reService.getMovieByName(code);
                                    //创建三个目录
                                    Set<String> actors = movie.getActors().stream().map(t -> t.getActor_name()).collect(Collectors.toList()).stream().collect(HashSet::new, HashSet::add, HashSet::addAll);
                                    String actorPath = ReptileUtil.createActorDir(actors);
                                    String moviePath = ReptileUtil.createTitleCodeDir(movie.getMovieName(), movie.getTitle());
                                    String p = parentPath + "/" + actorPath + "/" + moviePath;
                                    String aP = p + "/" + ConstansPath.ACTOR;
                                    String dP = p + "/" + ConstansPath.DETAIL;
                                    String cP = p + "/" + ConstansPath.COVER;
                                    ReptileUtil.createDir(aP);
                                    ReptileUtil.createDir(dP);
                                    ReptileUtil.createDir(cP);
                                    //复制detail
                                    movie.getMovieDetails().stream().forEach(f -> FileUtil.wirteToDest(f.getDetailPic(), dP + "/" + f.getName()));
                                    //复制cover
                                    FileUtil.wirteToDest(movie.getCover(), cP + "/" + movie.getMovieName() + ".jpg");
                                    //复制actor
                                    movie.getActors().stream().forEach(a -> FileUtil.wirteToDest(a.getActor_pic(), aP + "/" + a.getActor_name() + ".jpg"));
                                    String newName = dP + "/../" + file.getName();
                                    //移动
                                    file.renameTo(new File(newName));
                                    movie.setLocalPath(newName);
                                    reService.updateMoviePath(movie);
                                } else {

                                }
                            }
                        } else if (type.equals("movie")) {

                        }
                }
            }, path, target, "");
        });
    }
}
