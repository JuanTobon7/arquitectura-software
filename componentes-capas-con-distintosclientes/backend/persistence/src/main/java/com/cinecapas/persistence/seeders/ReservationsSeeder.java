package com.cinecapas.persistence.seeders;

import com.cinecapas.persistence.daos.ReservationModelDao;
import com.cinecapas.persistence.daos.ScreeningModelDao;
import com.cinecapas.persistence.repos.ReservationModelDaoRepository;
import com.cinecapas.persistence.repos.ScreeningModelDaoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationsSeeder implements CommandLineRunner {

    private final ReservationModelDaoRepository reservationsRepository;
    private final ScreeningModelDaoRepository screeningsRepository;

    private static final String ALFABETO = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final SecureRandom RNG = new SecureRandom();

    private record ClienteSeed(String nombre, String email, List<String> asientos) {}

    private static final List<ClienteSeed> CLIENTES = List.of(
        new ClienteSeed("Carlos Martínez",  "carlos@test.com",   List.of("A1", "A2", "A3")),
        new ClienteSeed("Laura Rodríguez",  "laura@test.com",    List.of("B5", "B6")),
        new ClienteSeed("Pedro Sánchez",    "pedro@test.com",    List.of("C10", "D10", "D11", "E10")),
        new ClienteSeed("Ana López",        "ana@test.com",      List.of("F1", "F2")),
        new ClienteSeed("Diego Herrera",    "diego@test.com",    List.of("G7", "G8", "H7", "H8"))
    );

    @Override
    public void run(String... args) {
        long existentes = reservationsRepository.count();
        if (existentes > 0) {
            log.info("[ReservationsSeeder] Ya existen {} reservas, se omite el seed.", existentes);
            return;
        }

        List<ScreeningModelDao> screenings = screeningsRepository.findAll();
        if (screenings.isEmpty()) {
            log.info("[ReservationsSeeder] No hay funciones disponibles, se omite el seed.");
            return;
        }

        int total = Math.min(CLIENTES.size(), screenings.size());
        for (int i = 0; i < total; i++) {
            ScreeningModelDao screening = screenings.get(i);
            ClienteSeed cliente = CLIENTES.get(i);

            ReservationModelDao reserva = ReservationModelDao.builder()
                .funcionId(screening.getId())
                .nombreCliente(cliente.nombre())
                .email(cliente.email())
                .codigo(generarCodigo())
                .asientos(cliente.asientos())
                .creadaEn(LocalDateTime.now())
                .estado("ACTIVA")
                .build();

            reservationsRepository.save(reserva);
            log.info("[ReservationsSeeder] Reserva creada: {} → función {} (asientos: {})",
                cliente.nombre(), screening.getId(), cliente.asientos());
        }
    }

    private String generarCodigo() {
        StringBuilder sb = new StringBuilder("MHK-");
        for (int i = 0; i < 6; i++) {
            sb.append(ALFABETO.charAt(RNG.nextInt(ALFABETO.length())));
        }
        return sb.toString();
    }
}
