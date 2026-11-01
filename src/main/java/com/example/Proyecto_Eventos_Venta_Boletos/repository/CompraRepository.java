package com.example.Proyecto_Eventos_Venta_Boletos.repository;

import com.example.Proyecto_Eventos_Venta_Boletos.model.Compra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CompraRepository extends JpaRepository<Compra, Long> {

    // Compras de los eventos de un organizador (o de un solo evento) desde una fecha, para la gráfica del panel
    @Query("select distinct c from Compra c join c.boletos b " +
           "where b.evento.organizador.id = :organizadorId " +
           "and (:eventoId is null or b.evento.id = :eventoId) " +
           "and c.fechaHora >= :desde")
    List<Compra> buscarVentasDesde(@Param("organizadorId") Long organizadorId,
                                   @Param("eventoId") Long eventoId,
                                   @Param("desde") LocalDateTime desde);

    boolean existsByCodigoPedido(String codigoPedido);
}
