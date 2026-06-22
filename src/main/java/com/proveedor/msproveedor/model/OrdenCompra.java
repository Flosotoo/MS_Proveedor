package com.proveedor.msproveedor.model;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
@Entity
@Table(name = "orden_compra")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrdenCompra {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idOrden;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_proveedor", nullable = false, foreignKey = @ForeignKey(name = "fk_orden_proveedor"))
    private Proveedor proveedor;

    @NotNull(message = "La sucursal es obligatoria")
    @Positive(message = "El id de sucursal debe ser un número positivo")
    @Column(name = "id_sucursal", nullable = false)
    private Long idSucursal;

    @Column(nullable = false)
    private LocalDateTime fechaSolicitud;

    @Column(nullable = true)
    private LocalDateTime fechaRecepcion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstadoOrden estado;

    @Column(nullable = false, precision = 10, scale = 0)
    private BigDecimal total;

    @OneToMany(mappedBy = "ordenCompra", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<DetalleOrden> detalles;

}
