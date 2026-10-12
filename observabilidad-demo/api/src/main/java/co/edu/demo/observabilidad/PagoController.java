package co.edu.demo.observabilidad;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@RestController
@RequestMapping("/api")
public class PagoController {

    private static final Logger log = LoggerFactory.getLogger(PagoController.class);

    private final Counter pagosAprobados;
    private final Counter pagosFallidos;

    public PagoController(MeterRegistry registry) {
        this.pagosAprobados = Counter.builder("pagos.procesados").tag("resultado", "aprobado").register(registry);
        this.pagosFallidos = Counter.builder("pagos.procesados").tag("resultado", "fallido").register(registry);
    }

    /**
     * Latencia simulada de una pasarela externa:
     * 90% rápida (20-80 ms), 8% lenta (200-500 ms), 2% muy lenta (1.2-2 s) y 4% de errores.
     */
    @PostMapping("/pagos")
    public Map<String, Object> pagar() throws InterruptedException {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        double suerte = random.nextDouble();
        long espera;
        if (suerte < 0.90) {
            espera = random.nextLong(20, 80);
        } else if (suerte < 0.98) {
            espera = random.nextLong(200, 500);
        } else {
            espera = random.nextLong(1200, 2000);
        }
        Thread.sleep(espera);

        if (random.nextDouble() < 0.04) {
            pagosFallidos.increment();
            throw new PasarelaException("La pasarela de pagos respondio timeout");
        }
        pagosAprobados.increment();
        return Map.of("estado", "aprobado", "latencia_pasarela_ms", espera);
    }

    /** Endpoint sano de referencia. */
    @GetMapping("/catalogo")
    public Map<String, Object> catalogo() {
        return Map.of("productos", 6, "moneda", "COP");
    }

    @ExceptionHandler(PasarelaException.class)
    public ResponseEntity<Map<String, String>> manejarError(PasarelaException e) {
        log.error("Error procesando pago: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
    }

    static class PasarelaException extends RuntimeException {
        PasarelaException(String mensaje) {
            super(mensaje);
        }
    }
}
