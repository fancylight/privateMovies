package com.light.privateMovies.service;

import com.light.privateMovies.dao.ModuleDao;
import com.light.privateMovies.dao.ModuleTypeDao;
import com.light.privateMovies.pojo.ModuleEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 关于电影模块处理
 * 1.添加新的模组
 * 2.改变模组类型和名称
 */
@Service
@Transactional
public class ModuleService {
    private ModuleDao moduleDao;
    private ModuleTypeDao moduleTypeDao;

    @Autowired
    public void setModuleTypeDao(ModuleTypeDao moduleTypeDao) {
        this.moduleTypeDao = moduleTypeDao;
    }

    @Autowired
    public void setModuleDao(ModuleDao moduleDao) {
        this.moduleDao = moduleDao;
    }

    public List<ModuleEntry> getAllModule() {
        return moduleDao.getAll();
    }

    public String addNewModule(ModuleEntry entry) {
        if (entry.getModuleType() == null)
            return "不存在对应的模块类型";
        moduleDao.add(entry);
        return "添加entry正常";
    }

    public String updateModule(ModuleEntry entry) {
        if (entry.getModuleType() == null)
            return "不存在对应的模块类型";
        moduleDao.update(entry);
        return "更新entry正确";
    }

    public void changeModule(String newEntryName, String type) {
        var entry = new ModuleEntry();
        entry.setModuleName(newEntryName);
        var list = moduleTypeDao.getAll();
        var re = list.stream().filter(t -> t.getTypeName().equals(type)).findFirst().get();
        if (re != null) {
            entry.setModuleType(re);
            //TODO 修改为日志
            System.out.println(updateModule(entry));
        } else {
            System.out.println("不存在" + type);
        }
    }

    public List<ModuleEntry> getModuleByName(String name) {
        return moduleDao.getModuleByName(name);
    }
}
