# stoTracker - Task Plan

## Overview
Build a Java 21 Spring Boot + Thymeleaf server with SQLite database and LCARS-styled HTML frontend for tracking Star Trek Online character data.

## Tech Stack
- **Java 21**
- **Spring Boot** (web, data-jpa, thymeleaf)
- **SQLite** (local database)
- **Maven** (build system)
- **JUnit 5** (testing)
- **Checkstyle** (code quality)
- **Docker & Docker Compose** (containerization, port 4545)
- **HTML/CSS/JS** (frontend with LCARS styling)

## Features
- Main page displays: `delete | name | event | dilithium ore | refining | recruitment | credits`
- Name column is readonly
- Header contains add new name form
- Footer shows current time (HH:mm CET, updated live)
- Credits and dilithium are numeric input fields with German number formatting:
  - Input uses `type="text"` with JavaScript parseGermanNumber() for German format (10.000)
  - Hidden inputs carry raw integer values for form submission
  - onchange handler parses German number format before submit
- Recruitment, Refining, Event timestamps:
  - Green checkmark (✓) button to SET timestamp
  - Red X (✗) button to UNSET timestamp
  - Countdown timer displayed between buttons when set
  - Recruitment: countdown from timestamp + 20 minutes
  - Refining/Event: countdown to 02:00 tomorrow CET
  - "overdue" shown in red when timer < 0
  - Last updated time shown smaller on second line (centered)
  - "unset" label shown when not set
- Delete column with X button (requires confirmation)
- Data sorted alphabetically by name
- CET timezone (Europe/Berlin) for all timestamps
- Favicon served locally from `/favicon.ico`

## Technical Details

### Database Schema
- Table: `sto_data`
- Columns: id, name, dilithium, credits, recruitment_time, convertion_time, event_time, updated_at
- All timestamp columns use SQLite DATETIME type

### Service Layer Annotations
- All mutating methods have `@Transactional` annotation
- This fixes EntityManager transaction errors on delete/update operations

### Timezone Configuration
- Hibernate JDBC time_zone: `Europe/Berlin` (CET)
- JavaScript clock also uses CET timezone

## Task Breakdown

### Phase 1: Foundation

- [x] **Task 01:** Create Spring Boot project structure & build configuration
  - Files: `pom.xml`, `src/main/java/...`, `src/main/resources/...`
  - Dependencies: spring-boot-starter-web, spring-boot-starter-data-jpa, spring-boot-starter-thymeleaf, sqlite-jdbc, spring-boot-starter-test
  - Verification: `mvn compile` succeeds

- [x] **Task 02:** Design and create SQLite database schema
  - File: `src/main/resources/schema.sql`
  - Table: `sto_data` with columns for name, dilithium, credits, recruitment_time, convertion_time, event_time, updated_at
  - Verification: Database initializes correctly

### Phase 2: Backend

- [x] **Task 03:** Create JPA entity (StoData)
  - File: `src/main/java/com/stotracker/model/StoData.java`
  - Fields: id, name, dilithium, credits, recruitmentTime, convertionTime, eventTime, updatedAt
  - Verification: Entity maps to database table correctly

- [x] **Task 04:** Create repository layer
  - File: `src/main/java/com/stotracker/repository/StoDataRepository.java`
  - Methods: findAllByOrderByNameAsc(), findByName(), deleteByName()
  - Verification: JUnit tests pass

- [x] **Task 05:** Create service layer
  - Files: `src/main/java/com/stotracker/service/StoDataService.java`, `Result.java`
  - Methods: getAllData(), addName(), updateData(), deleteByName(), recordTimestamp(), clearTimestamp()
  - All mutating methods have @Transactional
  - Verification: JUnit tests pass

- [x] **Task 06:** Create controller
  - File: `src/main/java/com/stotracker/controller/StoController.java`
  - Endpoints: GET /, POST /add, POST /update, POST /delete, POST /timestamp, POST /untimestamp
  - Verification: Application starts and routes work

### Phase 3: Frontend

- [x] **Task 07:** Create Thymeleaf template with LCARS styling
  - Files: `src/main/resources/templates/index.html`, `src/main/resources/static/css/lcars.css`, `src/main/resources/static/js/time.js`
  - Components: Header (add form), Main table, Footer (clock)
  - Timestamp cells with set/unset functionality
  - DELETE column header
  - Verification: Page renders with LCARS styling, all interactive elements work

- [x] **Task 08:** Add favicon
  - File: `src/main/resources/static/favicon.ico`
  - Downloaded from: http://stosite.com/favicon.ico
  - Verification: Favicon displays in browser

### Phase 4: Quality Assurance

- [x] **Task 09:** Write JUnit tests
  - File: `src/test/java/com/stotracker/service/StoDataServiceTest.java`
  - Coverage: All service methods, edge cases
  - Verification: All tests pass

- [x] **Task 10:** Configure git pre-commit hook
  - File: `.git/hooks/pre-commit` (executable)
  - Behavior: Run `mvn checkstyle:check` and `mvn test` before commit, fail if either fails
  - Verification: Commit fails when checkstyle or tests fail, succeeds when both pass

### Phase 5: Containerization

- [x] **Task 11:** Docker configuration
  - Files: `Dockerfile`, `docker-compose.yml`, `start.sh`
  - Multi-stage build, builds inside Docker image
  - Volume mount: `./data:/data` for persistent SQLite storage
  - Runs as root user (to ensure volume write permissions)
  - Verification: Docker image builds successfully

- [x] **Task 12:** Fix number input handling
  - Files: `src/main/resources/static/js/time.js`, `src/main/resources/templates/index.html`
  - Use `type="text"` with JavaScript parseGermanNumber() for German number formatting
  - parseGermanNumber() accepts 10.000 (German), 10,000 (American), or 10000 formats
  - Hidden inputs carry raw integer values for form submission
  - Verification: Enter `34.780` in dilithium field, submit, DB stores `34780`

- [x] **Task 13:** Selenium Firefox browser integration test
  - File: `src/test/java/com/stotracker/controller/StoControllerBrowserTest.java`
  - Creates character "unit-char", updates numbers, sets/unset timestamps, verifies DB, deletes
  - Tests both German (de-DE) and American (en-US) locale formatting
  - Dependency: `selenium-firefox-driver` 4.16.0, `webdrivermanager` 5.6.2
  - Uses RANDOM_PORT for test server to avoid port conflicts
  - Verification: `mvn test -Dtest=StoControllerBrowserTest` passes

- [x] **Task 14:** Comprehensive test coverage
  - Files: Added `ResultTest.java`, `StoDataTest.java`, extended `StoControllerIntegrationTest.java`, `StoControllerBrowserTest.java`
  - Service layer: 26 tests covering all methods including clearTimestamp, refining alias, updatedAt
  - Controller layer: 20 integration tests covering error handling for invalid/non-existent IDs
  - Model layer: 10 unit tests for StoData entity
  - Result record: 5 unit tests
  - Browser layer: 5 Firefox browser tests for full UI workflow
  - Verification: All 66 tests pass

## File Structure
```
/dataDisk/IdeaProjects/stoTracker/
├── Dockerfile
├── docker-compose.yml
├── start.sh
├── .gitignore
├── checkstyle.xml
├── pom.xml
├── PLAN.md
├── .git/hooks/pre-commit
├── data/                           # SQLite database (outside container)
│   └── stotracker.db
└── src/
    ├── main/
    │   ├── java/com/stotracker/
    │   │   ├── StoTrackerApplication.java
    │   │   ├── controller/StoController.java
    │   │   ├── model/StoData.java
    │   │   ├── repository/StoDataRepository.java
    │   │   └── service/
    │   │       ├── StoDataService.java
    │   │       └── Result.java
    │   └── resources/
    │       ├── application.properties
    │       ├── schema.sql
    │       ├── static/
    │       │   ├── favicon.ico
    │       │   ├── css/lcars.css
    │       │   └── js/time.js
    │       └── templates/index.html
     └── test/java/com/stotracker/
        ├── controller/
        │   ├── StoControllerIntegrationTest.java
        │   └── StoControllerBrowserTest.java
        ├── model/StoDataTest.java
        └── service/
            ├── StoDataServiceTest.java
            └── ResultTest.java
```

## Dependency Graph
```
01 → 02 → 03 → 04 → 05 → 06 → 07 → 08 → 10
                     ↓                    ↓
                    09                  11
```

## Exit Criteria
- [x] Application compiles without errors
- [x] Application starts successfully on localhost:4545
- [x] Main page displays all columns: delete | name | event | dilithium ore | refining | recruitment | credits
- [x] Add name form in header works
- [x] Delete button shows confirmation dialog and deletes data
- [x] Credits/dilithium numeric input fields update correctly
- [x] Recruitment/Convertion/Event buttons store timestamps (CET timezone)
- [x] Unset (✗) buttons clear timestamps
- [x] Green checkmark (✓) shown when timestamp not set
- [x] Footer shows current time (HH:mm CET)
- [x] Data sorted alphabetically by name
- [x] All JUnit tests pass
- [x] Checkstyle passes (no violations)
- [x] Git pre-commit hook blocks commits when checkstyle or tests fail
- [x] Git pre-commit hook allows commits when both pass
- [x] Docker image builds successfully
- [x] Docker Compose starts application on port 4545
- [x] Favicon displays correctly in browser
- [x] No database remains after character deletion

## Pending Tasks
- [x] Fix number input handling with German format parsing (Task 12)
- [x] Add Selenium Firefox browser integration test (Task 13)
- [x] Comprehensive test coverage - backend, frontend, integration (Task 14)
- [x] Add dilithium multiplier display (floor/8000) with color coding (Task 15)
- [x] Add credits warning icons (⚠️ <1M, 🚨 <100k) (Task 16)
- [x] Locale-aware number formatting (Task 17)
- [x] Rename "convertion" to "refining" in database schema, model, and service layer (Task 18)
- [x] Add input validation (name max length, dilithium/credits max bounds) (Task 19)
- [ ] Add flash messages for user feedback on success/error operations (Task 20) - incomplete due to headless browser compatibility issues
- [x] Extract inline JavaScript handlers to time.js for maintainability (Task 21) - already done
- [x] LCARS UI redesign with themes from thelcars.com (Task 22):
  - Classic LCARS theme (default TNG style)
  - Nemesis Blue theme
  - Lower Decks theme
  - Theme selector dropdown in top right
  - Vertical LCARS border with elbow panels
  - Responsive design
- [x] CSS cleanup and code review (Task 23):
  - Remove panel-10 borders
  - Remove orphaned CSS (broken rules without selectors)
  - Remove duplicate .sidebar-nav and .lcars-frame-filler definitions
  - Remove empty theme-selector div from HTML
  - Remove orphaned theme JavaScript from HTML
  - All 80 tests passing
- [x] Fix refining/event countdown to detect overdue based on timestamp date (Task 24):
  - Previously only compared time, not date
  - Now calculates deadline (02:00 CET after timestamp) and marks overdue if past

## Time Estimate
**Total:** ~6 hours (including bug fixes and enhancements)
**Complexity:** Medium
