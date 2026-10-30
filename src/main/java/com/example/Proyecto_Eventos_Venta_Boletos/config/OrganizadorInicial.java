package com.example.Proyecto_Eventos_Venta_Boletos.config;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Mientras no haya inicio de sesión, el panel del organizador trabaja con el organizador id = 1
 * (ver ORGANIZADOR_ID en OrganizadorController). Al arrancar se asegura de que exista;
 * INSERT IGNORE no modifica nada si ya está registrado.
 */
@Component
public class OrganizadorInicial implements CommandLineRunner {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public void run(String... args) {
        entityManager.createNativeQuery(
                        "INSERT IGNORE INTO organizadores (id, nombre, telefono, correo) " +
                        "VALUES (1, :nombre, :telefono, :correo)")
                .setParameter("nombre", "Organizador Konticket")
                .setParameter("telefono", "0000000000")
                .setParameter("correo", "organizador@konticket.mx")
                .executeUpdate();
    }
}
