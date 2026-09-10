package com.cinecapas.application.components.impl;

import com.cinecapas.application.components.contract.MailNotificator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

public class MailNotificatorImpl implements MailNotificator {
    private static final Logger log = LoggerFactory.getLogger(MailNotificatorImpl.class);

    private final JavaMailSender sender;
    private final String remitente;

    public MailNotificatorImpl(JavaMailSender sender, String remitente) {
        this.sender = sender;
        this.remitente = remitente;
    }

    @Override
    public void enviar(String destinatario, String asunto, String cuerpo) {
        if (destinatario == null || destinatario.isBlank()) {
            return;
        }
        try {
            SimpleMailMessage mensaje = new SimpleMailMessage();
            mensaje.setFrom(remitente);
            mensaje.setTo(destinatario);
            mensaje.setSubject(asunto);
            mensaje.setText(cuerpo);
            sender.send(mensaje);
            log.info("Correo enviado a {} — {}", destinatario, asunto);
        } catch (Exception e) {
            log.error("No se pudo enviar el correo a {}: {}", destinatario, e.getMessage());
        }
    }
}
