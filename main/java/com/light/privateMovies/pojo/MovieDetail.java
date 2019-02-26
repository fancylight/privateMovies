package com.light.privateMovies.pojo;

import com.light.privateMovies.util.FileUtil;

import javax.persistence.*;

@Entity
@Table(name = "movies_detail_table")
public class MovieDetail {
    @Id
    @GeneratedValue
    private Integer id;
    @Lob
    @Column(name = "detail_pic", columnDefinition = "mediumblob")
    private byte[] detailPic;
    @ManyToOne
    @JoinColumn(name = "movie_id")
    Movie movie;
    @Column(name = "name")
    private String name;
    public Movie getMovie() {
        return movie;
    }

    public void setMovie(Movie movie) {
        this.movie = movie;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public byte[] getDetailPic() {
        return detailPic;
    }

    public void setDetailPic(byte[] detailPic) {
        this.detailPic = detailPic;
    }

    public MovieDetail(byte[] detailPic, Movie movie) {
        this.detailPic = detailPic;
        this.movie = movie;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public MovieDetail(byte[] detailPic, Movie movie, String name) {
        this.detailPic = detailPic;
        this.movie = movie;
        this.name = name;
    }

    public MovieDetail() {
    }
}
