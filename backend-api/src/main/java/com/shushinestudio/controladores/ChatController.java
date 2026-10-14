package com.shushinestudio.controladores;

import com.shushinestudio.dtos.chat.ConversacionSalidaDto;
import com.shushinestudio.dtos.chat.MensajeEnviarDto;
import com.shushinestudio.dtos.chat.MensajeSalidaDto;
import com.shushinestudio.servicios.interfaces.IChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@Tag(name = "Chat Cliente", description = "Endpoints de comunicación bidireccional entre Cliente y Administración del Salón")
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("hasAnyAuthority('ROLE_CLIENTE', 'ROLE_ADMIN')")
public class ChatController {

    @Autowired
    private IChatService chatService;

    @GetMapping("/conversacion")
    @Operation(summary = "Obtener o crear conversación del cliente", description = "Recupera la conversación activa del cliente autenticado con el salón o la crea automáticamente.")
    public ResponseEntity<ConversacionSalidaDto> obtenerConversacion(Authentication authentication) {
        String login = authentication.getName();
        ConversacionSalidaDto conversacion = chatService.obtenerOCrearConversacionCliente(login);
        return ResponseEntity.ok(conversacion);
    }

    @GetMapping("/mensajes")
    @Operation(summary = "Obtener historial de mensajes del cliente", description = "Devuelve los mensajes de la conversación del cliente autenticado ordenados cronológicamente con paginación.")
    public ResponseEntity<Page<MensajeSalidaDto>> obtenerMensajes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size,
            Authentication authentication) {
        String login = authentication.getName();
        Page<MensajeSalidaDto> mensajes = chatService.obtenerMensajesCliente(login, PageRequest.of(page, size));
        return ResponseEntity.ok(mensajes);
    }

    @PostMapping("/mensajes")
    @Operation(summary = "Enviar mensaje desde el cliente", description = "Envía un nuevo mensaje al salón. La identidad del remitente se extrae de forma segura desde el JWT.")
    public ResponseEntity<MensajeSalidaDto> enviarMensaje(
            @Valid @RequestBody MensajeEnviarDto dto,
            Authentication authentication) {
        String login = authentication.getName();
        MensajeSalidaDto mensaje = chatService.enviarMensajeCliente(login, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(mensaje);
    }

    @RequestMapping(value = "/mensajes/leidos", method = {RequestMethod.PUT, RequestMethod.PATCH})
    @Operation(summary = "Marcar mensajes como leídos", description = "Marca como leídos todos los mensajes recibidos por el cliente en su conversación activa.")
    public ResponseEntity<Map<String, Object>> marcarMensajesLeidos(Authentication authentication) {
        String login = authentication.getName();
        int actualizados = chatService.marcarMensajesComoLeidosCliente(login);
        return ResponseEntity.ok(Map.of(
                "status", 200,
                "mensaje", "Mensajes marcados como leídos con éxito",
                "mensajesActualizados", actualizados
        ));
    }
}
