# Technical Assignment: Global Payment Service

## Overview

The application can be built with the following command:

```shell
./gradlew bootJar
```

And then run like so:

```shell
java -jar application.jar
```

You can run the tests with the following command:

```shell
./gradlew check
```

## Architectural choices and design decisions

In order to make sure no user gets charged twice for the same transaction and to prevent replay attacks, the transfer
process is as follows:

1. The `TransferOrchestratorService` gets the request along with the idempotency key. (The assumption has been made that
   the
   idempotency key is universally unique and cryptographically secure, meaning that such a key uniquely identifies the
   request being made.)
2. The orchestrator forwards the idempotency key to the `IdempotencyService`. Since the orchestrator is not
   transactional,
   the `reserve` method opens its own transaction,
   and tries to save the key to the idempotency table. Since the idempotency key column has a unique constraint, the
   commit after insertion
   shall succeed only if the request was never processed before. The transaction commits at the end of the method, any
   other
   subsequent thread trying the same will be thrown a `DataIntegrityViolationException`. If this happens, in a new
   transaction
   we query the status of the idempotency and send back the appropriate HTTP status code.
3. After we made sure that the request was never processed before, `TransferService` opens a new transaction. It first
   validates its input, then starts the processing. If no exception is thrown, the transfer is inserted into the table
   of transfers,
   meaning that the transfer successfully completed. Then the idempotency's status is updated accordingly.

Since the transfer table only contains transfers that successfully completed, the monitoring API just returns a paged
view of the records.
