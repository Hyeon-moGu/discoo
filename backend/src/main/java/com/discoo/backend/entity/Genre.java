package com.discoo.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Genre {
    @Id
    private Integer id;
    private String name;
}

