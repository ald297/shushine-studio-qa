package com.shushinestudio.servicios.interfaces;

import com.shushinestudio.dtos.solicitud.*;

import java.util.List;

public interface ISolicitudService {

    SolicitudSalidaDto crearSolicitud(String loginCliente, SolicitudCrearDto dto);

    List<SolicitudSalidaDto> obtenerMisSolicitudes(String loginCliente);

    SolicitudSalidaDto obtenerPorIdCliente(String loginCliente, Integer id);

    List<SolicitudSalidaDto> obtenerTodasAdmin();

    SolicitudSalidaDto obtenerPorIdAdmin(Integer id);

    SolicitudSalidaDto cotizarSolicitudAdmin(Integer idSolicitud, CotizacionCrearDto dto);

    SolicitudSalidaDto responderCotizacionCliente(String loginCliente, Integer idSolicitud, ResponderCotizacionDto dto);

    void cambiarEstadoAdmin(Integer idSolicitud, String nuevoEstado);
}
