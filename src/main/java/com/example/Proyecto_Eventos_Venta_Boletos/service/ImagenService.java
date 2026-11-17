package com.example.Proyecto_Eventos_Venta_Boletos.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

/**
 * Sube y borra imágenes en Cloudinary. En la base de datos solo se guarda la URL pública
 * y el public_id que regresa Cloudinary; el archivo nunca se guarda dentro de la entidad.
 *
 * La cuenta se configura con la variable de entorno CLOUDINARY_URL
 * (cloudinary://API_KEY:API_SECRET@CLOUD_NAME), ver application.properties.
 */
@Service
public class ImagenService {

    private static final Logger log = LoggerFactory.getLogger(ImagenService.class);

    /** Imagen ya subida: URL para mostrarla y public_id para poder borrarla después. */
    public record ImagenSubida(String url, String publicId) {}

    // null si no se configuró CLOUDINARY_URL
    private final Cloudinary cloudinary;

    public ImagenService(@Value("${cloudinary.url:}") String cloudinaryUrl) {
        this.cloudinary = (cloudinaryUrl == null || cloudinaryUrl.isBlank()) ? null : new Cloudinary(cloudinaryUrl);
        if (this.cloudinary == null) {
            log.warn("Cloudinary no está configurado (CLOUDINARY_URL vacía): no se podrán subir imágenes de eventos.");
        }
    }

    public boolean isConfigurado() {
        return cloudinary != null;
    }

    /**
     * Sube la imagen a la carpeta indicada dentro de "konticket/".
     * Devuelve null si no se eligió ningún archivo.
     */
    public ImagenSubida subir(MultipartFile archivo, String carpeta) {
        if (archivo == null || archivo.isEmpty()) {
            return null;
        }
        if (cloudinary == null) {
            throw new IllegalStateException("No se pudo subir la imagen: falta configurar Cloudinary (variable CLOUDINARY_URL).");
        }
        String tipo = archivo.getContentType();
        if (tipo == null || !tipo.startsWith("image/")) {
            throw new IllegalStateException("El archivo «" + archivo.getOriginalFilename() + "» no es una imagen.");
        }
        try {
            Map<?, ?> resultado = cloudinary.uploader().upload(archivo.getBytes(), ObjectUtils.asMap(
                    "folder", "konticket/" + carpeta,
                    "resource_type", "image"));
            return new ImagenSubida((String) resultado.get("secure_url"), (String) resultado.get("public_id"));
        } catch (IOException | RuntimeException e) {
            log.error("Error al subir la imagen a Cloudinary", e);
            throw new IllegalStateException("No se pudo subir la imagen a Cloudinary, intenta de nuevo.");
        }
    }

    /** Borra una imagen de Cloudinary. Si falla solo se registra: no debe impedir guardar el evento. */
    public void eliminar(String publicId) {
        if (cloudinary == null || publicId == null || publicId.isBlank()) {
            return;
        }
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (IOException | RuntimeException e) {
            log.warn("No se pudo borrar la imagen {} de Cloudinary", publicId, e);
        }
    }
}
