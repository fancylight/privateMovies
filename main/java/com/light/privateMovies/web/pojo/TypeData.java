package com.light.privateMovies.web.pojo;

import java.util.List;

public class TypeData {
    private List<String> types;

    public List<String> getTypes() {
        return types;
    }

    public void setTypes(List<String> types) {
        this.types = types;
    }

    public TypeData(List<String> types) {
        this.types = types;
    }
}
