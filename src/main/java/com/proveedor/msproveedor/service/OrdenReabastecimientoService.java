package com.proveedor.msproveedor.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.proveedor.msproveedor.model.EstadoOrden;
import com.proveedor.msproveedor.model.OrdenReabastecimiento;
import com.proveedor.msproveedor.model.SucursalDTO;
import com.proveedor.msproveedor.repository.OrdenReabastecimientoRepository;
import com.proveedor.msproveedor.repository.ProveedorRepository;

@Service
public class OrdenReabastecimientoService {

    @Autowired
    private OrdenReabastecimientoRepository ordenReabastecimientoRepository;

    @Autowired
    private ProveedorRepository proveedorRepository;

    @Autowired
    private RestTemplate restTemplate;

    public List<OrdenReabastecimiento> listarOrdenes() {
        return ordenReabastecimientoRepository.findAll();
    }

    public List<OrdenReabastecimiento> listarPorProveedor(String rut) {
        return ordenReabastecimientoRepository.findByRutProveedor(rut);
    }

    public OrdenReabastecimiento crearOrden(OrdenReabastecimiento orden) {

        // Verifica que el proveedor exista
        boolean proveedorExiste = proveedorRepository.existsById(orden.getRutProveedor());
        if (!proveedorExiste) {
            System.out.println("Proveedor no encontrado: " + orden.getRutProveedor());
            return null;
        }
        try {
            String url = "http://localhost:8084/api/v1/sucursales/" + orden.getIdSucursal();
            SucursalDTO sucursal = restTemplate.getForObject(url, SucursalDTO.class);
            if (sucursal != null) {
                System.out.println("Sucursal encontrada: " + sucursal.getNombre());
            }
        } catch (Exception e) {
            System.out.println("ms_sucursales no disponible, guardando sin validación: " + e.getMessage());
        }

        orden.setFechaSolicitud(LocalDate.now());
        orden.setEstado(EstadoOrden.EN_PROCESO);
        return ordenReabastecimientoRepository.save(orden);
    }
    public OrdenReabastecimiento autorizarOrden(Long idOrden) {
        OrdenReabastecimiento existente = ordenReabastecimientoRepository.findById(idOrden).orElse(null);
        if (existente != null) {
            existente.setEstado(EstadoOrden.COMPLETADA);
            return ordenReabastecimientoRepository.save(existente);
        }
        return null;
    }

    public OrdenReabastecimiento recibirOrden(Long idOrden) {
        OrdenReabastecimiento existente = ordenReabastecimientoRepository.findById(idOrden).orElse(null);
        if (existente != null) {
            existente.setEstado(EstadoOrden.RECIBIDA);
            existente.setFechaRecepcion(LocalDate.now());
            return ordenReabastecimientoRepository.save(existente);
        }
        return null;
    }

    public void cancelarOrden(Long idOrden) {
        OrdenReabastecimiento existente = ordenReabastecimientoRepository.findById(idOrden).orElse(null);
        if (existente != null) {
            existente.setEstado(EstadoOrden.CANCELADA);
            ordenReabastecimientoRepository.save(existente);
        }
    }

}
