package com.cinecapas.controller;

import com.cinecapas.application.dto.MovieDto;
import com.cinecapas.application.dto.StoreMovieDto;
import com.cinecapas.application.services.contracts.MoviesService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/peliculas")
@RequiredArgsConstructor
public class MoviesController {
    private final MoviesService moviesService;

    @GetMapping
    public List<MovieDto> listar(
            @RequestParam(required = false) String genero,
            @RequestParam(required = false) String formato) {
        return moviesService.buscar(genero, formato);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MovieDto registrar(@Valid @RequestBody StoreMovieDto dto) {
        return moviesService.registrar(dto);
    }
}