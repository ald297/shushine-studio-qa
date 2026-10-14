package com.shushinestudio.dtos.dashboard;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardMetricasSalida {
    private long totalCitasHoy;
    private long totalCitasSemana;
    private BigDecimal ingresosHoy;
    private BigDecimal ingresosMes;
    private long citasEnProceso;
    private long citasCompletadas;
    private long citasCanceladas;
    private long estilistasActivos;
}
