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

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/ordenes-compra")
public class OrdenCompraController {
    @Autowired
    private OrdenCompraService ordenCompraService;

    @PostMapping
    public ResponseEntity<OrdenCompra> postOrdenCompra(@Valid @RequestBody OrdenCompra orden) {
        return new ResponseEntity<>(ordenCompraService.crearOrdenCompra(orden), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<OrdenCompra>> getOrdenes(@RequestParam(required = false) EstadoOrden estado) {
        // HU-21 usa esto para listar las "Pendiente de Autorización"
        List<OrdenCompra> ordenes = (estado != null)
                ? ordenCompraService.listarPorEstado(estado)
                : ordenCompraService.listarOrdenes();
        if (ordenes.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(ordenes, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrdenCompra> getOrdenCompra(@PathVariable Long id) {
        OrdenCompra buscada = ordenCompraService.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la orden con id " + id));
        return new ResponseEntity<>(buscada, HttpStatus.OK);
    }

    @PutMapping("/{id}/autorizar")
    public ResponseEntity<OrdenCompra> autorizarOrden(@PathVariable Long id) {
        return new ResponseEntity<>(ordenCompraService.autorizarOrden(id), HttpStatus.OK); // HU-21
    }

    @PutMapping("/{id}/rechazar")
    public ResponseEntity<OrdenCompra> rechazarOrden(@PathVariable Long id) {
        return new ResponseEntity<>(ordenCompraService.rechazarOrden(id), HttpStatus.OK); // HU-21
    }

    @PutMapping("/{id}/recibir")
    public ResponseEntity<OrdenCompra> recibirOrden(@PathVariable Long id) {
        return new ResponseEntity<>(ordenCompraService.recibirOrden(id), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarOrdenCompra(@PathVariable Long id) {
        ordenCompraService.eliminarOrdenCompra(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
