package com.light.privateMovies.util.fileTargetDeal;

import com.light.privateMovies.reptile.ReptileUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

public class RenameAFileDealChain implements FileDealChain {
    Logger logger = LogManager.getLogger(RenameAFileDealChain.class);

    @Override
    public Object deal(File file, String[] targetType, String parentPath) {
        String filePath = ReptileUtil.fileToPath(file.getPath());
        String type = ReptileUtil.getSuffix(file.getName());
        String code = ReptileUtil.getACode(file.getPath());
        String newName = filePath + "/" + code + "." + type;
        logger.warn("重命名" + file.getPath() + "--------" + newName);
        file.renameTo(new File(newName));
        return newName;
    }
}
