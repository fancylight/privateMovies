package com.light.privateMovies.pojo;

import javax.persistence.*;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "modules_type_table", uniqueConstraints = {@UniqueConstraint(columnNames = "type_name")})
public class ModuleType {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    @Column(name = "type_name",columnDefinition = "varchar(30)")
    private String typeName;
    @OneToMany(mappedBy = "moduleType") //表示交由维护的属性
            List<ModuleEntry> moduleEntries;

    public List<ModuleEntry> getModuleEntries() {
        return moduleEntries;
    }

    public void setModuleEntries(List<ModuleEntry> moduleEntries) {
        this.moduleEntries = moduleEntries;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public ModuleType(String typeName, List<ModuleEntry> moduleEntries) {
        this.typeName = typeName;
        this.moduleEntries = moduleEntries;
    }

    @Override
    public String toString() {
        return "ModuleType{" +
                "id=" + id +
                ", typeName='" + typeName + '\'' +
                '}';
    }

    public ModuleType() {
    }

    public void setSimpleModuleEntry(ModuleEntry m) {
        setModuleEntries(Collections.singletonList(m));
    }
}
