package com.shushinestudio.controladores;

import com.shushinestudio.dtos.dashboard.DashboardMetricasSalida;
import com.shushinestudio.dtos.dashboard.ReporteResumenDto;
import com.shushinestudio.modelos.Cita;
import com.shushinestudio.repositorios.ICitaRepository;
import com.shushinestudio.repositorios.IProductoRepository;
import com.shushinestudio.servicios.interfaces.IDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/api/admin/reportes", "/api/reportes"})
@Tag(name = "Reportes Administrativos", description = "Endpoints de inteligencia de negocio, balances operativos y exportación oficial")
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class ReporteController {

    @Autowired
    private IDashboardService dashboardService;

    @Autowired
    private ICitaRepository citaRepository;

    @Autowired
    private IProductoRepository productoRepository;

    @GetMapping("/resumen")
    @Operation(summary = "Resumen integral de métricas y estados", description = "Calcula el desglose de citas por estado, stock de productos e ingresos consolidados.")
    public ResponseEntity<ReporteResumenDto> obtenerResumen() {
        DashboardMetricasSalida metricas = dashboardService.obtenerMetricas();

        Map<String, Long> porEstado = new HashMap<>();
        porEstado.put("En Proceso", metricas.getCitasEnProceso());
        porEstado.put("Completadas", metricas.getCitasCompletadas());
        porEstado.put("Canceladas", metricas.getCitasCanceladas());

        long bajoStock = productoRepository.findAll().stream()
                .filter(p -> p.getStockActual() != null && p.getStockMinimo() != null && p.getStockActual() <= p.getStockMinimo())
                .count();

        long total = citaRepository.count();

        ReporteResumenDto resumen = ReporteResumenDto.builder()
                .totalCitas(total)
                .citasCompletadas(metricas.getCitasCompletadas())
                .citasEnProceso(metricas.getCitasEnProceso())
                .citasCanceladas(metricas.getCitasCanceladas())
                .ingresosTotales(metricas.getIngresosMes())
                .ingresosHoy(metricas.getIngresosHoy())
                .ingresosMes(metricas.getIngresosMes())
                .productosBajoStock(bajoStock)
                .estilistasActivos(metricas.getEstilistasActivos())
                .citasPorEstado(porEstado)
                .build();

        return ResponseEntity.ok(resumen);
    }

    @GetMapping("/exportar")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    @Operation(summary = "Exportar reporte financiero y operativo a CSV", description = "Genera un archivo CSV con el balance de citas e ingresos para Excel u hojas de cálculo.")
    public ResponseEntity<byte[]> exportarReporte() {
        List<Cita> citas = citaRepository.findAll();

        StringBuilder csv = new StringBuilder();
        csv.append("REPORTE OFICIAL DE CITAS E INGRESOS - SHUSHINE STUDIO\n");
        csv.append("Generado el: ").append(java.time.LocalDateTime.now()).append("\n\n");
        csv.append("ID,Codigo,Fecha,Hora,Cliente,Estilista,Estado,Total,Estado Pago\n");

        for (Cita c : citas) {
            String cliente = "Cliente";
            if (c.getCliente() != null && c.getCliente().getUsuario() != null) {
                cliente = c.getCliente().getUsuario().getNombre() + " " + (c.getCliente().getUsuario().getApellido() != null ? c.getCliente().getUsuario().getApellido() : "");
            } else if (c.getCliente() != null && c.getCliente().getNombreWalkin() != null) {
                cliente = c.getCliente().getNombreWalkin();
            }

            String estilista = c.getEstilista() != null ? c.getEstilista().getNombreCompleto() : "No asignado";

            csv.append(c.getId()).append(",")
               .append("\"").append(c.getCodigoCita() != null ? c.getCodigoCita() : "").append("\",")
               .append(c.getFechaCita()).append(",")
               .append(c.getHoraInicio()).append(",")
               .append("\"").append(cliente.replace("\"", "\"\"")).append("\",")
               .append("\"").append(estilista.replace("\"", "\"\"")).append("\",")
               .append(c.getEstado()).append(",")
               .append(c.getTotal()).append(",")
               .append(c.getEstadoPago()).append("\n");
        }

        byte[] bytes = csv.toString().getBytes(StandardCharsets.UTF_8);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv; charset=UTF-8"));
        headers.setContentDispositionFormData("attachment", "reporte_shushine_studio.csv");
        headers.setContentLength(bytes.length);

        return ResponseEntity.ok()
                .headers(headers)
                .body(bytes);
    }
}
