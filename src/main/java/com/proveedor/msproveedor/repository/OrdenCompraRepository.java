package com.proveedor.msproveedor.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.proveedor.msproveedor.model.EstadoOrden;
import com.proveedor.msproveedor.model.OrdenCompra;

public interface OrdenCompraRepository extends JpaRepository<OrdenCompra, Long>{
    List<OrdenCompra> findByEstado(EstadoOrden estado);
}
