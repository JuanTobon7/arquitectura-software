package com.cinecapas.application.services.impl;

import com.cinecapas.application.components.TheatherLayout;
import com.cinecapas.application.dto.*;
import com.cinecapas.application.exceptions.InvalidRequestException;
import com.cinecapas.application.exceptions.NotAviableSeatException;
import com.cinecapas.application.exceptions.NotFoundReservationException;
import com.cinecapas.application.exceptions.NotFoundScreeningException;
import com.cinecapas.application.mappers.ReservationMappers;
import com.cinecapas.application.mappers.ScreeningMappers;
import com.cinecapas.application.services.contracts.ReservationsManagmentScreeningService;
import com.cinecapas.domain.enums.ReservationStatus;
import com.cinecapas.domain.models.ReservationModel;
import com.cinecapas.domain.models.ScreeningModel;
import com.cinecapas.domain.ports.in.InReservations;
import com.cinecapas.domain.ports.in.InScreenings;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReservationScreeningServiceImpl implements ReservationsManagmentScreeningService {

    private static final int MAX_ASIENTOS_POR_RESERVA = 8;
    private static final String ALFABETO_CODIGO = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

    private final InScreenings funciones;
    private final InReservations reservas;
    private final SecureRandom aleatorio = new SecureRandom();

    @Override
    public List<ScreeningDto> funcionesDePelicula(long peliculaId) {
        List<ScreeningModel> funcionesModel = funciones.porPelicula(peliculaId);
        return funcionesModel.stream().map(ScreeningMappers::toDto).toList();
    }

    @Override
    public Optional<ScreeningDto> funcionPorId(long funcionId) {
        ScreeningModel model = funciones.porId(funcionId).orElse(null);
        return Optional.ofNullable(model).map(ScreeningMappers::toDto);
    }

    @Override
    public Optional<ReservationsDto> reservaPorId(long reservaId) {
        ReservationModel model = reservas.porId(reservaId).orElse(null);
        return Optional.ofNullable(model).map(ReservationMappers::toDto);
    }

    @Override
    public SeatsAvailabilityDto disponibilidad(long funcionId) {
        ScreeningDto funcion = funcionObligatoria(funcionId);
        return SeatsAvailabilityDto.builder()
                .funcionId(funcion.getId())
                .filas(funcion.getFilas())
                .columnas(funcion.getColumnas())
                .ocupado(reservas.asientosOcupados(funcionId))
                .build();
    }

    @Override
    public ReservationsDto crear(ReservationRequestDto solicitud) {
        if (solicitud.getNombreCliente() == null || solicitud.getNombreCliente().isBlank()) {
            throw new InvalidRequestException("El nombre del cliente es obligatorio");
        }
        if (solicitud.getAsiento().size() > MAX_ASIENTOS_POR_RESERVA) {
            throw new InvalidRequestException(
                    "Máximo " + MAX_ASIENTOS_POR_RESERVA + " asientos por reserva");
        }

        ScreeningDto funcion = funcionObligatoria(solicitud.getScreeningId());

        List<String> pedidos = solicitud.getAsiento().stream()
                .map(a -> a.trim().toUpperCase())
                .distinct()
                .toList();

        new TheatherLayout(funcion.getFilas(), funcion.getColumnas()).validar(pedidos);

        List<String> ocupados = reservas.asientosOcupados(funcion.getId());
        List<String> enConflicto = pedidos.stream().filter(ocupados::contains).toList();
        if (!enConflicto.isEmpty()) {
            throw new NotAviableSeatException("Asientos no disponibles: " + enConflicto + "");
        }

        ReservationModel model = ReservationModel.builder()
                .id(funcion.getId())
                .nombreCliente(solicitud.getNombreCliente())
                .email(solicitud.getEmail())
                .codigo(generarCodigo())
                .asientos(pedidos)
                .creadaE(funcion.getFechaHora())
                .funcionId(funcion.getId())
                .estado(ReservationStatus.ACTIVA)
                .build();
        model = reservas.guardar(model);
        return ReservationMappers.toDto(model);
    }

    @Override
    public void cancelar(long reservaId) {
        reservas.porId(reservaId)
                .orElseThrow(() -> new NotFoundReservationException(
                        "reserva con id: "+reservaId+" no encontrada"));
        reservas.marcarCancelada(reservaId);
    }

    private ScreeningDto funcionObligatoria(long funcionId) {
        ScreeningModel model = funciones.porId(funcionId)
                .orElseThrow(() -> new NotFoundScreeningException(
                        "funcion con id: "+funcionId+" no encontrada"));

        return ScreeningMappers.toDto(model);
    }

    private String generarCodigo() {
        StringBuilder sb = new StringBuilder("MHK-");
        for (int i = 0; i < 6; i++) {
            sb.append(ALFABETO_CODIGO.charAt(aleatorio.nextInt(ALFABETO_CODIGO.length())));
        }
        return sb.toString();
    }
}
