##  Spring Boot Row Locking: Blocking Explained

**Q:** In Spring Boot, if a lock is placed on a row and another thread tries to retrieve that same row, does that thread block until the lock is released?

**A:** The short answer is: It depends on the type of lock you are using.

In Spring Boot (typically using Spring Data JPA), locking behavior is governed by your database's isolation levels and the specific LockModeType you apply to your repository methods.
1. Pessimistic Locking (The "Yes, it blocks" scenario)

When you use a Pessimistic Lock, Spring Boot tells the database to lock the row at the SQL level (usually using SELECT ... FOR UPDATE).

    Behavior: If Thread A holds a PESSIMISTIC_WRITE lock, Thread B will block (wait) at the database level if it also tries to acquire a lock or perform an update on that same row.

    Reading: Whether a simple SELECT (without a lock) blocks depends on your database (e.g., Postgres vs. Oracle), but generally, a standard read will still go through, while a second locked read will wait.

2. Optimistic Locking (The "No blocking" scenario)

Optimistic locking doesn't actually "lock" the row in the database. Instead, it uses a version column (annotated with @Version).

    Behavior: Thread B will not block. It can retrieve the row freely.

    The Catch: The "conflict" happens at the end. If both threads try to save changes, the first one to finish wins. When the second thread tries to save, Spring will throw an ObjectOptimisticLockingFailureException because the version number has changed.

Comparison Table
Feature	Pessimistic Locking	Optimistic Locking
Thread Blocking	Yes, threads wait for the lock to release.	No, threads proceed independently.
Use Case	High contention; frequent updates.	Low contention; rare conflicts.
Performance	Slower (due to waiting/overhead).	Faster (until a conflict occurs).
Failure Mode	Timeout (if it waits too long).	Exception on save().
3. Database Isolation Levels

Even without explicit @Lock annotations, your database's Isolation Level matters. For example:

    In Read Committed (default for many), Thread B can read the row even if Thread A is currently updating it.

    In Serializable, the database may force threads to wait to ensure perfect consistency, effectively blocking them to prevent "phantom reads."

    Quick Tip: If you are worried about threads hanging forever, always define a query hint for a timeout when using pessimistic locks so your application doesn't run out of available threads.

Would you like me to show you how to implement a timeout for a pessimistic lock to prevent infinite blocking?

##  Banking Transaction Security and Idempotency

**Q:** I'm developing a banking application with Java/Spring Boot. I want to prevent replay attacks and make sure that no one gets charged twice for the same transaction. My idea is the following: in my TransferService, I have only one public method: transfer(), which is not @Transactional. transfer() first calls a @Transactional method checkIfExists(), which check the database if the transfer already exists, and if not, creates it. Transaction commits, transfer only proceeds if checkIfExists returned false, and proceeds with the transfer in a new transaction in the @Transactional doTransfer(). Since checkIfExists is a separate transaction, no other thread should be able to start working on the same transfer at the same time. Correct me if I'm wrong and if there are possible attacks/exploits against this method

**A:** I like where your head is at—locking down banking logic is high-stakes, and you're thinking about isolation, which is exactly where you should be.

However, your proposed logic has a few "gotchas" that could still lead to double-charging or system inconsistency. The most significant issue isn't just the logic, but how Spring handles proxies and how databases handle concurrent reads.

Here is the breakdown of why this might fail and how to fix it.
1. The Proxy Problem (Self-Invocation)

In Spring, @Transactional works via AOP proxies. When you call a @Transactional method from a non-transactional method within the same class, the transaction interceptor is bypassed.

    The Result: checkIfExists() and doTransfer() will likely run without any transaction at all, meaning no atomicity and no isolation.

    The Fix: These methods either need to be in a separate service, or you need to use TransactionTemplate.

2. The "Check-then-Act" Race Condition

Even if the transactions work perfectly, your checkIfExists() method is vulnerable to a race condition.

If two identical requests hit two different threads at the exact same millisecond:

    Thread A calls checkIfExists() and queries the DB. The record doesn't exist yet.

    Thread B calls checkIfExists() and queries the DB. The record still doesn't exist because Thread A hasn't committed its insert yet.

    Thread A inserts the record and commits.

    Thread B inserts the record and commits (or fails with a Duplicate Key error).

If you don't have a Unique Constraint on a specific request_id or idempotency_key in your database, both threads will succeed, and doTransfer() will run twice.
3. The "Orphaned Record" Risk

By splitting this into two distinct transactions, you create a state where:

    The record is created (Transaction 1).

    The application crashes or the network blips.

    The actual money transfer (Transaction 2) never happens.

Now your database says the transfer exists, but the user was never charged. If the user retries, your checkIfExists() will see the record and block the transfer, even though the first one failed.
The "Industry Standard" Approach

To properly prevent replay attacks and double-charging, you should use an Idempotency Key strategy within a single transaction.

    Client-Generated Key: The client (frontend/mobile) must send a unique X-Idempotency-Key (usually a UUID) with the request.

    Unique Constraint: Your transfers table must have a UNIQUE constraint on that key.

    The Atomic Flow:

        Start one @Transactional method.

        Try to insert the transfer record with the idempotency_key and a status of PROCESSING.

        If the insert fails (Duplicate Key Exception), you know it’s a replay. Return the status of the existing record.

        If it succeeds, proceed with the money logic.

        Update the status to COMPLETED before the transaction ends.

Java

@Transactional
public TransferResponse executeTransfer(TransferRequest request) {
try {
// This insert will fail if the key already exists
TransferRecord record = repository.save(new TransferRecord(request.getIdempotencyKey(), "PROCESSING"));

        // Perform the actual balance logic here
        accountService.debit(request.getFromAccount(), request.getAmount());
        accountService.credit(request.getToAccount(), request.getAmount());
        
        record.setStatus("COMPLETED");
        return new TransferResponse("Success");
    } catch (DataIntegrityViolationException e) {
        // Handle the replay: fetch the existing record and return its status
        return handleReplay(request.getIdempotencyKey());
    }
}

Summary of Improvements
Feature	Your Proposal	Recommended Approach
Transaction Count	Two separate transactions	One atomic transaction
Concurrency	Relies on timing	Relies on DB Unique Constraints
Failure Mode	Risk of "stuck" pending records	Atomic rollback if money move fails
Spring Proxy	Self-invocation might break @Transactional	Use proper service boundaries

Would you like me to show you how to implement a cleanup scheduler for those "stuck" transactions in case the whole server goes down mid-transfer?

***

**Q:** In your executeTransfer example: is it safe to read the record from the database in the catch block to check its status?

**A:** The short answer is: No, it is usually not safe to do that within the same @Transactional method.

In Spring/JPA, once a DataIntegrityViolationException (or any exception that reaches the persistence provider like Hibernate) is thrown, the current transaction is typically marked as "rollback-only." If you try to perform a repository.findById() or any other query in that same catch block, Hibernate will likely throw another exception (like TransactionSystemException or UnexpectedRollbackException) because the transaction is already "poisoned." You cannot perform more work on a transaction that is destined to die.
How to handle this correctly

To safely check the status of a "replay" request, you have two primary architectural options:
Option 1: The "Wrapper" Approach (Recommended)

Don't handle the idempotency logic inside the same transaction as the money transfer. Use a non-transactional "Orchestrator" service that calls two separate transactional methods.

    Orchestrator: Calls idempotencyService.createRecord().

    If success: Orchestrator calls transferService.doWork().

    If DataIntegrityViolation: Orchestrator catches it and calls idempotencyService.getStatus() (which starts a new read-only transaction).

Option 2: Propagation.REQUIRES_NEW

If you want to keep the logic in one class, you need the "check" or "create" logic to happen in a separate transaction that can fail without killing the main flow.
Java

@Service
public class TransferService {

    @Autowired
    private IdempotencyRepository idempotencyRepo;

    public void handleTransfer(TransferRequest request) {
        try {
            // Start and commit a tiny transaction just for the key
            idempotencyService.reserveKey(request.getKey());
        } catch (DataIntegrityViolationException e) {
            // This catch block is safe because the "reserveKey" transaction 
            // is the only thing that rolled back.
            var status = idempotencyRepo.findByKey(request.getKey()); 
            return; // Return the existing status
        }
        
        // If we reach here, we have the "lock" and can proceed with a new transaction
        doActualTransfer(request);
    }
}

A Critical Detail: The "Processing" State

If you are building a banking app, you must handle the "In-Flight" state.

    Request 1 hits the server and starts the transfer (Transaction takes 2 seconds).

    Request 2 (the replay) hits the server 0.5 seconds later.

    Request 2 hits the Unique Constraint and goes to the catch block.

    Request 2 queries the DB. It sees the record from Request 1.

Wait! If Request 1 is still running, the status might be PENDING or PROCESSING. Your code needs to tell the user: "We are already working on this, please don't retry yet," rather than just saying "Transfer Complete" or "Failed."
The "Cleanest" Strategy (Upsert/Locking)

Instead of relying on an exception (which is expensive and messy for flow control), many high-scale banking systems use a Pessimistic Lock or a "Select for Update" on the idempotency key at the very beginning of the request. This forces Request 2 to wait until Request 1 is finished before it even sees the result.

Would you like to see how to implement a SELECT ... FOR UPDATE pattern to avoid the exception-handling mess entirely?

***

**Q:** Can you show an example to the Option 1: The "Wrapper" Approach (Recommended)?

**A:** This is the cleanest way to handle banking transactions because it avoids "poisoning" your main transaction with a constraint violation. By separating the Idempotency Check from the Money Movement, you ensure that a failed "check" doesn't roll back the "action."

Here is how you structure this across three distinct components.
1. The Idempotency Service

This service is solely responsible for managing the "lock" (the record in the database). We use REQUIRES_NEW to ensure this record is committed immediately, regardless of what happens later.
Java

@Service
public class IdempotencyService {
@Autowired private IdempotencyRepository repository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void reserve(String key) {
        // This will throw DataIntegrityViolationException if the key exists
        repository.saveAndFlush(new IdempotencyRecord(key, "PROCESSING"));
    }

    @Transactional(readOnly = true)
    public String getStatus(String key) {
        return repository.findById(key)
                .map(IdempotencyRecord::getStatus)
                .orElse("NOT_FOUND");
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateStatus(String key, String status) {
        repository.updateStatus(key, status);
    }
}

2. The Transfer Service (The "Workhorse")

This handles the actual banking logic. It doesn't care about idempotency; it just does its job.
Java

@Service
public class TransferService {
@Transactional
public void doTransfer(Long fromId, Long toId, BigDecimal amount) {
// Logic to debit/credit accounts
accountRepository.decreaseBalance(fromId, amount);
accountRepository.increaseBalance(toId, amount);
}
}

3. The Orchestrator (The "Wrapper")

This is the entry point. Notice it is not @Transactional. It coordinates the two services.
Java

@Service
public class TransferOrchestrator {
@Autowired private IdempotencyService idempotencyService;
@Autowired private TransferService transferService;

    public TransferResponse handleRequest(TransferRequest request) {
        try {
            // Transaction 1: Reserve the key
            idempotencyService.reserve(request.getIdempotencyKey());
        } catch (DataIntegrityViolationException e) {
            // Transaction 2: It's a replay! Fetch the status safely
            String status = idempotencyService.getStatus(request.getIdempotencyKey());
            return new TransferResponse("Duplicate request. Status: " + status);
        }

        try {
            // Transaction 3: The actual money move
            transferService.doTransfer(request.getFrom(), request.getTo(), request.getAmount());
            
            // Transaction 4: Mark as complete
            idempotencyService.updateStatus(request.getIdempotencyKey(), "COMPLETED");
            return new TransferResponse("Success");
        } catch (Exception e) {
            // If the transfer fails, we mark the key as FAILED so they can retry
            idempotencyService.updateStatus(request.getIdempotencyKey(), "FAILED");
            throw e; 
        }
    }
}

Why this is the "Golden Standard" for Banking

    No Transaction Poisoning: Because reserve() runs in its own transaction, the DataIntegrityViolationException is thrown and caught before the TransferService even starts. Your database connection remains healthy.

    The "In-Progress" Safety: If a second request comes in while the first is still running, getStatus() will return "PROCESSING". You can then tell the user: "Transaction is being processed, please check your history in a minute."

    Granular Failure Handling: If the money move fails (e.g., insufficient funds), you can catch that specifically and update the idempotency record to "FAILED". This allows the user to fix their balance and try again with the same key.

A Note on Performance

In a high-traffic system, this creates 3–4 small transactions instead of 1 large one. For banking, this trade-off is almost always worth it for the sake of consistency and clarity.

Would you like to see how to add a "Request Hash" to the idempotency record? This ensures that if a user sends the same key but changes the amount or the recipient, you can detect the fraud and reject it.

***

**Q:** I implemented the update query as such:


@Modifying

@Query("update Idempotency i set i.status = :status where i.idempotencyKey = :idempotencyKey")

void setStatusWhereIdempotencyKey(@Param("status") TransferStatus status, @Param("idempotencyKey") UUID idempotencyKey);


However, when testing, the status does not seem to update. Here's my test:


// given

var idempotency = idempotencyRepository.save(new Idempotency(UUID.randomUUID(), TransferStatus.PROCESSING));


// when

idempotencyRepository.setStatusWhereIdempotencyKey(TransferStatus.COMPLETED, idempotency.getIdempotencyKey());

idempotencyRepository.flush();


// then

var fetched = idempotencyRepository.findById(idempotency.getId()).orElseThrow();

assertEquals(TransferStatus.COMPLETED, fetched.getStatus());


What's the issue here?

**A:** You’ve just run head-first into the Persistence Context (L1 Cache)—one of Hibernate’s most famous "magic" features that often feels like a bug until you see under the hood.

The issue isn't that the database didn't update; it's that your Java code is looking at a stale version of the object that Hibernate is holding in memory.
The Cause: The Persistence Context Bypass

When you use a @Modifying query, Spring Data JPA executes a direct SQL (or JPQL) UPDATE statement against the database. However:

    Direct Updates bypass the Cache: Hibernate does not automatically update the entities it already has in its "First Level Cache" (the Persistence Context) when you run a manual update query.

    Stale Entity: Your idempotency object was loaded into memory with the status PROCESSING.

    The Query: Your setStatus... query changed the row in the DB to COMPLETED.

    The Fetch: When you call findById(), Hibernate thinks: "Wait, I already have an entity with this ID in my cache! No need to hit the database." 5.  The Result: It gives you the old cached object (PROCESSING) instead of the updated row from the DB.

The Fix: clearAutomatically = true

You need to tell Spring Data JPA to "forget" everything in the Persistence Context after the update query runs. This forces the next findById to actually go to the database and get the fresh data.

Update your repository method like this:
Java

@Modifying(clearAutomatically = true) // <--- Add this
@Query("update Idempotency i set i.status = :status where i.idempotencyKey = :idempotencyKey")
void setStatusWhereIdempotencyKey(@Param("status") TransferStatus status, @Param("idempotencyKey") UUID idempotencyKey);

Alternative: Use save() (Dirty Checking)

In a banking app, if you already have the entity loaded, it's often cleaner to just let Hibernate handle the update via its dirty checking mechanism. This keeps the cache and DB in sync naturally:
Java

// Inside your service
var idempotency = repository.findByKey(key).orElseThrow();
idempotency.setStatus(TransferStatus.COMPLETED);
// No need for a custom @Query; Hibernate will auto-update upon commit

Why use the @Modifying approach at all?

The custom @Query approach you chose is actually better for high-concurrency banking because:

    It's a single, atomic database operation.

    It avoids "Lost Updates" where two threads load the same entity, change different fields, and one overwrites the other.

Pro-Tip for Testing:
In your test, since you are manually managing the lifecycle, calling entityManager.clear() manually after the update would also fix the test.

Would you like me to show you how to combine this @Modifying update with a version check (@Version) to prevent two different threads from updating the status at the same time?
