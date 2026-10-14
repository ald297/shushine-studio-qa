package com.shushinestudio.controladores;

import com.shushinestudio.dtos.chat.ConversacionDetalleDto;
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

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/chat")
@Tag(name = "Chat Administrador", description = "Endpoints de gestión de conversaciones y mensajería para personal administrativo")
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AdminChatController {

    @Autowired
    private IChatService chatService;

    @GetMapping("/conversaciones")
    @Operation(summary = "Bandeja de conversaciones (Paginada)", description = "Devuelve el listado de conversaciones activas con clientes ordenadas por fecha del último mensaje.")
    public ResponseEntity<Page<ConversacionSalidaDto>> obtenerConversaciones(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size) {
        Page<ConversacionSalidaDto> resultado = chatService.obtenerConversacionesAdmin(PageRequest.of(page, size));
        return ResponseEntity.ok(resultado);
    }

    @GetMapping("/conversaciones/lista")
    @Operation(summary = "Listado completo de conversaciones", description = "Devuelve todas las conversaciones activas para selectores y listas rápidas.")
    public ResponseEntity<List<ConversacionSalidaDto>> obtenerConversacionesLista() {
        List<ConversacionSalidaDto> resultado = chatService.obtenerConversacionesAdmin();
        return ResponseEntity.ok(resultado);
    }

    @GetMapping("/conversaciones/{id}")
    @Operation(summary = "Obtener detalle de conversación", description = "Devuelve la información de la conversación y sus mensajes asociados.")
    public ResponseEntity<ConversacionDetalleDto> obtenerConversacionPorId(@PathVariable("id") Integer id) {
        ConversacionDetalleDto resultado = chatService.obtenerConversacionPorIdAdmin(id);
        return ResponseEntity.ok(resultado);
    }

    @GetMapping("/conversaciones/{id}/mensajes")
    @Operation(summary = "Historial paginado de mensajes de una conversación", description = "Devuelve los mensajes de la conversación especificada ordenados cronológicamente.")
    public ResponseEntity<Page<MensajeSalidaDto>> obtenerMensajesConversacion(
            @PathVariable("id") Integer id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size) {
        Page<MensajeSalidaDto> resultado = chatService.obtenerMensajesAdmin(id, PageRequest.of(page, size));
        return ResponseEntity.ok(resultado);
    }

    @PostMapping("/conversaciones/{id}/mensajes")
    @Operation(summary = "Enviar respuesta desde el Administrador", description = "Registra una respuesta del administrador hacia el cliente en la conversación especificada.")
    public ResponseEntity<MensajeSalidaDto> responderMensaje(
            @PathVariable("id") Integer id,
            @Valid @RequestBody MensajeEnviarDto dto,
            Authentication authentication) {
        String adminLogin = authentication.getName();
        MensajeSalidaDto resultado = chatService.enviarMensajeAdmin(id, adminLogin, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(resultado);
    }

    @RequestMapping(value = "/conversaciones/{id}/mensajes/leidos", method = {RequestMethod.PUT, RequestMethod.PATCH})
    @Operation(summary = "Marcar mensajes de la conversación como leídos por el Administrador", description = "Marca como leídos los mensajes enviados por el cliente.")
    public ResponseEntity<Map<String, Object>> marcarMensajesLeidos(
            @PathVariable("id") Integer id,
            Authentication authentication) {
        String adminLogin = authentication.getName();
        int actualizados = chatService.marcarMensajesComoLeidosAdmin(id, adminLogin);
        return ResponseEntity.ok(Map.of(
                "status", 200,
                "mensaje", "Mensajes marcados como leídos con éxito",
                "mensajesActualizados", actualizados
        ));
    }
}
