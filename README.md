# Konticket — Plataforma de Venta de Boletos para Eventos

Aplicación web para publicar eventos locales y vender boletos en línea. Los organizadores
crean eventos con sus tipos de boleto y consultan métricas de venta; los asistentes exploran
el catálogo y compran sus boletos.

## Tecnologías

- Java 25
- Spring Boot 4 (Spring MVC, Spring Data JPA, Spring Security)
- Thymeleaf
- MySQL 8
- Lombok
- Maven

## Requisitos

- JDK 25
- MySQL 8 en ejecución en `localhost:3306`

## Configuración

Las credenciales de la base de datos están en `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/konticket_db?serverTimezone=UTC&createDatabaseIfNotExist=true
spring.datasource.username=root
spring.datasource.password=123
```

Ajusta usuario y contraseña a tu instalación local (no subas tus credenciales personales al repositorio).
La base `konticket_db` y sus tablas se crean automáticamente al iniciar la aplicación.

## Ejecución

```bash
# Linux / macOS / Git Bash
./mvnw spring-boot:run

# Windows (CMD o PowerShell)
mvnw.cmd spring-boot:run
```

La aplicación queda disponible en http://localhost:8080

## Pantallas principales

| Ruta                          | Descripción                                      |
|-------------------------------|--------------------------------------------------|
| `/`                           | Catálogo de eventos                              |
| `/venta-boletos?id={id}`      | Selección y compra de boletos                    |
| `/mis-boletos`                | Boletos del asistente                            |
| `/organizador/panel`          | Panel del organizador con métricas e inventario  |
| `/organizador/evento/nuevo`   | Formulario para crear eventos                    |

## Estructura del proyecto

```
src/main/java/com/example/Proyecto_Eventos_Venta_Boletos/
├── config/        Configuración (seguridad, datos iniciales)
├── controller/    Controladores web
├── dto/           Objetos para métricas e inventario del panel
├── EstadosENUM/   Enumeraciones de estados y tipos
├── model/         Entidades JPA
├── repository/    Repositorios Spring Data
└── service/       Lógica de negocio
src/main/resources/
├── static/css/    Hojas de estilo
└── templates/     Vistas Thymeleaf
```
