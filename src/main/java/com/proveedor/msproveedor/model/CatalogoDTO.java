package com.proveedor.msproveedor.model;

import lombok.Data;

@Data
public class CatalogoDTO {
    private Long idProducto;
    private String nombre;
    private String descripcion;
    private double precio;
}
