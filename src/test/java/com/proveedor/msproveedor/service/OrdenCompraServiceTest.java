package com.proveedor.msproveedor.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import com.proveedor.msproveedor.exception.EstadoInvalidoException;
import com.proveedor.msproveedor.model.DetalleOrden;
import com.proveedor.msproveedor.model.EstadoOrden;
import com.proveedor.msproveedor.model.EstadoProveedor;
import com.proveedor.msproveedor.model.OrdenCompra;
import com.proveedor.msproveedor.model.Proveedor;
import com.proveedor.msproveedor.repository.OrdenCompraRepository;
import com.proveedor.msproveedor.repository.ProveedorRepository;
import static org.mockito.ArgumentMatchers.eq;
import com.proveedor.msproveedor.dto.ProductoDTO;

@ExtendWith(MockitoExtension.class)
class OrdenCompraServiceTest {
    @Mock
    private OrdenCompraRepository ordenCompraRepository;

    @Mock
    private ProveedorRepository proveedorRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private OrdenCompraService ordenCompraService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(ordenCompraService, "URL_MS_PRODUCTOS",
                "http://localhost:9999/api/productos/");
        ReflectionTestUtils.setField(ordenCompraService, "URL_MS_INVENTARIO_AJUSTE",
                "http://localhost:9999/api/inventario/ajustar");
    }

    private Proveedor crearProveedor(EstadoProveedor estado) {
        Proveedor proveedor = new Proveedor();
        proveedor.setIdProveedor(1L);
        proveedor.setRut("76543210-9");
        proveedor.setRazonSocial("Distribuidora Test");
        proveedor.setEstado(estado);
        return proveedor;
    }

    private OrdenCompra crearOrden() {
        DetalleOrden detalle = new DetalleOrden();
        detalle.setIdProducto(1L);
        detalle.setCantidad(10);
        detalle.setPrecioUnitario(new BigDecimal("30000"));
        Proveedor proveedor = new Proveedor();
        proveedor.setIdProveedor(1L);
        OrdenCompra orden = new OrdenCompra();
        orden.setProveedor(proveedor);
        orden.setIdSucursal(1L);
        orden.setDetalles(List.of(detalle));
        return orden;
    }

    @Test
    void testCrearOrdenCompra_calculaTotalYDejaPendiente() {
        // 10 x 30000 = 300000 queda en pendiente_autorizacion
        OrdenCompra orden = crearOrden();
        when(proveedorRepository.findById(1L)).thenReturn(Optional.of(crearProveedor(EstadoProveedor.ACTIVO)));
        // El producto existe: devolvemos y se devuelve un ProductoDTO real
        ProductoDTO productoDTO = new ProductoDTO();
        productoDTO.setIdProducto(1L);
        when(restTemplate.getForObject(anyString(), eq(ProductoDTO.class))).thenReturn(productoDTO);
        when(ordenCompraRepository.save(any(OrdenCompra.class))).thenAnswer(inv -> inv.getArgument(0));
        OrdenCompra resultado = ordenCompraService.crearOrdenCompra(orden);
        assertNotNull(resultado);
        assertEquals(new BigDecimal("300000"), resultado.getTotal());
        assertEquals(EstadoOrden.PENDIENTE_AUTORIZACION, resultado.getEstado());
        verify(ordenCompraRepository, times(1)).save(any(OrdenCompra.class));
    }

    @Test
    void testCrearOrdenCompra_proveedorInactivo_lanzaExcepcion() {
        // No se puede comprar a un proveedor inactivo
        OrdenCompra orden = crearOrden();
        when(proveedorRepository.findById(1L)).thenReturn(Optional.of(crearProveedor(EstadoProveedor.INACTIVO)));
        EstadoInvalidoException ex = assertThrows(
                EstadoInvalidoException.class,
                () -> ordenCompraService.crearOrdenCompra(orden));
        assertTrue(ex.getMessage().contains("inactivo"));
        verify(ordenCompraRepository, never()).save(any(OrdenCompra.class));
    }

    @Test
    void testAutorizarOrden_estadoInvalido_lanzaExcepcion() {
        // Solo se puede autorizar una orden pendiente
        OrdenCompra orden = crearOrden();
        orden.setIdOrden(1L);
        orden.setEstado(EstadoOrden.AUTORIZADA);
        when(ordenCompraRepository.findById(1L)).thenReturn(Optional.of(orden));
        EstadoInvalidoException ex = assertThrows(
                EstadoInvalidoException.class,
                () -> ordenCompraService.autorizarOrden(1L));
        assertTrue(ex.getMessage().contains("Pendiente de Autorización"));
        verify(ordenCompraRepository, never()).save(any(OrdenCompra.class));
    }

    @Test
    void testRecibirOrden_autorizadaAjustaStockYQuedaRecibida() {
        // Una orden autorizada al ser recibida ajusta stock y pasa a estado de recibido
        OrdenCompra orden = crearOrden();
        orden.setIdOrden(1L);
        orden.setEstado(EstadoOrden.AUTORIZADA);
        when(ordenCompraRepository.findById(1L)).thenReturn(Optional.of(orden));
        when(ordenCompraRepository.save(any(OrdenCompra.class))).thenAnswer(inv -> inv.getArgument(0));
        OrdenCompra resultado = ordenCompraService.recibirOrden(1L);
        assertEquals(EstadoOrden.RECIBIDA, resultado.getEstado());
        assertNotNull(resultado.getFechaRecepcion());
        verify(restTemplate, times(1)).put(contains("ajustar"), any());
    }
}