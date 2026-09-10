package com.cinecapas.application.components.contract;

public interface MailNotificator {
    void enviar(String destinatario, String asunto, String cuerpo);
}
