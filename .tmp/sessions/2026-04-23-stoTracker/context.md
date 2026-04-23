# Task Context: stoTracker

Session ID: 2026-04-23-stoTracker
Created: 2026-04-23T00:00:00
Status: in_progress

## Current Request
Build stoTracker application: Java 21 server with Spring Boot + Thymeleaf, SQLite database, HTML frontend with LCARS styling. Main page shows: name|dilithium|credits|recruitment|convertion|event. Features: add name in header, remove button with confirmation, numeric inputs for credits/dilithium, timestamp buttons for recruitment/convertion/event, footer clock with HH:mm, most recent data on load, delete removes all data.

## Context Files (Standards to Follow)
- .opencode/context/core/standards/code-quality.md
- .opencode/context/core/standards/test-coverage.md

## Reference Files (Source Material to Look At)
- PLAN.md (this project plan)

## External Docs Fetched
None yet

## Components
1. Project Structure (pom.xml, application.properties)
2. Database Schema (schema.sql)
3. JPA Entity (StoData.java)
4. Repository (StoDataRepository.java)
5. Service (StoDataService.java, Result.java)
6. Controller (StoController.java)
7. Frontend (index.html, lcars.css, time.js)
8. Tests (StoDataServiceTest.java)
9. Git Hook (pre-commit)

## Constraints
- Use Maven (not Gradle)
- Use SQLite (local file-based)
- Java 21
- LCARS styling for frontend
- Git pre-commit hook runs tests before commit

## Exit Criteria
- [ ] Application compiles without errors
- [ ] Application starts successfully on localhost
- [ ] Main page displays all columns
- [ ] Add name form in header works
- [ ] Remove button shows confirmation dialog and deletes data
- [ ] Numeric input fields update correctly
- [ ] Timestamp buttons store and display timestamps
- [ ] Footer shows current time (HH:mm)
- [ ] Page loads with most recent data
- [ ] All JUnit tests pass
- [ ] Git pre-commit hook blocks commits when tests fail