package com.cinecapas.persistence.daos;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity(name = "reservation")
@Table(name = "reservation")
@Getter
@Setter
@Builder @AllArgsConstructor
@NoArgsConstructor
public class ReservationModelDao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "funcion_id", nullable = false)
    private Long funcionId;

    @Column(nullable = false, unique = true)
    private String codigo;

    @Column(name = "nombre_cliente", nullable = false)
    private String nombreCliente;

    private String email;

    private String estado;

    @Column(name = "creada_en", nullable = false)
    private LocalDateTime creadaEn;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "reserva_asiento", joinColumns = @JoinColumn(name = "reserva_id"))
    @Column(name = "asiento", nullable = false)
    private List<String> asientos;
}
