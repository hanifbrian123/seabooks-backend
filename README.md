# SeaBooks Backend

REST API for **SeaBooks** — Fishing Fleet Management, Crew Cash Advances (*Kasbon*), and Trip Bookkeeping.

## Tech Stack
- **Java 21** & **Spring Boot 4**
- **Spring Data JPA** & **PostgreSQL** (Neon Serverless)
- **JWT & Google OAuth 2.0** Authentication

## Core Features
- **Multi-Tenant Auth:** Email/Password, Google OAuth 2.0, and auto-cleaned ephemeral Demo Workspaces.
- **Fleet & Crew (ABK):** CRUD for vessels and crew members with real-time `totalKasbon` debt tracking.
- **Kasbon Ledger:** Transactional loan and repayment records synced with crew debt balances.
- **Trip Accounting:** Trip income (fish sales), trip expenses, net profit calculation, and automatic crew wage kasbon deduction.

## Quick Start

1. Create `.env` in the project root:
   ```env
   DB_URL=jdbc:postgresql://<host>/neondb?sslmode=require
   DB_USERNAME=neondb_owner
   DB_PASSWORD=<password>
   JWT_SECRET=<your-secret-key>
   ```
2. Run the server:
   ```bash
   ./mvnw spring-boot:run
   ```
   API runs at `http://localhost:8080`.
