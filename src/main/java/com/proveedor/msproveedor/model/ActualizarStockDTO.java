package com.proveedor.msproveedor.model;

import java.util.List;

import lombok.Data;

@Data
public class ActualizarStockDTO {
    private Long sucursalId;
    private List<ItemStockDTO> items;
}
