package com.cinecapas.persistence.seeders;

import com.cinecapas.persistence.daos.MoviesModelDao;
import com.cinecapas.persistence.repos.MoviesModelDaoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class MoviesSeeder implements CommandLineRunner {

    private final MoviesModelDaoRepository moviesRepository;

    private record Pelicula(String titulo, String sinopsis, String genero,
                            String clasificacion, int duracion, double rating,
                            String formatos, String poster) {}

    private static final List<Pelicula> PELICULAS = List.of(
        new Pelicula("Avatar: El Camino del Agua",
            "Jake Sully y Neytiri deben proteger su familia en Pandora.",
            "Ciencia Ficción", "PG-13", 192, 8.5,
            "2D,3D,IMAX,IMAX 3D",
            "https://image.tmdb.org/t/p/w500/t6HIqrSEclZCA90JhU0e1zL0XzG.jpg"),

        new Pelicula("Oppenheimer",
            "La historia de J. Robert Oppenheimer y la creación de la bomba atómica.",
            "Drama", "R", 180, 9.0,
            "2D,IMAX",
            "https://image.tmdb.org/t/p/w500/8Gxv8gSFCU0XGDykEGv7zR1n2ua.jpg"),

        new Pelicula("Spider-Man: No Way Home",
            "Peter Parker desata multiversos con consecuencias devastadoras.",
            "Acción", "PG-13", 148, 8.3,
            "2D,3D,IMAX",
            "https://image.tmdb.org/t/p/w500/1g0dhYtq4irTY1GPXvft6k4YLjm.jpg"),

        new Pelicula("The Batman",
            "Bruce Wayne enfrenta al.hamcrest Zodiaco en Gotham City.",
            "Acción", "PG-13", 176, 7.8,
            "2D,IMAX",
            "https://image.tmdb.org/t/p/w500/74xTEgt7R36Fpooo50r9T25onhq.jpg"),

        new Pelicula("Dune: Parte Dos",
            "Paul Atreides se une a los Fremen para derrotar a los Harkonnen.",
            "Ciencia Ficción", "PG-13", 166, 8.8,
            "2D,3D,IMAX,IMAX 3D",
            "https://image.tmdb.org/t/p/w500/8b8R8l88Qje9dn9OE8PY05Nxl1X.jpg"),

        new Pelicula("Inside Out 2",
            "Riley enfrenta nuevas emociones en la adolescencia.",
            "Animación", "PG", 100, 8.0,
            "2D,3D",
            "https://image.tmdb.org/t/p/w500/vpnVM9B6NMmQpWeZvzLvDESb2QY.jpg"),

        new Pelicula("Deadpool & Wolverine",
            "Deadpool recluta a Wolverine para salvar su universo.",
            "Acción", "R", 128, 8.2,
            "2D,IMAX",
            "https://image.tmdb.org/t/p/w500/8cdWjvZQUExUUTzyp4t6EDMubfO.jpg"),

        new Pelicula("Gladiator II",
            "Lucius se ve obligado a luchar en el Coliseo tras la conquista de su hogar.",
            "Acción", "R", 148, 7.5,
            "2D,IMAX,IMAX 3D",
            "https://image.tmdb.org/t/p/w500/2cxhvwyEwRlysAmRH4iodkvo0z5.jpg")
    );

    @Override
    public void run(String... args) {
        long existentes = moviesRepository.count();
        if (existentes > 0) {
            log.info("[MoviesSeeder] Ya existen {} películas, se omite el seed.", existentes);
            return;
        }

        PELICULAS.forEach(p -> {
            moviesRepository.save(MoviesModelDao.builder()
                .titulo(p.titulo())
                .sinopsis(p.sinopsis())
                .genero(p.genero())
                .clasificacion(p.clasificacion())
                .duracionMinutos(p.duracion())
                .rating(p.rating())
                .formatos(p.formatos())
                .posterUrl(p.poster())
                .build());
            log.info("[MoviesSeeder] Película creada: {}", p.titulo());
        });
    }
}
