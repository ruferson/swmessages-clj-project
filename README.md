# HoloNet 🛰️

Una aplicación web de mensajería en tiempo real inspirada en terminales de transmisión galáctica. Construida con una arquitectura de datos unificada en **Clojure (Backend)** y **ClojureScript + Re-frame (Frontend)**, respaldada por la persistencia en **SQLite** y comunicación bidireccional vía **WebSockets**.

---

## 🚀 Tecnologías Utilizadas

| Capa         | Tecnología              | Descripción                                                                     |
| :----------- | :---------------------- | :------------------------------------------------------------------------------ |
| **Frontend** | `ClojureScript`         | Lenguaje compilado a JS con foco en inmutabilidad                               |
|              | `Re-frame` / `Reagent`  | Arquitectura reactiva unidireccional basada en data-driven UI                   |
|              | `shadow-cljs`           | Herramienta de compilación e integración del ecosistema JS                      |
|              | `cljs-ajax`             | Cliente HTTP asíncrono para consumo de API REST                                 |
| **Backend**  | `Clojure`               | Entorno de ejecución en la JVM                                                  |
|              | `Ring` / `Reitit`       | Servidor HTTP y enrutador optimizado con middlewares (`Muuntaja`, `Parameters`) |
|              | `http-kit`              | Servidor HTTP/WebSocket concurrente de alto rendimiento                         |
|              | `Buddy Hashers`         | Seguridad y encriptación de credenciales (bcrypt)                               |
|              | `SQLite` (via JDBC)     | Base de datos relacional ligera persistente en disco                            |
| **Realtime** | `WebSocket` (`/api/ws`) | Canal de baja latencia con registro activo de sockets por operador              |

---

## ✨ Características Principales

- **Registro de Operadores**: Validación de credenciales en tiempo real (formato de correo, complejidad de encriptación y coincidencia).
- **Autenticación Segura**: Hash de contraseñas con bcrypt persistido en base de datos SQLite (`holonet_users.db`).
- **Gestión de Contactos & Amistades**:
  - Búsqueda y envío de solicitudes de enlace entre operadores.
  - Gestión de solicitudes entrantes y salientes (Aceptar / Rechazar).
- **Mensajería en Tiempo Real & Persistencia**:
  - Canales bidireccionales vía WebSockets.
  - Historial de mensajes cruzados almacenado en SQLite e indexado por `timestamp`.
  - Desconexión y limpieza automática de sockets al cerrar sesión para evitar escuchas duplicadas.
- **Canales de Simulación**: Canales en memoria para pruebas de interfaz rápido en entorno de desarrollo.

---

## 🛠️ Requisitos Previos

Asegúrate de tener instalados los siguientes componentes en tu sistema:

- **JDK 17+**
- **Clojure CLI** (`clojure`)
- **Node.js** (v18+) & **npm**

---

## 🏁 Instalación y Ejecución

### 1. Clonar el repositorio

```bash
git clone [https://github.com/tu-usuario/wmessages-clj-project.git](https://github.com/tu-usuario/wmessages-clj-project.git)
cd wmessages-clj-project
```
