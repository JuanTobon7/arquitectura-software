package co.edu.demo.observabilidad;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Pedido {

    @Id
    @GeneratedValue
    private Long id;

    private String cliente;

    // LAZY: cada acceso a items dispara una consulta extra si no se hace JOIN FETCH
    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ItemPedido> items = new ArrayList<>();

    protected Pedido() {
    }

    public Pedido(String cliente) {
        this.cliente = cliente;
    }

    public void agregarItem(ItemPedido item) {
        item.setPedido(this);
        items.add(item);
    }

    public Long getId() {
        return id;
    }

    public String getCliente() {
        return cliente;
    }

    public List<ItemPedido> getItems() {
        return items;
    }
}
