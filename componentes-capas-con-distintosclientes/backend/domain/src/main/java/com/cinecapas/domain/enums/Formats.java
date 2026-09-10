package com.cinecapas.domain.enums;

import java.util.Arrays;
import java.util.Optional;

public enum Formats {
    DOS_D("2D"),
    TRES_D("3D"),
    IMAX("IMAX"),
    IMAX_3D("IMAX 3D"),
    CUATRO_D("4D");

    private final String etiqueta;

    Formats(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String etiqueta() {
        return etiqueta;
    }

    public static Optional<Formats> desdeEtiqueta(String texto) {
        if (texto == null || texto.isBlank()) {
            return Optional.empty();
        }
        String normalizado = texto.trim().toUpperCase().replace(" ", "").replace("_", "");
        return Arrays.stream(values())
                .filter(f -> f.etiqueta.replace(" ", "").equals(normalizado) || f.name().replace("_", "").equals(normalizado))
                .findFirst();
    }
}
