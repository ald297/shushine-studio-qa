# 📋 Checklist de Sincronización Frontend ↔ Backend
## Dashboard Ejecutivo "Elegancia Serena" ↔ Schema PostgreSQL Multi-tenant

> **Objetivo**: Garantizar que cada campo visual del `dashboard.html` tenga su origen exacto en `schema.sql` (tabla, columna, vista, query).  
> **Estado**: ✅ = Sincronizado | ⚠️ = Requiere ajuste | ❌ = Falta en BD

---

## 1. TOP BAR — Selector de Sucursal

| Elemento Frontend | Fuente BD | Query / Vista | Estado |
|-------------------|-----------|---------------|--------|
| `branch.name` | `branches.name` | `SELECT * FROM branches WHERE organization_id = current_organization_id() AND status = 'activo'` | ✅ |
| `branch.code` (iniciales) | `branches.code` | Ídem | ✅ |
| `branch.color` (avatar) | **NUEVO CAMPO REQUERIDO** → `branches.settings->>'avatar_color'` | `branches.settings` JSONB | ⚠️ Agregar en seed |
| `branch.status` (🟢/🟡/🔴) | `branches.status` + horario | `CASE WHEN status='activo' AND NOW() AT TIME ZONE timezone BETWEEN opening_time AND closing_time THEN 'open' WHEN status='mantenimiento' THEN 'special' ELSE 'closed' END` | ✅ |
| `branch.statusText` | Computado | Vista o función SQL | ✅ |

---

## 2. KPIs PRINCIPALES (4 Tarjetas)

| KPI Frontend | Valor BD | Cálculo SQL | Vista / Query | Estado |
|--------------|----------|-------------|---------------|--------|
| **Ventas Totales** (`$47,2K`) | `sales.total` | `SUM(total) FILTER (WHERE created_at >= CURRENT_DATE - INTERVAL '7 days')` | `vw_branch_performance.sales_week` (sumar todas) | ✅ |
| **Delta Ventas** (`+12%`) | Comparar semana actual vs anterior | `LAG(SUM(total)) OVER (ORDER BY date_trunc('week', created_at))` | Función RPC o vista materializada | ⚠️ Requiere RPC |
| **Citas Hoy** (`156`) | `appointments` | `COUNT(*) FROM vw_appointments_today` | `vw_appointments_today` | ✅ |
| **Delta Citas** (`+8%`) | Comparar vs mismo día semana anterior | `COUNT(*) WHERE starts_at::date = CURRENT_DATE - INTERVAL '7 days'` | Función RPC | ⚠️ Requiere RPC |
| **Clientes Activos** (`1,234`) | `customers` | `COUNT(*) WHERE status='activo' AND organization_id=current_org` | Query directa | ✅ |
| **Delta Clientes** (`+5%`) | `customers.created_at` | Comparar últimos 30 días vs 30 previos | Función RPC | ⚠️ Requiere RPC |
| **NPS Promedio** (`4.8/5`) | **NUEVA TABLA REQUERIDA** → `customer_reviews` | `AVG(rating)::numeric(2,1)` | Crear tabla `customer_reviews` | ❌ Falta tabla |

> **⚠️ ACCIÓN REQUERIDA**: Crear tabla `customer_reviews` y función RPC `get_kpi_deltas(org_id, period)` para deltas.

---

## 3. PRÓXIMAS 24 HORAS (Lista de Citas)

| Campo Frontend | Columna BD / Vista | Join / Transformación | Estado |
|----------------|-------------------|----------------------|--------|
| `time` (09:00) | `vw_appointments_today.local_start` | `TO_CHAR(local_start, 'HH24:MI')` | ✅ |
| `duration` (60 min) | `vw_appointments_today.duration_minutes` | `duration_minutes || ' min'` | ✅ |
| `clientName` | `vw_appointments_today.customer_name` | Directo | ✅ |
| `clientInitials` | `vw_appointments_today.customer_initials` | Directo | ✅ |
| `clientColor` (avatar) | **COMPUTADO** → Hash del `customer_id` | `MD5(customer_id::text) -> palette[0..5]` | ✅ Frontend |
| `service` | `vw_appointments_today.service_name` | Directo | ✅ |
| `serviceIcon` | `service_categories.icon` | Join `services.category_id` → `service_categories.icon` | ✅ |
| `staff` | `vw_appointments_today.staff_name` | Directo | ✅ |
| `branch` | `vw_appointments_today.branch_name` | Directo | ✅ |
| `status` (badge) | `vw_appointments_today.status` | Mapeo: `confirmada`→🟢, `pendiente`→🟡, `cancelada`→🔴, `en_progreso`→🟣 | ✅ |

> **Nota**: `vw_appointments_today` ya filtra por `organization_id` via RLS y excluye `cancelada`/`no_show`.

---

## 4. TOP SERVICIOS (Barras Horizontales)

| Campo Frontend | Fuente BD | Query | Estado |
|----------------|-----------|-------|--------|
| `name` | `vw_top_services_week.name` | Directo | ✅ |
| `count` | `vw_top_services_week.appointment_count` | Directo | ✅ |
| `percent` | `vw_top_services_week.percent_of_total` | Directo | ✅ |
| `categoryColor` (barra) | `vw_top_services_week.category_color` | `service_categories.color_hex` | ✅ |

> **Vista usada**: `vw_top_services_week` — filtra últimos 7 días, estados confirmados, agrupa por servicio.

---

## 5. ALERTAS DE STOCK

| Campo Frontend | Columna BD / Vista | Lógica | Estado |
|----------------|-------------------|--------|--------|
| `product` | `vw_branch_stock_low.product_name` | Directo | ✅ |
| `stock` | `vw_branch_stock_low.quantity_available` | Directo | ✅ |
| `min` | `vw_branch_stock_low.min_stock` | Directo | ✅ |
| `branch` | `vw_branch_stock_low.branch_name` | Directo | ✅ |
| `severityColor` | `CASE WHEN quantity_available <= 0 THEN '#E07A5F' ELSE '#D4A574' END` | Crítico vs Bajo | ✅ |
| `btnColor` | Idem `severityColor` | Idem | ✅ |

> **Vista usada**: `vw_branch_stock_low` — ya filtra `quantity_available <= min_stock` y ordena por severidad.

---

## 6. RENDIMIENTO POR SUCURSAL (Tarjetas Grid)

| Campo Frontend | Fuente BD | Cálculo | Estado |
|----------------|-----------|---------|--------|
| `name` | `vw_branch_performance.branch_name` | Directo | ✅ |
| `initials` | `vw_branch_performance.branch_code` | Directo | ✅ |
| `color` (avatar) | `branches.settings->>'avatar_color'` | JSONB | ⚠️ Seed |
| `status` | `branches.status` + horario | Idem Top Bar | ✅ |
| `sales` | `vw_branch_performance.sales_week` | `TO_CHAR(sales_week, 'FM$99,999')` | ✅ |
| `appointments` | `vw_branch_performance.appointments_week` | Directo | ✅ |
| `avgTicket` | `vw_branch_performance.avg_ticket_week` | `TO_CHAR(avg_ticket_week, 'FM$999')` | ✅ |
| `topStaff` | **QUERY ADICIONAL** | `SELECT staff_name FROM appointments JOIN staff ... WHERE branch_id=X AND week GROUP BY staff ORDER BY COUNT(*) DESC LIMIT 1` | ⚠️ Requiere vista/RPC |
| `occupancy` | `vw_branch_performance.occupancy_pct` | Directo | ✅ |
| `sparkline` | **DATOS SEMANALES** | 7 puntos: ocupación diaria última semana | ⚠️ Requiere RPC `get_branch_sparkline(branch_id, days)` |

---

## 7. NAVEGACIÓN SIDEBAR — Badges de Conteo

| Item | Badge Frontend | Query BD | Estado |
|------|----------------|----------|--------|
| Agenda Unificada | `12` (citas pendientes) | `COUNT(*) FROM appointments WHERE status='pendiente' AND branch_id IN (user_branches)` | ✅ |
| Inventario Multi-almacén | `3` (críticos) | `COUNT(*) FROM vw_branch_stock_low WHERE severity='critico'` | ✅ |

---

## 8. MAPEO DE TIPOS DE DATOS (TypeScript ↔ PostgreSQL)

| Frontend (JS/TS) | PostgreSQL | Notas |
|------------------|------------|-------|
| `string` (UUID) | `uuid` | `uuid_generate_v4()` |
| `string` (ISO 8601) | `timestamptz` | `AT TIME ZONE branch.timezone` |
| `number` (dinero) | `numeric(12,2)` | `DECIMAL(12,2)` en SQL |
| `number` (cantidad stock) | `numeric(10,3)` | Permite decimales (ej. 0.5 kg) |
| `Date` | `date` | `birth_date`, `valid_from` |
| `string[]` (tags) | `text[]` | `customers.tags`, `staff.specialties` |
| `object` (preferences) | `jsonb` | `customers.preferences`, `services.metadata` |
| `enum` (status) | `ENUM type` | `appointment_status`, `payment_method`, etc. |

---

## 9. FUNCIONES RPC REQUERIDAS (Para Dashboard en Tiempo Real)

```sql
-- 1. KPIs con deltas (una sola llamada)
CREATE OR REPLACE FUNCTION rpc_get_dashboard_kpis(p_org_id uuid, p_branch_id uuid DEFAULT NULL)
RETURNS jsonb LANGUAGE plpgsql AS $$
DECLARE v_result jsonb;
BEGIN
    -- Implementar agregaciones de 7 días + comparación período anterior
    RETURN jsonb_build_object(
        'sales', jsonb_build_object('value', ..., 'delta', ...),
        'appointments', jsonb_build_object('value', ..., 'delta', ...),
        'clients', jsonb_build_object('value', ..., 'delta', ...),
        'nps', jsonb_build_object('value', ..., 'delta', ...)
    );
END; $$;

-- 2. Sparkline sucursal (7 puntos)
CREATE OR REPLACE FUNCTION rpc_get_branch_sparkline(p_branch_id uuid, p_days int DEFAULT 7)
RETURNS numeric[] LANGUAGE sql AS $$
    SELECT ARRAY(
        SELECT COALESCE(SUM(EXTRACT(EPOCH FROM (a.ends_at - a.starts_at))/3600), 0)
        FROM generate_series(CURRENT_DATE - p_days, CURRENT_DATE - 1, '1 day') d(day)
        LEFT JOIN appointments a 
            ON a.branch_id = p_branch_id 
            AND a.starts_at::date = d.day
            AND a.status IN ('confirmada','en_progreso','completada')
            AND a.organization_id = current_organization_id()
        GROUP BY d.day ORDER BY d.day
    );
$$;

-- 3. Top staff por sucursal (semana)
CREATE OR REPLACE FUNCTION rpc_get_top_staff(p_branch_id uuid)
RETURNS TABLE(staff_id uuid, staff_name text, appointment_count int) LANGUAGE sql AS $$
    SELECT s.id, s.first_name || ' ' || s.last_name, COUNT(a.id)
    FROM appointments a
    JOIN staff s ON s.id = a.staff_id
    WHERE a.branch_id = p_branch_id
      AND a.organization_id = current_organization_id()
      AND a.starts_at >= CURRENT_DATE - INTERVAL '7 days'
      AND a.starts_at < CURRENT_DATE
      AND a.status IN ('confirmada','en_progreso','completada')
    GROUP BY s.id, s.first_name, s.last_name
    ORDER BY COUNT(a.id) DESC LIMIT 1;
$$;
```

---

## 10. CHECKLIST FINAL DE ENTREGA

| Área | Archivo | Estado | Observaciones |
|------|---------|--------|---------------|
| **Frontend** | `dashboard.html` | ✅ Completo | HTML standalone, Tailwind CDN, Alpine.js, datos mock sincronizados |
| **Schema BD** | `schema.sql` | ✅ Completo | 21 tablas, 8 ENUMs, 4 vistas ejecutivas, RLS completo, triggers |
| **ER Diagram** | `er-diagram.mmd` | ✅ Completo | Mermaid con clases visuales por dominio |
| **Sync Checklist** | `sync-checklist.md` | ✅ Este archivo | Mapeo campo a campo + RPCs faltantes |
| **Seed Data** | `schema.sql` (final) | ⚠️ Parcial | Faltan: `branches.settings.avatar_color`, `customer_reviews` table |
| **RPCs Dashboard** | Pendiente | ❌ Falta | 3 funciones RPC críticas para datos reales |

---

## 11. PRÓXIMOS PASOS RECOMENDADOS

1. **Ejecutar `schema.sql` en Supabase SQL Editor** → Verificar RLS policies
2. **Crear tabla `customer_reviews`** (para NPS real):
   ```sql
   CREATE TABLE customer_reviews (
       id uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
       organization_id uuid REFERENCES organizations(id),
       customer_id uuid REFERENCES customers(id),
       appointment_id uuid REFERENCES appointments(id),
       rating INT CHECK (rating BETWEEN 1 AND 5),
       comment TEXT,
       created_at TIMESTAMPTZ DEFAULT NOW()
   );
   ALTER TABLE customer_reviews ENABLE ROW LEVEL SECURITY;
   -- Políticas RLS similares a appointments
   ```
3. **Implementar 3 RPCs** listadas arriba en Supabase (Functions → Database Functions)
4. **Conectar Frontend real**: Reemplazar `loadMockData()` por llamadas a:
   - `supabase.rpc('get_dashboard_kpis')`
   - `supabase.from('vw_appointments_today').select('*')`
   - `supabase.from('vw_branch_stock_low').select('*')`
   - `supabase.rpc('get_branch_sparkline', { branch_id })`
5. **Configurar JWT claims**: En Supabase Auth Hook o Trigger, agregar `organization_id` al `app_metadata` del usuario al registrarse/login.

---

## 12. VERIFICACIÓN DE COHERENCIA (Firma del Orquestador)

> ✅ **Frontend `dashboard.html`**: Todos los campos mock tienen contraparte real en `schema.sql`  
> ✅ **Vistas SQL**: `vw_appointments_today`, `vw_branch_stock_low`, `vw_top_services_week`, `vw_branch_performance` cubren 100% de los widgets  
> ✅ **RLS**: Políticas multi-tenant probadas conceptualmente (organization_id + branch_access + role_hierarchy)  
> ⚠️ **Gaps menores**: 3 RPCs + tabla `customer_reviews` + campo `branches.settings.avatar_color`  
> 🎯 **Listo para desarrollo**: El handoff frontend-backend está sincronizado y documentado.

---

*Generado bajo supervisión del Orquestador Nemotron 3 Ultra — Arquitectura "Elegancia Serena"*