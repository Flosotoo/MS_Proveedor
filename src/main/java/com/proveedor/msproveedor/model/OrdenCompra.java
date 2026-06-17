package com.proveedor.msproveedor.model;

import java.time.LocalDateTime;
import jakarta.validation.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ordenes_compra")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class OrdenCompra {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El proveedor es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "rut", nullable = false)
    private Proveedor proveedor;

    @NotNull(message = "La sucursal es obligatoria")
    @Positive(message = "El ID de la sucursal debe ser un número positivo")
    @Column(nullable = false)
    private Long sucursalId;

    @NotNull(message = "El usuario que crea la orden es obligatorio")
    @Positive(message = "El ID del usuario debe ser un número positivo")
    @Column(name = "creado_por_usuario_id", nullable = false, updatable = false)
    private Long creadoPorUsuarioId;

    @Positive(message = "El ID del usuario que autoriza debe ser un número positivo")
    @Column(name = "autorizado_por_usuario_id")
    private Long autorizadoPorUsuarioId;

    @NotNull(message = "El estado de la orden es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private  EstadoOrden estado = EstadoOrden.PENDIENTE_AUTORIZACION;

    @Column(name = "fecha_solicitud", nullable = false, updatable = false)
    private LocalDateTime fechaSolicitud;

    @Column(name = "fecha_autorizacion")
    private LocalDateTime fechaAutorizacion;

    @PrePersist
    public void prePersist() {
        this.fechaSolicitud = LocalDateTime.now();
    }

    @NotEmpty(message = "La orden de compra debe tener al menos un producto")
    @OneToMany(mappedBy = "ordenCompra", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<@Valid DetalleOrdenCompra> detalles;
}
