# stoTracker

A Java 21 Spring Boot + Thymeleaf web application for tracking Star Trek Online character data.

## Features

- Track multiple characters with dilithium, credits, and timestamps
- Set/unset recruitment (20-min timer), refining, and event timestamps
- LCARS-styled frontend
- Persistent SQLite database via Docker

## Tech Stack

- Java 21
- Spring Boot 3.2
- Thymeleaf (HTML templates)
- SQLite (via JDBC)
- Maven
- Docker / Docker Compose
- Selenium (browser integration tests)

## Quick Start

### Prerequisites

- Docker and Docker Compose installed
- Maven 3.8+ (for local development)
- Chrome browser (for integration tests)

### Run with Docker

```bash
docker compose up -d
```

Access at http://localhost:4545

### Run Locally (Development)

```bash
mvn spring-boot:run
```

Access at http://localhost:4545

### Run Tests

```bash
# Run all tests
mvn test

# Run only browser integration tests
mvn test -Dtest=StoControllerBrowserTest

# Run only backend tests
mvn test -Dtest=StoControllerIntegrationTest,StoDataServiceTest
```

## Project Structure

```
├── src/main/java/com/stotracker/
│   ├── controller/    # Web controller
│   ├── model/        # JPA entities
│   ├── repository/   # Data access
│   └── service/      # Business logic
├── src/main/resources/
│   ├── static/       # CSS, JS, favicon
│   └── templates/    # Thymeleaf HTML
├── src/test/         # Unit & integration tests
├── data/             # SQLite database (mounted volume)
├── Dockerfile
├── docker-compose.yml
└── pom.xml
```

## Character Data

| Field | Description |
|-------|-------------|
| Name | Character name (unique, readonly after creation) |
| Dilithium Ore | Numeric value |
| Refining | Timestamp for 02:00 CET daily reset |
| Recruitment | Timestamp for 20-minute cooldown |
| Event | Timestamp for general events |
| Credits | Numeric value |

## Timezone

All timestamps use **CET (Europe/Berlin)** timezone. The Docker container is configured with `ENV TZ=Europe/Berlin`.

## API Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/` | GET | Main page |
| `/add` | POST | Add new character |
| `/update` | POST | Update dilithium/credits |
| `/delete` | POST | Delete character |
| `/timestamp` | POST | Set timestamp (recruitment/refining/event) |
| `/untimestamp` | POST | Clear timestamp |

## Development

### Build

```bash
mvn package
```

### Build Docker Image

```bash
docker compose build
```

### Clean

```bash
mvn clean
```

### Run Checkstyle

```bash
mvn checkstyle:check
```

## Testing

### Browser Integration Tests

The project includes Selenium Chrome browser tests that verify the full UI workflow:

1. Create character "unit-char"
2. Update dilithium and credits values
3. Set and unset recruitment, refining, and event timestamps
4. Verify values in database
5. Delete character

These tests require Chrome browser installed and the `webdrivermanager` dependency to automatically download ChromeDriver.

### Running Specific Tests

```bash
# Browser tests only
mvn test -Dtest=StoControllerBrowserTest

# Controller integration tests
mvn test -Dtest=StoControllerIntegrationTest

# Service unit tests
mvn test -Dtest=StoDataServiceTest
```

## Database

The SQLite database is stored at `./data/stotracker.db` (outside the container for persistence).

### Schema

```sql
CREATE TABLE sto_data (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT UNIQUE NOT NULL,
    dilithium INTEGER DEFAULT 0,
    credits INTEGER DEFAULT 0,
    recruitment_time DATETIME,
    convertion_time DATETIME,
    event_time DATETIME,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
);
```

## Docker Configuration

- Container runs on port 4545
- Data volume: `./data:/data` (SQLite database persists outside container)
- Timezone: Europe/Berlin (CET)
