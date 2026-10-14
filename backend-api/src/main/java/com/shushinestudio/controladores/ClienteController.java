package com.shushinestudio.controladores;

import com.shushinestudio.dtos.cliente.ClienteSalidaDto;
import com.shushinestudio.servicios.interfaces.IClienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/admin/clientes", "/api/clientes"})
@Tag(name = "Directorio de Clientes", description = "Endpoints administrativos para gestión y consulta del directorio de clientes de Shushine Studio")
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class ClienteController {

    @Autowired
    private IClienteService clienteService;

    @GetMapping
    @Operation(summary = "Listado paginado de clientes", description = "Retorna todos los clientes registrados y walk-in con paginación.")
    public ResponseEntity<Page<ClienteSalidaDto>> obtenerClientesPaginados(Pageable pageable) {
        return ResponseEntity.ok(clienteService.obtenerTodosAdminPaginados(pageable));
    }

    @GetMapping("/lista")
    @Operation(summary = "Listado completo de clientes", description = "Retorna todos los clientes para selectores rápidos y directorio móvil.")
    public ResponseEntity<List<ClienteSalidaDto>> obtenerClientesLista() {
        return ResponseEntity.ok(clienteService.obtenerTodosAdmin());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener detalle de cliente por ID", description = "Retorna el perfil, fidelidad, preferencias e historial del cliente.")
    public ResponseEntity<ClienteSalidaDto> obtenerClientePorId(@PathVariable Integer id) {
        ClienteSalidaDto cliente = clienteService.obtenerPorIdAdmin(id);
        if (cliente == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(cliente);
    }
}
