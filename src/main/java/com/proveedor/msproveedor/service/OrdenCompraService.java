package com.proveedor.msproveedor.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import com.proveedor.msproveedor.dto.AjusteStockDTO;
import com.proveedor.msproveedor.dto.ProductoDTO;
import com.proveedor.msproveedor.exception.EstadoInvalidoException;
import com.proveedor.msproveedor.exception.RecursoNoEncontradoException;
import com.proveedor.msproveedor.model.DetalleOrden;
import com.proveedor.msproveedor.model.EstadoOrden;
import com.proveedor.msproveedor.model.EstadoProveedor;
import com.proveedor.msproveedor.model.OrdenCompra;
import com.proveedor.msproveedor.model.Proveedor;
import com.proveedor.msproveedor.repository.OrdenCompraRepository;
import com.proveedor.msproveedor.repository.ProveedorRepository;

@Service
public class OrdenCompraService {
    private static final Logger log = LoggerFactory.getLogger(OrdenCompraService.class);

    @Autowired
    private OrdenCompraRepository ordenCompraRepository;

    @Autowired
    private ProveedorRepository proveedorRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${ms.productos.url}")
    private String URL_MS_PRODUCTOS;

    @Value("${ms.inventario.ajuste.url}")
    private String URL_MS_INVENTARIO_AJUSTE;

    public OrdenCompra crearOrdenCompra(OrdenCompra orden) {
        Proveedor proveedorCompleto = proveedorRepository.findById(orden.getProveedor().getIdProveedor())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró el proveedor con id " + orden.getProveedor().getIdProveedor()));

        // No se puede comprar a un proveedor dado de baja
        if (proveedorCompleto.getEstado() == EstadoProveedor.INACTIVO) {
            throw new EstadoInvalidoException(
                    "No se puede crear una orden para un proveedor inactivo (id "
                            + proveedorCompleto.getIdProveedor() + ")");
        }

        orden.setProveedor(proveedorCompleto);
        BigDecimal totalOrden = BigDecimal.ZERO;

        for (DetalleOrden detalle : orden.getDetalles()) {
            String url = URL_MS_PRODUCTOS + detalle.getIdProducto();
            try {
                ProductoDTO producto = restTemplate.getForObject(url, ProductoDTO.class);
                if (producto == null) {
                    throw new RecursoNoEncontradoException(
                            "El producto " + detalle.getIdProducto() + " no existe en el catálogo");
                }
            } catch (ResourceAccessException ex) {
                // MS_Productos_Stock no responde (apagado, timeout, etc.)
                // Decisión de negocio: no bloquear la orden, solo advertir.
                log.warn("No se pudo validar el producto {} contra MS Productos y Stock: {}",
                        detalle.getIdProducto(), ex.getMessage());
            }

            detalle.setOrdenCompra(orden);
            BigDecimal subtotal = detalle.getPrecioUnitario().multiply(BigDecimal.valueOf(detalle.getCantidad()));
            detalle.setSubtotal(subtotal);
            totalOrden = totalOrden.add(subtotal);
        }

        orden.setTotal(totalOrden);
        orden.setEstado(EstadoOrden.PENDIENTE_AUTORIZACION);
        orden.setFechaSolicitud(LocalDateTime.now());

        return ordenCompraRepository.save(orden);
    }

    public OrdenCompra autorizarOrden(Long id) {
        // HU-21: el Gerente autoriza una orden Pendiente de Autorización
        OrdenCompra orden = buscarOrConFallar(id);
        if (orden.getEstado() != EstadoOrden.PENDIENTE_AUTORIZACION) {
            throw new EstadoInvalidoException(
                    "Solo se pueden autorizar órdenes en estado Pendiente de Autorización");
        }
        orden.setEstado(EstadoOrden.AUTORIZADA);
        return ordenCompraRepository.save(orden);
    }

    public OrdenCompra rechazarOrden(Long id) {
        OrdenCompra orden = buscarOrConFallar(id);
        if (orden.getEstado() != EstadoOrden.PENDIENTE_AUTORIZACION) {
            throw new EstadoInvalidoException(
                    "Solo se pueden rechazar órdenes en estado Pendiente de Autorización");
        }
        orden.setEstado(EstadoOrden.RECHAZADA);
        return ordenCompraRepository.save(orden);
    }

    public OrdenCompra recibirOrden(Long id) {
        OrdenCompra orden = buscarOrConFallar(id);
        if (orden.getEstado() != EstadoOrden.AUTORIZADA) {
            throw new EstadoInvalidoException("Solo se puede recibir una orden que esté Autorizada");
        }

        for (DetalleOrden detalle : orden.getDetalles()) {
            AjusteStockDTO ajuste = new AjusteStockDTO(
                    detalle.getIdProducto(), orden.getIdSucursal(), detalle.getCantidad(),
                    "orden-" + orden.getIdOrden() + "-producto-" + detalle.getIdProducto());
            restTemplate.put(URL_MS_INVENTARIO_AJUSTE, ajuste);
        }

        orden.setEstado(EstadoOrden.RECIBIDA);
        orden.setFechaRecepcion(LocalDateTime.now());
        return ordenCompraRepository.save(orden);
    }

    public Optional<OrdenCompra> findById(Long id) {
        return ordenCompraRepository.findById(id);
    }

    public List<OrdenCompra> listarOrdenes() {
        return ordenCompraRepository.findAll();
    }

    public List<OrdenCompra> listarPorEstado(EstadoOrden estado) {
        return ordenCompraRepository.findByEstado(estado);
    }

    private OrdenCompra buscarOrConFallar(Long id) {
        return ordenCompraRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la orden con id " + id));
    }

    public void eliminarOrdenCompra(Long id) {
        OrdenCompra orden = buscarOrConFallar(id);
        if (orden.getEstado() != EstadoOrden.PENDIENTE_AUTORIZACION) {
            throw new EstadoInvalidoException(
                    "Solo se pueden eliminar órdenes en estado Pendiente de Autorización");
        }
        ordenCompraRepository.delete(orden);
    }

}