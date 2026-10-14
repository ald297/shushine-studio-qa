package com.shushinestudio.seguridad.servicios;

import com.shushinestudio.seguridad.modelos.Rol;
import com.shushinestudio.seguridad.repositorios.RolRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RolService {

    @Autowired
    private RolRepository rolRepository;

    public List<Rol> obtenerRoles() {
        return rolRepository.findAll();
    }

    public Rol obtenerPorId(Integer id) {
        return rolRepository.findById(id).orElse(null);
    }

    public Optional<Rol> obtenerPorNombre(String nombre) {
        return rolRepository.findByNombreIgnoreCase(nombre);
    }
}
