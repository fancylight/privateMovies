import com.light.privateMovies.init.SubDeal;
import com.light.privateMovies.pojo.Movie;
import com.light.privateMovies.reptile.ja.ArzonData;
import com.light.privateMovies.reptile.annotation.Step;
import com.light.privateMovies.reptile.ja.JavData;
import com.light.privateMovies.util.FileUtil;
import org.jsoup.Jsoup;
import org.testng.annotations.Test;

import javax.persistence.Id;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Test
public class Test3 {
    @Id
    private String t;
    @Step(url = "测试")
    static int TIMEOUT = 1000 * 10;

    //测试jsoup连接
    @Test
    public void test() throws IOException {
        var doc = Jsoup.connect("https://pics.javbus.com/cover/6msn_b.jpg").proxy(new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("127.0.0.1", 1080))).get();
    }

    @Test
    public void testJav() {
        new JavData("VRTM360", "H:\\1/VRTM360.mp4").getResult();
    }

    @Test
    public void test2() {
        new ArzonData("H:\\temp4\\AGEMIX-417.mp4").getReFromArzon();
    }

    /**
     * 测试如何使用注解
     */
    @Test
    public void test3() {
        var fs = this.getClass().getDeclaredFields();
        for (var f : fs) {
            var ans = f.getAnnotations();
            for (var an : ans) {
                if (an.annotationType().getTypeName().equals(Id.class.getTypeName())) {
                    System.out.println(f);
                }
                if (an.annotationType().getTypeName().equals(Step.class.getTypeName())) {
                    Step step = f.getAnnotation(Step.class); //此处要如此,我暂时没有找到转换的函数
                    System.out.println(step.url());
                }
            }
        }
    }

    //构造器反射
    @Test
    public void consTest() {
        try {
            Movie.class.getConstructor().newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    //测试filter
    @Test
    public void streamFilter() {
        var list = Stream.of(1, 2, 3, 4).collect(Collectors.toList());
        //不满足条件去除
        list.stream().filter(t -> t > 1).forEach(System.out::println);
        System.out.println(list);
    }

    //时间api
    @Test
    public void timeTest() {
        System.out.println(LocalDate.parse(""));
        System.out.println(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC));
    }

    //貌似不能这么删除
    @Test
    public void deleteDir() {
        //H:\temp4\吉沢明歩\SSNI065-同人界の新生児おるとろの人気作品を忠実実写化 不貞交尾妻ほのか～婚姻を継続し難い重大な理由～ 吉沢明歩\SSNI065.mp4/../..
//        FileUtil.deleteDir("H:\\temp4\\吉沢明歩\\SSNI065-同人界の新生児おるとろの人気作品を忠実実写化 不貞交尾妻ほのか～婚姻を継続し難い重大な理由～ 吉沢明歩\\SSNI065.mp4/../..", modules);
    }

    //测试字幕
    @Test
    public void subTest() {
        var subs = new SubDeal();
        System.out.println(subs.getSubPath("ABP041"));
    }
    //测试srt处理
    @Test
    public void subDeal(){
//        SubDeal.SubToVtt(new File("F:\\heydouga4146-169-1.srt"));
    }
}
