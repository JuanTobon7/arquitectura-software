package com.cinecapas.persistence.seeders;

import com.cinecapas.persistence.daos.ScreeningModelDao;
import com.cinecapas.persistence.repos.MoviesModelDaoRepository;
import com.cinecapas.persistence.repos.ScreeningModelDaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ScreeningsSeeder implements CommandLineRunner {
    private final ScreeningModelDaoRepository screeningsRepository;
    private final MoviesModelDaoRepository moviesModelDaoRepository;

    private record Turno(LocalTime hora, String formato, String sala, double precio) {
    }
    private static final List<Turno> TURNOS = List.of(
            new Turno(LocalTime.of(11, 30), "2D", "Sala 1", 6.50),
            new Turno(LocalTime.of(17, 0), "3D", "Sala 2", 8.00),
            new Turno(LocalTime.of(19, 45), "IMAX", "Sala IMAX", 10.50),
            new Turno(LocalTime.of(21, 45), "IMAX 3D", "Sala IMAX", 12.00));

    @Override
    public void run(String... args) throws Exception {
        Set<Long> conFunciones = screeningsRepository.findAll().stream()
                .map(ScreeningModelDao::getPeliculaId)
                .collect(Collectors.toSet());

        LocalDate hoy = LocalDate.now();
        moviesModelDaoRepository.findAll().stream()
                .filter(p -> !conFunciones.contains(p.getId()))
                .forEach(pelicula -> {
                    for (int dia = 0; dia < 7; dia++) {
                        for (Turno turno : TURNOS) {
                            if ((pelicula.getId() + dia + turno.hora().getHour()) % 3 == 0) {
                                continue;
                            }
                            LocalDateTime dateTime = LocalDateTime.of(hoy.plusDays(dia), turno.hora());
                            screeningsRepository.save(ScreeningModelDao.builder()
                                    .peliculaId(pelicula.getId())
                                    .fechaHora(dateTime)
                                    .formato(turno.formato())
                                    .sala(turno.sala())
                                    .filas(10)
                                    .columnas(10)
                                    .precio(turno.precio())
                                    .build());
                        }
                    }
                });
        };
}
