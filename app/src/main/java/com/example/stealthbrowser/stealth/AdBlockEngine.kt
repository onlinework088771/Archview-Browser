package com.example.stealthbrowser.stealth

import android.net.Uri
import android.webkit.WebResourceResponse
import java.io.ByteArrayInputStream

object AdBlockEngine {

  private val BLOCKED_HOST_PATTERNS = listOf(
    "doubleclick.net",
    "googlesyndication.com",
    "google-analytics.com",
    "googletagmanager.com",
    "adservice.google.com",
    "pagead2.googlesyndication.com",
    "facebook.net",
    "connect.facebook.net",
    "criteo.com",
    "criteo.net",
    "adnxs.com",
    "rubiconproject.com",
    "pubmatic.com",
    "openx.net",
    "outbrain.com",
    "taboola.com",
    "scorecardresearch.com",
    "quantserve.com",
    "hotjar.com",
    "clarity.ms",
    "mixpanel.com",
    "amazon-adsystem.com",
    "adroll.com",
    "moatads.com",
    "advertising.com",
    "popads.net",
    "popcash.net",
    "propellerads.com",
    "zergnet.com",
    "revcontent.com",
    "smartadserver.com",
    "bidswitch.net",
    "casalemedia.com",
    "indexww.com",
    "adcolony.com",
    "chartbeat.com",
    "onesignal.com"
  )

  fun shouldBlock(url: String?): Boolean {
    if (url.isNullOrBlank()) return false
    val host = try {
      Uri.parse(url).host?.lowercase() ?: return false
    } catch (_: Exception) {
      return false
    }

    return BLOCKED_HOST_PATTERNS.any { pattern ->
      host == pattern || host.endsWith(".$pattern")
    }
  }

  fun createEmptyResponse(): WebResourceResponse {
    return WebResourceResponse(
      "text/plain",
      "UTF-8",
      200,
      "OK",
      mapOf(
        "Access-Control-Allow-Origin" to "*",
        "Cache-Control" to "no-cache"
      ),
      ByteArrayInputStream(ByteArray(0))
    )
  }
}
