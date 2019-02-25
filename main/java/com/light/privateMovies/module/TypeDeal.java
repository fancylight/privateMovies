package com.light.privateMovies.module;

import java.util.regex.Pattern;

/**
 * 关于不同类型可能出现的静态函数
 */
public class TypeDeal {
    /**
     * 根据输入获取code
     *
     * @param input
     * @return
     */
    public static String getACode(String input) {
        String regex = "[a-z|A-Z]{3,4}[-]?[0-9]{3}";
        var match = Pattern.compile(regex).matcher(input);
        if (match.find())
            return match.group();
        return input;
    }
}
