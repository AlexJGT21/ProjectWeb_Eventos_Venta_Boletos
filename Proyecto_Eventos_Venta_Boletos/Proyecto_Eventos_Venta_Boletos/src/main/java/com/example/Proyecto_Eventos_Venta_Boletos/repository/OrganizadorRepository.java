package com.example.Proyecto_Eventos_Venta_Boletos.repository;

import com.example.Proyecto_Eventos_Venta_Boletos.model.Organizador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface OrganizadorRepository extends JpaRepository<Organizador, Long> {
    // Te servirá más adelante cuando implementes Spring Security real
    Optional<Organizador> findByEmail(String email);
}