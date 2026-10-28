# HoloNet

A full-stack messaging web application: a **ClojureScript / Re-frame** frontend with a **Clojure** backend, featuring operator registration, login, and real-time chat channels over WebSocket.

## Tech Stack

| Layer     | Technology |
|-----------|-----------|
| Frontend  | ClojureScript, Re-frame, Reagent, cljs-ajax, shadow-cljs |
| Backend   | Clojure, Ring, Reitit, http-kit, Buddy Hashers, SQLite (JDBC) |
| Realtime  | WebSocket (`/ws`) with active-channel tracking |

## Features

- Operator registration with validation (name, email format, password strength, confirmation match).
- Login with hashed passwords (Buddy Hashers) against a persistent SQLite database.
- Re-frame-powered UI with events, effects and subscriptions.
- Real-time messaging channels over WebSocket.

## Getting Started

### Prerequisites

- JDK 17+
- Clojure CLI (`clojure`)
- Node.js (for shadow-cljs)

### Run the backend

```bash
clojure -M:dev -m server
```

The API server starts on `http://localhost:3000`.

### Run the frontend

```bash
npx shadow-cljs watch app
```

The development server starts on `http://localhost:8080`.

## API

### Register

```
POST /api/register
Content-Type: application/json

{
  "name": "string",
  "email": "string",
  "password": "string",
  "password-confirmation": "string",
  "rank": "string",
  "photo-url": "string"
}
```

### Login

```
POST /api/login
Content-Type: application/json

{
  "email": "string",
  "password": "string"
}
```

### WebSocket

```
WS /ws
```

## Project Structure

```
src/
  main/
    app/       Re-frame frontend (events, subs, db, views)
    server/    Ring/Reitit backend (API + WebSocket)
    dev/        Development server entry point
  shared/      Shared code between frontend and backend
```

## Notes

- User data is stored in a local SQLite database (`holonet_users.db`, git-ignored).
- Passwords are stored as bcrypt hashes (Buddy Hashers).
