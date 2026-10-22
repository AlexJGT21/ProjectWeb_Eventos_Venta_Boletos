package com.example.Proyecto_Eventos_Venta_Boletos.repository;

import com.example.Proyecto_Eventos_Venta_Boletos.model.Organizador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrganizadorRepository extends JpaRepository<Organizador, Long> {
}