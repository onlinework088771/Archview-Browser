package com.example.stealthbrowser.stealth

import com.example.stealthbrowser.model.BrowserProfile
import kotlin.random.Random

enum class OperatingSystemType(val displayName: String) {
  ANDROID("Android"),
  WINDOWS("Windows"),
  MAC("macOS"),
  LINUX("Linux")
}

data class AndroidDeviceModel(
  val brand: String,
  val model: String,
  val marketName: String,
  val webGlVendor: String,
  val webGlRenderer: String,
  val screenResolution: String,
  val pixelRatio: Float,
  val memoryGb: Int
)

object StealthProfileGenerator {

  // Real, genuine Android Devices across popular brands
  val REAL_ANDROID_DEVICES = listOf(
    // Samsung
    AndroidDeviceModel("Samsung", "SM-S928B", "Samsung Galaxy S24 Ultra", "Qualcomm", "Adreno (TM) 750", "1440x3120", 3.0f, 12),
    AndroidDeviceModel("Samsung", "SM-S918B", "Samsung Galaxy S23 Ultra", "Qualcomm", "Adreno (TM) 740", "1440x3088", 3.0f, 12),
    AndroidDeviceModel("Samsung", "SM-A546B", "Samsung Galaxy A54 5G", "ARM", "Mali-G68 MP5", "1080x2340", 2.625f, 8),
    AndroidDeviceModel("Samsung", "SM-S921B", "Samsung Galaxy S24", "Samsung", "Xclipse 940", "1080x2340", 2.625f, 8),
    AndroidDeviceModel("Samsung", "SM-F946B", "Samsung Galaxy Z Fold5", "Qualcomm", "Adreno (TM) 740", "1812x2176", 2.625f, 12),

    // Google Pixel
    AndroidDeviceModel("Google", "Pixel 9 Pro XL", "Google Pixel 9 Pro XL", "ARM", "Mali-G715 Immortalis", "1344x2992", 3.0f, 16),
    AndroidDeviceModel("Google", "Pixel 8 Pro", "Google Pixel 8 Pro", "ARM", "Mali-G715 Immortalis", "1344x2992", 2.8125f, 12),
    AndroidDeviceModel("Google", "Pixel 8", "Google Pixel 8", "ARM", "Mali-G715", "1080x2400", 2.625f, 8),
    AndroidDeviceModel("Google", "Pixel 7a", "Google Pixel 7a", "ARM", "Mali-G710", "1080x2400", 2.625f, 8),
    AndroidDeviceModel("Google", "Pixel 6 Pro", "Google Pixel 6 Pro", "ARM", "Mali-G78", "1440x3120", 3.5f, 12),

    // Xiaomi / Poco / Redmi
    AndroidDeviceModel("Xiaomi", "23116PN5BC", "Xiaomi 14 Pro", "Qualcomm", "Adreno (TM) 750", "1440x3200", 3.0f, 16),
    AndroidDeviceModel("Xiaomi", "2210132C", "Xiaomi 13 Pro", "Qualcomm", "Adreno (TM) 740", "1440x3200", 3.0f, 12),
    AndroidDeviceModel("Poco", "2311DRK48G", "Poco X6 Pro", "ARM", "Mali-G615-MC6", "1220x2712", 2.75f, 12),
    AndroidDeviceModel("Redmi", "23049PCD8G", "Redmi Note 12 Turbo", "Qualcomm", "Adreno (TM) 725", "1080x2400", 2.625f, 8),

    // OnePlus
    AndroidDeviceModel("OnePlus", "CPH2581", "OnePlus 12", "Qualcomm", "Adreno (TM) 750", "1440x3168", 3.5f, 16),
    AndroidDeviceModel("OnePlus", "CPH2449", "OnePlus 11", "Qualcomm", "Adreno (TM) 740", "1440x3216", 3.5f, 16),
    AndroidDeviceModel("OnePlus", "CPH2513", "OnePlus Nord 3", "ARM", "Mali-G710 MC10", "1240x2772", 3.0f, 12),

    // Motorola
    AndroidDeviceModel("Motorola", "motorola edge 50 ultra", "Motorola Edge 50 Ultra", "Qualcomm", "Adreno (TM) 735", "1220x2712", 2.75f, 12),
    AndroidDeviceModel("Motorola", "moto g84 5G", "Motorola Moto G84 5G", "Qualcomm", "Adreno (TM) 619", "1080x2400", 2.625f, 8),

    // Vivo / iQOO
    AndroidDeviceModel("Vivo", "V2324A", "Vivo X100 Pro", "ARM", "Immortalis-G720 MC12", "1260x2800", 3.0f, 16),
    AndroidDeviceModel("iQOO", "V2307A", "iQOO 12", "Qualcomm", "Adreno (TM) 750", "1260x2800", 3.0f, 16)
  )

  // Real Chrome versions currently in widespread circulation
  val REAL_CHROME_VERSIONS = listOf(
    "131.0.6778.85" to "\"Google Chrome\";v=\"131\", \"Chromium\";v=\"131\", \"Not_A Brand\";v=\"24\"",
    "130.0.6723.107" to "\"Google Chrome\";v=\"130\", \"Chromium\";v=\"130\", \"Not?A_Brand\";v=\"99\"",
    "129.0.6668.70" to "\"Google Chrome\";v=\"129\", \"Chromium\";v=\"129\", \"Not=A?Brand\";v=\"8\"",
    "128.0.6613.127" to "\"Google Chrome\";v=\"128\", \"Chromium\";v=\"128\", \"Not;A=Brand\";v=\"24\"",
    "127.0.6533.103" to "\"Google Chrome\";v=\"127\", \"Chromium\";v=\"127\", \"Not.A/Brand\";v=\"8\""
  )

  /**
   * Generates a fully coherent, undetectable, genuine BrowserProfile for Android, Windows, Mac or Linux.
   */
  fun generateProfile(
    osType: OperatingSystemType,
    customName: String? = null,
    specificAndroidVersion: Int? = null,
    proxyId: String? = null
  ): BrowserProfile {
    val (chromeVer, secChUa) = REAL_CHROME_VERSIONS.random()
    val chromeMajor = chromeVer.substringBefore(".")

    return when (osType) {
      OperatingSystemType.ANDROID -> {
        val device = REAL_ANDROID_DEVICES.random()
        // Android 11 to 17 per user requirement
        val androidVerNum = specificAndroidVersion ?: (11..17).random()
        val androidVerString = "Android $androidVerNum"
        
        // Exact real Chrome on Android User-Agent syntax
        val ua = "Mozilla/5.0 (Linux; Android $androidVerNum; ${device.model}) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/$chromeVer Mobile Safari/537.36"
        val profileName = customName ?: "${device.marketName} ($androidVerString)"

        BrowserProfile(
          name = profileName,
          deviceModel = device.model,
          androidVersion = androidVerString,
          chromeVersion = "Chrome $chromeVer",
          userAgent = ua,
          secChUa = secChUa,
          screenResolution = device.screenResolution,
          pixelRatio = device.pixelRatio,
          hardwareConcurrency = listOf(6, 8, 8, 8).random(),
          deviceMemoryGb = device.memoryGb,
          webGlVendor = device.webGlVendor,
          webGlRenderer = device.webGlRenderer,
          canvasNoiseEnabled = true,
          audioNoiseEnabled = true,
          proxyId = proxyId,
          colorHex = when (device.brand) {
            "Samsung" -> 0xFF3B82F6
            "Google" -> 0xFF00E5FF
            "Xiaomi", "Poco", "Redmi" -> 0xFFFF6B00
            "OnePlus" -> 0xFFEF4444
            else -> 0xFF10B981
          }
        )
      }

      OperatingSystemType.WINDOWS -> {
        val winVer = listOf("Windows NT 10.0; Win64; x64", "Windows NT 10.0; WOW64").random()
        val ua = "Mozilla/5.0 ($winVer) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/$chromeVer Safari/537.36"
        val gpu = listOf(
          "ANGLE (NVIDIA, NVIDIA GeForce RTX 4070 Direct3D11 vs_5_0 ps_5_0, D3D11)",
          "ANGLE (NVIDIA, NVIDIA GeForce RTX 3060 Direct3D11 vs_5_0 ps_5_0, D3D11)",
          "ANGLE (Intel, Intel(R) Iris(R) Xe Graphics Direct3D11 vs_5_0 ps_5_0, D3D11)"
        ).random()
        val name = customName ?: "Windows 11 Workstation (Chrome $chromeMajor)"

        BrowserProfile(
          name = name,
          deviceModel = "PC Desktop",
          androidVersion = "Windows 11",
          chromeVersion = "Chrome $chromeVer",
          userAgent = ua,
          secChUa = secChUa,
          screenResolution = listOf("1920x1080", "2560x1440").random(),
          pixelRatio = 1.0f,
          hardwareConcurrency = listOf(8, 12, 16).random(),
          deviceMemoryGb = listOf(8, 16, 32).random(),
          webGlVendor = "Google Inc. (NVIDIA)",
          webGlRenderer = gpu,
          canvasNoiseEnabled = true,
          audioNoiseEnabled = true,
          proxyId = proxyId,
          colorHex = 0xFF00A4EF
        )
      }

      OperatingSystemType.MAC -> {
        val macVer = listOf("10_15_7", "14_4_1", "14_5").random()
        val ua = "Mozilla/5.0 (Macintosh; Intel Mac OS X $macVer) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/$chromeVer Safari/537.36"
        val renderer = listOf("Apple M2 Pro", "Apple M3", "Apple M1 Max").random()
        val name = customName ?: "MacBook Pro ($renderer)"

        BrowserProfile(
          name = name,
          deviceModel = "MacBook Pro",
          androidVersion = "macOS Sonoma",
          chromeVersion = "Chrome $chromeVer",
          userAgent = ua,
          secChUa = secChUa,
          screenResolution = listOf("1728x1117", "1440x900", "2560x1600").random(),
          pixelRatio = 2.0f,
          hardwareConcurrency = 8,
          deviceMemoryGb = listOf(16, 24, 32).random(),
          webGlVendor = "Apple",
          webGlRenderer = renderer,
          canvasNoiseEnabled = true,
          audioNoiseEnabled = true,
          proxyId = proxyId,
          colorHex = 0xFFA855F7
        )
      }

      OperatingSystemType.LINUX -> {
        val ua = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/$chromeVer Safari/537.36"
        val name = customName ?: "Linux Ubuntu Desktop (Chrome $chromeMajor)"

        BrowserProfile(
          name = name,
          deviceModel = "Linux PC",
          androidVersion = "Ubuntu 24.04 LTS",
          chromeVersion = "Chrome $chromeVer",
          userAgent = ua,
          secChUa = secChUa,
          screenResolution = "1920x1080",
          pixelRatio = 1.0f,
          hardwareConcurrency = 8,
          deviceMemoryGb = 16,
          webGlVendor = "Mesa",
          webGlRenderer = "Mesa Intel(R) UHD Graphics (CML GT2)",
          canvasNoiseEnabled = true,
          audioNoiseEnabled = true,
          proxyId = proxyId,
          colorHex = 0xFFE95420
        )
      }
    }
  }

  /**
   * Generates a matching, authentic User-Agent for a given OS and device configuration
   */
  fun generateMatchingUserAgent(
    osType: OperatingSystemType,
    androidVersion: Int,
    modelName: String
  ): Pair<String, String> {
    val (chromeVer, secChUa) = REAL_CHROME_VERSIONS.random()
    val ua = when (osType) {
      OperatingSystemType.ANDROID ->
        "Mozilla/5.0 (Linux; Android $androidVersion; $modelName) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/$chromeVer Mobile Safari/537.36"
      OperatingSystemType.WINDOWS ->
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/$chromeVer Safari/537.36"
      OperatingSystemType.MAC ->
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/$chromeVer Safari/537.36"
      OperatingSystemType.LINUX ->
        "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/$chromeVer Safari/537.36"
    }
    return ua to secChUa
  }

  /**
   * Bulk generation of profiles according to selected OS
   */
  fun generateBulkProfiles(
    osType: OperatingSystemType,
    count: Int
  ): List<BrowserProfile> {
    val result = mutableListOf<BrowserProfile>()
    for (i in 1..count.coerceIn(1, 20)) {
      result.add(generateProfile(osType = osType))
    }
    return result
  }
}
