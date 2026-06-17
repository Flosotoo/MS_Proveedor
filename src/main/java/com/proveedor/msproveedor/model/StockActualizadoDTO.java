package com.proveedor.msproveedor.model;

import java.util.List;

import lombok.Data;

@Data
public class StockActualizadoDTO {
    private boolean exitoso;
    private List<String> itemsActualizados;
    private List<String> itemsFallidos;
}
