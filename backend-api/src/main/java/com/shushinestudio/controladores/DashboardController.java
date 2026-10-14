package com.shushinestudio.controladores;

import com.shushinestudio.dtos.dashboard.DashboardMetricasSalida;
import com.shushinestudio.servicios.interfaces.IDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/dashboard")
@Tag(name = "Dashboard Administrativo", description = "Endpoints de inteligencia de negocio, KPIs diarios, ingresos consolidados y métricas operativas del salón")
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class DashboardController {

    @Autowired
    private IDashboardService dashboardService;

    @GetMapping
    @Operation(summary = "Obtener KPIs y métricas gerenciales", description = "Calcula en tiempo real los ingresos del día, mes, volumen de citas y ocupación de estilistas.")
    public ResponseEntity<DashboardMetricasSalida> obtenerMetricas() {
        return ResponseEntity.ok(dashboardService.obtenerMetricas());
    }
}
