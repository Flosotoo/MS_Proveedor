# Microservicio Proveedores

## Descripción

Microservicio de gestión de proveedores y órdenes de compra para Perfulandia SPA. Administra el catálogo de proveedores, el ciclo de autorización de órdenes de compra y la recepción de mercadería, ingresando el stock recibido al MS Productos y Stock.

- Historias de usuario: HU-16 a HU-21 y HU-46.
- Swagger/OpenAPI disponible en: <http://localhost:8084/swagger-ui.html>

## Estudiante

Florencia Soto

## Tecnologías

- Java 25, Spring Boot 4.x, JPA/Hibernate, Bean Validation
- MySQL 8.x (para Duoc/XAMPP)
- Comunicación entre microservicios vía RestTemplate (consume MS Productos y Stock, y MS Sucursales)
- Maven, Swagger/OpenAPI (springdoc)

## Microservicios que consume

Este MS se comunica con otros microservicios vía REST para validar y operar:

| MS destino | Puerto | Para qué |
| ---------- | ------ | -------- |
| MS Productos y Stock | 8082 | Validar que el producto exista al crear la orden, e ingresar stock al recibir la mercadería |
| MS Sucursales y Logística | 8087 | Validar que la sucursal de la orden exista |

Ambas llamadas usan **degradación elegante**: si el MS externo está caído (timeout, `ResourceAccessException`), la operación continúa con una advertencia en el log en vez de fallar, para no acoplar la disponibilidad de este MS a la de los otros. El `RestTemplate` se configura con timeouts de conexión y lectura de 3 segundos (`RestTemplateConfig`). Nota: si el MS responde pero el producto o la sucursal no existen, sí se rechaza la operación (404); la degradación solo aplica cuando el MS externo no responde.

## Endpoints

### Proveedores

| Método | Ruta | HU | Descripción |
| ------ | ---- | -- | ----------- |
| POST | `/api/proveedores` | HU-16 | Registrar proveedor (queda en estado ACTIVO) |
| GET | `/api/proveedores` | HU-17 | Listar proveedores activos |
| GET | `/api/proveedores/{id}` | HU-17 | Obtener un proveedor por id |
| PUT | `/api/proveedores/{id}` | HU-18 | Actualizar datos de un proveedor (el RUT no se puede cambiar) |
| DELETE | `/api/proveedores/{id}` | HU-19 | Desactivar proveedor (baja lógica: estado INACTIVO) |

### Órdenes de compra

| Método | Ruta | HU | Descripción |
| ------ | ---- | -- | ----------- |
| POST | `/api/ordenes-compra` | HU-46 | Crear orden de compra (queda PENDIENTE_AUTORIZACION) |
| GET | `/api/ordenes-compra` | HU-21 | Listar órdenes (filtro opcional por `?estado=`) |
| GET | `/api/ordenes-compra/{id}` | HU-21 | Obtener una orden por id |
| PUT | `/api/ordenes-compra/{id}/autorizar` | HU-21 | Autorizar una orden pendiente → AUTORIZADA |
| PUT | `/api/ordenes-compra/{id}/rechazar` | HU-21 | Rechazar una orden pendiente → RECHAZADA |
| PUT | `/api/ordenes-compra/{id}/recibir` | HU-20 | Recibir mercadería de una orden autorizada → RECIBIDA (ingresa stock) |
| DELETE | `/api/ordenes-compra/{id}` | — | Eliminar una orden (solo si está PENDIENTE_AUTORIZACION) |

## Ejecución

```
./mvnw spring-boot:run
```

El servidor corre en **<http://localhost:8084>**.

Requiere que MySQL esté corriendo (XAMPP). La base de datos `db_proveedor` se crea automáticamente (`createDatabaseIfNotExist=true`) y las tablas vía Hibernate (`ddl-auto=update`).

## Pruebas automatizadas

### Tests unitarios y de integración (JUnit + Mockito)

```
./mvnw test
```

El MS incluye cuatro clases de prueba, organizadas en tres niveles (unitario, web e integración):

- **`OrdenCompraServiceTest`** (unitario, Mockito): valida las reglas de negocio del service de órdenes — cálculo del total de la orden y estado inicial PENDIENTE_AUTORIZACION, rechazo de orden a proveedor inactivo, rechazo por proveedor inexistente, control de estados en el ciclo de autorización (autorizar/rechazar solo una orden pendiente), recepción que ingresa stock y pasa a RECIBIDA, rechazo de recepción si la orden no está autorizada, eliminación solo en estado pendiente y listado filtrado por estado. Mockea las llamadas a los otros microservicios.
- **`ProveedorServiceTest`** (unitario, Mockito): valida las reglas de negocio del service de proveedores — creación que queda ACTIVO, rechazo por RUT duplicado, listado de solo activos, conservación del RUT original al actualizar, conservación del estado existente cuando no se envía, baja lógica (paso a INACTIVO sin borrar) y manejo de proveedor inexistente al actualizar o desactivar.
- **`OrdenCompraControllerTest`** (`@WebMvcTest`): valida la capa web aislada — códigos HTTP correctos (201/200/204/404/409) con el service mockeado, incluyendo el rechazo por proveedor inactivo (409) y el listado filtrado por estado.
- **`OrdenCompraControllerIT`** (`@SpringBootTest` + `@ActiveProfiles("test")`): valida la cadena completa controller → service → base de datos (H2 en memoria), mockeando solo las llamadas a otros microservicios. Verifica la creación con cálculo de total, el 404 por proveedor inexistente y el 404 por orden inexistente.

## Estructura de requests y respuestas

### POST /api/proveedores — Registrar proveedor

```
// Request
{
  "rut": "76543210-9",
  "razonSocial": "Distribuidora Aromas Ltda",
  "direccion": "Av. Providencia 1234, Santiago",
  "correo": "ventas@aromas.cl",
  "telefono": "+56912345678"
}

// Response: 201 Created
{
  "idProveedor": 1,
  "rut": "76543210-9",
  "razonSocial": "Distribuidora Aromas Ltda",
  "direccion": "Av. Providencia 1234, Santiago",
  "correo": "ventas@aromas.cl",
  "telefono": "+56912345678",
  "estado": "ACTIVO"
}
```

**Validaciones:**

- RUT único (409 Conflict si ya existe un proveedor con ese RUT)
- Todos los campos son obligatorios: rut, razonSocial, direccion, correo, telefono
- El estado se asigna automáticamente como ACTIVO (no se envía)

### GET /api/proveedores — Listar proveedores activos

```
Response: 200 OK → lista de proveedores en estado ACTIVO
Response: 204 No Content → si no hay proveedores activos
```

Solo devuelve los proveedores ACTIVO; los desactivados (INACTIVO) quedan excluidos.

### PUT /api/proveedores/{id} — Actualizar proveedor

```
// Request
{
  "razonSocial": "Distribuidora Aromas SpA",
  "direccion": "Av. Nueva 999, Santiago",
  "correo": "contacto@aromas.cl",
  "telefono": "+56987654321"
}

// Response: 200 OK → proveedor actualizado
```

**Regla:** El RUT no se puede cambiar (se conserva el original aunque se envíe otro). Si no se envía estado, se conserva el que ya tenía. 404 si el proveedor no existe.

### DELETE /api/proveedores/{id} — Desactivar proveedor

```
Response: 200 OK → proveedor con estado INACTIVO
```

Baja lógica: el proveedor no se borra, solo pasa a estado INACTIVO y deja de aparecer en el listado. 404 si no existe.

### POST /api/ordenes-compra — Crear orden de compra

```
// Request
{
  "proveedor": { "idProveedor": 1 },
  "idSucursal": 1,
  "detalles": [
    {
      "idProducto": 1,
      "cantidad": 10,
      "precioUnitario": 30000
    }
  ]
}

// Response: 201 Created
{
  "idOrden": 1,
  "proveedor": { "idProveedor": 1, "rut": "76543210-9", "estado": "ACTIVO", ... },
  "idSucursal": 1,
  "fechaSolicitud": "2026-06-30T12:00:00",
  "fechaRecepcion": null,
  "estado": "PENDIENTE_AUTORIZACION",
  "total": 300000,
  "detalles": [
    { "idDetalleOrden": 1, "idProducto": 1, "cantidad": 10, "precioUnitario": 30000, "subtotal": 300000 }
  ]
}
```

**Reglas de negocio:**

- El proveedor debe existir (404 si no)
- No se puede crear una orden para un proveedor INACTIVO (409 Conflict)
- El total se calcula como la suma de los subtotales (cantidad × precioUnitario)
- La orden queda en estado PENDIENTE_AUTORIZACION
- Cada producto se valida contra MS Productos y Stock; la sucursal contra MS Sucursales (con degradación elegante si están caídos)

### GET /api/ordenes-compra — Listar órdenes

```
GET /api/ordenes-compra
GET /api/ordenes-compra?estado=PENDIENTE_AUTORIZACION

Response: 200 OK → lista de órdenes
Response: 204 No Content → si no hay resultados
```

El filtro `?estado=` (HU-21) permite listar las órdenes pendientes de autorización. Valores: `PENDIENTE_AUTORIZACION`, `AUTORIZADA`, `RECHAZADA`, `RECIBIDA`.

### PUT /api/ordenes-compra/{id}/autorizar — Autorizar orden

```
Response: 200 OK → orden con estado AUTORIZADA
```

**Regla:** Solo se pueden autorizar órdenes en estado PENDIENTE_AUTORIZACION (409 Conflict en caso contrario). 404 si la orden no existe.

### PUT /api/ordenes-compra/{id}/rechazar — Rechazar orden

```
Response: 200 OK → orden con estado RECHAZADA
```

**Regla:** Solo se pueden rechazar órdenes en estado PENDIENTE_AUTORIZACION (409 Conflict en caso contrario).

### PUT /api/ordenes-compra/{id}/recibir — Recibir mercadería

```
Response: 200 OK → orden con estado RECIBIDA y fechaRecepcion seteada
```

**Reglas de negocio:**

- Solo se puede recibir una orden en estado AUTORIZADA (409 Conflict en caso contrario)
- Al recibir, ingresa el stock de cada detalle al MS Productos y Stock (ajuste positivo)
- El ajuste de stock usa un `idOperacion` único (`orden-{id}-producto-{id}`) para garantizar idempotencia: si la llamada se repite, el stock no se duplica

### DELETE /api/ordenes-compra/{id} — Eliminar orden

```
Response: 204 No Content
```

**Regla:** Solo se pueden eliminar órdenes en estado PENDIENTE_AUTORIZACION (409 Conflict si ya fue autorizada, rechazada o recibida). 404 si no existe.

## Ciclo de vida de una orden de compra

```
                  crear
                    │
                    ▼
        PENDIENTE_AUTORIZACION ──── eliminar ──► (borrada)
            │              │
       autorizar        rechazar
            │              │
            ▼              ▼
        AUTORIZADA      RECHAZADA
            │
         recibir (ingresa stock)
            │
            ▼
         RECIBIDA
```

## Manejo de errores

El MS usa un `GlobalExceptionHandler` que traduce las excepciones a códigos HTTP coherentes:

| Excepción | Código | Cuándo |
| --------- | ------ | ------ |
| `RecursoNoEncontradoException` | 404 Not Found | Proveedor u orden inexistente |
| `RecursoDuplicadoException` | 409 Conflict | RUT de proveedor duplicado |
| `EstadoInvalidoException` | 409 Conflict | Operación no válida para el estado actual (ej. autorizar una orden ya autorizada, orden a proveedor inactivo) |
| `DataIntegrityViolationException` | 409 Conflict | El recurso ya existe o viola una restricción de la base de datos (ej. RUT duplicado a nivel de BD) |
| `MethodArgumentNotValidException` | 400 Bad Request | Validación de campos fallida |
| `HttpMessageNotReadableException` | 400 Bad Request | JSON mal formado |
| `RestClientException` | 502 Bad Gateway | Error al comunicarse con otro microservicio |

## Configuración de base de datos

La aplicación usa MySQL. La base de datos `db_proveedor` se crea automáticamente (`createDatabaseIfNotExist=true`). Las tablas se crean vía Hibernate (`ddl-auto=update`).

Credenciales por defecto en `application.properties`:

- Usuario: `root`
- Contraseña: *(vacía, como en XAMPP por defecto)*

URLs de los microservicios que consume (en `application.properties`):

```
ms.productos.url=http://localhost:8082/api/productos/
ms.inventario.ajuste.url=http://localhost:8082/api/inventario/ajustar
ms.sucursales.url=http://localhost:8087/api/v1/sucursales/
```

## Swagger / OpenAPI

Documentación interactiva disponible en:

- Swagger UI: <http://localhost:8084/swagger-ui.html>
- API Docs (JSON): <http://localhost:8084/v3/api-docs>

Cada endpoint está documentado con su Historia de Usuario correspondiente para trazabilidad.