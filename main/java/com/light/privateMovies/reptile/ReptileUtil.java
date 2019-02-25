package com.light.privateMovies.reptile;

import java.io.File;
import java.util.regex.Pattern;

public class ReptileUtil {
    /**
     * 处理如//-->/这些
     */
    public static String dealDouble(String path) {
        path = path.replaceAll("//", "/");
        //此处要使用\\ \\ 首先,对于java编译器来说将这两个\\都转义为\符号,然后按照regex引擎解析,regex识别到\\意思是识别一个\符号
        //因此要在java中匹配\ 要这么写
        path = path.replaceAll("\\\\", "/");
        return path;
    }

    public static String pathToCode(String path) {
        path = dealDouble(path);
        path = path.substring(path.lastIndexOf("/") + 1);
        path = path.substring(0, path.indexOf("."));
        return path;
    }

    public static void createDir(String path) {
        var f = new File(path);
        if (!f.exists())
            f.mkdirs();
    }

    public static boolean filterTarget(String target, String[] regx) {
        for (String re : regx) {
            if (Pattern.compile(re).matcher(target).find())
                return true;
        }
        return false;
    }
}
