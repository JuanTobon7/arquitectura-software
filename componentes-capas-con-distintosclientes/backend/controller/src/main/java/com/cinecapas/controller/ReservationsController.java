package com.cinecapas.controller;

import com.cinecapas.application.dto.ReservationRequestDto;
import com.cinecapas.application.dto.ReservationsDto;
import com.cinecapas.application.dto.ScreeningDto;
import com.cinecapas.application.dto.SeatsAvailabilityDto;
import com.cinecapas.application.services.contracts.ReservationsManagmentScreeningService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ReservationsController {
    private final ReservationsManagmentScreeningService gestionReservas;

    @GetMapping("/funciones/{peliculaId}")
    public List<ScreeningDto> funcionesDePelicula(@PathVariable long peliculaId) {
        return gestionReservas.funcionesDePelicula(peliculaId);
    }

    @GetMapping("/funciones/{id}/asientos")
    public SeatsAvailabilityDto asientos(@PathVariable long id) {
        return gestionReservas.disponibilidad(id);
    }

    @PostMapping("/reservas")
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationsDto crear(@Valid @RequestBody ReservationRequestDto dto) {
        return gestionReservas.crear(dto);
    }

    @DeleteMapping("/reservas/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelar(@PathVariable long id) {
        gestionReservas.cancelar(id);
    }
}
