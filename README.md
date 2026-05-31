# ShrinkPath — URL shortener (Spring Boot + MongoDB)

Shorten URLs with **MongoDB** persistence, **public redirects** at `/r/{code}`, and optional **“crazy”** behavior: password gate, **expiry**, **max clicks** (burn after N), **HTTP 301/302**, **countdown interstitial**, **UTM query append**, **QR codes**, **per-click analytics** (hashed IP fingerprint + user-agent), **chaos mode** (configurable chance to redirect to a classic surprise clip), and **soft-delete** via management token.

## Run

1. **MongoDB** on `localhost:27017` (or set `MONGODB_URI`).
2. `./mvnw spring-boot:run`
3. UI: **http://localhost:8080/index.html**  
   Short links: **http://localhost:8080/r/{code}**

Set the public URL used in API responses and QR payloads:

```bash
export PUBLIC_BASE_URL='https://your.domain'
```

## API

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/links` | JSON body: `targetUrl`, optional `customCode`, `password`, `expiresAt` (ISO-8601 instant), `maxClicks`, `httpStatus` (301\|302), `utmPreset`, `redirectDelaySeconds` (0–120), `chaosMode`, `title`. Returns `shortUrl`, `managementSecret`, `qrUrl`, etc. |
| `GET` | `/api/links/{code}/qr.png` | PNG QR for the short URL. |
| `GET` | `/api/links/{code}/stats` | Header **`X-Manage-Token`**: management secret from create response. |
| `DELETE` | `/api/links/{code}` | Same header; disables the link. |

### Enterprise (marketing ops + mobile growth)

All of the following require header **`X-API-Key`**.

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/api/account/enterprise-settings` | Governance: optional **allow-list / block-list** host rules, **require approval** for new links, **HTTPS posture** override, declared **click retention** window; **SSO metadata** (`ssoIdpIssuerUri`, `enterpriseExternalId`) stored for onboarding (no SAML/OIDC enforcement in this OSS build). |
| `PUT` | `/api/account/enterprise-settings` | Replace enterprise settings (JSON body mirrors the GET shape). |
| `GET` | `/api/account/audit-events` | Last **200** immutable audit rows for the account (link created / approved / rejected / governance updated). |
| `POST` | `/api/links/{code}/approve` | Owner: move a **PENDING** link to **APPROVED** (public redirects allowed). |
| `POST` | `/api/links/{code}/reject` | Owner: **reject** pending link and **disable** it. |

**Create link** (`POST /api/links`) optional fields for **platform routing** (app growth / store campaigns): `platformRoutingEnabled`, `iosTargetUrl`, `androidTargetUrl`, `desktopTargetUrl`. When enabled, redirects use the matching destination with fallback to `targetUrl`. Response includes `linkApprovalStatus` and `platformRoutingEnabled`.

## Configuration

| Property | Default | Description |
|----------|---------|-------------|
| `spring.mongodb.uri` / `MONGODB_URI` | `mongodb://localhost:27017/greenpath` | Mongo connection (Spring Boot 4) |
| `app.public-base-url` | `http://localhost:8080` | Base for short URLs and QR text |
| `app.chaos-probability` | `0.12` | Per-hit probability when `chaosMode` is true |
| `app.click-fingerprint-salt` | `greenpath-clicks` | Salt for hashing client IPs in click logs |

## Tests

```bash
./mvnw test
```

Integration test uses Testcontainers when Docker is available.

## Observability and docs (resume-friendly)

- **Actuator**: `GET /actuator/health`, `/actuator/info`, `/actuator/metrics` (exposure configured in `application.properties`).
- **OpenAPI / Swagger UI**: `http://localhost:8080/swagger-ui.html` (OpenAPI JSON at `/v3/api-docs`).

## Security controls

- **Safe target validation** (on by default): only `http`/`https` targets; host must resolve to a **public** address (blocks loopback, RFC1918, link-local, multicast, and bare `localhost`). Toggle with `app.redirect-target-safety.enabled=false` for local-only demos (e.g. shortening `http://localhost:3000`). In production, keep safety **on** and set `app.url-require-https=true` so only `https://` targets are accepted.

## Container image

```bash
docker build -t shrinkpath:local .
# Requires Mongo; pass URI at runtime, e.g.:
docker run -e MONGODB_URI=mongodb://host.docker.internal:27017/greenpath -p 8080:8080 shrinkpath:local
```

## CI

GitHub Actions workflow `.github/workflows/ci.yml` runs `./mvnw verify` on push/PR.

## Notes

- **Chaos mode** is off by default on each link; enable only if everyone hitting the link accepts joke redirects.
- Management tokens are **shown only once** in the create response; store them if you need stats or delete.

---

## One-page resume story (2026)

**ShrinkPath** — production-style URL shortener: Spring Boot, MongoDB, validated redirects to reduce **open-redirect / internal SSRF** risk, **BCrypt**-protected links, **expiring / burn-after-N** policies, **click analytics** with **hashed IP** fingerprints, **ZXing** QR generation, **Spring Actuator** health/metrics for operations, **OpenAPI**-documented REST API, **Dockerfile** packaging, and **GitHub Actions** CI.

**Example bullets**

- Designed and implemented a **MongoDB-backed** short-link service with **stateless management tokens**, **soft delete**, and **per-click audit** records.
- Added **DNS-aware URL safety checks** before persisting targets to mitigate **SSRF-style abuse** in a redirect-heavy service.
- Exposed **Actuator** health/metrics and **OpenAPI (Swagger UI)** for operability and API discoverability; added **container build** and **CI** pipeline.
