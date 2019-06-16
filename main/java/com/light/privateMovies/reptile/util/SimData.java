package com.light.privateMovies.reptile.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

//http 请求模拟浏览器 头
public class SimData {
    private static Logger logger = LogManager.getLogger(SimData.class);

    public static Map<String, String> getHeader() {
        var pro = new Properties();
        var hashMap = new HashMap<String, String>();
        try {
            pro.load(SimData.class.getResourceAsStream("/config/browserHeader.properties"));
        } catch (IOException e) {
            logger.warn("header配置文件不存在");
            e.printStackTrace();
        }
        for (var entry : pro.entrySet()) {
            hashMap.put((String) entry.getKey(), (String) entry.getValue());
        }
        return hashMap;
    }
}
