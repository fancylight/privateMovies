package com.light.privateMovies.reptile;

import java.io.File;
import java.util.Set;
import java.util.regex.Pattern;

public class ReptileUtil {
    /**
     * 处理如//-->/这些
     */
    public static String dealDouble(String path) {
        //此处要使用\\ \\ 首先,对于java编译器来说将这两个\\都转义为\符号,然后按照regex引擎解析,regex识别到\\意思是识别一个\符号
        //因此要在java中匹配\ 要这么写
        path = path.replaceAll("\\\\", "/");
        path = path.replaceAll("//", "/");
        return path;
    }

    /**
     * @param path 截取path路径中表示文件名的部分
     * @return
     */
    public static String pathToName(String path) {
        path = dealDouble(path);
        if (path.contains("/"))
            path = path.substring(path.lastIndexOf("/") + 1);
        if (path.contains("."))
            path = path.substring(0, path.indexOf("."));
        return path;
    }

    /**
     * @param file
     * @return
     */
    public static String fileToPath(String file) {
        file = dealDouble(file);
        return file.substring(0, file.lastIndexOf("/"));
    }

    /**
     * 创建文件夹
     *
     * @param path
     */
    public static void createDir(String path) {
        var f = new File(path);
        if (!f.exists())
            f.mkdirs();
    }

    /**
     * 目的将 传入进的name,如果能够匹配就必须转换成
     * 大写+数字
     *
     * @param name
     * @return
     */
    public static String getACode(String name) {
        name = pathToName(name);
        var match = Pattern.compile("([a-z]|[A-Z]){3,5}-?_?[0-9]{3}").matcher(name);
        if (match.find()) {
            name = match.group();
            name = name.replaceAll("-", "");
            name = name.replaceAll("_", "");
            name = name.toUpperCase();
        }
        return name;
    }

    /**
     * @param target 待处理的目标
     * @param regx   要匹配的正则
     * @return <tt>true</tt>表示目标和regx中一个匹配上
     */
    public static boolean filterTarget(String target, String[] regx) {
        for (String re : regx) {
            if (Pattern.compile(re).matcher(target).find())
                return true;
        }
        return false;
    }

    public static String getSuffix(String fileName) {
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }

    /**
     * 判断当前目录是否存在正确的文件夹
     *
     * @param path 应该是一个目录
     * @return
     */
    public static boolean ifNeedLocal(String path) {

        if (!new File(path + "/" + ConstansPath.COVER).exists())
            return true;
        if (!new File(path + "/" + ConstansPath.DETAIL).exists())
            return true;
        if (!new File(path + "/" + ConstansPath.ACTOR).exists())
            return true;
        return false;
    }

    public static String createActorDir(Set<String> actors) {
        String path = actors.stream().reduce(new StringBuffer(), (t, u) -> t.append("-").append(u), StringBuffer::append).toString();
        return path.substring(path.indexOf("-")+1);
    }

    public static String createTitleCodeDir(String code, String title) {
        return code + "-" + title;
    }
}
