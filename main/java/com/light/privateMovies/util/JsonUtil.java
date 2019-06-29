package com.light.privateMovies.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.*;

public class JsonUtil {
    public static JsonObject jsonFromFile(File file) {
        if (!file.exists())
            return null;
        JsonParser parser = new JsonParser();  //创建JSON解析器
        BufferedReader in = null; //设置缓冲区 编码
        JsonObject object = null;
        try {
            in = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"), 50 * 1024 * 1024);
            object = (JsonObject) parser.parse(in);  //创建JsonObject对象
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return object;
    }

    public static void write(File file, JsonObject jsonObject) {
        try {
            FileUtil.wirteToDest(jsonObject.toString().getBytes("UTF-8"), file.getCanonicalPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
