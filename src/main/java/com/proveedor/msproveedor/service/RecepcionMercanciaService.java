package com.proveedor.msproveedor.service;
import com.proveedor.msproveedor.model.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;

@Service
public class RecepcionMercanciaService {
    @Autowired
    private RecepcionMercanciaRepository recepcionMercanciaRepository;

    @Autowired
    private OrdenCompraRepository ordenCompraRepository;

    public RecepcionMercancia registrarRecepcion(RecepcionMercancia recepcion) {
    OrdenCompra orden = ordenCompraRepository.findById(recepcion.getOrdenCompra().getId())
            .orElseThrow(() -> new RuntimeException("Orden de compra no encontrada"));
            if(orden.getEstado() != EstadoOrden.AUTORIZADA) {
                throw new RuntimeException("Solo se pueden registrar recepciones para órdenes autorizadas");
            }
    List<ItemStockDTO> items = recepcion.getDetalles().stream()
            .map(det -> {
                ItemStockDTO item = new ItemStockDTO();
                item.setSkuProducto(det.getSkuProducto());
                item.setCantidadRecibida(det.getCantidadRecibida());
                return item;
            })
            .toList();
    
    }
}
