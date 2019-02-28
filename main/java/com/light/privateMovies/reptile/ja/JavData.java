package com.light.privateMovies.reptile.ja;

import com.light.privateMovies.pojo.MovieType;
import com.light.privateMovies.reptile.annotation.Step;
import com.light.privateMovies.reptile.core.Reptile;
import com.light.privateMovies.reptile.core.ReptileUtil;
import com.light.privateMovies.reptile.core.StepMethod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Connection;

import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

/**
 * 目前来看仅仅打算获取类别这一项
 */
@Step(url = "https://www.javbus.com/")
public class JavData {
    private String code;
    private List<MovieType> types = new ArrayList<>();
    private Logger logger = LogManager.getLogger(JavData.class);

    public void setCode(String code) {
        this.code = code;
    }

    class JavStep extends StepMethod {
        public JavStep(String path, String host, String pro, Map<String, String> header, Map<String, String> cookies) {
            super(path, host, pro, header, cookies);
        }

        public JavStep(String path) {
            super(path);
        }

        @Override
        public void deal(Connection.Response response, List<StepMethod> methods, int deep, Reptile reptile) {
            super.deal(response, methods, deep, reptile);
            try {
                var doc = response.parse();
                var title = doc.title();
                var elements = doc.select(".genre a");
                if (title.equals("404 Page Not Found! - JavBus")) {
                    logger.warn("java页面不存在" + code);
                }
                for (var ele : elements) {
                    var t = new MovieType();
                    t.setMovieType(ele.text());
                    types.add(t);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //该网站属于rest风格, 例如:https://www.javbus.com/CESD-721 就是目标页面,此时有个问题要给code中间加一个-
    public JavData(String code) {
        //
        var m = Pattern.compile("^[a-z|A-Z]{3,5}").matcher(code);
        String codePart = "";
        if (m.find())
            codePart = m.group();
        var m2 = Pattern.compile("[0-9]{3,4}$").matcher(code);
        String number = "";
        if (m2.find())
            number = m2.group();
        this.code = codePart + "-" + number;
    }

    public List<MovieType> getType() {
        var step = new JavStep("/" + code, "www.javbus.com", "https", ReptileUtil.getBrowerParas(), new HashMap<>());
        new Reptile(Collections.singletonList(step)).init();
        return types;
    }
}
