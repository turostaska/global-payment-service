# Technical Assignment: Global Payment Service

## Overview
We are building a next-generation payment gateway. Your task is to implement a **Spring Boot Service** that manages user accounts and processes fund transfers between them.

## The Objective
Create a **production-ready** backend service. We are looking for clean code, solid architecture, and attention to detail regarding reliability and consistency in distributed systems.

**Tech Stack:**
- **Language:** Java 21+
- **Framework:** Spring Boot 3.x
- **Database:** H2 (In-memory) or any SQL-based DB.
- **Build Tool:** Maven or Gradle.

---

## Functional Requirements

### 1. Account & Transfer Logic
- **Create Account:** Create a user account with a balance and currency (EUR, USD, HUF).
- **Money Transfer:** `POST /api/transfers`
    - Transfer funds between two accounts.
    - If currencies differ, fetch an exchange rate from a mocked external API.
    - **Constraint:** The external API is "flaky" (503s/Latency). Handle this gracefully.

### 2. Mandatory: Idempotency
- Every transfer request **must** include an `X-Idempotency-Key` header.
- If the same key is sent twice with the same payload:
    - If the first request succeeded, return the original `201 Created` result.
    - If the first request is still processing, return a `409 Conflict` (or appropriate status).
    - If the first request failed, the user should be allowed to retry.
- **Goal:** Ensure a network retry never results in a double-charge.

### 3. System Integration
- The payment service is part of a larger architecture. Other domain services (e.g., Fraud Detection, Notification Center) need to be aware of every successful transfer.
- **Goal:** Implement a solution to propagate this information to the outside world.

---

## Non-Functional Requirements

### 1. Concurrency & Data Integrity
Think about how the system behaves under load. How do you handle multiple requests affecting the same account or the same idempotency key simultaneously?

### 2. Testing
Demonstrate your testing approach. We are interested in how you verify the correctness and reliability of your service across different scenarios.

### 3. AI Usage Policy
You are **allowed** to use AI tools (ChatGPT, Claude, Copilot, etc.) to assist you.
- **Requirement:** If used, you must include a `PROMPTS.md` file.
- **Content:** Include the significant prompts you used to generate code, tests, or architectural ideas.

---

## Submission Guidelines

1. **Repository:** Provide a link to a Git repository or a ZIP file.
2. **README.md:** Explain:
   - Your architectural choices and design decisions.
   - How you handled the edge cases mentioned in the requirements (resilience, concurrency, reliability).
   - Instructions on how to build and run the application and its tests.
3. **PROMPTS.md:** Your AI conversation history (if applicable).
