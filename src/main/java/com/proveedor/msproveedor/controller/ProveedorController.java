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

import com.proveedor.msproveedor.exception.RecursoNoEncontradoException;
import com.proveedor.msproveedor.model.Proveedor;
import com.proveedor.msproveedor.service.ProveedorService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/proveedores")
@Tag(name = "Proveedores", description = "Gestión del catálogo de proveedores (HU-17)")
public class ProveedorController {
    @Autowired
    private ProveedorService proveedorService;

    @Operation(summary = "Listar proveedores", description = "HU-17: devuelve los proveedores activos. 204 si no hay ninguno.")
    @GetMapping
    public ResponseEntity<List<Proveedor>> getProveedores() {
        List<Proveedor> proveedores = proveedorService.listarProveedores();
        if (proveedores.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(proveedores, HttpStatus.OK); // HU-17
    }

    @Operation(summary = "Crear proveedor", description = "Registra un proveedor nuevo en estado ACTIVO. 409 si ya existe uno con el mismo RUT.")
    @PostMapping
    public ResponseEntity<Proveedor> postProveedor(@Valid @RequestBody Proveedor proveedor) {
        return new ResponseEntity<>(proveedorService.crearProveedor(proveedor), HttpStatus.CREATED);
    }

    @Operation(summary = "Obtener proveedor por id", description = "Devuelve un proveedor. 404 si no existe.")
    @GetMapping("/{id}")
    public ResponseEntity<Proveedor> getProveedor(@PathVariable Long id) {
        Proveedor buscado = proveedorService.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el proveedor con id " + id));
        return new ResponseEntity<>(buscado, HttpStatus.OK);
    }

    @Operation(summary = "Actualizar proveedor", description = "Modifica los datos de un proveedor. El RUT no se puede cambiar. 404 si no existe.")
    @PutMapping("/{id}")
    public ResponseEntity<Proveedor> putProveedor(@PathVariable Long id, @Valid @RequestBody Proveedor proveedor) {
        return new ResponseEntity<>(proveedorService.actualizarProveedor(id, proveedor), HttpStatus.OK);
    }

    @Operation(summary = "Desactivar proveedor", description = "Da de baja lógica a un proveedor (estado INACTIVO) en vez de borrarlo. 404 si no existe.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Proveedor> eliminarProveedor(@PathVariable Long id) {
        return new ResponseEntity<>(proveedorService.desactivarProveedor(id), HttpStatus.OK);
    }
}
