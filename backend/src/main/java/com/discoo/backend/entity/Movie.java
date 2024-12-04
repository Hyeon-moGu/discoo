package com.discoo.backend.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Movie {
    @Id
    private Long id; // TMDB 고유 ID

    private String title; // 영화 제목
    private Double voteAverage; // 평균 평점
    private String posterPath; // 포스터 이미지 경로
    private String releaseDate; // 개봉일
    private Double popularity; // 인기 점수
    private String language; // 영화의 언어
    private Integer voteCount; // 투표 수

    @Column(columnDefinition = "TEXT")
    private String overview; // 줄거리

    @Column(columnDefinition = "TEXT")
    private String backdropPath; // 배경 이미지 경로

    @Enumerated(EnumType.STRING)
    private MovieStatus status; // 상태 (CURRENT, UPCOMING, ENDED)

    private LocalDateTime storedAt; // 저장 시간
    private LocalDateTime updatedAt; // 업데이트 시간

    @Column(columnDefinition = "TEXT")
    private String genreNames; // 장르 이름

    @PrePersist
    protected void onCreate() {
        this.storedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
