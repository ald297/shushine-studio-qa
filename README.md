# 💇‍♀️ Sistema de Gestión — Salón de Belleza Multi-Sucursal

Sistema integral de administración para salones de belleza y spas con arquitectura multi-sucursal, diseñado bajo el concepto estético **"Elegancia Serena"** (#1A1A2E / #D4A574).

---

## 📁 Estructura del Proyecto

```text
salon-de-belleza/
├── dashboard.html        # Frontend interactivo completo (Tailwind CSS + Alpine.js)
├── schema.sql            # Base de datos relacional para PostgreSQL / Supabase
├── er-diagram.mmd        # Diagrama de Entidad-Relación (Formato Mermaid)
├── sync-checklist.md     # Mapeo y auditoría técnica Frontend ↔ Base de datos
└── README.md             # Esta documentación
```

---

## 🎨 1. Frontend (`dashboard.html`)

* **Tecnología:** HTML5 + Tailwind CSS (vía CDN) + Alpine.js.
* **Características:**
  * Selector dinámico multi-sucursal con estados en tiempo real (🟢 Abierto / 🔴 Cerrado / 🟡 Horario especial).
  * 4 KPIs ejecutivos con gráficos sparkline interactivos (Canvas).
  * Lista de citas de las próximas 24 horas con badges de estado.
  * Módulo de alertas de inventario crítico y bajo stock.
  * Selector de tema Modo Oscuro / Modo Claro (persistente en localStorage).
  * Menú lateral (Sidebar) colapsable.

### Cómo visualizarlo:
Haz doble clic sobre `dashboard.html` o ejecuta en la terminal:
```bash
xdg-open dashboard.html
```

---

## 🗄️ 2. Base de Datos (`schema.sql`)

* **Motor:** PostgreSQL (optimizado para **Supabase**).
* **Capacidades:**
  * **21 tablas normalizadas** (sucursales, clientes, servicios, citas, pagos, inventario, personal).
  * **8 tipos ENUM** para estados consistentes (`appointment_status`, `payment_method`, etc.).
  * **4 vistas SQL ejecutivas** (`vw_branch_performance`, `vw_appointments_today`, `vw_branch_stock_low`, `vw_top_services_week`).
  * **Seguridad RLS (Row Level Security):** Aislamiento multi-tenant por sucursal.
  * Triggers automáticos para timestamp `updated_at`.

### Cómo desplegarlo en Supabase:
1. Abre tu panel de **Supabase** y ve al **SQL Editor**.
2. Copia y pega el contenido de `schema.sql`.
3. Haz clic en **Run**.

---

## 📊 3. Diagrama Entidad-Relación (`er-diagram.mmd`)

Contiene el modelo de datos en sintaxis Mermaid. Puedes visualizarlo en editores compatibles con Markdown / Mermaid o pegarlo en [mermaid.live](https://mermaid.live).

---

## 🔍 4. Matriz de Sincronización (`sync-checklist.md`)

Mapeo exhaustivo campo a campo que audita la correspondencia entre los widgets del frontend y las columnas de la base de datos SQL, con recomendaciones para implementar funciones RPC.
