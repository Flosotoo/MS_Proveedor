package com.proveedor.msproveedor.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proveedor.msproveedor.exception.EstadoInvalidoException;
import com.proveedor.msproveedor.model.DetalleOrden;
import com.proveedor.msproveedor.model.EstadoOrden;
import com.proveedor.msproveedor.model.OrdenCompra;
import com.proveedor.msproveedor.model.Proveedor;
import com.proveedor.msproveedor.service.OrdenCompraService;

@WebMvcTest(OrdenCompraController.class)
class OrdenCompraControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrdenCompraService ordenCompraService;

    private ObjectMapper objectMapper = new ObjectMapper();

    private OrdenCompra crearOrden() {
        DetalleOrden detalle = new DetalleOrden();
        detalle.setIdProducto(1L);
        detalle.setCantidad(10);
        detalle.setPrecioUnitario(new BigDecimal("30000"));

        Proveedor proveedor = new Proveedor();
        proveedor.setIdProveedor(1L);

        OrdenCompra orden = new OrdenCompra();
        orden.setIdOrden(1L);
        orden.setProveedor(proveedor);
        orden.setIdSucursal(1L);
        orden.setEstado(EstadoOrden.PENDIENTE_AUTORIZACION);
        orden.setTotal(new BigDecimal("300000"));
        orden.setDetalles(List.of(detalle));
        return orden;
    }

    @Test
    void testPostOrdenCompra_devuelve201() throws Exception {
        OrdenCompra orden = crearOrden();
        when(ordenCompraService.crearOrdenCompra(any(OrdenCompra.class))).thenReturn(orden);
        mockMvc.perform(post("/api/ordenes-compra")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orden)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idOrden").value(1))
                .andExpect(jsonPath("$.total").value(300000));
        verify(ordenCompraService, times(1)).crearOrdenCompra(any(OrdenCompra.class));
    }

    @Test
    void testPostOrdenCompra_proveedorInactivo_devuelve409() throws Exception {
        OrdenCompra orden = crearOrden();
        when(ordenCompraService.crearOrdenCompra(any(OrdenCompra.class)))
                .thenThrow(new EstadoInvalidoException("No se puede crear una orden para un proveedor inactivo"));
        mockMvc.perform(post("/api/ordenes-compra")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orden)))
                .andExpect(status().isConflict());
    }

    @Test
    void testGetOrdenes_devuelve200() throws Exception {
        when(ordenCompraService.listarOrdenes()).thenReturn(List.of(crearOrden()));
        mockMvc.perform(get("/api/ordenes-compra"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idOrden").value(1));
    }

    @Test
    void testGetOrdenes_vacio_devuelve204() throws Exception {
        when(ordenCompraService.listarOrdenes()).thenReturn(List.of());
        mockMvc.perform(get("/api/ordenes-compra"))
                .andExpect(status().isNoContent());
    }

    @Test
    void testGetOrdenes_filtradoPorEstado_devuelve200() throws Exception {
        when(ordenCompraService.listarPorEstado(EstadoOrden.PENDIENTE_AUTORIZACION))
                .thenReturn(List.of(crearOrden()));
        mockMvc.perform(get("/api/ordenes-compra").param("estado", "PENDIENTE_AUTORIZACION"))
                .andExpect(status().isOk());
    }

    @Test
    void testGetOrdenCompra_existente_devuelve200() throws Exception {
        when(ordenCompraService.findById(1L)).thenReturn(Optional.of(crearOrden()));
        mockMvc.perform(get("/api/ordenes-compra/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idOrden").value(1));
    }

    @Test
    void testGetOrdenCompra_inexistente_devuelve404() throws Exception {
        when(ordenCompraService.findById(9999L)).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/ordenes-compra/9999"))
                .andExpect(status().isNotFound());
    }

    
}
