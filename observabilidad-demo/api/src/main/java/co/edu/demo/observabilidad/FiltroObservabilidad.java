package co.edu.demo.observabilidad;

import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerMapping;

import java.io.IOException;
import java.util.UUID;

/**
 * Por cada request: genera un requestId, mide la duración, cuenta las consultas SQL
 * y deja un log estructurado que luego se consulta en Loki.
 */
@Component
public class FiltroObservabilidad extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(FiltroObservabilidad.class);

    private final MeterRegistry registry;

    public FiltroObservabilidad(MeterRegistry registry) {
        this.registry = registry;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return request.getRequestURI().startsWith("/actuator");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        long inicio = System.nanoTime();
        MDC.put("request_id", UUID.randomUUID().toString().substring(0, 8));
        ContadorSql.consultasYReiniciar();
        try {
            chain.doFilter(request, response);
        } finally {
            long duracionMs = (System.nanoTime() - inicio) / 1_000_000;
            int consultas = ContadorSql.consultasYReiniciar();
            Object patron = request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
            String uri = patron != null ? patron.toString() : "UNKNOWN";

            DistributionSummary.builder("sql.consultas.por.request")
                    .description("Sentencias SQL ejecutadas por request")
                    .tag("uri", uri)
                    .register(registry)
                    .record(consultas);

            MDC.put("uri", uri);
            MDC.put("metodo", request.getMethod());
            MDC.put("status", String.valueOf(response.getStatus()));
            MDC.put("duracion_ms", String.valueOf(duracionMs));
            MDC.put("sql_consultas", String.valueOf(consultas));
            if (duracionMs > 500) {
                log.warn("request lento {} {} -> {} en {} ms ({} consultas SQL)",
                        request.getMethod(), uri, response.getStatus(), duracionMs, consultas);
            } else {
                log.info("request {} {} -> {} en {} ms ({} consultas SQL)",
                        request.getMethod(), uri, response.getStatus(), duracionMs, consultas);
            }
            MDC.clear();
        }
    }
}
