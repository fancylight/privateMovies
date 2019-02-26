import com.light.privateMovies.pojo.Movie;
import com.light.privateMovies.reptile.ArzonData;
import com.light.privateMovies.reptile.annotation.Step;
import org.jsoup.Jsoup;
import org.testng.annotations.Test;

import javax.persistence.Id;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.net.Proxy;


@Test
public class Test3 {
    @Id
    private String t;
    @Step(url = "测试")
    static int TIMEOUT = 1000 * 10;

    //测试jsoup连接
    @Test
    public void test() throws IOException {
        var doc = Jsoup.connect("http://www.javlibrary.com/cn/").proxy(new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("127.0.0.1", 1080))).get();
        System.out.println(doc.title());
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
}