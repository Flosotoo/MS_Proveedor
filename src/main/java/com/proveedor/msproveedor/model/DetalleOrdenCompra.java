package com.proveedor.msproveedor.model;

import java.math.BigDecimal;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(name = "detalles_orden_compra")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DetalleOrdenCompra {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "La orden de compra es obligatoria")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "orden_compra_id", nullable = false)
    private OrdenCompra ordenCompra;

    @NotBlank(message = "El SKU del producto no puede ir vacio")
    @Size(min = 3, max = 50, message = "El SKU debe tener entre 3 a 50 caracteres")
    @Column(nullable = false, name = "sku")
    private String skuProducto;

    @NotBlank(message = "El nombre del producto no puede ir vacio")
    @Size(max = 150, message = "El nombre del producto no puede exceder los 150 caracteres")
    @Column(nullable = false, name = "nombre_producto")
    private String nombreProducto;

    @Min(value = 1, message = "La cantidad debe ser minimo 1")
    @Column(nullable = false, name = "cantidad")
    private int cantidad;

    @NotNull(message = "El precio unitario es obligatorio")
    @Min(value = 1, message = "El precio unitario debe ser minimo 1 peso")
    @Digits(integer = 10, fraction = 0, message = "El precio unitario no puede tener mas de 10 digitos, sin decimales")
    @Column(nullable = false, name = "precio_unitario", precision = 10, scale = 0)
    private BigDecimal precioUnitario;

    @Column(nullable = false, name = "total", precision = 12, scale = 0)
    private BigDecimal total;

    @PrePersist
    @PreUpdate
    public void calcularTotal() {
        if(this.precioUnitario != null && this.cantidad > 0) {
            this.total = this.precioUnitario.multiply(BigDecimal.valueOf(this.cantidad));
        }
    }
}
