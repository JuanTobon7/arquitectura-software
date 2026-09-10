package com.cinecapas.application.config;

import com.cinecapas.application.components.contract.MailNotificator;
import com.cinecapas.application.components.impl.MailConsoleNotificatorImpl;
import com.cinecapas.application.components.impl.MailNotificatorImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;

@Configuration
public class MailSenderConfig {
    // spring.mail.host con valor real (p.ej. smtp.gmail.com) → correo SMTP
    // spring.mail.host vacío o inexistente → consola (fallback)
    @Bean
    @ConditionalOnExpression("T(org.springframework.util.StringUtils).hasText('${spring.mail.host:}')")
    public MailNotificator notificadorSmtp(
            JavaMailSender sender,
            @Value("${app.correo.remitente:no-reply@micoshakari.cine}") String remitente) {
        return new MailNotificatorImpl(sender, remitente);
    }

    @Bean
    @ConditionalOnMissingBean(MailNotificator.class)
    public MailNotificator notificadorConsola() {
        return new MailConsoleNotificatorImpl();
    }
}
