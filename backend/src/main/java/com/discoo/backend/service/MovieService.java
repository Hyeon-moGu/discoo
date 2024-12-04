package com.discoo.backend.service;

import com.discoo.backend.entity.Genre;
import com.discoo.backend.entity.Movie;
import com.discoo.backend.entity.MovieStatus;
import com.discoo.backend.repository.GenreRepository;
import com.discoo.backend.repository.MovieRepository;

import jakarta.transaction.Transactional;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Log4j2
@Service
public class MovieService {

    @Value("${movie.bearerToken}")
    private String bearerToken;

    @Value("${movie.now_playing.api.url}")
    private String nowplayingUrl;

    @Value("${movie.upcoming.api.url}")
    private String upcomingUrl;

    @Value("${movie.genre.api.url}")
    private String genreUrl;

    private final MovieRepository movieRepository;
    private final GenreRepository genreRepository;
    private final RestTemplate restTemplate;

    public MovieService(MovieRepository movieRepository, GenreRepository genreRepository) {
        this.movieRepository = movieRepository;
        this.genreRepository = genreRepository;
        this.restTemplate = new RestTemplate();
    }

    @Scheduled(cron = "10 55 0 * * ?")
    public void updateNowPlayingMovies() {
        syncGenres();
        fetchMovies(nowplayingUrl, MovieStatus.CURRENT);
        fetchMovies(upcomingUrl, MovieStatus.UPCOMING);
    }

    @Transactional
    private void fetchMovies(String apiUrl, MovieStatus status) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("accept", "application/json");
        headers.set("Authorization", "Bearer " + bearerToken);

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<Map> response = restTemplate.exchange(
                apiUrl + "?language=ko-KR&page=1&region=KR",
                HttpMethod.GET,
                entity,
                Map.class);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            Map<String, Object> body = response.getBody();
            if (body.containsKey("results")) {
                List<Map<String, Object>> movies = (List<Map<String, Object>>) body.get("results");

                movies.forEach(movieData -> {
                    Long tmdbId = ((Number) movieData.get("id")).longValue();
                    Movie movie = movieRepository.findById(tmdbId).orElse(new Movie());

                    if (status == MovieStatus.UPCOMING) {
                        LocalDate releaseDate = parseDate((String) movieData.get("release_date"));
                        if (releaseDate != null && releaseDate.isBefore(LocalDate.now())) {
                            movie.setStatus(MovieStatus.CURRENT);
                        } else {
                            movie.setStatus(MovieStatus.UPCOMING);
                        }
                    } else {
                        movie.setStatus(status);
                    }

                    mapMovieData(movie, movieData);
                    movieRepository.save(movie);
                });
            } else {
                log.info(" ==== No movies found for the status: {}", status);
            }
        } else {
            log.error(" ==== Failed to fetch movies. Status: {}", response.getStatusCode());
        }

        log.info(" ==== Movies with status '{}' updated successfully.", status);
    }

    private void mapMovieData(Movie movie, Map<String, Object> movieData) {
        movie.setId(((Number) movieData.get("id")).longValue());
        movie.setTitle((String) movieData.get("title"));
        movie.setVoteAverage(((Number) movieData.get("vote_average")).doubleValue());
        movie.setPosterPath((String) movieData.get("poster_path"));
        movie.setReleaseDate((String) movieData.get("release_date"));
        movie.setPopularity(((Number) movieData.get("popularity")).doubleValue());
        movie.setLanguage((String) movieData.get("original_language"));
        movie.setVoteCount(((Number) movieData.get("vote_count")).intValue());
        movie.setOverview((String) movieData.get("overview"));
        movie.setBackdropPath((String) movieData.get("backdrop_path"));

        List<Integer> genreIds = (List<Integer>) movieData.get("genre_ids");
        List<Genre> genres = genreRepository.findAllById(genreIds);
        String genreNames = genres.stream()
                .map(Genre::getName)
                .collect(Collectors.joining(", "));
        movie.setGenreNames(genreNames);
    }

    // 날짜 문자열 파싱 TODO: 공통코드 예정
    private LocalDate parseDate(String dateStr) {
        try {
            return LocalDate.parse(dateStr, DateTimeFormatter.ISO_DATE);
        } catch (Exception e) {
            log.warn(" ==== Invalid date format: {}", dateStr);
            return null;
        }
    }

    @Transactional
    public void syncGenres() {
        String url = genreUrl + "?language=ko";
        HttpHeaders headers = new HttpHeaders();
        headers.set("accept", "application/json");
        headers.set("Authorization", "Bearer " + bearerToken);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> body = response.getBody();

                if (body.containsKey("genres")) {
                    List<Map<String, Object>> genres = (List<Map<String, Object>>) body.get("genres");

                    genres.forEach(genreData -> {
                        Integer id = ((Number) genreData.get("id")).intValue();
                        String name = (String) genreData.get("name");

                        Genre genre = genreRepository.findById(id).orElse(new Genre());
                        genre.setId(id);
                        genre.setName(name);
                        genreRepository.save(genre);
                    });

                    log.info(" ==== Genres synchronized successfully");
                } else {
                    log.warn(" ==== No genres found in the API response");
                }
            } else {
                log.error(" ==== Failed to synchronize genres. Status code: {}", response.getStatusCode());
                throw new RuntimeException("Genre synchronization failed: Unexpected status code");
            }
        } catch (Exception e) {
            log.error(" ==== An error occurred during genre synchronization: {}", e.getMessage(), e);
            throw new RuntimeException(" ==== Genre synchronization failed", e);
        }
    }
}
