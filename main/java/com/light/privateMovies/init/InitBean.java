package com.light.privateMovies.init;


import com.light.privateMovies.pojo.ModuleEntry;
import com.light.privateMovies.reptile.ArzonData;
import com.light.privateMovies.reptile.ReptileUtil;
import com.light.privateMovies.service.ModuleService;
import com.light.privateMovies.util.FileDealInterface;
import com.light.privateMovies.util.FileUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class InitBean {
    Logger logger = LogManager.getLogger(InitBean.class);
    /**
     * 根据模块表去处理不同模块对应本地文件
     * <p>
     * 1.递归遍历处理目标
     * 2.根据module_type 对不同目标获取不同数据
     */
    //init方法
    private ModuleService moduleService;

    @Autowired
    public void setModuleService(ModuleService moduleService) {
        this.moduleService = moduleService;
    }

    void scanInitByModule() {
        //获取到模块指定的所有目标
        logger.info("初始化");
        var list = moduleService.getAllModule();
        String[] target = {"mp4", "mkv", "avi", "wmk", "wmv"};
        scanLocal(list, target);
    }

    private Map<String, String> targetLocal = new HashMap<>();

    /**
     * //[0]  扫描每个目标,匹配目标
     * //[1]  若目标在数据库中存在,则不处理
     * //[2]  若目标在数据库中不存在,则调用爬虫获取数据,并且正确数据存档到数据库
     * //       1.封面,desc-->movie_table   2.detail->detail_table
     * //TODO:处理本地文件的一些建议:
     * 1.对于不存在target的文件夹进行删除,无论内部存在什么数据
     *
     * @param entries
     * @param target
     */
    public void scanLocal(List<ModuleEntry> entries, String[] target) {
        entries.forEach(t -> {
            String path = t.getLocalPath();
            String type = t.getModuleType().getTypeName();
            ArrayList<String> targets = new ArrayList<>();
            FileUtil.scanDir(new FileDealInterface() {
                @Override
                public void deal(File file, String[] targetType, String parentPath) {
                    String filePath = file.getPath();
                    if (ReptileUtil.filterTarget(filePath.substring(filePath.lastIndexOf(".") + 1), target))
                        targets.add(filePath);
                }
            }, path, target, "");
            if (type.equals("av")) {
                targets.stream().forEach(targetPath -> {
                    //TODO:进行爬虫流程的条件是 targetName不存在于movies中;当存在但localpath不一致说名该电影位置发生了改变,此时将数据库中数据迁移到本地,并且更新localpath
                    var re = new ArzonData(targetPath).getReFromArzon();
                });
            } else if (type.equals("movie")) {
                //TODO:处理一般电影
            }
        });

    }
}
