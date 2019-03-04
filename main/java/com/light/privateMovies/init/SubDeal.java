package com.light.privateMovies.init;

import com.light.privateMovies.reptile.core.ReptileUtil;
import com.light.privateMovies.util.FileUtil;
import com.light.privateMovies.util.fileTargetDeal.AbstractFileDeal;
import info.monitorenter.cpdetector.io.ASCIIDetector;
import info.monitorenter.cpdetector.io.CodepageDetectorProxy;
import info.monitorenter.cpdetector.io.JChardetFacade;
import info.monitorenter.cpdetector.io.UnicodeDetector;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 处理字幕文件
 */
public class SubDeal {
    public static final String DISK = "F:/";
    private String SubDir = "F:/资源/";
    private HashMap<String, List<String>> subMap = new HashMap<>();
    private String[] targets = new String[]{"ass", "ssa", "srt", "vtt"};
    private Set<String> t = new HashSet<>();
    private String sp = "分割字幕";
    private Logger logger = LogManager.getLogger(this.getClass());

    public SubDeal() {
        init();
    }

    private void init() {
        FileUtil.scanDir(new AbstractFileDeal() {
            @Override
            public void deal(File file, String[] targetType, String parentPath) {
                if (ReptileUtil.filterTarget(file.getName(), targets)) {
                    String code = ReptileUtil.getACode(file.getPath());
                    //todo:有可能出现如 cherd-66 数字不是三位的情况
                    if (code.equals(""))
                        code = ReptileUtil.getACode2(file.getPath());
                    if (subMap.containsKey(code)) {
                        subMap.get(code).add(file.getPath());
                        SubToVtt(file);
                    } else {
                        subMap.put(code, new ArrayList<>());
                        SubToVtt(file);
                    }
                }
            }
        }, SubDir, targets, "");
        logger.info("共有" + subMap.size() + "字幕");
    }

    public List<String> getSubPath(String movieName) {
        return subMap.get(movieName);
    }

    /**
     * 将符合srt
     * 1
     * data
     * <p>
     * 2
     * <p>
     * <p>
     * 3
     * 中间这部分实际就是vtt格式
     *
     * @param file
     */
    //FIXME:存在编码问题
    public static void SubToVtt(File file) {
        if (ReptileUtil.getType(file.getPath()).equals("srt")) {
            var pattern = Pattern.compile("^[0-9]+$"); //删除只有数字的行
            List<String> list = null;
            //0编码
            var pp= CodepageDetectorProxy.getInstance();
            pp.add(UnicodeDetector.getInstance());
            pp.add(JChardetFacade.getInstance());
            pp.add(ASCIIDetector.getInstance());
            String name="utf-8";
            try {
                System.out.println(file.getPath());
                name=pp.detectCodepage(file.toURL()).name();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //1.清楚不符合的行
            try (var in = new BufferedReader(new FileReader(file,Charset.forName(name)))) {
                list = in.lines().filter(s -> !pattern.matcher(s).matches()).collect(Collectors.toList());

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //2.重写
            try (var out = new BufferedWriter(new FileWriter(file, Charset.forName("utf-8"), false))) {
                if (list != null) {
                    //追加
                    out.write("WEBVTT");
                    out.newLine();
                    out.newLine();
                    list.stream().forEach(t -> {
                        try {
                            //总之此处要做这个转换
                            out.write(t.replaceAll(",","."));
                            out.newLine();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            //3.重命名
            String newPath = file.getPath().substring(0, file.getPath().lastIndexOf(".") + 1) + "vtt";
            file.renameTo(new File(newPath));
        }
    }
}
