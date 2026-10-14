package com.shushinestudio.servicios.interfaces;

import com.shushinestudio.dtos.cita.CitaCambiarEstado;
import com.shushinestudio.dtos.cita.CitaGuardar;
import com.shushinestudio.dtos.cita.CitaSalida;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ICitaService {
    CitaSalida crearCita(CitaGuardar citaGuardar, String userLogin);
    CitaSalida crearCitaWalkin(com.shushinestudio.dtos.cita.CitaWalkinGuardar walkinGuardar);
    List<CitaSalida> obtenerMisCitas(String userLogin);
    Page<CitaSalida> obtenerTodasPaginadas(Pageable pageable);
    CitaSalida obtenerPorId(Integer id);
    CitaSalida obtenerPorCodigo(String codigoCita);
    CitaSalida cambiarEstado(CitaCambiarEstado cambio);
    CitaSalida cambiarEstado(CitaCambiarEstado cambio, String userLogin, boolean isAdmin);
    CitaSalida cancelarCita(Integer id, String motivo, String userLogin, boolean isAdmin);
}
