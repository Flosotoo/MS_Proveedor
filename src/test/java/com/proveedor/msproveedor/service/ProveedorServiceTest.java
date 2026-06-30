package com.proveedor.msproveedor.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.proveedor.msproveedor.exception.RecursoDuplicadoException;
import com.proveedor.msproveedor.exception.RecursoNoEncontradoException;
import com.proveedor.msproveedor.model.EstadoProveedor;
import com.proveedor.msproveedor.model.Proveedor;
import com.proveedor.msproveedor.repository.ProveedorRepository;

@ExtendWith(MockitoExtension.class)
class ProveedorServiceTest {
    @Mock
    private ProveedorRepository proveedorRepository;

    @InjectMocks
    private ProveedorService proveedorService;

    private Proveedor crearProveedor(Long id, EstadoProveedor estado) {
        Proveedor p = new Proveedor();
        p.setIdProveedor(id);
        p.setRut("76543210-9");
        p.setRazonSocial("Distribuidora Test");
        p.setDireccion("Av. Test 123");
        p.setCorreo("test@test.cl");
        p.setTelefono("+56912345678");
        p.setEstado(estado);
        return p;
    }

    @Test
    void testCrearProveedor_exitoso_quedaActivo() {
        Proveedor nuevo = crearProveedor(null, null);
        when(proveedorRepository.existsByRut("76543210-9")).thenReturn(false);
        when(proveedorRepository.save(any(Proveedor.class))).thenAnswer(inv -> inv.getArgument(0));
        Proveedor resultado = proveedorService.crearProveedor(nuevo);
        assertNotNull(resultado);
        // todo proveedor nuevo queda ACTIVO
        assertEquals(EstadoProveedor.ACTIVO, resultado.getEstado());
        verify(proveedorRepository, times(1)).save(any(Proveedor.class));
    }

    @Test
    void testCrearProveedor_rutDuplicado_lanzaExcepcion() {
        Proveedor nuevo = crearProveedor(null, null);
        when(proveedorRepository.existsByRut("76543210-9")).thenReturn(true);
        RecursoDuplicadoException ex = assertThrows(
                RecursoDuplicadoException.class,
                () -> proveedorService.crearProveedor(nuevo));
        assertTrue(ex.getMessage().contains("RUT"));
        // No debe guardar si el rut ya existe
        verify(proveedorRepository, never()).save(any(Proveedor.class));
    }

    @Test
    void testListarProveedores_devuelveSoloActivos() {
        Proveedor p1 = crearProveedor(1L, EstadoProveedor.ACTIVO);
        Proveedor p2 = crearProveedor(2L, EstadoProveedor.ACTIVO);
        when(proveedorRepository.findByEstado(EstadoProveedor.ACTIVO))
                .thenReturn(List.of(p1, p2));
        List<Proveedor> resultado = proveedorService.listarProveedores();
        assertEquals(2, resultado.size());
        verify(proveedorRepository, times(1)).findByEstado(EstadoProveedor.ACTIVO);
    }

    @Test
    void testFindById_existente() {
        Proveedor p = crearProveedor(1L, EstadoProveedor.ACTIVO);
        when(proveedorRepository.findById(1L)).thenReturn(Optional.of(p));
        Optional<Proveedor> resultado = proveedorService.findById(1L);
        assertTrue(resultado.isPresent());
        assertEquals(1L, resultado.get().getIdProveedor());
    }

    @Test
    void testActualizarProveedor_exitoso_conservaRut() {
        Proveedor existente = crearProveedor(1L, EstadoProveedor.ACTIVO);
        Proveedor datosNuevos = new Proveedor();
        datosNuevos.setRazonSocial("Nueva Razón Social");
        datosNuevos.setDireccion("Nueva Dirección");
        datosNuevos.setCorreo("nuevo@test.cl");
        datosNuevos.setTelefono("+56999999999");
        datosNuevos.setRut("11111111-1"); // intenta cambiar el RUT
        when(proveedorRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(proveedorRepository.save(any(Proveedor.class))).thenAnswer(inv -> inv.getArgument(0));
        Proveedor resultado = proveedorService.actualizarProveedor(1L, datosNuevos);
        // El rut no se puede cambiar, se conserva el original
        assertEquals("76543210-9", resultado.getRut());
        assertEquals(1L, resultado.getIdProveedor());
        verify(proveedorRepository, times(1)).save(any(Proveedor.class));
    }

    @Test
    void testActualizarProveedor_estadoNull_conservaEstadoExistente() {
        Proveedor existente = crearProveedor(1L, EstadoProveedor.ACTIVO);
        Proveedor datosNuevos = new Proveedor();
        datosNuevos.setRazonSocial("Otra Razón");
        datosNuevos.setEstado(null); // no envía estado
        when(proveedorRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(proveedorRepository.save(any(Proveedor.class))).thenAnswer(inv -> inv.getArgument(0));
        Proveedor resultado = proveedorService.actualizarProveedor(1L, datosNuevos);
        // Si no se envía estado, se conserva el que ya tenía
        assertEquals(EstadoProveedor.ACTIVO, resultado.getEstado());
    }

    @Test
    void testActualizarProveedor_noExistente_lanzaExcepcion() {
        Proveedor datosNuevos = crearProveedor(null, null);
        when(proveedorRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RecursoNoEncontradoException.class,
                () -> proveedorService.actualizarProveedor(99L, datosNuevos));
        verify(proveedorRepository, never()).save(any(Proveedor.class));
    }

    @Test
    void testDesactivarProveedor_exitoso_quedaInactivo() {
        Proveedor existente = crearProveedor(1L, EstadoProveedor.ACTIVO);
        when(proveedorRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(proveedorRepository.save(any(Proveedor.class))).thenAnswer(inv -> inv.getArgument(0));
        Proveedor resultado = proveedorService.desactivarProveedor(1L);
        // Pasa a INACTIVO y no se borra
        assertEquals(EstadoProveedor.INACTIVO, resultado.getEstado());
        verify(proveedorRepository, times(1)).save(any(Proveedor.class));
    }

    @Test
    void testDesactivarProveedor_noExistente_lanzaExcepcion() {
        when(proveedorRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RecursoNoEncontradoException.class,
                () -> proveedorService.desactivarProveedor(99L));
        verify(proveedorRepository, never()).save(any(Proveedor.class));
    }

}
