package com.example.Proyecto_Eventos_Venta_Boletos.repository;

import com.example.Proyecto_Eventos_Venta_Boletos.EstadosENUM.EstadoEventoENUM;
import com.example.Proyecto_Eventos_Venta_Boletos.model.Evento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Repository
public interface EventoRepository extends JpaRepository<Evento, Long> {

    // Método personalizado para que el organizador pueda ver solo sus eventos
    List<Evento> findByOrganizadorId(Long organizadorId);

    // Catálogo público: eventos futuros que no estén en los estados indicados (ej. CANCELADO), ordenados por fecha
    List<Evento> findByFechaHoraEventoAfterAndEstadoEventoNotInOrderByFechaHoraEventoAsc(
            LocalDateTime desde, Collection<EstadoEventoENUM> estadosExcluidos);
}
