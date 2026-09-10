package com.cinecapas.persistence.daos;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity(name = "screening")
@Table(name = "screening")
@Getter @Setter @Builder @AllArgsConstructor @NoArgsConstructor
public class ScreeningModelDao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "pelicula_id", nullable = false)
    private Long peliculaId;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    @Column(nullable = false)
    private String formato;

    @Column(nullable = false)
    private String sala;

    @Column(nullable = false)
    private int filas;

    @Column(nullable = false)
    private int columnas;

    @Column(nullable = false)
    private double precio;
}
