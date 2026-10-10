# URL Resolution Anomaly Investigation & Resolution

**Date:** 2026-10-10  
**Status:** Resolved  
**Scope:** `api/helpers/utils.js` (`extractDailymotionLinks`), `fetchLatestEpisodes`, `fetchDailyLinks`  

---

## 1. Problem Statement

Every scraped episode URL across all series and latest episode listings was resolving to the identical video URL:  
`https://www.dailymotion.com/video/x6mp5r`

---

## 2. Root Cause Analysis

### 2.1 The Upstream Dummy Placeholder
On `qeseh.net`, each episode page embeds a player or watch link formatted as:
```text
https://qesen.net/watch?post=<base64-payload>
```
When decoding `<base64-payload>`, every single post on the upstream site contains:
```json
{
  "codeDaily": "x6mp5r",
  "servers": [ ... ],
  "postID": "...",
  "type": "episodes",
  "backUrl": "..."
}
```
Querying the official Dailymotion API (`https://api.dailymotion.com/video/x6mp5r`) reveals that `x6mp5r` is a legacy 2008 soccer clip:
```json
{
  "id": "x6mp5r",
  "title": "Deroulement de la finale du tournoi de pes 2008 la Ola",
  "status": "published",
  "created_time": 1220355837
}
```
The upstream WordPress theme developer hardcoded `"codeDaily": "x6mp5r"` as a static template default across all posts on the site.

### 2.2 Flawed Extraction Logic
In `api/helpers/utils.js`, `extractDailymotionLinks` evaluated `data.codeDaily` first:
```javascript
// Previous flawed logic:
if (data.codeDaily) {
    return data.codeDaily.startsWith('http')
        ? data.codeDaily
        : `https://www.dailymotion.com/video/${data.codeDaily}`
}
```
Because `data.codeDaily` was always present and populated with `"x6mp5r"`, the extractor never reached `data.servers` and returned `https://www.dailymotion.com/video/x6mp5r` for every episode.

---

## 3. Stream Resolution Discovery

Genuine video streams are stored inside the `data.servers` array for each episode:

1. **Dailymotion Streams:**
   - Episodes with Dailymotion provide entries like: `{"name": "dailymotion", "id": "x80g2kn"}` or `{"name": "dailymotion", "id": "k19KuUPKYnUcQlLjL5Y"}`.
   - Dailymotion API queries verified these are published, genuine episode videos:
     - `x80g2kn` &rarr; *"اركنجي كش - الحلقة 1"* (Erkenci Kus - Episode 1)
     - `x80g2km` &rarr; *"اركنجي كش - الحلقة 2"* (Erkenci Kus - Episode 2)
     - `k6q6yBxcgDXHxWw2vsH` &rarr; *"إركانجي كوش - الحلقة 03"* (Erkenci Kus - Episode 3)
     - `k19KuUPKYnUcQlLjL5Y` &rarr; *"Kalife 38"* (Halef - Episode 38)
2. **Direct Streams (Express / Cloud Mail):**
   - Direct URLs formatted with `https://cloud.mail.ru/public/...`.
3. **OK.ru Streams:**
   - Server name `ok` with numeric IDs (e.g. `16973677267554` &rarr; `https://ok.ru/video/16973677267554`).
4. **Arab HD / estream / Red HD:**
   - Video IDs hosted on `vidshar.org` / `vidbom.com`.

---

## 4. Resolution Applied

In `api/helpers/utils.js`:

1. **Banned `x6mp5r` Dummy Value:**
   - Never resolve `data.codeDaily` if it equals `"x6mp5r"`.
2. **Prioritized Server Inspection Order:**
   - **Priority 1:** Server named `dailymotion` &rarr; `https://www.dailymotion.com/video/${id}`
   - **Priority 2:** Direct HTTP links (e.g., Express / Cloud Mail) &rarr; `id`
   - **Priority 3:** OK.ru server &rarr; `https://ok.ru/video/${id}`
   - **Priority 4:** Arab HD / estream / Red HD &rarr; `https://vidshar.org/${id}`
   - **Priority 5:** Generic server fallback &rarr; `https://vidshar.org/${id}`
3. **Direct iframe fallback:**
   - Inspects `iframe[src*="dailymotion.com"]` and `iframe[src*="ok.ru"]` if present in the raw page DOM.

---

## 5. Verification Results

### 5.1 Latest Episodes (`GET /api/latest?count=5`)
| Episode Title | Resolved Video URL |
|---|---|
| هذا البحر سوف يفيض الحلقة 33 | `https://cloud.mail.ru/public/kpyd/EZJ88UkzX` |
| مسلسل الخليفة الحلقة 38 | `https://www.dailymotion.com/video/k19KuUPKYnUcQlLjL5Y` |
| مسلسل حب محتمل الحلقة 17 | `https://cloud.mail.ru/public/mRws/bu2LCtyQX` |
| حيث تشرق الشمس الحلقة 4 | `https://www.dailymotion.com/video/k4N98mySD77azBLiqgq` |
| مسلسل أنت من أحب الحلقة 20 | `https://cloud.mail.ru/public/RJDL/JsHpk8chc` |

### 5.2 Series Episodes (`GET /api/scrape/erkenci-kus-vqstm`)
| Episode | Resolved Video URL | Verified Stream Title |
|---|---|---|
| Episode 1 | `https://www.dailymotion.com/video/x80g2kn` | اركنجي كش - الحلقة 1 |
| Episode 2 | `https://www.dailymotion.com/video/x80g2km` | اركنجي كش - الحلقة 2 |
| Episode 3 | `https://www.dailymotion.com/video/k6q6yBxcgDXHxWw2vsH` | إركانجي كوش - الحلقة 03 |
