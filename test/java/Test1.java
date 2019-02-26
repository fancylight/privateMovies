import com.light.privateMovies.dao.*;
import com.light.privateMovies.pojo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTransactionalTestNGSpringContextTests;
import org.springframework.transaction.annotation.Transactional;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

@Transactional  //设置事务
@ContextConfiguration("classpath*:/spring.xml") //启动spring容器
@Rollback(false) //指定测试不自动回滚
public class Test1 extends AbstractTransactionalTestNGSpringContextTests {
    private ActorDao actorDao;
    private ModuleDao moduleDao;
    private ModuleTypeDao moduleTypeDao;
    private MovieDao movieDao;
    private MovieDetailDao movieDetailDao;
    private MovieTypeDao movieTypeDao;
    private Actor actor;
    private Movie movie;
    private ModuleEntry module;
    private ModuleType moduleType;
    private MovieDetail movieDetail;
    private MovieType movieType;

    @Autowired
    public void setActorDao(ActorDao actorDao) {
        this.actorDao = actorDao;
    }

    @Autowired
    public void setModuleDao(ModuleDao moduleDao) {
        this.moduleDao = moduleDao;
    }

    @Autowired
    public void setModuleTypeDao(ModuleTypeDao moduleTypeDao) {
        this.moduleTypeDao = moduleTypeDao;
    }

    @Autowired
    public void setMovieDao(MovieDao movieDao) {
        this.movieDao = movieDao;
    }

    @Autowired
    public void setMovieDetailDao(MovieDetailDao movieDetailDao) {
        this.movieDetailDao = movieDetailDao;
    }

    @Autowired
    public void setMovieTypeDao(MovieTypeDao movieTypeDao) {
        this.movieTypeDao = movieTypeDao;
    }

    public void init() {
        actor = Actor.createActor("测试", "/pic/阿部乃みく.jpg", true);
        movie = new Movie();
//        movie.setActors(Stream.of(actor).collect(Collectors.toList()));
        movie.setMovieName("测试电影");
        module = new ModuleEntry("test", "H:\\temp4", null);
        moduleType = new ModuleType("avTemp", null);
//        movieDetail = MovieDetail.createMovieDetail("/1496604P-01.jpg", true, movie);
        movieType = new MovieType("巨乳", null);
    }

    @Test
    public void test() {
        System.out.println("1123");
    }

    @Test
    public void add() {
        //添加时无关系
        this.movieTypeDao.add(movieType);
        this.movieDao.add(movie);
        var x = 3;
    }

    @Test
    public void update() {
        //更新关系
//        movieType=this.movieTypeDao.getTypeByName("巨乳");
//        movieType.setSimpleMovie(this.movieDao.getMovieByMovieName("测试电影"));
//        this.movieTypeDao.update(movieType);
        movie = movieDao.getMovieByMovieName("测试电影");
        movie.setSimpleType(movieTypeDao.getTypeByName("巨乳"));
        movieDao.update(movie);
    }

}
