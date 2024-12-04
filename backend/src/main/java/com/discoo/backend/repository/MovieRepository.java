package com.discoo.backend.repository;

import com.discoo.backend.entity.Movie;
import com.discoo.backend.entity.MovieStatus;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {
    List<Movie> findByStatus(MovieStatus status);
}
