package com.esheeq.app.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.webkit.WebResourceResponse
import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets

object AdBlockManager {

    private const val PREFS_NAME = "esheeq_adblock_prefs"
    private const val KEY_SHIELD_ENABLED = "key_shield_enabled"

    private var prefs: SharedPreferences? = null

    fun init(context: Context) {
        if (prefs == null) {
            prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }
    }

    fun isAdBlockEnabled(context: Context): Boolean {
        init(context)
        return prefs?.getBoolean(KEY_SHIELD_ENABLED, true) ?: true
    }

    fun setAdBlockEnabled(context: Context, enabled: Boolean) {
        init(context)
        prefs?.edit()?.putBoolean(KEY_SHIELD_ENABLED, enabled)?.apply()
    }

    /**
     * Curated list of high-impact ad networks, popup engines, and telemetry domains.
     */
    private val BLOCKED_DOMAINS = hashSetOf(
        "popads.net",
        "adsterra.com",
        "propellerads.com",
        "exoclick.com",
        "monetag.com",
        "hilltopads.net",
        "clickadu.com",
        "onclickmega.com",
        "trafficfactory.biz",
        "juicyads.com",
        "a-ads.com",
        "adcash.com",
        "popcash.net",
        "adx.adform.net",
        "adnxs.com",
        "criteo.com",
        "criteo.net",
        "outbrain.com",
        "taboola.com",
        "pubmatic.com",
        "rubiconproject.com",
        "smartadserver.com",
        "bidswitch.net",
        "casalemedia.com",
        "openx.net",
        "yieldmo.com",
        "vungle.com",
        "unity3d.com",
        "applovin.com",
        "adcolony.com",
        "chartboost.com",
        "doubleclick.net",
        "googlesyndication.com",
        "googleadservices.com",
        "adservice.google.com",
        "an.yandex.ru",
        "mc.yandex.ru",
        "scorecardresearch.com",
        "histats.com",
        "toplist.cz",
        "hotjar.com",
        "clarity.ms",
        "gemius.pl",
        "ad-maven.com",
        "yllix.com",
        "clicksor.com",
        "infolinks.com",
        "adblade.com",
        "revcontent.com",
        "mgid.com",
        "adroll.com",
        "zergnet.com",
        "content.ad",
        "conversantmedia.com",
        "adpushup.com",
        "exponential.com"
    )

    /**
     * Legitimate streaming CDNs and player domains that must never be blocked entirely.
     */
    private val WHITELISTED_HOSTS = listOf(
        "dailymotion.com",
        "dmcdn.net",
        "ok.ru",
        "vk.com",
        "mail.ru",
        "my.mail.ru",
        "vidshar",
        "qeseh",
        "qesen",
        "cloudflare.com",
        "gstatic.com",
        "googleapis.com"
    )

    /**
     * Returns true if a given resource URL corresponds to an advertisement, tracker, or popup script.
     */
    fun isAd(uri: Uri): Boolean {
        val host = uri.host?.lowercase() ?: return false
        val path = uri.path?.lowercase() ?: ""
        val query = uri.query?.lowercase() ?: ""
        val fullUrl = uri.toString().lowercase()

        // 1. Dailymotion in-stream ad manifests and synchronization scripts
        if (host.contains("dmxleo.dailymotion.com") || host.contains("advertising.dailymotion.com")) {
            return true
        }
        if (host.contains("dailymotion.com") && (path.contains("/ad/") || query.contains("&bs=1") || query.contains("ad_sync_script"))) {
            return true
        }

        // 2. Direct domain blacklist matching
        for (blocked in BLOCKED_DOMAINS) {
            if (host == blocked || host.endsWith(".$blocked")) {
                return true
            }
        }

        // 3. If host is whitelisted, skip generic heuristics to avoid breaking video segments
        val isWhitelisted = WHITELISTED_HOSTS.any { host.contains(it) }
        if (isWhitelisted) {
            // Still block explicit advertising paths on whitelisted hosts
            if (path.contains("/ads/") || path.contains("/adframe") || path.contains("/adserver") || path.contains("/ima3.js")) {
                return true
            }
            return false
        }

        // 4. Generic heuristic keywords for third-party embed mirrors
        if (path.contains("/ads/") ||
            path.contains("/ad/") ||
            path.contains("/adserver") ||
            path.contains("popunder") ||
            path.contains("pop_under") ||
            path.contains("adframe") ||
            path.contains("/vast.xml") ||
            path.contains("/vmap.xml") ||
            path.endsWith("/ads.js") ||
            query.contains("popunder") ||
            query.contains("ad_unit") ||
            fullUrl.contains("doubleclick") ||
            fullUrl.contains("googleads") ||
            fullUrl.contains("adzerk")
        ) {
            return true
        }

        return false
    }

    /**
     * Supplies an empty HTTP 200 text response to silence intercepted ad requests.
     */
    fun getEmptyResponse(): WebResourceResponse {
        return WebResourceResponse(
            "text/plain",
            "UTF-8",
            ByteArrayInputStream(ByteArray(0))
        )
    }

    /**
     * JavaScript defuser injected into WebViews to neutralize pop-ups and click-jackers.
     */
    val SCRIPT_DEFUSER: String = """
        (function() {
            try {
                window.open = function() { return null; };
                window.alert = function() {};
                window.confirm = function() { return true; };
                window.prompt = function() { return null; };

                document.addEventListener('click', function(e) {
                    var t = e.target;
                    while (t && t !== document.body) {
                        if (t.tagName === 'A' && t.target === '_blank') {
                            var href = (t.href || '').toLowerCase();
                            var isAllowed = href.indexOf('dailymotion.com') !== -1 ||
                                            href.indexOf('ok.ru') !== -1 ||
                                            href.indexOf('mail.ru') !== -1 ||
                                            href.indexOf('vk.com') !== -1;
                            if (!isAllowed) {
                                e.preventDefault();
                                e.stopPropagation();
                                return false;
                            }
                        }
                        t = t.parentElement;
                    }
                }, true);
            } catch(e) {}
        })();
    """.trimIndent()

    /**
     * Cosmetic CSS rules to suppress floating overlays, banner boxes, and deceptive click layers.
     */
    val COSMETIC_HIDE_CSS: String = """
        (function() {
            try {
                var style = document.createElement('style');
                style.type = 'text/css';
                style.innerHTML = " \
                    [id*='ad-'], [id*='ad_'], [id*='adbox'], [class*='ad-banner'], \
                    [class*='ad_banner'], [class*='ad-container'], [class*='adsbygoogle'], \
                    [class*='popunder'], div[style*='z-index: 2147483647'], \
                    iframe[src*='ad'], iframe[src*='pop'] { \
                        display: none !important; \
                        visibility: hidden !important; \
                        opacity: 0 !important; \
                        pointer-events: none !important; \
                        width: 0 !important; \
                        height: 0 !important; \
                    } \
                ";
                (document.head || document.documentElement).appendChild(style);
            } catch(e) {}
        })();
    """.trimIndent()
}
