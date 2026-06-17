package com.proveedor.msproveedor.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(name = "detalles_recepcion")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DetalleRecepcion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "La recepción de mercancia asociada es obligatoria")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recepcion_id", nullable = false)
    private RecepcionMercancia recepcion;

    @NotBlank(message = "El SKU del producto no puede ir vacio")
    @Size(min = 3, max = 50, message = "El SKU debe tener entre 3 a 50 caracteres")
    @Column(nullable = false, name = "sku_producto")
    private String skuProducto;

    @NotNull(message = "La cantidad recibida es obligatoria")
    @Min(value = 1, message = "La cantidad recibida debe ser un número positivo y mayor a 0")
    @Column(nullable = false, name = "cantidad_recibida")
    private Integer cantidadRecibida;
}
