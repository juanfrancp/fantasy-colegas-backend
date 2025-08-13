# Fantasy Colegas Backend

El backend de Fantasy Colegas es una aplicación construida con Spring Boot que gestiona toda la lógica de negocio para una plataforma de ligas de fantasía de fútbol. Permite a los usuarios crear ligas personalizadas, gestionar sus equipos (rosters), seguir el rendimiento de los jugadores y calcular las puntuaciones de cada jornada.

## 🚀 Características Principales

* **Autenticación y Autorización:**
    * Registro de nuevos usuarios y autenticación segura mediante JWT.
    * Gestión de perfiles de usuario.
    * Control de acceso basado en roles (`ADMIN` y `PARTICIPANT`) dentro de cada liga.

* **Gestión de Ligas:**
    * Creación de ligas públicas y privadas con opciones de personalización (nombre, descripción, tamaño del equipo, etc.).
    * Unión a ligas privadas mediante un código de invitación.
    * Manejo de participantes y roles de administrador de liga.

* **Gestión de Jugadores y Puntos:**
    * Creación, actualización y eliminación de jugadores en una liga por parte de los administradores.
    * Registro de estadísticas de jugadores por partido.
    * Cálculo automático de puntos de jugadores basado en reglas de puntuación configurables por rol (jugador de campo o portero).

* **Gestión de Equipos (Rosters):**
    * Los participantes pueden crear y modificar sus equipos.
    * Validaciones de tamaño y roles de los jugadores en el equipo.
    * Sustitución automática de jugadores eliminados por un jugador "placeholder".

## 🛠️ Tecnologías Utilizadas

* **Backend:** Spring Boot, Spring Security, JWT.
* **Base de Datos:** JPA / Hibernate (compatible con cualquier base de datos SQL).
* **Dependencias:** Lombok para reducir código repetitivo, Jakarta Validation para la validación de DTOs.
* **Patrones de Diseño:** Arquitectura de capas (Controller, Service, Repository).

## ⚙️ Configuración y Ejecución

Para ejecutar la aplicación, necesitas tener instalado Java 17 o superior y Maven.

1.  **Clonar el repositorio:**
    ```bash
    git clone [URL_DEL_REPOSITORIO]
    cd fantasy_colegas_backend
    ```
2.  **Configurar la base de datos:**
    Asegúrate de configurar la conexión a tu base de datos en el archivo `application.properties` o `application.yml`.
3.  **Compilar y ejecutar:**
    Puedes usar tu IDE o ejecutar el siguiente comando en la terminal:
    ```bash
    mvn spring-boot:run
    ```
    La aplicación se iniciará en `http://localhost:8080`.

## 📌 Puntos de la API (Ejemplos)

* `POST /api/auth/register`: Registro de un nuevo usuario.
* `POST /api/auth/login`: Autenticación y obtención de JWT.
* `GET /api/leagues/{leagueId}`: Obtener detalles de una liga.
* `POST /api/leagues/{leagueId}/roster`: Guardar el equipo de un usuario.
* `PUT /api/leagues/{leagueId}/players/{playerId}`: Actualizar un jugador (requiere rol de administrador).
