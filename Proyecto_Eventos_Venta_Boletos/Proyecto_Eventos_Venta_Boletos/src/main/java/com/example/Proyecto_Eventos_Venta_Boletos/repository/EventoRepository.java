package com.example.Proyecto_Eventos_Venta_Boletos.repository;

import com.example.Proyecto_Eventos_Venta_Boletos.model.Evento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventoRepository extends JpaRepository<Evento, Long> {
}