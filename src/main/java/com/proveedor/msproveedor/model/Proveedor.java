package com.proveedor.msproveedor.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor

public class Proveedor {
    @Id
    private String rutProveedor;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false, unique = true)
    private String correo;

    @Column(nullable = false, length = 12)
    private String telefono;

    @Column(nullable = false)
    private String direccion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoProveedor estado = EstadoProveedor.ACTIVO;

    public Proveedor(String correo, String direccion, String nombre, String rutProveedor, String telefono) {
        this.correo = correo;
        this.direccion = direccion;
        this.nombre = nombre;
        this.rutProveedor = rutProveedor;
        this.telefono = telefono;
    }
}
