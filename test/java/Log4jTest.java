import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.annotations.Test;

//https://blog.csdn.net/u013870094/article/details/79518028
//如果使用新版log4j-x 则和之前的版本不同
public class Log4jTest {

    private Logger logger= LogManager.getLogger(Log4jTest.class);

    @Test
    public void log4jTest() {
        logger.warn("warn");
    }

}
