package com.light.privateMovies.pojo;


import com.light.privateMovies.reptile.core.ReptileUtil;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "movies_table", uniqueConstraints = {@UniqueConstraint(columnNames = "movie_name")})
public class Movie {
    @Id
    @GeneratedValue
    private Integer id;
    @Column(name = "movie_name", unique = true, columnDefinition = "varchar(40)")
    private String movieName;
    @Lob
    @Column(name = "movie_cover", columnDefinition = "mediumblob")
    private byte[] cover;
    @Column(name = "description", columnDefinition = "text")
    private String desc;
    @Column(name = "title")
    private String title;
    @Column(name = "releaseTime")
    private LocalDate releaseTime;
    @Column(name = "length", columnDefinition = "int")
    private Integer length;
    @Column(name = "local_path")
    private String localPath;
    @Column(name = "create_time")
    private LocalDateTime createTime;
    @ManyToOne
    @JoinColumn(name = "modules_id")
    private ModuleEntry moduleEntry;
    @Column(name = "FAVORITE")
    @Type(type = "yes_no")
    private Boolean favorite;

    public boolean isFavorite() {
        if (favorite == null)
            return false;
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    public ModuleEntry getModuleEntry() {
        return moduleEntry;
    }

    public void setModuleEntry(ModuleEntry moduleEntry) {
        this.moduleEntry = moduleEntry;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public Integer getLength() {
        return length;
    }

    public void setLength(Integer length) {
        this.length = length;
    }

    public LocalDate getReleaseTime() {
        return releaseTime;
    }

    public void setReleaseTime(LocalDate releaseTime) {
        this.releaseTime = releaseTime;
    }

    @ManyToMany(fetch = FetchType.EAGER)
    @Fetch(FetchMode.SUBSELECT)
    @JoinTable(
            name = "movie_movie_type_table",   //中间表名称
            joinColumns = @JoinColumn(name = "movie_id"), //该表在关系表中的键
            inverseJoinColumns = @JoinColumn(name = "movie_type_id") //另一张表连接时使用的
    )
    List<MovieType> movieTypes;


    @ManyToMany(fetch = FetchType.EAGER)  //不使用懒加载
    @JoinTable(
            name = "movie_actor_table",
            joinColumns = @JoinColumn(name = "movie_id"),
            inverseJoinColumns = @JoinColumn(name = "actor_id")
    )
    List<Actor> actors;


    @OneToMany(mappedBy = "movie", fetch = FetchType.EAGER)
    @Fetch(FetchMode.SUBSELECT)
    List<MovieDetail> movieDetails;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<MovieType> getMovieTypes() {
        return movieTypes;
    }

    public void setMovieTypes(List<MovieType> movieTypes) {
        this.movieTypes = movieTypes;
    }

    public List<Actor> getActors() {
        return actors;
    }

    public void setActors(List<Actor> actors) {
        this.actors = actors;
    }

    public List<MovieDetail> getMovieDetails() {
        return movieDetails;
    }

    public void setMovieDetails(List<MovieDetail> movieDetails) {
        this.movieDetails = movieDetails;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getMovieName() {
        return movieName;
    }

    public void setMovieName(String movieName) {
        this.movieName = movieName;
    }

    public byte[] getCover() {
        return cover;
    }

    public void setCover(byte[] cover) {
        this.cover = cover;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public void setSimpleType(MovieType movieType) {
        this.movieTypes = Collections.singletonList(movieType);
    }

    public void setSimpleActor(Actor actor) {
        this.actors = Collections.singletonList(actor);
    }

    public String getLocalPath() {
        return localPath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }

    /**
     * 创建的不从爬虫拿取数据的情况
     *
     * @param file
     */
    public static Movie CreateNMovie(File file) {
        Movie movie = new Movie();
        String name = file.getName();
        name = name.substring(0, name.lastIndexOf("."));
        movie.setMovieName(name);
        movie.setLocalPath(ReptileUtil.dealDouble(file.getPath()));
        movie.setCreateTime(LocalDateTime.now());
        movie.setActors(new ArrayList<>());
        movie.setDesc("");
        movie.setTitle("");
        movie.setMovieTypes(new ArrayList<>());
        return movie;
    }

    //模块类型名
    private String moduleTypeName;
    //所属模块名
    private String moduleName;

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public String getModuleTypeName() {
        return moduleTypeName;
    }

    public void setModuleTypeName(String moduleTypeName) {
        this.moduleTypeName = moduleTypeName;
    }
}
