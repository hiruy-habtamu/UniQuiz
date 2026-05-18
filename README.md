# UniQuiz

UniQuiz is a Java client/server quiz system built with Gradle. The project contains a socket-based server, a JavaFX client, and a shared module for models and serialized message types.

## Modules

- `server`: database access, services, socket server, quiz/session logic
- `client`: JavaFX desktop client for teachers and students
- `shared`: models, messages, and shared config helpers

## Tech Stack

- Java 21
- Gradle
- JavaFX
- MariaDB / MySQL JDBC
- dotenv-java for `.env` loading

## Project Structure

- `schema.sql`: base database schema
- `SERVER_FEATURE_SPEC.md`: current server capabilities and limitations
- `client/src/main/resources/com/quizapp/client/fxml`: JavaFX views
- `server/src/main/java/com/quizapp/server`: DAOs, services, networking, session state
- `shared/src/main/java/com/quizapp/shared`: shared models, messages, and config helper

## Environment Configuration

The app now auto-loads a local `.env` file using `dotenv-java`.

Create a `.env` file in the project root using `.env.example` as a starting point.

### Supported Variables

#### Database

- `DB_URL`
- `DB_HOST`
- `DB_PORT`
- `DB_NAME`
- `DB_USER`
- `DB_PASSWORD`

`DB_URL` is optional. If it is not set, the server builds a JDBC URL from the other database variables.

#### Server

- `SERVER_PORT`

#### Client

- `CLIENT_SERVER_HOST`
- `CLIENT_SERVER_PORT`

### Example `.env`

```env
DB_HOST=localhost
DB_PORT=3306
DB_NAME=quizapp
DB_USER=root
DB_PASSWORD=root
SERVER_PORT=5050
CLIENT_SERVER_HOST=localhost
CLIENT_SERVER_PORT=5050
```

## Database Setup

Create or update your MariaDB database before running the server.

For a fresh database:

```bash
mysql -u root -p < schema.sql
```

If your database already exists, prefer targeted `ALTER TABLE` statements instead of rerunning the full schema file.

## Running The Server

```bash
./gradlew :server:run
```

The server listens on `SERVER_PORT` from `.env`, defaulting to `5050`.

## Running The Client

```bash
./gradlew :client:run
```

The client connects to `CLIENT_SERVER_HOST` and `CLIENT_SERVER_PORT` from `.env`, defaulting to `localhost:5050`.

## Build And Verification

```bash
./gradlew build
```

This builds all modules and runs the server smoke tests.

## Current Behavior Notes

- One active semester per academic year can be enforced through the UI activation flow.
- A quiz can only be created for an active semester.
- A closed quiz can be started again if its semester is active.
- A student is considered to have taken a quiz after the first persisted answer and cannot join that quiz again.
- Quiz results are currently shown client-side after finishing; there is no persistent results history yet.

## Future Work

- export quiz results
- stronger authorization rules
- richer teacher analytics and history screens
- improved student/teacher results reporting
