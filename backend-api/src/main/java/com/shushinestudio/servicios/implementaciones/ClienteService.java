package com.shushinestudio.servicios.implementaciones;

import com.shushinestudio.dtos.cliente.ClienteSalidaDto;
import com.shushinestudio.modelos.Cliente;
import com.shushinestudio.repositorios.ICitaRepository;
import com.shushinestudio.repositorios.IClienteRepository;
import com.shushinestudio.servicios.interfaces.IClienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClienteService implements IClienteService {

    @Autowired
    private IClienteRepository clienteRepository;

    @Autowired
    private ICitaRepository citaRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<ClienteSalidaDto> obtenerTodosAdminPaginados(Pageable pageable) {
        Page<Cliente> page = clienteRepository.findAll(pageable);
        List<ClienteSalidaDto> dtos = page.getContent().stream()
                .map(this::mapearADto)
                .collect(Collectors.toList());
        return new PageImpl<>(dtos, page.getPageable(), page.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClienteSalidaDto> obtenerTodosAdmin() {
        return clienteRepository.findAll().stream()
                .map(this::mapearADto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteSalidaDto obtenerPorIdAdmin(Integer id) {
        return clienteRepository.findById(id)
                .map(this::mapearADto)
                .orElse(null);
    }

    private ClienteSalidaDto mapearADto(Cliente c) {
        String nombre = "";
        String apellido = "";
        String nombreCompleto = "";
        String telefono = null;
        String correo = null;
        String login = null;
        Boolean activo = true;

        if (c.getUsuario() != null) {
            nombre = c.getUsuario().getNombre() != null ? c.getUsuario().getNombre() : "";
            apellido = c.getUsuario().getApellido() != null ? c.getUsuario().getApellido() : "";
            nombreCompleto = (nombre + " " + apellido).trim();
            telefono = c.getUsuario().getTelefono();
            correo = c.getUsuario().getCorreo() != null ? c.getUsuario().getCorreo() :
                    (c.getUsuario().getLogin() != null && c.getUsuario().getLogin().contains("@")
                            ? c.getUsuario().getLogin()
                            : c.getUsuario().getLogin() + "@shushinestudio.com");
            login = c.getUsuario().getLogin();
            activo = c.getUsuario().getActivo();
        } else {
            nombreCompleto = c.getNombreWalkin() != null ? c.getNombreWalkin() : "Cliente Walk-in";
            nombre = nombreCompleto;
            telefono = c.getTelefonoWalkin();
        }

        int totalCitas = citaRepository.findByClienteIdOrderByFechaCitaDescHoraInicioDesc(c.getId()).size();

        return ClienteSalidaDto.builder()
                .id(c.getId())
                .nombreCompleto(nombreCompleto.isEmpty() ? "Cliente" : nombreCompleto)
                .nombre(nombre)
                .apellido(apellido)
                .telefono(telefono)
                .correo(correo)
                .login(login)
                .nivelFidelidad(c.getNivelFidelidad() != null ? c.getNivelFidelidad() : "Bronce")
                .puntosAcumulados(c.getPuntosAcumulados() != null ? c.getPuntosAcumulados() : 0)
                .tipoCabello(c.getTipoCabello())
                .notasPreferencias(c.getNotasPreferencias())
                .esWalkin(c.getEsWalkin() != null ? c.getEsWalkin() : false)
                .totalCitas(totalCitas)
                .activo(activo != null ? activo : true)
                .fechaCreacion(c.getFechaCreacion())
                .build();
    }
}
