package com.cinecapas.persistence.daos;

import jakarta.persistence.*;
import lombok.*;

@Entity(name = "movies")
@Table(name = "movies")
@Getter @Setter @Builder @NoArgsConstructor
@AllArgsConstructor
public class MoviesModelDao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titulo;

    @Column(length = 1000)
    private String sinopsis;

    private String genero;

    private String clasificacion;

    @Column(name = "duracion_minutos", nullable = false)
    private int duracionMinutos;

    private double rating;

    @Column(nullable = false)
    private String formatos;

    @Column(name = "poster_url")
    private String posterUrl;
}
