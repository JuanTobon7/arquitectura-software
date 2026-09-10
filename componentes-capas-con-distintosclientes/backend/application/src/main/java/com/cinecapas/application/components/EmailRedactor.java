package com.cinecapas.application.components;

import com.cinecapas.application.dto.UserDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class EmailRedactor {
    public record Correo(String asunto, String cuerpo) {
    }

    public record FuncionInfo(LocalDateTime fechaHora, String sala, String formato, double precio) {
    }

    public record ReservaConfirmada(String nombreCliente, String codigo, List<String> asientos) {
    }

    private static final DateTimeFormatter FECHA = DateTimeFormatter
            .ofPattern("EEEE d 'de' MMMM · HH:mm", Locale.of("es"));

    private EmailRedactor() {
    }

    public static Correo bienvenida(UserDto usuario) {
        return new Correo(
                "Bienvenido a MICOS HAKARI, " + usuario.getNombre(),
                """
                Hola %s:

                Tu cuenta en la red MICOS HAKARI quedo creada con este correo (%s).

                Ya puedes reservar asientos en la cartelera: al iniciar sesion,
                tus datos se llenan solos en cada reserva.

                Nos vemos en la proxima dimension.
                — MICOS HAKARI
                """.formatted(usuario.getNombre(), usuario.getEmail()));
    }

    public static Correo boleta(ReservaConfirmada reserva, FuncionInfo funcion, String tituloPelicula) {
        double total = funcion.precio() * reserva.asientos().size();
        return new Correo(
                "Tu boleta MICOS HAKARI — " + reserva.codigo(),
                """
                Hola %s:

                ¡Reserva confirmada! Esta es tu boleta:

                  Pelicula : %s
                  Funcion  : %s
                  Sala     : %s (%s)
                  Asientos : %s
                  Total    : %.2f

                  CODIGO   : %s

                Presenta este codigo en taquilla para reclamar tus entradas.
                Puedes cancelar la reserva desde la misma pantalla de asientos.

                — MICOS HAKARI
                """.formatted(
                        reserva.nombreCliente(),
                        tituloPelicula,
                        FECHA.format(funcion.fechaHora()),
                        funcion.sala(),
                        funcion.formato(),
                        String.join(", ", reserva.asientos()),
                        total,
                        reserva.codigo()));
    }

    public static Correo cancelacion(ReservaConfirmada reserva) {
        return new Correo(
                "Reserva cancelada — " + reserva.codigo(),
                """
                Hola %s:

                Tu reserva %s fue cancelada y los asientos %s quedaron libres.

                Esperamos verte pronto en otra funcion.
                — MICOS HAKARI
                """.formatted(
                        reserva.nombreCliente(),
                        reserva.codigo(),
                        String.join(", ", reserva.asientos())));
    }
}
