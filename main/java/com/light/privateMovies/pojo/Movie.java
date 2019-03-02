package com.light.privateMovies.pojo;


import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "movies_table", uniqueConstraints = {@UniqueConstraint(columnNames = "movie_name")})
public class Movie {
    @Id
    @GeneratedValue
    private Integer id;
    @Column(name = "movie_name",unique = true,columnDefinition = "varchar(40)")
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
}
