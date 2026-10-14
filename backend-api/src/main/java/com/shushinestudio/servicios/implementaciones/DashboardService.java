package com.shushinestudio.servicios.implementaciones;

import com.shushinestudio.dtos.dashboard.DashboardMetricasSalida;
import com.shushinestudio.repositorios.ICitaRepository;
import com.shushinestudio.repositorios.IEstilistaRepository;
import com.shushinestudio.repositorios.IPagoRepository;
import com.shushinestudio.servicios.interfaces.IDashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;

@Service
public class DashboardService implements IDashboardService {

    @Autowired
    private ICitaRepository citaRepository;

    @Autowired
    private IPagoRepository pagoRepository;

    @Autowired
    private IEstilistaRepository estilistaRepository;

    @Override
    @Transactional(readOnly = true)
    public DashboardMetricasSalida obtenerMetricas() {
        LocalDate hoy = LocalDate.now();
        LocalDate inicioSemana = hoy.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate finSemana = hoy.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        LocalDateTime inicioDia = hoy.atStartOfDay();
        LocalDateTime finDia = hoy.atTime(LocalTime.MAX);

        LocalDate primerDiaMes = hoy.withDayOfMonth(1);
        LocalDate ultimoDiaMes = hoy.with(TemporalAdjusters.lastDayOfMonth());
        LocalDateTime inicioMes = primerDiaMes.atStartOfDay();
        LocalDateTime finMes = ultimoDiaMes.atTime(LocalTime.MAX);

        long totalCitasHoy = citaRepository.countByFechaCita(hoy);
        long totalCitasSemana = citaRepository.countByFechaCitaBetween(inicioSemana, finSemana);

        BigDecimal ingresosHoy = pagoRepository.sumTotalRecaudadoEntre(inicioDia, finDia);
        if (ingresosHoy == null) ingresosHoy = BigDecimal.ZERO;

        BigDecimal ingresosMes = pagoRepository.sumTotalRecaudadoEntre(inicioMes, finMes);
        if (ingresosMes == null) ingresosMes = BigDecimal.ZERO;

        long citasEnProceso = citaRepository.countByEstado("InProgress");
        long citasCompletadas = citaRepository.countByEstado("Completed");
        long citasCanceladas = citaRepository.countByEstado("Cancelled");

        long estilistasActivos = estilistaRepository.findByActivoTrue().size();

        return DashboardMetricasSalida.builder()
                .totalCitasHoy(totalCitasHoy)
                .totalCitasSemana(totalCitasSemana)
                .ingresosHoy(ingresosHoy)
                .ingresosMes(ingresosMes)
                .citasEnProceso(citasEnProceso)
                .citasCompletadas(citasCompletadas)
                .citasCanceladas(citasCanceladas)
                .estilistasActivos(estilistasActivos)
                .build();
    }
}
