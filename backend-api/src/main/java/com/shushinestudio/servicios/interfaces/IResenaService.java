package com.shushinestudio.servicios.interfaces;

import com.shushinestudio.dtos.resena.ResenaCrearDto;
import com.shushinestudio.dtos.resena.ResenaSalidaDto;

import java.util.List;

public interface IResenaService {

    ResenaSalidaDto crearResena(String loginCliente, ResenaCrearDto dto);

    List<ResenaSalidaDto> obtenerMisResenas(String loginCliente);

    List<ResenaSalidaDto> obtenerTodasResenasAdmin();

    ResenaSalidaDto obtenerPorId(Integer id);
}
