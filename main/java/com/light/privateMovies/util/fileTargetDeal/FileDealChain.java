package com.light.privateMovies.util.fileTargetDeal;

import java.io.File;

/**
 * 遍历处理链
 */
public interface FileDealChain {
    public Object deal(File file, String[] targetType, String parentPath);
}
