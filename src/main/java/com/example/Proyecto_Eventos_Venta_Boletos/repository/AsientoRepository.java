package com.example.Proyecto_Eventos_Venta_Boletos.repository;

import com.example.Proyecto_Eventos_Venta_Boletos.model.Asiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AsientoRepository extends JpaRepository<Asiento, Long> {

    Optional<Asiento> findByZonaIdAndFilaAndNumero(Long zonaId, Integer fila, Integer numero);

    // Datos mínimos de todos los asientos de un recinto: [asientoId, zonaId, fila, numero].
    // Se piden como columnas (no como entidades) para no cargar cientos de objetos en cada visita.
    @Query("select a.id, a.zona.id, a.fila, a.numero from Asiento a where a.zona.recinto.id = :recintoId")
    List<Object[]> resumenPorRecinto(@Param("recintoId") Long recintoId);
}
