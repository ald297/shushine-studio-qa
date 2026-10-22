package com.shushinestudio.dtos.disponibilidad;

import java.time.LocalTime;

/** La disponibilidad solo necesita límites horarios, no clientes ni detalles de servicios. */
public record FranjaOcupada(LocalTime horaInicio, LocalTime horaFin) {}
