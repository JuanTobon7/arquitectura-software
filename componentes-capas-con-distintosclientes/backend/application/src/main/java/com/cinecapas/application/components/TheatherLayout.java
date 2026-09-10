package com.cinecapas.application.components;

import com.cinecapas.application.exceptions.InvalidRequestException;

import java.util.List;
import java.util.regex.Pattern;

public final class TheatherLayout {
    private static final Pattern FORMATO_ASIENTO = Pattern.compile("^([A-Z])(\\d{1,2})$");

    private final int filas;
    private final int columnas;

    public TheatherLayout(int filas, int columnas) {
        this.filas = filas;
        this.columnas = columnas;
    }

    public void validar(List<String> asientos) {
        if (asientos.isEmpty()) {
            throw new InvalidRequestException("Debe seleccionar al menos un asiento");
        }
        for (String asiento : asientos) {
            var m = FORMATO_ASIENTO.matcher(asiento == null ? "" : asiento.trim().toUpperCase());
            if (!m.matches()) {
                throw new InvalidRequestException("Etiqueta de asiento inválida: " + asiento);
            }
            int fila = m.group(1).charAt(0) - 'A' + 1;
            int columna = Integer.parseInt(m.group(2));
            if (fila > filas || columna < 1 || columna > columnas) {
                throw new InvalidRequestException("El asiento " + asiento + " no existe en la sala");
            }
        }
    }
}
