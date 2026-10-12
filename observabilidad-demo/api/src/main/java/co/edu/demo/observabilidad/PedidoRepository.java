package co.edu.demo.observabilidad;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    // Versión corregida: una sola consulta con JOIN FETCH
    @Query("select distinct p from Pedido p left join fetch p.items")
    List<Pedido> findAllConItems();
}
