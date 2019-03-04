package com.light.privateMovies.util;

import com.light.privateMovies.init.InitService;
import com.light.privateMovies.util.fileTargetDeal.AbstractFileDeal;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.Set;
import java.util.stream.Stream;

public class FileUtil {
    static Logger logger = LogManager.getLogger(InitService.class);

    /**
     * 读取本地数据,基于本地绝对地址或classPath
     *
     * @param path
     * @param isClassPath <tt>true</tt>表示从classPath读取,必须以/开头
     * @return
     */
    public static byte[] getFileData(String path, boolean isClassPath) {
        BufferedInputStream in = null;
        if (isClassPath) {
            in = new BufferedInputStream(FileUtil.class.getResourceAsStream(path));
        } else {
            try {
                in = new BufferedInputStream(new FileInputStream(path));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        try {
            return in.readAllBytes();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 遍历文件夹,并对目标进行处理
     *
     * @param fileDealInterface 目标处理接口
     * @param path
     * @param targetType
     * @param parentPath
     */
    public static void scanDir(AbstractFileDeal fileDealInterface, String path, String targetType[], String parentPath) {
        File file = new File(path);
        if (file.isDirectory()) {
            //这里操作就是前序
            fileDealInterface.beforeDir(file, targetType, parentPath);
            var files = file.listFiles();
            Stream.of(files).forEach(t -> scanDir(fileDealInterface, t.getPath(), targetType, file.getPath()));
            //这里操作就是后续
            fileDealInterface.afterDir(file, targetType, parentPath);
        } else if (file.isFile()) {
            fileDealInterface.deal(file, targetType, parentPath);
            //delete 实际上这就两步加起来就是后续便利,有趣的是便利文件夹会产生两种node类型,和二叉树不同
        } else return;
    }

    /**
     * @param fileDealInterface
     * @param path
     * @param targetType
     * @param parentPath
     * @param isDelete          表示是否删除未匹配目标文件夹
     * @return 是否有匹配
     * //TODO: 这样不能实现我要的目的,比如说   xx.avi detail cover ,该函数执行后会将detail cover文件夹也视为不匹配
     */
//    public static boolean scanDir(AbstractFileDeal fileDealInterface, String path, String targetType[], String parentPath, boolean isDelete) {
//        File file = new File(path);
//        if (file.isDirectory()) {
//            var files = file.listFiles();
//            AtomicBoolean hasTarget = new AtomicBoolean(false);
//            Stream.of(files).forEach(t -> {
//                if (!scanDir(fileDealInterface, t.getPath(), targetType, file.getPath(), isDelete)) {
//                    t.delete();
//                } else hasTarget.set(true);
//            });
//            return hasTarget.get();
//        } else if (file.isFile()) {
//            return fileDealInterface.deal(file, targetType, parentPath);
//        }
//        return false;//此时说明文件不存在,自然返回false
//    }

    /**
     * 拷贝文件到目的
     *
     * @param src
     * @param dest
     */
    public static void copyFileToDst(String src, String dest) {
        try {
            var in = new BufferedInputStream(new FileInputStream(src));
            var out = new BufferedOutputStream(new FileOutputStream(dest));
            int len = 0;
            byte[] buf = new byte[1024];
            while ((len = in.read(buf)) != -1) {
                out.write(buf, 0, len);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取in中的字节
     *
     * @param in
     * @return
     */
    public static byte[] getInBytes(InputStream in) {
        var out = new ByteArrayOutputStream(2048);
        int len = 0;
        byte[] buf = new byte[1024];
        var in0 = new BufferedInputStream(in);
        try {
            while ((len = in0.read(buf)) != -1) {
                out.write(buf, 0, len);
                buf = new byte[1024];
            }

            //in0.close(); 这里不能这么关闭
        } catch (IOException e) {
            e.printStackTrace();
        }
        return out.toByteArray();
    }

    /**
     * 写
     *
     * @param srcBytes
     * @param dest
     */
    public static void wirteToDest(byte[] srcBytes, String dest) {
        try (BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(dest))) {
            out.write(srcBytes);
            out.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void deleteDir(String dir, Set<String> modules) {
        logger.warn(dir + "删除");
        String[] temp = dir.replaceAll("\\\\", "/").split("/");
        if (temp.length > 2 && modules.contains(temp[1])) {
            logger.error("要删除模块????????---------退出");
            return;
        }
        scanDir(new AbstractFileDeal() {
            @Override
            public void deal(File file, String[] targetType, String parentPath) {
                file.delete();
                logger.info("删除文件" + file.getName());
            }

            @Override
            public void afterDir(File file, String[] targetType, String parentPath) {
                file.delete();
                logger.info("删除文件夹" + file.getPath());
            }
        }, dir, new String[]{}, "");
    }
}
