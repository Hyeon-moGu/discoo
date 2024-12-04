package com.discoo.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.discoo.backend.entity.Movie;
import com.discoo.backend.entity.MovieStatus;
import com.discoo.backend.repository.MovieRepository;

@RestController
@RequestMapping("/v1/movie")
public class MovieController {

    private final MovieRepository movieRepository;

    public MovieController(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    // 현재 상영 영화 조회
    @GetMapping("/nowplaying")
    public ResponseEntity<List<Movie>> getNowPlayingMovies() {
        List<Movie> currentMovies = movieRepository.findByStatus(MovieStatus.CURRENT);
        return ResponseEntity.ok(currentMovies);
    }

    // 개봉 예정 영화 조회
    @GetMapping("/upcoming")
    public ResponseEntity<List<Movie>> getUpcomingMovies() {
        List<Movie> upcomingMovies = movieRepository.findByStatus(MovieStatus.UPCOMING);
        return ResponseEntity.ok(upcomingMovies);
    }

}
