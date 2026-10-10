# Architecture Refinement Plan

## Goal
Optimize and refine the `esheeq-scraper` architecture: implement bounded concurrent scraping with graceful degradation, add resilient in-memory cache fallbacks, decouple client API access via a SvelteKit BFF proxy, and make deployment configs portable and multi-platform.

## Confirmed Specifications
- **Scraping Concurrency & Resilience**: Bounded batch concurrency (5 concurrent requests per batch). Partial episode failures gracefully returned with retry markers, using a shorter 5-minute TTL for partial caches.
- **Client Topology**: SvelteKit BFF internal proxy (`/api/*` routes forwarded to the backend service). No hardcoded external URLs, unified public origin, eliminated CORS hurdles.
- **Cache Resilience**: Redis with automated in-memory LRU fallback when Redis is unconfigured or offline.
- **Container Portability**: Standardized multi-arch Dockerfiles (removing fixed `--platform=arm64`) and unified `docker-compose.yml`.
- **Documentation**: All plans and markdown artifacts stored in `Docs/`.

## Tasks
- [x] Task 1: Refactor `api/helpers/utils.js` for bounded parallel episode scraping (batch size 5), graceful partial results handling, and defensive embed parsing → Verify: test script runs scrape with bounded concurrency and handles missing/partial data.
- [x] Task 2: Refactor `api/db/redis.js` to provide in-memory LRU cache fallback when Redis is unconfigured or unreachable → Verify: API starts and serves cached responses even without a running Redis server.
- [x] Task 3: Implement SvelteKit BFF proxy in `client/src/routes/api/[...path]/+server.js` and update `client/src/lib/constants.js` to dynamic/relative endpoints → Verify: Frontend calls `/api/search/:q` and `/api/scrape/:id` through SvelteKit without CORS or hardcoded external IPs.
- [x] Task 4: Standardize `api/Dockerfile` and `client/Dockerfile` (remove `--platform=arm64`) and create `docker-compose.yml` → Verify: Dockerfiles parse cleanly and support both ARM and x86_64.
- [x] Task 5: End-to-end verification and testing → Verify: API linting/formatting passes, client builds cleanly with `pnpm run build`.

## Done When
- [x] Concurrency reduces episode scraping time by ~80% on uncached multi-episode series.
- [x] Backend runs seamlessly with or without Redis.
- [x] Client uses SvelteKit BFF proxy with dynamic environment configuration.
- [x] Docker builds are multi-architecture ready.
