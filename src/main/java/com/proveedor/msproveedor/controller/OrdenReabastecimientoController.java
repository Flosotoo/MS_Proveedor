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
import org.springframework.web.bind.annotation.RestController;

import com.proveedor.msproveedor.model.OrdenReabastecimiento;
import com.proveedor.msproveedor.service.OrdenReabastecimientoService;

@RestController
@RequestMapping("/api/v1/ordenes")
public class OrdenReabastecimientoController {
    @Autowired
    OrdenReabastecimientoService ordenReabastecimientoService;

    @GetMapping
    public ResponseEntity<List<OrdenReabastecimiento>> getOrdenes() {
        List<OrdenReabastecimiento> lista = ordenReabastecimientoService.listarOrdenes();
        if (lista.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(lista, HttpStatus.OK);
    }

    @GetMapping("/proveedor/{rut}")
    public ResponseEntity<List<OrdenReabastecimiento>> getOrdenesPorProveedor(@PathVariable String rut) {
        List<OrdenReabastecimiento> lista = ordenReabastecimientoService.listarPorProveedor(rut);
        if (lista.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(lista, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<OrdenReabastecimiento> postOrden(@RequestBody OrdenReabastecimiento orden) {
        try {
            OrdenReabastecimiento nueva = ordenReabastecimientoService.crearOrden(orden);
            return new ResponseEntity<>(nueva, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
    }

    @PutMapping("/autorizar/{idOrden}")
    public ResponseEntity<OrdenReabastecimiento> autorizarOrden(@PathVariable Long idOrden) {
        try {
            OrdenReabastecimiento autorizada = ordenReabastecimientoService.autorizarOrden(idOrden);
            return new ResponseEntity<>(autorizada, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/recibir/{idOrden}")
    public ResponseEntity<OrdenReabastecimiento> recibirOrden(@PathVariable Long idOrden) {
        try {
            OrdenReabastecimiento recibida = ordenReabastecimientoService.recibirOrden(idOrden);
            return new ResponseEntity<>(recibida, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{idOrden}")
    public ResponseEntity<HttpStatus> cancelarOrden(@PathVariable Long idOrden) {
        try {
            ordenReabastecimientoService.cancelarOrden(idOrden);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
