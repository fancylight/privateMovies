package com.light.privateMovies.pojo;


import javax.persistence.*;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "movies_type", uniqueConstraints = {@UniqueConstraint(columnNames = "type_name")})
public class MovieType {
    @Id
    @GeneratedValue
    private Integer id;
    @Column(name = "type_name", columnDefinition = "varchar(30)")
    private String movieType;
    @ManyToMany(mappedBy = "movieTypes")
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

    public String getMovieType() {
        return movieType;
    }

    public void setMovieType(String movieType) {
        this.movieType = movieType;
    }

    public MovieType(String movieType, List<Movie> movies) {
        this.movieType = movieType;
        this.movies = movies;
    }

    public MovieType() {
    }

    public void setSimpleMovie(Movie movie) {
        setMovies(Collections.singletonList(movie));
    }

    public void setMovieType(MovieType typeByName) {
        setId(typeByName.id);
        setMovieType(typeByName.movieType);
        setMovies(typeByName.movies);
    }
}
