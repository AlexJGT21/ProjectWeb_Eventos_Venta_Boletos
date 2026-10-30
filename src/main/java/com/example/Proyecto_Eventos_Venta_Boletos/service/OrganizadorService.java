package com.example.Proyecto_Eventos_Venta_Boletos.service;

import com.example.Proyecto_Eventos_Venta_Boletos.model.Evento;
import com.example.Proyecto_Eventos_Venta_Boletos.model.Organizador;
import com.example.Proyecto_Eventos_Venta_Boletos.repository.EventoRepository;
import com.example.Proyecto_Eventos_Venta_Boletos.repository.OrganizadorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;

@Service
public class OrganizadorService {

    private final OrganizadorRepository organizadorRepository;
    private final EventoRepository eventoRepository;

    @Autowired
    public OrganizadorService(OrganizadorRepository organizadorRepository, EventoRepository eventoRepository) {
        this.organizadorRepository = organizadorRepository;
        this.eventoRepository = eventoRepository;
    }

    // --- CRUD del Organizador ---
    public Organizador guardarOrganizador(Organizador organizador) {
        return organizadorRepository.save(organizador);
    }

    public Organizador obtenerPorId(Long id) {
        return organizadorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Organizador no encontrado"));
    }

    public Organizador actualizarPerfil(Long id, Organizador datosActualizados) {
        Organizador organizador = obtenerPorId(id);
        organizador.setNombre(datosActualizados.getNombre());
        organizador.setCorreo(datosActualizados.getCorreo());

        if(datosActualizados.getContrasenia() != null && !datosActualizados.getContrasenia().isEmpty()){
            organizador.setContrasenia(datosActualizados.getContrasenia());
        }
        return organizadorRepository.save(organizador);
    }

    public void eliminarOrganizador(Long id) {
        organizadorRepository.deleteById(id);
    }

    // --- Lógica de Eventos ---
    public List<Evento> obtenerEventosPorOrganizador(Long organizadorId) {
        return eventoRepository.findByOrganizadorId(organizadorId);
    }

    public void procesarYGuardarEvento(Evento evento, MultipartFile imagenFile, Long organizadorId) {
        try {
            Organizador organizador = obtenerPorId(organizadorId);
            evento.setOrganizador(organizador);

            if (imagenFile != null && !imagenFile.isEmpty()) {
                evento.setImagenEvento((imagenFile.getBytes()));
            }
            eventoRepository.save(evento);
        } catch (IOException e) {
            throw new RuntimeException("Error al procesar la imagen del evento", e);
        }
    }

    public void eliminarEvento(Long eventoId, Long organizadorId) {
        Evento evento = eventoRepository.findById(eventoId)
                .orElseThrow(() -> new IllegalArgumentException("Evento no encontrado"));

        // Regla de negocio: Validar que el evento pertenezca a este organizador
        if(evento.getOrganizador().getId().equals(organizadorId)) {
            eventoRepository.delete(evento);
        } else {
            throw new SecurityException("No tienes permiso para eliminar este evento");
        }
    }
}