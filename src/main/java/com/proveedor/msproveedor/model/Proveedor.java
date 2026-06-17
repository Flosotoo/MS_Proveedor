package com.proveedor.msproveedor.model;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "proveedores")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class Proveedor {
    @Id
    @NotBlank(message = "El RUT es obligatorio")
    private String rut;

    @Column(name = "razon_social", nullable = false)
    @NotBlank(message = "La razón social es obligatoria")
    @Size(max = 150, message = "La razón social no puede exceder los 150 caracteres")
    private String razonSocial;

    @Column(name = "direccion", nullable = false)
    @Size(max = 255, message = "La dirección no puede exceder los 255 caracteres")
    @NotBlank(message = "La dirección es obligatoria")
    private String direccion;

    @Column(name = "telefono", nullable = false)
    @NotBlank(message = "El teléfono es obligatorio")
    private String telefono;

    @Column(name = "correo", nullable = false)
    @NotBlank(message = "El correo es obligatorio")
    private String correo;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_proveedor", nullable = false)
    @Builder.Default
    private EstadoProveedor estado = EstadoProveedor.ACTIVO;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @PrePersist
    public void prePersist() {
        this.fechaCreacion = LocalDateTime.now();
    }
    @OneToMany(mappedBy = "proveedor", fetch = FetchType.LAZY)
    private List<OrdenCompra> ordenesDeCompra;
}
