package com.cinecapas.application.components.contract;

public interface EncryptPasswords {
    /**
     *
     * @param claveTextoPlano
     * @return
     */
    String codificar(String claveTextoPlano);

    /**
     *
     * @param claveTextoPlano
     * @param claveHash
     * @return
     */
    boolean coincide(String claveTextoPlano, String claveHash);
}
