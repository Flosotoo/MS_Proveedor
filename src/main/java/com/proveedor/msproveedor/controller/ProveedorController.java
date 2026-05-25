package com.proveedor.msproveedor.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.proveedor.msproveedor.model.Proveedor;
import com.proveedor.msproveedor.service.ProveedorService;

import ch.qos.logback.core.recovery.ResilientFileOutputStream;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/api/v1/proveedores")
public class ProveedorController {
    @Autowired
    private ProveedorService proveedorService;

    @GetMapping
    public ResponseEntity<List<Proveedor>> getProveedores(){
        List<Proveedor> lista = proveedorService.listarProveedores();
        if(lista.isEmpty()){
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(lista, HttpStatus.OK);
    }

    @GetMapping("/{rut}")
    public ResponseEntity<Proveedor> getProveedor(@PathVariable String rut) {
        Proveedor buscado = proveedorService.getProveedor(rut).orElse(null);
        if (buscado == null) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(buscado, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<Proveedor> postProveedor(@RequestBody Proveedor proveedor) {
        try {
            Proveedor nuevo = proveedorService.crearProveedor(proveedor);
            return new ResponseEntity<>(nuevo, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
    }

    @PutMapping("/{rut}")
    public ResponseEntity<Proveedor> putProveedor(
            @PathVariable String rut, @RequestBody Proveedor proveedor) {
        try {
            Proveedor actualizado = proveedorService.actualizarProveedor(rut, proveedor);
            return new ResponseEntity<>(actualizado, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{rut}")
    public ResponseEntity<HttpStatus> deleteProveedor(@PathVariable String rut) {
        try {
            proveedorService.eliminarProveedor(rut);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    
}
