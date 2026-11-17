package com.example.Proyecto_Eventos_Venta_Boletos.repository;

import com.example.Proyecto_Eventos_Venta_Boletos.model.Organizador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface OrganizadorRepository extends JpaRepository<Organizador, Long> {
    // El correo es el usuario con el que inicia sesión (ver UsuarioDetailsService)
    Optional<Organizador> findByCorreo(String correo);

    boolean existsByCorreo(String correo);
}
