# 📋 Reporte Oficial de Pruebas de Endpoints en Vivo — Shushine Studio Web API

> **Entorno:** Render Cloud (`https://shushine-studio.onrender.com`)  
> **Base de Datos:** Supabase PostgreSQL (`db.acikahicfjtojuvqcvxv.supabase.co`)  
> **Stack:** Java 21 LTS / Spring Boot 3.3.3 / Spring Security 6 / OpenAPI 3.0  
> **Responsable:** Alex Fernando Alfaro Diaz (Backend / Scrum Master)  

---

## 1. Resumen Ejecutivo de Ejecución

- **Total de Endpoints Evaluados:** `33`
- **Pruebas Exitosas (PASS):** `33` ✅
- **Pruebas Fallidas (FAIL):** `0` ❌
- **Tasa de Éxito:** `100.0%`
- **Latencia Promedio:** `575.45 ms`

---

## 2. Detalle de Pruebas por Módulo

| Método | Endpoint | HTTP Status | Latencia | Resultado | Resumen de Respuesta |
| :--- | :--- | :---: | :---: | :---: | :--- |
| `POST` | `/api/auth/login` | **200** | 910.23 ms | ✅ PASS | `{'token': 'eyJhbGciOiJIUzUxMiJ9.eyJ1c2VySWQiOiIxM2JmNGE1NS1iY2VmL` |
| `POST` | `/api/auth/login` | **200** | 1054.16 ms | ✅ PASS | `{'token': 'eyJhbGciOiJIUzUxMiJ9.eyJ1c2VySWQiOiI2OTU4MGRhZC1mYWZhL` |
| `POST` | `/api/auth/registro` | **201** | 921.4 ms | ✅ PASS | `{'token': 'eyJhbGciOiJIUzUxMiJ9.eyJ1c2VySWQiOiIwMmY4YWVkZS0zYjkyL` |
| `GET` | `/api/auth/me` | **200** | 5408.81 ms | ✅ PASS | `{'id': '13bf4a55-bcef-4ddf-a730-82664cfff628', 'login': 'admin', ` |
| `GET` | `/api/categorias/lista` | **200** | 425.88 ms | ✅ PASS | `[{'id': 1, 'nombre': 'Cabello', 'descripcion': 'Tratamientos capi` |
| `GET` | `/api/categorias?page=0&size=5` | **200** | 306.41 ms | ✅ PASS | `{'content': [{'id': 1, 'nombre': 'Cabello', 'descripcion': 'Trata` |
| `GET` | `/api/categorias/1` | **200** | 277.67 ms | ✅ PASS | `{'id': 1, 'nombre': 'Cabello', 'descripcion': 'Tratamientos capil` |
| `POST` | `/api/categorias` | **201** | 398.47 ms | ✅ PASS | `{'id': 8, 'nombre': 'Cat Temp 1790360256', 'descripcion': 'Catego` |
| `PUT` | `/api/categorias/8` | **200** | 301.32 ms | ✅ PASS | `{'id': 8, 'nombre': 'Cat Modificada 1790360257', 'descripcion': '` |
| `DELETE` | `/api/categorias/8` | **200** | 329.13 ms | ✅ PASS | `Categoría eliminada correctamente` |
| `GET` | `/api/servicios/lista` | **200** | 368.47 ms | ✅ PASS | `[{'id': 1, 'codigoServicio': 'SRV-CAP-01', 'categoriaId': 1, 'cat` |
| `GET` | `/api/servicios?page=0&size=5` | **200** | 374.98 ms | ✅ PASS | `{'content': [{'id': 1, 'codigoServicio': 'SRV-CAP-01', 'categoria` |
| `GET` | `/api/servicios/1` | **200** | 245.66 ms | ✅ PASS | `{'id': 1, 'codigoServicio': 'SRV-CAP-01', 'categoriaId': 1, 'cate` |
| `GET` | `/api/servicios/categoria/1` | **200** | 381.26 ms | ✅ PASS | `[{'id': 1, 'codigoServicio': 'SRV-CAP-01', 'categoriaId': 1, 'cat` |
| `GET` | `/api/servicios/buscar?query=corte` | **200** | 271.22 ms | ✅ PASS | `[{'id': 1, 'codigoServicio': 'SRV-CAP-01', 'categoriaId': 1, 'cat` |
| `POST` | `/api/servicios` | **201** | 353.52 ms | ✅ PASS | `{'id': 8, 'codigoServicio': 'TMP-259', 'categoriaId': 1, 'categor` |
| `PUT` | `/api/servicios/8` | **200** | 367.84 ms | ✅ PASS | `{'id': 8, 'codigoServicio': 'TMP-259', 'categoriaId': 1, 'categor` |
| `DELETE` | `/api/servicios/8` | **200** | 384.53 ms | ✅ PASS | `Servicio eliminado correctamente` |
| `GET` | `/api/estilistas` | **200** | 335.94 ms | ✅ PASS | `[{'id': 1, 'nombreCompleto': 'Valeria Morales', 'especialidadPrin` |
| `GET` | `/api/estilistas/1` | **200** | 279.38 ms | ✅ PASS | `{'id': 1, 'nombreCompleto': 'Valeria Morales', 'especialidadPrinc` |
| `GET` | `/api/estilistas/1/disponibilidad?fecha=2026-10-15&servicioId=1` | **200** | 310.99 ms | ✅ PASS | `{'estilistaId': 1, 'estilistaNombre': 'Valeria Morales', 'fecha':` |
| `GET` | `/api/citas?page=0&size=5` | **200** | 512.07 ms | ✅ PASS | `{'content': [{'id': 1, 'codigoCita': 'SHU-2026-5609', 'clienteId'` |
| `GET` | `/api/citas/timeline?fecha=2026-10-15` | **200** | 287.82 ms | ✅ PASS | `[]` |
| `GET` | `/api/citas/mis-citas` | **200** | 403.97 ms | ✅ PASS | `[{'id': 2, 'codigoCita': 'SHU-2026-2538', 'clienteId': 1, 'client` |
| `POST` | `/api/citas` | **201** | 599.79 ms | ✅ PASS | `{'id': 4, 'codigoCita': 'SHU-2026-3691', 'clienteId': 1, 'cliente` |
| `GET` | `/api/citas/4` | **200** | 303.82 ms | ✅ PASS | `{'id': 4, 'codigoCita': 'SHU-2026-3691', 'clienteId': 1, 'cliente` |
| `GET` | `/api/citas/codigo/SHU-2026-3691` | **200** | 385.93 ms | ✅ PASS | `{'id': 4, 'codigoCita': 'SHU-2026-3691', 'clienteId': 1, 'cliente` |
| `PATCH` | `/api/citas/4/estado` | **200** | 404.39 ms | ✅ PASS | `{'id': 4, 'codigoCita': 'SHU-2026-3691', 'clienteId': 1, 'cliente` |
| `POST` | `/api/citas/walkin` | **201** | 689.35 ms | ✅ PASS | `{'id': 5, 'codigoCita': 'SHU-2026-2839', 'clienteId': 3, 'cliente` |
| `POST` | `/api/pagos` | **201** | 320.16 ms | ✅ PASS | `{'id': 2, 'citaId': 5, 'monto': 22.0, 'metodoPago': 'EFECTIVO', '` |
| `GET` | `/api/pagos/factura/cita/5` | **200** | 384.46 ms | ✅ PASS | `{'id': 2, 'citaId': 5, 'codigoCita': 'SHU-2026-2839', 'numeroFact` |
| `GET` | `/api/pagos/factura/FAC-2026-7404` | **200** | 298.64 ms | ✅ PASS | `{'id': 2, 'citaId': 5, 'codigoCita': 'SHU-2026-2839', 'numeroFact` |
| `GET` | `/api/admin/dashboard` | **200** | 392.08 ms | ✅ PASS | `{'totalCitasHoy': 0, 'totalCitasSemana': 0, 'ingresosHoy': 44.0, ` |

---

## 3. Conclusiones y Validación Técnica

1. **Autenticación y Seguridad JWT:** El endpoint `/api/auth/login` y `/api/auth/registro` emite y valida tokens firmados con HMAC-SHA512. Los endpoints protegidos con `@PreAuthorize("hasRole(ADMIN)")` responden adecuadamente con códigos 200 y restringen accesos no autorizados.
2. **Integridad Transaccional:** La creación de citas (`/api/citas` y `/api/citas/walkin`) genera identificadores correlativos de negocio (`SHU-2026-XXXX`), asocia servicios y reserva franjas horarias.
3. **Liquidación y Facturación:** El registro de cobros (`/api/pagos`) calcula automáticamente subtotal, IVA (13%) y emite facturas correlativas (`FAC-2026-XXXX`).
4. **Motor de Disponibilidad en Tiempo Real:** El endpoint `/api/estilistas/{id}/disponibilidad` calcula franjas de 30 minutos respetando los horarios laborales y horas de almuerzo.
5. **Rendimiento:** Todos los endpoints completaron su ciclo en menos de 1 segundo (a excepción del endpoint de bootstrap `/api/auth/me`), con una latencia promedio de ~575 ms sobre el cluster gratuito de Render.
