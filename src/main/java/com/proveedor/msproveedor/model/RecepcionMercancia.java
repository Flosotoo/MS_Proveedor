package com.proveedor.msproveedor.model;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Entity
@Table(name = "recepciones_mercancia")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class RecepcionMercancia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull(message = "La orden de compra asociada es obligatoria")
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "orden_compra_id", nullable = false, unique = true)
    private OrdenCompra ordenCompra;

    @Column(name = "fecha_recepcion", updatable = false)
    private LocalDateTime fechaRecepcion;

    @Size(max = 500, message = "Las observaciones no pueden exceder los 500 caracteres")
    @Column(name = "observaciones", columnDefinition = "TEXT", nullable = true)
    private String observaciones;

    @NotEmpty(message = "La recepción debe incluir al menos un producto detallado")
    @OneToMany(mappedBy = "recepcion", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<@Valid DetalleRecepcion> detalles;
    @PrePersist
    public void prePersist() {
        this.fechaRecepcion = LocalDateTime.now();
    }
}
