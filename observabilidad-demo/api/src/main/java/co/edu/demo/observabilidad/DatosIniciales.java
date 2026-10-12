package co.edu.demo.observabilidad;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class DatosIniciales implements CommandLineRunner {

    private static final String[] PRODUCTOS = {"teclado", "mouse", "monitor", "audifonos", "webcam", "silla"};

    private final PedidoRepository repositorio;

    public DatosIniciales(PedidoRepository repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    public void run(String... args) {
        Random random = new Random(42);
        for (int i = 1; i <= 100; i++) {
            Pedido pedido = new Pedido("cliente-" + i);
            for (int j = 0; j < 3; j++) {
                pedido.agregarItem(new ItemPedido(PRODUCTOS[random.nextInt(PRODUCTOS.length)],
                        1 + random.nextInt(3), 10 + random.nextInt(200)));
            }
            repositorio.save(pedido);
        }
    }
}
