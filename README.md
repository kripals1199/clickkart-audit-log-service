# ClickKart Audit Log Service

The platform's central, tamper-evident audit trail. Every security-relevant state change across
the platform is recorded here as a link in a cryptographic hash chain.

- **Port:** `8083`
- **Datastore:** PostgreSQL (`clickkart_audit_log`)
- **Callers:** Auth Service, via a Feign client wrapped in a Resilience4j circuit breaker

## Why a hash chain

Each entry stores the hash of the previous entry alongside its own. Altering or deleting any
historical row breaks every hash from that point forward, so tampering is *detectable* rather
than merely *discouraged* by table permissions. `GET /events/verify` walks the chain and reports
the first entry where it breaks.

The repository layer is append-only by construction — it extends the bare `Repository` marker
interface and exposes no delete or update method, so there is no code path that can rewrite
history even accidentally.

## Endpoints

All require an `X-Correlation-Id` header; a missing one is rejected with `400`.

| Method | Path | Purpose |
|---|---|---|
| `POST` | `/api/v1/audit-log/events` | Append an event (called by other services) |
| `GET` | `/api/v1/audit-log/events` | Browse the trail, paginated |
| `GET` | `/api/v1/audit-log/events/verify` | Verify chain integrity end to end |

`action` is a plain `String`, not an enum — this is a decoupled sink, so a new caller can record
its own vocabulary without requiring a change and redeploy here.

> **Note:** the browse and verify endpoints currently have no RBAC. They are ClusterIP-only and
> not Gateway-routed, so they aren't externally reachable — but role checks should be added
> before any admin UI or Gateway route exposes them.

## Concurrency

Appending takes a pessimistic write lock on a single chain-head row, so concurrent writers
serialize and the chain can never fork. That is a deliberate throughput trade-off: correctness of
the chain matters more than append parallelism for an audit log.

## Configuration

| Variable | Required in | Notes |
|---|---|---|
| `DB_HOST` | prod | Managed Postgres endpoint |
| `DB_USERNAME` | — | Defaults to `clickkart_audit_log_app`, this service's own least-privilege role |
| `DB_PASSWORD` | **all** | No default on any profile, dev included |
| `EUREKA_DASHBOARD_USERNAME` / `_PASSWORD` | test/qa/prod | |
| `AUDIT_LOG_SERVICE_HOSTNAME` | prod | Eureka advertise address |
| `CONFIG_SERVER_PASSWORD` | test/qa/prod | |

## Running locally

```bash
docker compose -f docker-compose.dev-infra.yml -f docker-compose.app-tier.yml up -d

# browse
curl -H "X-Correlation-Id: manual-check" http://localhost:8083/api/v1/audit-log/events

# verify the chain is intact
curl -H "X-Correlation-Id: manual-check" http://localhost:8083/api/v1/audit-log/events/verify
```

API docs: <http://localhost:8083/swagger-ui.html>, or via the Gateway's aggregated UI.

## Build

```bash
mvn -B verify
```

The test suite includes a genuine tamper-detection test: it mutates a persisted entry after its
hash is computed and asserts that verification reports the break at the correct entry.

## Related

- [clickkart-platform](https://github.com/kripals1199/clickkart-platform) — architecture, local setup
- [clickkart-auth-service](https://github.com/kripals1199/clickkart-auth-service) — primary producer of audit events
