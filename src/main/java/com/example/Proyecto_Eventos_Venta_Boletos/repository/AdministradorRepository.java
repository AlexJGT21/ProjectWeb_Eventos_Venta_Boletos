package com.example.Proyecto_Eventos_Venta_Boletos.repository;

import com.example.Proyecto_Eventos_Venta_Boletos.model.Administrador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdministradorRepository extends JpaRepository<Administrador, Integer> {

    // Para buscar al administrador por su correo durante el inicio de sesión
    Optional<Administrador> findByCorreo(String correo);

    boolean existsByCorreo(String correo);
}
