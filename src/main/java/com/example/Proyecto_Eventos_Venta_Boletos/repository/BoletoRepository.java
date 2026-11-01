package com.example.Proyecto_Eventos_Venta_Boletos.repository;

import com.example.Proyecto_Eventos_Venta_Boletos.model.Boleto;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BoletoRepository extends JpaRepository<Boleto, Long> {

    /**
     * Bloquea el tipo de boleto (SELECT ... FOR UPDATE) mientras se registra una compra, para que
     * dos personas no puedan comprar el mismo asiento o el último boleto al mismo tiempo.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from Boleto b where b.id = :id")
    Optional<Boleto> buscarParaComprar(@Param("id") Long id);

    // Asientos ya vendidos de una zona en un evento (se pintan como ocupados en el mapa)
    @Query("select b.codigoAsiento from Boleto b " +
           "where b.evento.id = :eventoId and b.zonaCodigo = :zonaCodigo " +
           "and b.compra is not null and b.codigoAsiento is not null")
    List<String> asientosOcupados(@Param("eventoId") Long eventoId, @Param("zonaCodigo") String zonaCodigo);
}
