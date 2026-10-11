<p align="center">
  <img width="380px" src="https://res.cloudinary.com/ydevcloud/image/upload/v1688314005/YsnIrix/uaqw3nssint6ornhfv77.svg" alt="Esheeq Scraper" />
</p>

<p align="center">
  <b>High-performance media scraper, REST API, web client, and native Android application for Turkish drama streaming.</b>
</p>

<p align="center">
  <a href="https://github.com/Ysn4Irix/esheeq-scraper/releases/latest"><img src="https://img.shields.io/github/v/release/Ysn4Irix/esheeq-scraper?style=flat-square&color=10b981" alt="Latest Release" /></a>
  <a href="https://github.com/Ysn4Irix/esheeq-scraper/blob/main/LICENSE"><img src="https://img.shields.io/badge/license-MIT-blue.svg?style=flat-square" alt="License" /></a>
  <img src="https://img.shields.io/badge/Android-Jetpack%20Compose-3ddc84?style=flat-square&logo=android&logoColor=white" alt="Android" />
  <img src="https://img.shields.io/badge/Frontend-SvelteKit-ff3e00?style=flat-square&logo=svelte&logoColor=white" alt="SvelteKit" />
  <img src="https://img.shields.io/badge/Backend-Express.js-000000?style=flat-square&logo=express&logoColor=white" alt="Express" />
  <img src="https://img.shields.io/badge/Cache-Redis-dc382d?style=flat-square&logo=redis&logoColor=white" alt="Redis" />
</p>

---

## Overview

**Esheeq Scraper** is a full-stack media extraction and streaming suite. It extracts clean streaming links, episode lists, and series metadata directly from Esheeq drama catalogs without invasive redirects or advertisements.

The repository is structured as a monorepo containing three core components:
1. **Android App (`android/`)**: Native Android client written in 100% Kotlin with Jetpack Compose, Media3 ExoPlayer, Picture-in-Picture (PiP), and ad-resilient hybrid playback.
2. **REST API (`api/`)**: Express.js microservice powered by Cheerio scrapers, Redis in-memory caching, rate-limiting, and security middleware.
3. **Web Client (`client/`)**: Fast, lightweight web interface built with SvelteKit, Tailwind CSS, and Preline UI.

> [!NOTE]
> **Educational Disclaimer**: This project is developed strictly for educational and research purposes into web scraping, headless media extraction, and cross-platform frontend architecture.

---

## Live Deployments

- **Web Application**: [https://esheeq.ysnirix.xyz](https://esheeq.ysnirix.xyz)
- **Production API**: [https://esheeq.ysnirix.xyz/api/](https://esheeq.ysnirix.xyz/api/)
- **Android APK Downloads**: [GitHub Releases](https://github.com/Ysn4Irix/esheeq-scraper/releases)

---

## Project Structure

```
esheeq-scraper/
├── android/               # Native Android application (Jetpack Compose & Kotlin)
│   ├── app/               # App module, UI components, ViewModels, and signing configs
│   │   ├── src/main/assets/config.json  # Configurable backend API endpoint
│   │   └── version.properties           # SemVer version code & version name tracking
│   └── build.gradle.kts
├── api/                   # Express.js REST API & Scraper service
│   ├── routes/            # API endpoints (alive, latest, search, scrape)
│   ├── scrapers/          # Cheerio scraping logic
│   ├── db/                # Redis connection handler
│   └── server.js
├── client/                # SvelteKit web frontend
│   ├── src/               # Svelte components, stores, and routes
│   └── svelte.config.js
├── docker-compose.yml     # Multi-container orchestration (Redis, API, Client)
└── README.md
```

---

## Android Application

The native Android app delivers a clutter-free, terminal-inspired streaming experience tailored for mobile devices.

### Key Capabilities
- **Hybrid Playback Engine**: Automatically switches between **Media3 ExoPlayer** (for direct `.m3u8` HLS / `.mp4` video feeds) and a sandboxed **Hardware-Accelerated WebView** (for Dailymotion and OK.ru embeds with origin referrer spoofing).
- **Picture-in-Picture (PiP)**: Keep watching episodes while navigating other applications on Android 8.0+.
- **Lifecycle Audio Guard**: Playback and background audio immediately terminate upon navigating away from the player screen or closing the app.
- **RTL Arabic Typography**: Designed with right-to-left layout direction, balanced cards, and high-contrast monospace metadata badges.
- **Direct Clipboard Integration**: One-tap copying of episode stream links.
- **Configurable Backend**: API endpoint is dynamically loaded from `app/src/main/assets/config.json`.

### Building the Android App

Prerequisites: JDK 17 and Android SDK 35 (`compileSdk = 35`, `minSdk = 26`).

```bash
cd android

# Build debug APK
./gradlew :app:assembleDebug

# Build signed release APK
./gradlew :app:assembleRelease
```

Generated APKs are located at:
- `android/app/build/outputs/apk/release/app-release.apk`
- `android/app/build/outputs/apk/debug/app-debug.apk`

---

## REST API Reference

The API runs on port `4000` by default and prefixes all routes with `/api`.

| Method | Endpoint | Description | Query / Body Parameters |
|:---|:---|:---|:---|
| `GET` | `/api/alive` | Service health check | None |
| `GET` | `/api/latest` | Fetch recent episode releases feed | None |
| `GET` | `/api/search` | Search series catalog | `q={search_term}` |
| `POST` | `/api/scrape` | Scrape series details and episode links | `{"url": "series-or-episode-url"}` |

### Example Request

```bash
# Search for a series
curl -X GET "https://esheeq.ysnirix.xyz/api/search?q=الحفرة"

# Scrape episode stream
curl -X POST "https://esheeq.ysnirix.xyz/api/scrape" \
  -H "Content-Type: application/json" \
  -d '{"url": "https://esheeq.org/series/..."}'
```

---

## Quick Start (Docker)

Launch the entire stack (Redis cache, Express API, and SvelteKit client) using Docker Compose:

```bash
# Clone the repository
git clone https://github.com/Ysn4Irix/esheeq-scraper.git
cd esheeq-scraper

# Start all containers in the background
docker compose up -d --build
```

Container endpoints:
- **Client Frontend**: `http://localhost:5173`
- **REST API**: `http://localhost:4000/api`
- **Redis Cache**: `localhost:6379`

---

## Local Development

### 1. API Service

```bash
cd api
pnpm install
cp .env.example .env   # Configure PORT and REDIS host if needed
pnpm run dev
```

### 2. Web Client

```bash
cd client
pnpm install
pnpm run dev
```

---

## Versioning & CI/CD

- **Semantic Versioning (SemVer)**: Project releases follow `vMAJOR.MINOR.PATCH`.
- **Release Tracking**: Android version codes and names are centrally synchronized in [`android/app/version.properties`](android/app/version.properties).
- **Automated GitHub Releases**: Pushing a version tag or running GitHub Actions triggers `.github/workflows/release.yml`, building signed release APKs and publishing them directly to [GitHub Releases](https://github.com/Ysn4Irix/esheeq-scraper/releases).

---

## Contributing

Pull requests and issues are welcome. When submitting bug fixes or features:
1. Ensure commits follow [Conventional Commits](https://www.conventionalcommits.org/) (`feat:`, `fix:`, `chore:`).
2. Bump `version.properties` for corresponding Android changes.
3. Test builds with `./gradlew :app:assembleRelease` and `pnpm test` (where applicable).

---

## License

Distributed under the **MIT License**. See [`LICENSE`](LICENSE) for full details.

Copyright © 2023-present [Ysn4Irix](https://github.com/Ysn4Irix).
