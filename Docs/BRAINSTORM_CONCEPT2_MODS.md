# Brainstorm: Concept 2 (Terminal Utility) with Focused Search & Latest 5 Episodes

> **Context:** The user approved **Concept 2 (Command Palette / Terminal Fast-Tool)** for the `esheeq-scraper` frontend redesign with two specific requirements:
> 1. The initial page shall feature **only a search bar**.
> 2. The **final five episodes** must be fetched live from `https://qeseh.net/son-bolumler`.

---

## 🎯 Architectural & UX Breakdown

### 1. Scraping & Backend Integration (`api/`)
- **Source Target:** `https://qeseh.net/son-bolumler`
- **DOM Structure:** `#load-post-episodes article.post` contains:
  - Episode URL: `a[href]` (e.g. `https://qeseh.net/clarus/tasak-bu-deniz-episode-33/`)
  - Title & Number: `.title` and `.episodeNum span` (e.g. Episode 33)
  - Thumbnail: `.posterThumb .imgBg` (background CSS) or `img[src]`
- **Cache Strategy:** Latest episodes update multiple times a day; needs a 10–15 minute TTL cache (compared to 24 hours for completed series).
- **Endpoint:** `GET /api/latest-episodes` returning a clean JSON array of the 5 most recent episodes.

### 2. Frontend Layout & Terminal Ergonomics (`client/`)
How should the search-bar-first layout and the 5 latest episodes coexist within the approved **Concept 2 Terminal aesthetic** (`#0D1117`, JetBrains Mono, status dots, tabular data)?

---

## 💡 Exploration Options

### Option A: The Terminal Pipeline (Prominent Command Input + Recent Queue)
- **Concept:** The homepage opens with a focused command input (`> search series [query]...`) centered in the viewport. Directly beneath it sits a clean, compact terminal table showing the 5 latest ingested episodes with monospaced metadata: `[SERIES] [EP #] [STATUS: READY] [COPY_LINK]`.
- **User Flow:**
  1. User arrives: immediate focus on search input.
  2. Right below the prompt: 5 recent episode rows ready for instant 1-click copy without searching.
  3. When the user types in the search bar, the recent table fades out and transitions into live search results.
- **Pros:**
  - Zero layout clutter: maintains strict utility feel.
  - No scrolling required on desktop or mobile.
  - Direct 1-click clipboard copy for the newest episodes.
- **Cons:**
  - Shows 5 rows by default below the search bar (though minimal).
- **Effort:** Low (Clean SSR integration via SvelteKit `+page.server.js`).

---

### Option B: The Strict Minimalist CLI (Spotlight Search + Expandable Tray / Hotkey)
- **Concept:** The initial viewport contains **strictly only the search bar** and a subtle terminal status line (`ESHEEQ_CLI v2.0 // PRESS [L] OR CLICK TO VIEW LATEST 5 RELEASES`).
- **User Flow:**
  - Default view is 100% focused on the input field with keyboard autofocus.
  - Clicking a toggle or pressing `L` / `Arrow Down` smoothly slides open a clean terminal tray displaying the 5 episodes.
- **Pros:**
  - 100% literal execution of "the initial page shall feature only a search bar".
  - Ultra-pure, distraction-free interface like Raycast or Alfred.
- **Cons:**
  - Extra click or keystroke required to see the latest episodes.
  - Mobile users might miss the toggle if not clearly indicated.
- **Effort:** Medium (Requires smooth Svelte transition state + keyboard listeners).

---

### Option C: Micro-Ticker Stream (Command Bar + Horizontal Episode Pills)
- **Concept:** Top command-line search bar. Immediately below it, an ultra-compact single-row horizontal ticker / 5 micro-badges: `[NEW: هذا البحر 33] [NEW: الخليفة 38] ...`.
- **User Flow:**
  - Compact inline badges take up less than 40px of vertical space.
  - Clicking any badge instantly opens the episode modal or copies its Dailymotion embed link.
- **Pros:**
  - Ultra-low vertical footprint.
  - Visually separates search utility from recent activity.
- **Cons:**
  - Horizontal scrolling or wrapping needed on narrow mobile screens (390px).
- **Effort:** Medium.

---

## ⚙️ Link Resolution Decision: What happens when an episode is clicked?

1. **Direct Scrape & Copy (Recommended):** The API resolves the episode's Dailymotion streaming URL and copies it directly to the clipboard with an instant toast notification.
2. **Series Redirect:** Clicking the episode opens the full series overview page (`/series/:slug`) with all available episodes.
3. **Hybrid:** A button to copy the direct episode link, and a small title link to navigate to the series archive.

---

## 🏆 Recommendation

**Option A (Terminal Pipeline) with Hybrid Episode Actions**:
- Centers the command-line search bar as the primary focal point.
- Displays the 5 latest episodes directly beneath it in a compact 5-row terminal table (`#161B22` background, green status badge, 1-click copy button).
- Typing into the search bar dynamically swaps the 5 recent episodes with the search results.
- Provides immediate utility for both searchers and viewers returning to grab today's new episode.
