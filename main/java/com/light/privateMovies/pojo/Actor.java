package com.light.privateMovies.pojo;

import com.light.privateMovies.util.FileUtil;

import javax.persistence.*;
import java.util.Collections;
import java.util.List;

/**
 * 表示演员
 */
@Entity
@Table(name = "actors_table",uniqueConstraints = {@UniqueConstraint(columnNames = "actor_name")})
public class Actor {
    @Id
    @GeneratedValue
    private Integer id;
    @Column(name = "actor_name",columnDefinition = "varchar(20)")
    private String actor_name;
    //使用Lob注解,并且说明列定义,可以将BLOB<--->byte[]映射
    @Lob
    @Column(name = "actor_pic", columnDefinition = "mediumblob")
    private byte[] actor_pic;
    @ManyToMany(mappedBy = "actors")
    List<Movie> movies;
    public List<Movie> getMovies() {
        return movies;
    }

    public void setMovies(List<Movie> movies) {
        this.movies = movies;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getActor_name() {
        return actor_name;
    }

    public void setActor_name(String actor_name) {
        this.actor_name = actor_name;
    }

    public byte[] getActor_pic() {
        return actor_pic;
    }

    public void setActor_pic(byte[] actor_pic) {
        this.actor_pic = actor_pic;
    }

    //创建actor
    public static Actor createActor(String actorName, String picPath, boolean isClassPath) {
        Actor actor = new Actor();
        actor.setActor_name(actorName);
        actor.setActor_pic(FileUtil.getFileData(picPath,isClassPath));
        return actor;
    }
    public void setSimpleMovie(Movie movie){
        setMovies(Collections.singletonList(movie));
    }
    public void setActor(Actor actor){
        setId(actor.id);
        setActor_pic(actor.actor_pic);
        setActor_name(actor.actor_name);
        setMovies(actor.movies);
    }
}
