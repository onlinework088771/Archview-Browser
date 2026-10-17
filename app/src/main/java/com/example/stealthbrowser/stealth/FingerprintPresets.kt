package com.example.stealthbrowser.stealth

import com.example.stealthbrowser.model.BrowserProfile

object FingerprintPresets {

  val PRESET_PIXEL_8_PRO = BrowserProfile(
    name = "Pixel 8 Pro (Android 14)",
    deviceModel = "Pixel 8 Pro",
    androidVersion = "Android 14",
    chromeVersion = "Chrome 131.0.6778.85",
    userAgent = "Mozilla/5.0 (Linux; Android 14; Pixel 8 Pro) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.6778.85 Mobile Safari/537.36",
    secChUa = "\"Google Chrome\";v=\"131\", \"Chromium\";v=\"131\", \"Not_A Brand\";v=\"24\"",
    screenResolution = "1080x2400",
    pixelRatio = 2.625f,
    hardwareConcurrency = 8,
    deviceMemoryGb = 12,
    webGlVendor = "ARM",
    webGlRenderer = "Mali-G715 Immortalis",
    colorHex = 0xFF00E5FF
  )

  val PRESET_SAMSUNG_S24 = BrowserProfile(
    name = "Galaxy S24 Ultra (Android 14)",
    deviceModel = "SM-S928B",
    androidVersion = "Android 14",
    chromeVersion = "Chrome 130.0.6723.107",
    userAgent = "Mozilla/5.0 (Linux; Android 14; SM-S928B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.6723.107 Mobile Safari/537.36",
    secChUa = "\"Google Chrome\";v=\"130\", \"Chromium\";v=\"130\", \"Not?A_Brand\";v=\"99\"",
    screenResolution = "1440x3120",
    pixelRatio = 3.0f,
    hardwareConcurrency = 8,
    deviceMemoryGb = 12,
    webGlVendor = "Qualcomm",
    webGlRenderer = "Adreno (TM) 750",
    colorHex = 0xFF3B82F6
  )

  val PRESET_ONEPLUS_12 = BrowserProfile(
    name = "OnePlus 12 (Android 14)",
    deviceModel = "CPH2581",
    androidVersion = "Android 14",
    chromeVersion = "Chrome 131.0.6778.39",
    userAgent = "Mozilla/5.0 (Linux; Android 14; CPH2581) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.6778.39 Mobile Safari/537.36",
    secChUa = "\"Google Chrome\";v=\"131\", \"Chromium\";v=\"131\", \"Not_A Brand\";v=\"24\"",
    screenResolution = "1440x3168",
    pixelRatio = 3.5f,
    hardwareConcurrency = 8,
    deviceMemoryGb = 16,
    webGlVendor = "Qualcomm",
    webGlRenderer = "Adreno (TM) 750",
    colorHex = 0xFFA855F7
  )

  val PRESET_PIXEL_7A = BrowserProfile(
    name = "Pixel 7a (Android 14)",
    deviceModel = "Pixel 7a",
    androidVersion = "Android 14",
    chromeVersion = "Chrome 129.0.6668.70",
    userAgent = "Mozilla/5.0 (Linux; Android 14; Pixel 7a) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/129.0.6668.70 Mobile Safari/537.36",
    secChUa = "\"Google Chrome\";v=\"129\", \"Chromium\";v=\"129\", \"Not=A?Brand\";v=\"8\"",
    screenResolution = "1080x2400",
    pixelRatio = 2.625f,
    hardwareConcurrency = 8,
    deviceMemoryGb = 8,
    webGlVendor = "ARM",
    webGlRenderer = "Mali-G710",
    colorHex = 0xFF10B981
  )

  fun getAllDefaultPresets(): List<BrowserProfile> = listOf(
    PRESET_PIXEL_8_PRO,
    PRESET_SAMSUNG_S24,
    PRESET_ONEPLUS_12,
    PRESET_PIXEL_7A
  )
}
