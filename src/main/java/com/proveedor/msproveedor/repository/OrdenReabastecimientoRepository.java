package com.proveedor.msproveedor.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.proveedor.msproveedor.model.OrdenReabastecimiento;

public interface OrdenReabastecimientoRepository extends JpaRepository<OrdenReabastecimiento, Long>{
    List<OrdenReabastecimiento> findByRutProveedor(String rutProveedor);
}
