package com.proveedor.msproveedor.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.proveedor.msproveedor.dto.ProductoDTO;
import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proveedor.msproveedor.model.DetalleOrden;
import com.proveedor.msproveedor.model.EstadoProveedor;
import com.proveedor.msproveedor.model.OrdenCompra;
import com.proveedor.msproveedor.model.Proveedor;
import com.proveedor.msproveedor.repository.OrdenCompraRepository;
import com.proveedor.msproveedor.repository.ProveedorRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OrdenCompraControllerIT {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrdenCompraRepository ordenCompraRepository;

    @Autowired
    private ProveedorRepository proveedorRepository;

    @MockitoBean
    private RestTemplate restTemplate;

    private ObjectMapper objectMapper = new ObjectMapper();

    private Long idProveedorActivo;

    @BeforeEach
    void setUp() {
        ordenCompraRepository.deleteAll();
        proveedorRepository.deleteAll();
        // Crear proveedor activo
        Proveedor proveedor = new Proveedor();
        proveedor.setRut("76543210-9");
        proveedor.setRazonSocial("Distribuidora Test");
        proveedor.setDireccion("Av. Test 123");
        proveedor.setCorreo("test@test.cl");
        proveedor.setTelefono("+56912345678");
        proveedor.setEstado(EstadoProveedor.ACTIVO);
        idProveedorActivo = proveedorRepository.save(proveedor).getIdProveedor();
        // Devuelve un producto válido
        ProductoDTO productoDTO = new ProductoDTO();
        productoDTO.setIdProducto(1L);
        productoDTO.setNombre("Producto Test");
        when(restTemplate.getForObject(anyString(), eq(ProductoDTO.class))).thenReturn(productoDTO);
        doNothing().when(restTemplate).put(anyString(), any());
    }

    private OrdenCompra crearOrden(Long idProveedor) {
        DetalleOrden detalle = new DetalleOrden();
        detalle.setIdProducto(1L);
        detalle.setCantidad(10);
        detalle.setPrecioUnitario(new BigDecimal("30000"));
        Proveedor proveedor = new Proveedor();
        proveedor.setIdProveedor(idProveedor);
        OrdenCompra orden = new OrdenCompra();
        orden.setProveedor(proveedor);
        orden.setIdSucursal(1L);
        orden.setDetalles(List.of(detalle));
        return orden;
    }

    @Test
    void testCrearOrden_devuelve201YCalculaTotal() throws Exception {
        OrdenCompra orden = crearOrden(idProveedorActivo);
        mockMvc.perform(post("/api/ordenes-compra")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orden)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idOrden").exists())
                .andExpect(jsonPath("$.total").value(300000))
                .andExpect(jsonPath("$.estado").value("PENDIENTE_AUTORIZACION"));
    }

    @Test
    void testCrearOrden_proveedorInexistente_devuelve404() throws Exception {
        OrdenCompra orden = crearOrden(9999L); // proveedor inexistente
        mockMvc.perform(post("/api/ordenes-compra")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orden)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetOrden_inexistente_devuelve404() throws Exception {
        mockMvc.perform(get("/api/ordenes-compra/9999"))
                .andExpect(status().isNotFound());
    }

}
