package com.proveedor.msproveedor.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.proveedor.msproveedor.model.EstadoProveedor;
import com.proveedor.msproveedor.model.Proveedor;
import com.proveedor.msproveedor.repository.ProveedorRepository;

@Service
public class ProveedorService {
    @Autowired
    private ProveedorRepository proveedorRepository;

    public List<Proveedor> listarProveedores() {
        return proveedorRepository.findAll();
    }

    public Optional<Proveedor> getProveedor(String rut) {
        return proveedorRepository.findById(rut);
    }

    public Proveedor crearProveedor(Proveedor proveedor) {
        return proveedorRepository.save(proveedor);
    }

    public Proveedor actualizarProveedor(String rut, Proveedor proveedor) {
        Proveedor existente = proveedorRepository.findById(rut).orElse(null);
        if (existente != null) {
            existente.setNombre(proveedor.getNombre());
            existente.setCorreo(proveedor.getCorreo());
            existente.setTelefono(proveedor.getTelefono());
            existente.setDireccion(proveedor.getDireccion());
            existente.setEstado(proveedor.getEstado());
            return proveedorRepository.save(existente);
        }
        return null;
    }

    public void eliminarProveedor(String rut) {
        Proveedor existente = proveedorRepository.findById(rut).orElse(null);
        if (existente != null) {
            existente.setEstado(EstadoProveedor.INACTIVO);
            proveedorRepository.save(existente);
        }
    }

}
