# HoloNet 🛰️

A real-time web messaging application inspired by galactic transmission terminals. Built with a unified data architecture using **Clojure (Backend)** and **ClojureScript + Re-frame (Frontend)**, backed by persistent **SQLite** storage and bidirectional communication via **WebSockets**.

---

## 🚀 Tech Stack

| Layer        | Technology              | Description                                                                     |
| :----------- | :---------------------- | :------------------------------------------------------------------------------ |
| **Frontend** | `ClojureScript`         | Compiles to JS with emphasis on immutability and functional design              |
|              | `Re-frame` / `Reagent`  | Unidirectional reactive architecture powered by data-driven UI                  |
|              | `shadow-cljs`           | Build tool and seamless JS ecosystem integration                                |
|              | `cljs-ajax`             | Asynchronous HTTP client for REST API consumption                               |
| **Backend**  | `Clojure`               | Hosted runtime on the JVM                                                       |
|              | `Ring` / `Reitit`       | High-performance HTTP server & router with middlewares (`Muuntaja`, `Parameters`)|
|              | `http-kit`              | Concurrent, high-throughput HTTP/WebSocket server                               |
|              | `Buddy Hashers`         | Secure credential hashing (bcrypt)                                              |
|              | `SQLite` (via JDBC)     | Lightweight, persistent on-disk relational database                             |
| **Realtime** | `WebSocket` (`/api/ws`) | Low-latency channel with active per-operator socket registration                |

---

## ✨ Key Features

- **Operator Registration**: Real-time credential validation (email format, encryption key complexity, and key confirmation).
- **Secure Authentication**: Bcrypt password hashing persisted in an SQLite database (`holonet_users.db`).
- **Contact & Friendship Management**:
  - Search and send link requests between operators.
  - Manage incoming and outgoing link requests (Accept / Reject).
- **Real-Time Messaging & Persistence**:
  - Bidirectional WebSocket channels.
  - Cross-operator message history stored in SQLite and indexed by `timestamp`.
  - Automatic socket cleanup upon logout to avoid duplicate listeners.
- **Simulation Channels**: In-memory mock channels for swift UI testing in development environments.

---

## 🛠️ Prerequisites

Make sure the following tools are installed on your system:

- **JDK 17+**
- **Clojure CLI** (`clojure`)
- **Node.js** (v18+) & **npm**

---

## 🏁 Installation & Setup

### 1. Clone the repository

```bash
git clone https://github.com/your-username/messages-clj-project.git
cd messages-clj-project
```

### 2. Install dependencies

```bash
npm install
```

### 3. Start the Backend Server

Start the Clojure Ring/http-kit server (port 3000):

```bash
clj -M:dev -m server
```

### 4. Start the Frontend Dev Server

In a separate terminal, launch `shadow-cljs`:

```bash
npx shadow-cljs watch app
```

Open your browser at [http://localhost:8080](http://localhost:8080).
