package com.proveedor.msproveedor.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.proveedor.msproveedor.exception.RecursoNoEncontradoException;
import com.proveedor.msproveedor.model.EstadoOrden;
import com.proveedor.msproveedor.model.OrdenCompra;
import com.proveedor.msproveedor.service.OrdenCompraService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/ordenes-compra")
@Tag(name = "Órdenes de Compra", description = "Gestión de órdenes de compra a proveedores y su ciclo de autorización (HU-21)")
public class OrdenCompraController {
    @Autowired
    private OrdenCompraService ordenCompraService;

    @Operation(summary = "Crear orden de compra", description = "Registra una orden a un proveedor activo. Calcula el total y la deja en PENDIENTE_AUTORIZACION. 409 si el proveedor está inactivo, 404 si no existe.")
    @PostMapping
    public ResponseEntity<OrdenCompra> postOrdenCompra(@Valid @RequestBody OrdenCompra orden) {
        return new ResponseEntity<>(ordenCompraService.crearOrdenCompra(orden), HttpStatus.CREATED);
    }

    @Operation(summary = "Listar órdenes de compra", description = "Lista todas las órdenes, o filtra por estado con el parámetro estado (HU-21 lista las PENDIENTE_AUTORIZACION). 204 si no hay resultados.")
    @GetMapping
    public ResponseEntity<List<OrdenCompra>> getOrdenes(@RequestParam(required = false) EstadoOrden estado) {
        List<OrdenCompra> ordenes = (estado != null)
                ? ordenCompraService.listarPorEstado(estado)
                : ordenCompraService.listarOrdenes();
        if (ordenes.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(ordenes, HttpStatus.OK);
    }

    @Operation(summary = "Obtener orden por id", description = "Devuelve una orden con sus detalles. 404 si no existe.")
    @GetMapping("/{id}")
    public ResponseEntity<OrdenCompra> getOrdenCompra(@PathVariable Long id) {
        OrdenCompra buscada = ordenCompraService.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la orden con id " + id));
        return new ResponseEntity<>(buscada, HttpStatus.OK);
    }

    @Operation(summary = "Autorizar orden", description = "HU-21: el gerente autoriza una orden pendiente, pasándola a AUTORIZADA. 409 si la orden no está en estado Pendiente de Autorización.")
    @PutMapping("/{id}/autorizar")
    public ResponseEntity<OrdenCompra> autorizarOrden(@PathVariable Long id) {
        return new ResponseEntity<>(ordenCompraService.autorizarOrden(id), HttpStatus.OK); // HU-21
    }

    @Operation(summary = "Rechazar orden", description = "HU-21: el gerente rechaza una orden pendiente, pasándola a RECHAZADA. 409 si la orden no está en estado Pendiente de Autorización.")
    @PutMapping("/{id}/rechazar")
    public ResponseEntity<OrdenCompra> rechazarOrden(@PathVariable Long id) {
        return new ResponseEntity<>(ordenCompraService.rechazarOrden(id), HttpStatus.OK); // HU-21
    }

    @Operation(summary = "Recibir orden", description = "Marca una orden autorizada como RECIBIDA e ingresa la mercadería al stock de la sucursal vía MS Productos y Stock. 409 si la orden no está Autorizada.")
    @PutMapping("/{id}/recibir")
    public ResponseEntity<OrdenCompra> recibirOrden(@PathVariable Long id) {
        return new ResponseEntity<>(ordenCompraService.recibirOrden(id), HttpStatus.OK);
    }

    @Operation(summary = "Eliminar orden", description = "Elimina una orden solo si está en PENDIENTE_AUTORIZACION. 409 si ya fue autorizada, rechazada o recibida, 404 si no existe.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarOrdenCompra(@PathVariable Long id) {
        ordenCompraService.eliminarOrdenCompra(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
