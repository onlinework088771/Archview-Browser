package com.example.stealthbrowser.stealth

import com.example.stealthbrowser.model.ProxyConfig

data class GeoTimezoneInfo(
  val timezoneId: String,
  val offsetMinutes: Int, // e.g. 240 for EDT, -60 for CET, 0 for UTC/GMT, -480 for SGT
  val tzAbbreviation: String,
  val tzName: String,
  val latitude: Double,
  val longitude: Double,
  val countryCode: String,
  val city: String,
  val primaryLocale: String = "en-US",
  val languages: List<String> = listOf("en-US", "en")
)

object GeoTimezoneResolver {

  fun resolve(proxy: ProxyConfig?): GeoTimezoneInfo {
    if (proxy == null) {
      // Default to US New York profile if no proxy linked
      return GeoTimezoneInfo(
        timezoneId = "America/New_York",
        offsetMinutes = 240,
        tzAbbreviation = "EDT",
        tzName = "Eastern Daylight Time",
        latitude = 40.7128,
        longitude = -74.0060,
        countryCode = "US",
        city = "New York",
        primaryLocale = "en-US",
        languages = listOf("en-US", "en")
      )
    }

    val cc = proxy.countryCode.uppercase().trim()
    val cityLower = (proxy.city ?: "").lowercase().trim()

    return when (cc) {
      "US" -> {
        if (cityLower.contains("los angeles") || cityLower.contains("california") || cityLower.contains("san francisco")) {
          GeoTimezoneInfo("America/Los_Angeles", 420, "PDT", "Pacific Daylight Time", 34.0522, -118.2437, "US", "Los Angeles", "en-US", listOf("en-US", "en"))
        } else if (cityLower.contains("chicago") || cityLower.contains("dallas") || cityLower.contains("texas") || cityLower.contains("houston")) {
          GeoTimezoneInfo("America/Chicago", 300, "CDT", "Central Daylight Time", 41.8781, -87.6298, "US", "Chicago", "en-US", listOf("en-US", "en"))
        } else if (cityLower.contains("denver") || cityLower.contains("phoenix") || cityLower.contains("colorado")) {
          GeoTimezoneInfo("America/Denver", 360, "MDT", "Mountain Daylight Time", 39.7392, -104.9903, "US", "Denver", "en-US", listOf("en-US", "en"))
        } else {
          GeoTimezoneInfo("America/New_York", 240, "EDT", "Eastern Daylight Time", 40.7128, -74.0060, "US", "New York", "en-US", listOf("en-US", "en"))
        }
      }
      "DE" -> {
        if (cityLower.contains("berlin")) {
          GeoTimezoneInfo("Europe/Berlin", -120, "CEST", "Central European Summer Time", 52.5200, 13.4050, "DE", "Berlin", "de-DE", listOf("de-DE", "de", "en-US", "en"))
        } else {
          GeoTimezoneInfo("Europe/Berlin", -120, "CEST", "Central European Summer Time", 50.1109, 8.6821, "DE", "Frankfurt", "de-DE", listOf("de-DE", "de", "en-US", "en"))
        }
      }
      "GB", "UK" -> {
        GeoTimezoneInfo("Europe/London", -60, "BST", "British Summer Time", 51.5074, -0.1278, "GB", "London", "en-GB", listOf("en-GB", "en-US", "en"))
      }
      "SG" -> {
        GeoTimezoneInfo("Asia/Singapore", -480, "SGT", "Singapore Standard Time", 1.3521, 103.8198, "SG", "Singapore", "en-SG", listOf("en-SG", "en-GB", "en"))
      }
      "FR" -> {
        GeoTimezoneInfo("Europe/Paris", -120, "CEST", "Central European Summer Time", 48.8566, 2.3522, "FR", "Paris", "fr-FR", listOf("fr-FR", "fr", "en-US", "en"))
      }
      "NL" -> {
        GeoTimezoneInfo("Europe/Amsterdam", -120, "CEST", "Central European Summer Time", 52.3676, 4.9041, "NL", "Amsterdam", "nl-NL", listOf("nl-NL", "nl", "en-US", "en"))
      }
      "JP" -> {
        GeoTimezoneInfo("Asia/Tokyo", -540, "JST", "Japan Standard Time", 35.6762, 139.6503, "JP", "Tokyo", "ja-JP", listOf("ja-JP", "ja", "en-US", "en"))
      }
      "CA" -> {
        if (cityLower.contains("vancouver")) {
          GeoTimezoneInfo("America/Vancouver", 420, "PDT", "Pacific Daylight Time", 49.2827, -123.1207, "CA", "Vancouver", "en-CA", listOf("en-CA", "en-US", "en"))
        } else {
          GeoTimezoneInfo("America/Toronto", 240, "EDT", "Eastern Daylight Time", 43.6532, -79.3832, "CA", "Toronto", "en-CA", listOf("en-CA", "en-US", "en"))
        }
      }
      "AU" -> {
        GeoTimezoneInfo("Australia/Sydney", -600, "AEST", "Australian Eastern Standard Time", -33.8688, 151.2093, "AU", "Sydney", "en-AU", listOf("en-AU", "en-GB", "en"))
      }
      "IN" -> {
        GeoTimezoneInfo("Asia/Kolkata", -330, "IST", "India Standard Time", 19.0760, 72.8777, "IN", "Mumbai", "en-IN", listOf("en-IN", "en-GB", "en"))
      }
      "BD" -> {
        GeoTimezoneInfo("Asia/Dhaka", -360, "BST", "Bangladesh Standard Time", 23.8103, 90.4125, "BD", "Dhaka", "bn-BD", listOf("bn-BD", "en-US", "en"))
      }
      else -> {
        GeoTimezoneInfo("America/New_York", 240, "EDT", "Eastern Daylight Time", 40.7128, -74.0060, cc, proxy.city ?: "City", "en-US", listOf("en-US", "en"))
      }
    }
  }
}
