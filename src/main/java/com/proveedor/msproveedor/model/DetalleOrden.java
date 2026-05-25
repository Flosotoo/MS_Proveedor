package com.proveedor.msproveedor.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DetalleOrden {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idDetalle;

    @Column(nullable = false)
    private Long idOrden;

    @Column(nullable = false)
    private Long idProducto;

    @Column(nullable = false)
    private int cantidad;

    @Column(nullable = true, length = 255)
    private String detalle;

    @Column(nullable = false)
    private double precioUnitario;
}
