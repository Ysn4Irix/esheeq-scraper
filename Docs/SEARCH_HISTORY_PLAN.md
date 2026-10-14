# Search History Feature - Implementation Plan

## Overview
This document specifies the design, architecture, and step-by-step execution plan for introducing a persistent search history feature to the Esheeq Android application. Users will be able to view their recent search queries, execute past searches with a single tap, remove individual queries, or clear their entire search history.

---

## 1. Architectural Components

### 1.1 SearchHistoryManager (`SearchHistoryManager.kt`)
- **Package**: `com.esheeq.app.data.repository`
- **Storage**: `SharedPreferences` (`esheeq_search_history_prefs`, `KEY_RECENT_QUERIES`) with Gson serialization.
- **Capacity**: Capped at the 12 most recent unique queries (FIFO).
- **Reactive State**: Exposes `StateFlow<List<String>>` (`recentQueries`) to provide reactive updates across Compose screens.
- **Operations**:
  - `addQuery(context, query)`: Trims query, eliminates case-insensitive duplicates, prepends to index 0, caps at 12 items, and persists.
  - `removeQuery(context, query)`: Removes specific query from history and updates state.
  - `clearHistory(context)`: Clears all stored queries and updates state.
  - `getHistory(context)`: Synchronous fetch for initial state loading.

### 1.2 HomeViewModel Integration (`HomeViewModel.kt`)
- Connect `HomeViewModel` to `SearchHistoryManager`.
- Add handlers:
  - `onSearchSubmitted(query, context)`: Records query on keyboard IME action.
  - `onSearchResultClicked(query, context)`: Records query when user taps a search result.
  - `onHistoryItemClicked(query)`: Re-populates search query and triggers search.
  - `removeHistoryItem(query, context)`: Removes individual entry.
  - `clearSearchHistory(context)`: Clears entire history.

### 1.3 Search Bar & Home UI Integration (`SearchBarComponent.kt` & `HomeScreen.kt`)
- **`SearchBarComponent.kt`**:
  - Add `onSearch: () -> Unit` callback triggered via `KeyboardActions(onSearch = { ... })`.
- **`HomeScreen.kt`**:
  - When the search query is empty and history exists:
    - Display a dedicated Terminal Section:
      - Header: `"سجل البحث // RECENT QUERIES"` with an `Icons.Default.History` icon and a `"مسح الكل"` (Clear All) button.
      - Chips Flow / Horizontal Row: Terminal-styled query chips with prompt `> query`, one-tap search action, and a delete `x` button per chip.
  - When tapping a result card in the search results list, record the query in history.

---

## 2. Implementation Steps

1. **Create `SearchHistoryManager.kt`**:
   - SharedPreferences-backed FIFO manager with reactive `StateFlow`.
2. **Update `SearchBarComponent.kt`**:
   - Support `onSearch` action in keyboard options and actions.
3. **Update `HomeViewModel.kt`**:
   - Expose recent queries and connect history actions.
4. **Update `HomeScreen.kt`**:
   - Render recent search history chips and integrate action listeners.
5. **Update `version.properties`**:
   - Bump to `versionCode=210` and `versionName=2.3.0` (`feat(android)`).
6. **Build & Quality Verification**:
   - Compile `:app:assembleDebug` and `:app:assembleRelease`.
   - Check `output-metadata.json`.
   - Run mobile design audit.
   - Commit directly to `main`.
