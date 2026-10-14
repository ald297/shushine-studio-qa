package com.shushinestudio.dtos.dashboard;

import lombok.*;

import java.math.BigDecimal;
import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReporteResumenDto {

    private long totalCitas;
    private long citasCompletadas;
    private long citasEnProceso;
    private long citasCanceladas;
    private BigDecimal ingresosTotales;
    private BigDecimal ingresosHoy;
    private BigDecimal ingresosMes;
    private long productosBajoStock;
    private long estilistasActivos;
    private Map<String, Long> citasPorEstado;
}
