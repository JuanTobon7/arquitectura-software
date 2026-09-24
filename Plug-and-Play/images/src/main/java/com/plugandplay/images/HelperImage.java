package com.plugandplay.images;

public final class HelperImage {
    public static String getFormat(String nombre) {
        int punto = nombre.lastIndexOf('.');

        if (punto == -1 || punto == nombre.length() - 1) {
            throw new IllegalArgumentException(
                    "El archivo no tiene una extensión válida: " + nombre
            );
        }

        String extension = nombre
                .substring(punto + 1)
                .toLowerCase();

        return switch (extension) {
            case "jpg", "jpeg" -> "jpg";
            case "png" -> "png";
            default -> throw new IllegalArgumentException(
                    "Formato de imagen no soportado: " + extension
            );
        };
    }
}
