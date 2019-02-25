package com.light.privateMovies.pojo;

import javax.persistence.*;

@Entity
@Table(name = "modules_table",uniqueConstraints = {@UniqueConstraint(columnNames = "module_name")})
public class ModuleEntry {
    @Id
    @GeneratedValue
    private Integer id;
    @Column(name = "module_name", unique = true, columnDefinition = "varchar(20)")
    private String moduleName;
    @Column(name = "localPath", columnDefinition = "varchar(30)")
    private String localPath;
    @ManyToOne
    @JoinColumn(name = "module_type_id")
    private ModuleType moduleType;
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public String getLocalPath() {
        return localPath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }

    public ModuleType getModuleType() {
        return moduleType;
    }

    public void setModuleType(ModuleType moduleType) {
        this.moduleType = moduleType;
    }

    public ModuleEntry(String moduleName, String localPath, ModuleType moduleType) {
        this.moduleName = moduleName;
        this.localPath = localPath;
        this.moduleType = moduleType;
    }

    public ModuleEntry() {
    }
    public void setSimpleModuleType(ModuleType moduleType){
        setModuleType(moduleType);
    }
}