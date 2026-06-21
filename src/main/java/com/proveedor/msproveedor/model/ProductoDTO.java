package com.proveedor.msproveedor.model;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class ProductoDTO {
    private Long idProducto;
    private String sku;
    private String nombre;
    private BigDecimal precio;
    private String categoria;
    private String estado;
}
