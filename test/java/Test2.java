import com.light.privateMovies.module.TypeDeal;
import com.light.privateMovies.util.fileTargetDeal.AbstractFileDeal;
import com.light.privateMovies.util.FileUtil;
import org.testng.annotations.Test;

import javax.swing.plaf.metal.OceanTheme;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 关于java Type相关由一篇博客
 * https://www.jianshu.com/p/7649f86614d3
 * <p>
 * https://stackoverflow.com/questions/22953732/retrieve-generic-type-information-with-reflection
 * <p>
 * https://stackoverflow.com/questions/3437897/how-to-get-a-class-instance-of-generics-type-t
 * <p>
 * 1.泛型在继承过程中当作参数传递是使用,由子类泛型实例化时,将类型传递给上层
 * 2.如果想要通过未知类型获取T.class是不可能的
 */
public class Test2 {
    class xx<T> {

    }

    class xxx<T> extends xx<Integer> {
        public Integer t = null;

        public void p() {
            Type genType = getClass().getGenericSuperclass();
            System.out.println(((ParameterizedType) genType).getActualTypeArguments()[0].getTypeName());
        }
    }

    @Test
    public void test() throws ClassNotFoundException {
        FileUtil.scanDir(new AbstractFileDeal() {
            @Override
            public void deal(File file, String[] targetType, String parentPath) {
                System.out.println(parentPath + File.separator + file.getName());
            }
        }, "H:\\temp3", new String[]{"mp4", "mkv", "avi"}, "");
    }

    //https://www.cnblogs.com/ggjucheng/p/3423731.html 正则,事时间api测试
    @Test
    public void timeTest() {
        System.out.println(LocalDate.parse(LocalDate.of(2018, 12, 1).toString()));
        System.out.println("\\".replaceAll("\\\\", "123"));
        String time = "2019/01/18 (DVD セルorレンタル)";
        String rex = "[0-9]{1,4}/[0-9]{1,2}/[0-9]{1,2}";
        Pattern rexP = Pattern.compile(rex);
        Matcher matcher = rexP.matcher(time);
        System.out.println(matcher.find());
        System.out.println(matcher.group());
    }

    @Test
    public void fileTest() {
        try {
            FileUtil.getInBytes(new FileInputStream("I:\\1.png"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void regexTest() {
        System.out.println(TypeDeal.getACode("ABS-128  廃盤"));
        //abs123-->abs-123
        String code="CESD721";
        var m=Pattern.compile("^[a-z|A-Z]{3,5}").matcher(code);
        String codePart="";
        if(m.find())
            codePart=m.group();
        var m2=Pattern.compile("[0-9]{3,4}$").matcher(code);
        String number="";
        if(m2.find())
            number=m2.group();
        System.out.println(codePart);
        System.out.println(number);
    }

}
