package com.example.stealthbrowser.model

import java.util.UUID

data class BrowserProfile(
  val id: String = UUID.randomUUID().toString(),
  val name: String,
  val deviceModel: String,
  val androidVersion: String,
  val chromeVersion: String,
  val userAgent: String,
  val secChUa: String,
  val screenResolution: String = "1080x2400",
  val pixelRatio: Float = 2.625f,
  val hardwareConcurrency: Int = 8,
  val deviceMemoryGb: Int = 8,
  val webGlVendor: String = "Qualcomm",
  val webGlRenderer: String = "Adreno (TM) 750",
  val canvasNoiseEnabled: Boolean = true,
  val audioNoiseEnabled: Boolean = true,
  val webrtcPolicy: String = "BLOCK_LEAK", // BLOCK_LEAK, REALISTIC, DISABLE
  val proxyId: String? = null,
  val startupUrl: String = "https://www.google.com",
  val userScript: String = "",
  val adBlockEnabled: Boolean = true,
  val clearDataOnClose: Boolean = true,
  val savedCookies: String = "",
  val colorHex: Long = 0xFF00E5FF,
  val createdAt: Long = System.currentTimeMillis()
)
