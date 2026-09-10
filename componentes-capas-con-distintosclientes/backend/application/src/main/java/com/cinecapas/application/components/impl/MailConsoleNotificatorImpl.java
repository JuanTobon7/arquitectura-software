package com.cinecapas.application.components.impl;

import com.cinecapas.application.components.contract.MailNotificator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MailConsoleNotificatorImpl implements MailNotificator {
    private static final Logger log = LoggerFactory.getLogger(MailConsoleNotificatorImpl.class);

    @Override
    public void enviar(String destinatario, String asunto, String cuerpo) {
        if (destinatario == null || destinatario.isBlank()) {
            return;
        }
        log.info("""
            ================= CORREO (modo consola) =================
            Para   : {}
            Asunto : {}
            cuerpo: {}
            ----------------------------------------------------------
            ==========================================================""",
            destinatario, asunto, cuerpo);
    }
}
