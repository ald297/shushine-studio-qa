package com.shushinestudio.servicios.interfaces;

import com.shushinestudio.dtos.cliente.ClienteSalidaDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IClienteService {
    Page<ClienteSalidaDto> obtenerTodosAdminPaginados(Pageable pageable);
    List<ClienteSalidaDto> obtenerTodosAdmin();
    ClienteSalidaDto obtenerPorIdAdmin(Integer id);
}
