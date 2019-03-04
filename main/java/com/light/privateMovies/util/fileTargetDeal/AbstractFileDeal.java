package com.light.privateMovies.util.fileTargetDeal;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractFileDeal {
    private List<FileDealChain> fileDealChains = new ArrayList<>();

    public void addFileChain(FileDealChain chain) {
        fileDealChains.add(chain);
    }

    public void removeChain(FileDealChain chain) {
        fileDealChains.remove(chain);
    }

    protected List<FileDealChain> getFileDealChains() {
        return fileDealChains;
    }

    //afterFile
    public void deal(File file, String[] targetType, String parentPath) {

    }
    //beforeDir
    public void beforeDir(File file, String[] targetType, String parentPath) {

    }

    //afterDir
    public void afterDir(File file, String[] targetType, String parentPath) {

    }
}
