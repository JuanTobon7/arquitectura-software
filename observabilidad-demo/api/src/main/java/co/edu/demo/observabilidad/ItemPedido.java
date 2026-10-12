package co.edu.demo.observabilidad;

import jakarta.persistence.*;

@Entity
public class ItemPedido {

    @Id
    @GeneratedValue
    private Long id;

    private String producto;

    private int cantidad;

    private double precio;

    @ManyToOne(fetch = FetchType.LAZY)
    private Pedido pedido;

    protected ItemPedido() {
    }

    public ItemPedido(String producto, int cantidad, double precio) {
        this.producto = producto;
        this.cantidad = cantidad;
        this.precio = precio;
    }

    public double subtotal() {
        return cantidad * precio;
    }

    void setPedido(Pedido pedido) {
        this.pedido = pedido;
    }
}
