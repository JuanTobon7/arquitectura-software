package co.edu.demo.observabilidad;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    private final PedidoRepository repositorio;

    public PedidoController(PedidoRepository repositorio) {
        this.repositorio = repositorio;
    }

    /** Problema N+1: 1 consulta para los pedidos + 1 consulta por cada pedido para sus items. */
    @GetMapping("/n-mas-1")
    @Transactional(readOnly = true)
    public List<Map<String, Object>> listarConNMas1() {
        return resumir(repositorio.findAll());
    }

    /** Corrección: JOIN FETCH, una sola consulta. */
    @GetMapping("/optimizado")
    @Transactional(readOnly = true)
    public List<Map<String, Object>> listarOptimizado() {
        return resumir(repositorio.findAllConItems());
    }

    private List<Map<String, Object>> resumir(List<Pedido> pedidos) {
        return pedidos.stream()
                .map(p -> Map.<String, Object>of(
                        "id", p.getId(),
                        "cliente", p.getCliente(),
                        "total", p.getItems().stream().mapToDouble(ItemPedido::subtotal).sum()))
                .toList();
    }
}
