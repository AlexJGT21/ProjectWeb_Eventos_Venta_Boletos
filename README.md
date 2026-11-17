# Konticket — Plataforma de Venta de Boletos para Eventos

Aplicación web para publicar eventos locales y vender boletos en línea. Los organizadores
crean eventos con sus tipos de boleto y consultan métricas de venta; los asistentes exploran
el catálogo y compran sus boletos.

## Tecnologías

- Java 25
- Spring Boot 4 (Spring MVC, Spring Data JPA, Spring Security)
- Thymeleaf
- MySQL 8
- Cloudinary (almacenamiento de las imágenes de los eventos)
- Lombok
- Maven

## Requisitos

- JDK 25
- MySQL 8 en ejecución en `localhost:3306`
- Una cuenta de Cloudinary (el plan gratuito es suficiente) para subir imágenes de eventos

## Configuración

Las credenciales se leen de variables de entorno para no subirlas al repositorio
(ver `src/main/resources/application.properties`):

| Variable         | Para qué sirve                                                     | Valor por defecto |
|------------------|--------------------------------------------------------------------|-------------------|
| `DB_USERNAME`    | Usuario de MySQL                                                   | `root`            |
| `DB_PASSWORD`    | Contraseña de MySQL                                                | `123`             |
| `CLOUDINARY_URL` | Cuenta de Cloudinary: `cloudinary://API_KEY:API_SECRET@CLOUD_NAME` | (vacía)           |

La `CLOUDINARY_URL` se copia del *Dashboard* de Cloudinary ("API environment variable").
En IntelliJ se configuran en *Run → Edit Configurations → Environment variables*.
Sin `CLOUDINARY_URL` la aplicación funciona, pero no se pueden subir banners ni croquis.

La base `konticket_db` y sus tablas se crean automáticamente al iniciar la aplicación.

### Datos de prueba

Si la base no tiene eventos, al arrancar se cargan 6 eventos de ejemplo (de entrada general y con
mapa numerado) con compras repartidas en los últimos 30 días, para que el catálogo y los paneles
se vean funcionando (`config/DatosPrueba.java`). Se desactiva con `konticket.datos-prueba=false`.

## Ejecución

```bash
# Linux / macOS / Git Bash
./mvnw spring-boot:run

# Windows (CMD o PowerShell)
mvnw.cmd spring-boot:run
```

La aplicación queda disponible en http://localhost:8080

Al iniciar se crean estos usuarios (todos entran por `/user-login` y cada uno llega a su panel según su rol):

| Rol                           | Correo                     | Contraseña     |
|-------------------------------|----------------------------|----------------|
| Administrador                 | `admin@konticket.mx`       | `Admin12345`   |
| Organizador                   | `organizador@konticket.mx` | `Konticket123` |
| Organizador (datos de prueba) | `sonoralive@konticket.mx`  | `Konticket123` |

## Pantallas principales

| Ruta                          | Descripción                                      |
|-------------------------------|--------------------------------------------------|
| `/`                           | Catálogo de eventos                              |
| `/venta-boletos?id={id}`      | Selección y compra de boletos                    |
| `/mis-boletos`                | Boletos del asistente                            |
| `/organizador/panel`          | Panel del organizador con métricas e inventario  |
| `/organizador/evento/nuevo`   | Formulario para crear eventos                    |
| `/organizador/perfil`         | Perfil del organizador                           |
| `/admin/dashboard`            | Panel de administración (rol ADMIN)              |
| `/admin/organizadores`        | Alta y baja de organizadores (rol ADMIN)         |
| `/admin/eventos`              | Control y cancelación de eventos (rol ADMIN)     |

## Estructura del proyecto

```
src/main/java/com/example/Proyecto_Eventos_Venta_Boletos/
├── config/        Configuración (seguridad, usuarios iniciales y datos de prueba)
├── controller/    Controladores web
├── dto/           Objetos para métricas e inventario del panel
├── EstadosENUM/   Enumeraciones de estados y tipos
├── model/         Entidades JPA
├── repository/    Repositorios Spring Data
├── service/       Lógica de negocio (los controladores solo hablan con los servicios)
└── util/          Utilerías (plantilla del mapa del recinto, generador de QR)
src/main/resources/
├── static/css/    Hojas de estilo
└── templates/     Vistas Thymeleaf (admin/ contiene las vistas de administración)
```
