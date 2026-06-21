package com.proveedor.msproveedor.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.proveedor.msproveedor.model.EstadoProveedor;
import com.proveedor.msproveedor.model.Proveedor;

public interface ProveedorRepository extends JpaRepository<Proveedor, Long>{
    boolean existsByRut(String rut);
    List<Proveedor> findByEstado(EstadoProveedor estado);
}
