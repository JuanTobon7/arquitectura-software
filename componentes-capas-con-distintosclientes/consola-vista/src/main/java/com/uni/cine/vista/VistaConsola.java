package com.uni.cine.vista;

import java.util.List;
import java.util.Scanner;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.cinecapas.application.dto.MovieDto;
import com.cinecapas.application.dto.ReservationRequestDto;
import com.cinecapas.application.dto.ReservationsDto;
import com.cinecapas.application.dto.ScreeningDto;
import com.cinecapas.application.dto.SeatsAvailabilityDto;
import com.cinecapas.application.dto.StoreMovieDto;
import com.cinecapas.application.services.contracts.MoviesService;
import com.cinecapas.application.services.contracts.ReservationsManagmentScreeningService;

@Component
public class VistaConsola implements CommandLineRunner {

    private final MoviesService moviesService;
    private final ReservationsManagmentScreeningService reservationsService;

    public VistaConsola(MoviesService moviesService,
                        ReservationsManagmentScreeningService reservationsService) {
        this.moviesService = moviesService;
        this.reservationsService = reservationsService;
    }

    /* ────────────────── flujo principal ────────────────── */

    @Override
    public void run(String... args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("""

                =============================================
                   SISTEMA DE GESTIÓN DE CINE (consola)
                   Películas → MySQL | Reservas → H2
                =============================================""");

        boolean salir = false;
        while (!salir && scanner.hasNextLine()) {
            imprimirMenu();
            String opcion = scanner.nextLine().trim();
            try {
                switch (opcion) {
                    case "1" -> registrarPelicula(scanner);
                    case "2" -> listarPeliculas();
                    case "3" -> buscarPorGenero(scanner);
                    case "4" -> listarFunciones(scanner);
                    case "5" -> crearReserva(scanner);
                    case "6" -> cancelarReserva(scanner);
                    case "7" -> consultarDisponibilidad(scanner);
                    case "0" -> salir = true;
                    default -> System.out.println(">> Opción no válida, intente de nuevo.");
                }
            } catch (IllegalArgumentException | IllegalStateException e) {
                System.out.println(">> ERROR: " + e.getMessage());
            }
        }
        System.out.println("¡Hasta pronto!");
    }

    /* ────────────────── menú ────────────────── */

    private void imprimirMenu() {
        System.out.println("""

                --------- MENÚ PRINCIPAL ---------
                  PELÍCULAS (MySQL)
                    1. Registrar película
                    2. Listar películas
                    3. Buscar películas por género
                  FUNCIONES / RESERVAS (H2)
                    4. Ver funciones de una película
                    5. Crear reserva de asiento
                    6. Cancelar reserva
                    7. Consultar disponibilidad de una función
                  0. Salir
                ----------------------------------""");
        System.out.print("Elija una opción: ");
    }

    /* ────────────────── películas ────────────────── */

    private void registrarPelicula(Scanner scanner) {
        System.out.print("Título: ");
        String titulo = scanner.nextLine();
        System.out.print("Género: ");
        String genero = scanner.nextLine();
        System.out.print("Duración (minutos): ");
        int duracion = leerEntero(scanner);
        System.out.print("Formatos (separados por coma, ej: 2D,3D,IMAX): ");
        String formatos = scanner.nextLine();
        List<String> listaFormatos = List.of(formatos.split(",")).stream()
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();

        StoreMovieDto datos = new StoreMovieDto(
                titulo, null, genero, null, duracion, 0.0, listaFormatos, null);

        MovieDto creada = moviesService.registrar(datos);
        System.out.println(">> Película registrada: " + formatear(creada));
    }

    private void listarPeliculas() {
        List<MovieDto> peliculas = moviesService.buscar(null, "todas");
        if (peliculas.isEmpty()) {
            System.out.println(">> No hay películas registradas.");
            return;
        }
        System.out.println(">> Cartelera (" + peliculas.size() + "):");
        peliculas.forEach(p -> System.out.println("   " + formatear(p)));
    }

    private void buscarPorGenero(Scanner scanner) {
        System.out.print("Género a buscar: ");
        String genero = scanner.nextLine();
        List<MovieDto> encontradas = moviesService.buscar(genero, "todas");
        if (encontradas.isEmpty()) {
            System.out.println(">> No hay películas del género '" + genero + "'.");
            return;
        }
        System.out.println(">> Películas de '" + genero + "':");
        encontradas.forEach(p -> System.out.println("   " + formatear(p)));
    }

    /* ────────────────── funciones ────────────────── */

    private void listarFunciones(Scanner scanner) {
        System.out.print("Id de la película: ");
        long peliculaId = leerEntero(scanner);
        List<ScreeningDto> funciones = reservationsService.funcionesDePelicula(peliculaId);
        if (funciones.isEmpty()) {
            System.out.println(">> No hay funciones para la película " + peliculaId + ".");
            return;
        }
        System.out.println(">> Funciones de la película " + peliculaId + ":");
        funciones.forEach(f -> System.out.println("   " + formatear(f)));
    }

    /* ────────────────── reservas ────────────────── */

    private void crearReserva(Scanner scanner) {
        System.out.print("Id de la función: ");
        long funcionId = leerEntero(scanner);
        System.out.print("Nombre del cliente: ");
        String nombre = scanner.nextLine();
        System.out.print("Email del cliente: ");
        String email = scanner.nextLine();
        System.out.print("Asientos (separados por coma, ej: A1,A2): ");
        String asientos = scanner.nextLine();
        List<String> listaAsientos = List.of(asientos.split(",")).stream()
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();

        ReservationRequestDto solicitud = new ReservationRequestDto();
        solicitud.setScreeningId(funcionId);
        solicitud.setNombreCliente(nombre);
        solicitud.setEmail(email);
        solicitud.setAsiento(listaAsientos);

        ReservationsDto creada = reservationsService.crear(solicitud);
        System.out.println(">> Reserva creada: " + formatear(creada));
    }

    private void cancelarReserva(Scanner scanner) {
        System.out.print("Id de la reserva a cancelar: ");
        long id = leerEntero(scanner);
        reservationsService.cancelar(id);
        System.out.println(">> Reserva " + id + " cancelada correctamente.");
    }

    private void consultarDisponibilidad(Scanner scanner) {
        System.out.print("Id de la función: ");
        long funcionId = leerEntero(scanner);
        SeatsAvailabilityDto disponibilidad = reservationsService.disponibilidad(funcionId);
        int total = disponibilidad.getFilas() * disponibilidad.getColumnas();
        int ocupados = disponibilidad.getOcupado().size();
        int libres = total - ocupados;
        System.out.println(">> Función " + funcionId
                + " — " + libres + "/" + total + " asientos libres:");
        if (!disponibilidad.getOcupado().isEmpty()) {
            System.out.println("   Ocupados: " + String.join(" ", disponibilidad.getOcupado()));
        }
    }

    /* ────────────────── formateo ────────────────── */

    private String formatear(MovieDto p) {
        return "[%d] %s (%s, %d min) — %s".formatted(
                p.getId(), p.getTitulo(), p.getGenero(),
                p.getDuracionMinutos(),
                p.getFormatos() != null ? String.join(", ", p.getFormatos()) : "N/A");
    }

    private String formatear(ScreeningDto f) {
        return "[Función %d] Sala %s | %s | %s | $%.2f".formatted(
                f.getId(), f.getSala(),
                f.getFechaHora() != null ? f.getFechaHora().toString() : "?",
                f.getFormato(), f.getPreci());
    }

    private String formatear(ReservationsDto r) {
        return "[%d] código %s | función %d | asientos %s | cliente %s (%s)".formatted(
                r.getId(), r.getCodigo(), r.getFuncionId(),
                r.getAsientos() != null ? String.join(", ", r.getAsientos()) : "?",
                r.getNombreCliente(), r.getEstado());
    }

    private int leerEntero(Scanner scanner) {
        String texto = scanner.nextLine().trim();
        try {
            return Integer.parseInt(texto);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("'" + texto + "' no es un número válido");
        }
    }
}
