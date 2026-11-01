package com.example.Proyecto_Eventos_Venta_Boletos.config;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Asegura que exista un organizador de prueba para poder iniciar sesión:
 *   correo: organizador@konticket.mx   contraseña: Konticket123
 * INSERT IGNORE no modifica nada si ya está registrado; la contraseña solo se asigna
 * si el registro no tiene una.
 */
@Component
public class OrganizadorInicial implements CommandLineRunner {

    private static final String CONTRASENIA_INICIAL = "Konticket123";

    @PersistenceContext
    private EntityManager entityManager;

    private final PasswordEncoder passwordEncoder;

    public OrganizadorInicial(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

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

        entityManager.createNativeQuery(
                        "UPDATE organizadores SET contrasenia = :hash " +
                        "WHERE id = 1 AND (contrasenia IS NULL OR contrasenia = '')")
                .setParameter("hash", passwordEncoder.encode(CONTRASENIA_INICIAL))
                .executeUpdate();
    }
}
