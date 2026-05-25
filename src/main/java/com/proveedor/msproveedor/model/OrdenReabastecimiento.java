package com.proveedor.msproveedor.model;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor

public class OrdenReabastecimiento {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long idOrden;

    @Column(nullable = false)
    private String rutProveedor;

    @Column(nullable = false)
    private Long idSucursal;

    @Column(nullable = true)
    private LocalDate fechaSolicitud;

    @Column(nullable = true)
    private LocalDate fechaRecepcion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoOrden estado = EstadoOrden.PENDIENTE;

    @Column(nullable = false)
    private double total;

    public OrdenReabastecimiento(LocalDate fechaRecepcion, LocalDate fechaSolicitud, Long idOrden, Long idSucursal, String rutProveedor, double total) {
        this.fechaRecepcion = fechaRecepcion;
        this.fechaSolicitud = fechaSolicitud;
        this.idOrden = idOrden;
        this.idSucursal = idSucursal;
        this.rutProveedor = rutProveedor;
        this.total = total;
    }
}
