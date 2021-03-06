package com.light.privateMovies.reptile.core;

import com.light.privateMovies.reptile.ja.ConstantPath;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

public class ReptileUtil {
    private static Map<String, String> browerParas;

    static {
        browerParas = new HashMap<>();
        try {
            Properties prop = new Properties();
            prop.load(ReptileUtil.class.getResourceAsStream(ConstantPath.BROWSERPARA));
            prop.entrySet().stream().forEach(t -> browerParas.put((String) t.getKey(), (String) t.getValue()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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
        var pattern = Pattern.compile("([a-z]|[A-Z]){2,5}-?_?[0-9]{3}");
        return getString(name, pattern);
    }

    private static String getString(String name, Pattern pattern) {
        var match = pattern.matcher(name);
        if (match.find()) {
            name = match.group();
            name = name.replaceAll("-", "");
            name = name.replaceAll("_", "");
            name = name.toUpperCase();
        } else
            name = "";
        return name;
    }

    public static String getACode2(String name) {
        name = pathToName(name);
        var pattern = Pattern.compile("([a-z]|[A-Z]){2,5}-?_?[0-9]{2}");
        return getString(name, pattern);
    }

    /**
     * @param target 待处理的目标
     * @param regx   要匹配的正则
     * @return <tt>true</tt>表示目标和regx中一个匹配上
     */
    public static boolean filterTarget(String target, String[] regx) {
        boolean res = false;
        for (String re : regx) {
            if (Pattern.compile(re, Pattern.CASE_INSENSITIVE).matcher(target).find()) {
                res = true;
                break;
            }
        }
        return res;
    }

    /**
     * 返回.后边的后缀,如果没有则返回""
     *
     * @param path
     * @return
     */
    public static String getType(String path) {
        String re = "";
        if (path.lastIndexOf(".") > 0)
            return path.substring(path.lastIndexOf(".") + 1);
        return re;
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

        if (!new File(path + "/" + ConstantPath.COVER).exists())
            return true;
        if (!new File(path + "/" + ConstantPath.DETAIL).exists())
            return true;
        if (!new File(path + "/" + ConstantPath.ACTOR).exists())
            return true;
        return false;
    }

    public static String createActorDir(Set<String> actors) {
        if (actors.size() == 0)
            return "null";
        String path = actors.stream().reduce(new StringBuffer(), (t, u) -> t.append("-").append(u), StringBuffer::append).toString();
        return path.substring(path.indexOf("-") + 1);
    }

    public static String createTitleCodeDir(String code, String title) {
        return code + "-" + title;
    }

    public static Map<String, String> getBrowerParas() {
        return browerParas;
    }

    public static String getLengthTime(String time) {
        String reg = "[0-9]{1,3}";
        Pattern rexp = Pattern.compile(reg);
        var match = rexp.matcher(time);
        if (match.find()) {
            time = match.group();
        } else
            time = "0";
        return time;
    }
}
