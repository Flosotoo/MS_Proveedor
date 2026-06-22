package com.proveedor.msproveedor.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.proveedor.msproveedor.exception.RecursoDuplicadoException;
import com.proveedor.msproveedor.exception.RecursoNoEncontradoException;
import com.proveedor.msproveedor.model.EstadoProveedor;
import com.proveedor.msproveedor.model.Proveedor;
import com.proveedor.msproveedor.repository.ProveedorRepository;

@Service
public class ProveedorService {
    @Autowired
    private ProveedorRepository proveedorRepository;

    public Proveedor crearProveedor(Proveedor proveedor) {
        if (proveedorRepository.existsByRut(proveedor.getRut())) {
            throw new RecursoDuplicadoException("Ya existe un proveedor con el RUT: " + proveedor.getRut());
        }
        proveedor.setEstado(EstadoProveedor.ACTIVO);
        return proveedorRepository.save(proveedor);
    }

    public List<Proveedor> listarProveedores() {
        return proveedorRepository.findByEstado(EstadoProveedor.ACTIVO);
    }

    public Optional<Proveedor> findById(Long id) {
        return proveedorRepository.findById(id);
    }

    public Proveedor actualizarProveedor(Long id, Proveedor proveedor) {
        Proveedor existente = proveedorRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el proveedor con id " + id));
        proveedor.setIdProveedor(existente.getIdProveedor());
        proveedor.setRut(existente.getRut());
        if (proveedor.getEstado() == null) {
            proveedor.setEstado(existente.getEstado());
        }
        return proveedorRepository.save(proveedor);
    }

    public Proveedor desactivarProveedor(Long id) {
        Proveedor existente = proveedorRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el proveedor con id " + id));
        existente.setEstado(EstadoProveedor.INACTIVO);
        return proveedorRepository.save(existente);
    }

}
