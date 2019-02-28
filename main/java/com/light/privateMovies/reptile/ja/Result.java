package com.light.privateMovies.reptile.ja;

import com.light.privateMovies.pojo.Actor;
import com.light.privateMovies.pojo.Movie;
import com.light.privateMovies.pojo.MovieDetail;

import java.util.List;

public class Result {
    private List<Actor> actor;
    private Movie movie;
    private List<MovieDetail> movieDetail;

    public Result(List<Actor> actor, Movie movie, List<MovieDetail> movieDetail) {
        this.actor = actor;
        this.movie = movie;
        this.movieDetail = movieDetail;
    }

    public List<Actor> getActor() {
        return actor;
    }

    public void setActor(List<Actor> actor) {
        this.actor = actor;
    }

    public Movie getMovie() {
        return movie;
    }

    public void setMovie(Movie movie) {
        this.movie = movie;
    }

    public List<MovieDetail> getMovieDetail() {
        return movieDetail;
    }

    public void setMovieDetail(List<MovieDetail> movieDetail) {
        this.movieDetail = movieDetail;
    }
}
