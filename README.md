ms-proveedor
Microservicio desarrollado con Spring Boot para la gestión de proveedores y órdenes de reabastecimiento. Forma parte de una arquitectura de microservicios y se comunica con ms-sucursales para validar sucursales al momento de crear órdenes.

Tecnologías utilizadas
Java + Spring Boot
Spring Data JPA
Spring Web (RestTemplate)
Lombok
Base de datos relacional (JPA/Hibernate)


Estructura del proyecto
msproveedor/
├── config/
│   └── RestTemplateConfig.java         # Bean de RestTemplate para llamadas HTTP
├── controller/
│   ├── ProveedorController.java         # Endpoints REST de proveedores
│   └── OrdenReabastecimientoController.java  # Endpoints REST de órdenes
├── model/
│   ├── Proveedor.java                   # Entidad Proveedor
│   ├── OrdenReabastecimiento.java       # Entidad OrdenReabastecimiento
│   ├── DetalleOrden.java                # Entidad DetalleOrden
│   ├── EstadoProveedor.java             # Enum: ACTIVO, INACTIVO
│   ├── EstadoOrden.java                 # Enum: PENDIENTE, EN_PROCESO, COMPLETADA, RECIBIDA, CANCELADA
│   ├── CatalogoDTO.java                 # DTO para productos del catálogo
│   └── SucursalDTO.java                 # DTO para sucursales externas
├── repository/
│   ├── ProveedorRepository.java
│   └── OrdenReabastecimientoRepository.java
└── service/
    ├── ProveedorService.java
    └── OrdenReabastecimientoService.java

Endpoints disponibles
Proveedores — /api/v1/proveedores
MétodoRutaDescripciónGET/api/v1/proveedoresListar todos los proveedoresGET/api/v1/proveedores/{rut}Obtener proveedor por RUTPOST/api/v1/proveedoresCrear nuevo proveedorPUT/api/v1/proveedores/{rut}Actualizar proveedor existenteDELETE/api/v1/proveedores/{rut}Desactivar proveedor (soft delete)
Órdenes de Reabastecimiento — /api/v1/ordenes
MétodoRutaDescripciónGET/api/v1/ordenesListar todas las órdenesGET/api/v1/ordenes/proveedor/{rut}Listar órdenes de un proveedorPOST/api/v1/ordenesCrear nueva ordenPUT/api/v1/ordenes/autorizar/{idOrden}Autorizar orden → estado COMPLETADAPUT/api/v1/ordenes/recibir/{idOrden}Marcar orden como recibida → estado RECIBIDADELETE/api/v1/ordenes/{idOrden}Cancelar orden → estado CANCELADA

Modelos principales
Proveedor
CampoTipoDescripciónrutProveedorStringIdentificador único (PK)nombreStringNombre del proveedorcorreoStringCorreo electrónico (único)telefonoStringTeléfono (máx. 12 caracteres)direccionStringDirecciónestadoEstadoProveedorACTIVO o INACTIVO
OrdenReabastecimiento
CampoTipoDescripciónidOrdenLongIdentificador único (PK, auto)rutProveedorStringRUT del proveedor asociadoidSucursalLongID de la sucursal destinofechaSolicitudLocalDateFecha de creación de la ordenfechaRecepcionLocalDateFecha de recepciónestadoEstadoOrdenEstado actual de la ordentotaldoubleMonto total de la orden

Estados de una orden
PENDIENTE → EN_PROCESO → COMPLETADA → RECIBIDA
                ↓
            CANCELADA
Al crear una orden, el estado se establece automáticamente en EN_PROCESO.

Comunicación entre microservicios
Al crear una orden, el servicio intenta validar la sucursal llamando a:
GET http://localhost:8084/api/v1/sucursales/{idSucursal}
Si ms-sucursales no está disponible, la orden se guarda igualmente (tolerancia a fallos).

Lógica de negocio destacada

Eliminar proveedor: aplica soft delete, cambiando el estado a INACTIVO en lugar de borrar el registro.
Cancelar orden: cambia el estado a CANCELADA sin borrar el registro.
Recibir orden: registra la fecha de recepción automáticamente con LocalDate.now().
Crear orden: valida que el proveedor exista antes de persistir.
