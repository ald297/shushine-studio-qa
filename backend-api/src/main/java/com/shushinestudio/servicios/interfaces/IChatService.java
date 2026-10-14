package com.shushinestudio.servicios.interfaces;

import com.shushinestudio.dtos.chat.ConversacionDetalleDto;
import com.shushinestudio.dtos.chat.ConversacionSalidaDto;
import com.shushinestudio.dtos.chat.MensajeEnviarDto;
import com.shushinestudio.dtos.chat.MensajeSalidaDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IChatService {

    // === Operaciones para CLIENTE ===
    ConversacionSalidaDto obtenerOCrearConversacionCliente(String userLogin);

    Page<MensajeSalidaDto> obtenerMensajesCliente(String userLogin, Pageable pageable);

    MensajeSalidaDto enviarMensajeCliente(String userLogin, MensajeEnviarDto dto);

    int marcarMensajesComoLeidosCliente(String userLogin);

    // === Operaciones para ADMINISTRADOR ===
    Page<ConversacionSalidaDto> obtenerConversacionesAdmin(Pageable pageable);

    List<ConversacionSalidaDto> obtenerConversacionesAdmin();

    ConversacionDetalleDto obtenerConversacionPorIdAdmin(Integer conversacionId);

    Page<MensajeSalidaDto> obtenerMensajesAdmin(Integer conversacionId, Pageable pageable);

    MensajeSalidaDto enviarMensajeAdmin(Integer conversacionId, String adminLogin, MensajeEnviarDto dto);

    int marcarMensajesComoLeidosAdmin(Integer conversacionId, String adminLogin);
}
