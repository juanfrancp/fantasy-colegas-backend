# Fantasy Colegas Backend

El backend de **Fantasy Colegas** es una aplicación robusta construida con **Spring Boot** y **Java 24** que gestiona toda la lógica de negocio para una plataforma de ligas de fantasía de fútbol. Permite a los usuarios crear ligas personalizadas, gestionar sus equipos, seguir el rendimiento de los jugadores y calcular las puntuaciones de cada jornada de forma automática.

## Características Principales

* **Autenticación y Autorización Segura**:
    * Registro de nuevos usuarios y autenticación mediante **JWT (JSON Web Tokens)**.
    * Gestión de perfiles de usuario con actualización de datos y contraseña.
    * Control de acceso basado en roles (`ADMIN`, `PARTICIPANT`) dentro de cada liga para una gestión segura.

* **Gestión Completa de Ligas**:
    * Creación de ligas **públicas y privadas** con opciones de personalización (nombre, descripción, tamaño del equipo, etc.).
    * Sistema de unión a ligas mediante código de invitación o solicitud a ligas privadas.
    * Manejo de participantes y asignación de roles de administrador de liga.

* **Gestión de Jugadores y Puntuaciones**:
    * **CRUD** completo de jugadores en una liga, gestionado por los administradores.
    * Registro de estadísticas detalladas de jugadores por partido.
    * Cálculo automático de puntos basado en **reglas de puntuación configurables** para distintos roles (jugador de campo o portero).

* **Gestión de Equipos (Rosters)**:
    * Creación y modificación de equipos por parte de los participantes.
    * Validaciones para asegurar el cumplimiento de las reglas de la liga (tamaño del equipo, un único portero, etc.).
    * Sustitución automática de jugadores eliminados por un "placeholder" para mantener la integridad del equipo.

## Tecnologías Utilizadas

* **Backend**: Spring Boot 3, Spring Security, JWT.
* **Base de Datos**: JPA / Hibernate (configurado para H2, pero compatible con cualquier base de datos SQL como PostgreSQL o MySQL).
* **Dependencias Clave**: Lombok, Jakarta Validation.
* **Testing**: JUnit 5, Mockito, Spring Boot Test para pruebas de integración y unitarias.
* **Arquitectura**: Diseño basado en capas (Controlador, Servicio, Repositorio).

## Configuración y Ejecución

Para poner en marcha la aplicación en tu entorno local, necesitarás tener instalado lo siguiente:

* **Java 17** o superior.
* **Maven** 3.8 o superior.

Sigue estos pasos para ejecutar el proyecto:

1.  **Clonar el repositorio**:
    ```bash
    git clone [URL_DEL_REPOSITORIO]
    cd fantasy-colegas-backend
    ```

2.  **Configurar la base de datos**:
    El proyecto está preconfigurado para usar una base de datos en memoria **H2**. La configuración se encuentra en `src/main/resources/application.properties`. No necesitas hacer cambios para empezar.

3.  **Compilar y ejecutar la aplicación**:
    Puedes usar tu IDE favorito o ejecutar el siguiente comando en la terminal desde la raíz del proyecto:
    ```bash
    ./mvnw spring-boot:run
    ```
    La aplicación se iniciará en `http://localhost:8080`.

## Puntos de la API (Ejemplos)

La API está estructurada por recursos para una fácil comprensión. Todos los endpoints requieren un token JWT en la cabecera `Authorization: Bearer <token>`, a excepción de `/api/auth/**`.

* **Autenticación (`/api/auth`)**
    * `POST /register`: Registro de un nuevo usuario.
    * `POST /login`: Autenticación y obtención de un token JWT.

* **Ligas (`/api/leagues`)**
    * `POST /`: Crear una nueva liga.
    * `GET /{leagueId}`: Obtener los detalles de una liga.
    * `POST /{leagueId}/rosters`: Guardar o actualizar el equipo de un usuario en la liga.
    * `PATCH /{leagueId}/players/{playerId}`: Actualizar un jugador (requiere rol de `ADMIN`).

* **Usuarios (`/api/users`)**
    * `GET /{id}`: Obtener los detalles de un usuario.
    * `PUT /{id}`: Actualizar la información de un usuario.
    * `PUT /{id}/password`: Actualizar la contraseña de un usuario.
