package com.example.stealthbrowser.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import android.webkit.CookieManager
import android.webkit.WebStorage
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.stealthbrowser.model.BrowserProfile
import com.example.stealthbrowser.model.FloatingBrowserInstance
import com.example.stealthbrowser.model.ProxyConfig
import com.example.stealthbrowser.model.ProxyProtocol
import com.example.stealthbrowser.model.ProxyStatus
import com.example.stealthbrowser.model.WindowLayoutMode
import com.example.stealthbrowser.stealth.FingerprintPresets
import com.example.stealthbrowser.stealth.ProxyManager
import com.example.stealthbrowser.data.AppDatabase
import com.example.stealthbrowser.data.BookmarkEntity
import com.example.stealthbrowser.data.BookmarkRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder

class MainViewModel(application: Application) : AndroidViewModel(application) {

  private val proxyManager = ProxyManager(application.applicationContext)
  private val database = AppDatabase.getDatabase(application.applicationContext)
  private val bookmarkRepository = BookmarkRepository(database.bookmarkDao())

  val bookmarks: StateFlow<List<BookmarkEntity>> = bookmarkRepository.allBookmarks
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  private val _profiles = MutableStateFlow<List<BrowserProfile>>(emptyList())
  val profiles: StateFlow<List<BrowserProfile>> = _profiles.asStateFlow()

  private val _proxies = MutableStateFlow<List<ProxyConfig>>(emptyList())
  val proxies: StateFlow<List<ProxyConfig>> = _proxies.asStateFlow()

  private val _floatingWindows = MutableStateFlow<List<FloatingBrowserInstance>>(emptyList())
  val floatingWindows: StateFlow<List<FloatingBrowserInstance>> = _floatingWindows.asStateFlow()

  private val _selectedTab = MutableStateFlow(0)
  val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

  private val _currentLayoutMode = MutableStateFlow(WindowLayoutMode.SIDE_BY_SIDE)
  val currentLayoutMode: StateFlow<WindowLayoutMode> = _currentLayoutMode.asStateFlow()

  private val _selectedProfileIds = MutableStateFlow<Set<String>>(emptySet())
  val selectedProfileIds: StateFlow<Set<String>> = _selectedProfileIds.asStateFlow()

  private val _isSyncMode = MutableStateFlow(false)
  val isSyncMode: StateFlow<Boolean> = _isSyncMode.asStateFlow()

  private val _syncScriptAction = MutableSharedFlow<Pair<String, String>>(extraBufferCapacity = 20)
  val syncScriptAction: SharedFlow<Pair<String, String>> = _syncScriptAction.asSharedFlow()

  private var maxZIndex = 1f

  init {
    loadInitialData()
  }

  private fun loadInitialData() {
    val defaultProxies = listOf(
      ProxyConfig(
        name = "US Residential (Custom)",
        protocol = ProxyProtocol.SOCKS5,
        host = "104.28.19.45",
        port = 1080,
        countryCode = "US",
        city = "Los Angeles",
        rotationIntervalMinutes = 10,
        status = ProxyStatus.UNTESTED,
        latencyMs = 0,
        lastTestedIp = null
      ),
      ProxyConfig(
        name = "EU Mobile 5G (Custom)",
        protocol = ProxyProtocol.HTTP,
        host = "185.190.140.22",
        port = 8080,
        countryCode = "DE",
        city = "Frankfurt",
        rotationIntervalMinutes = 15,
        status = ProxyStatus.UNTESTED,
        latencyMs = 0,
        lastTestedIp = null
      ),
      ProxyConfig(
        name = "UK Clean IP (Custom)",
        protocol = ProxyProtocol.SOCKS5,
        host = "146.70.12.90",
        port = 1080,
        countryCode = "GB",
        city = "London",
        rotationIntervalMinutes = 0,
        status = ProxyStatus.UNTESTED,
        latencyMs = 0,
        lastTestedIp = null
      ),
      ProxyConfig(
        name = "SG Fast Node (Custom)",
        protocol = ProxyProtocol.SOCKS5,
        host = "103.110.155.12",
        port = 1080,
        countryCode = "SG",
        city = "Singapore",
        rotationIntervalMinutes = 30,
        status = ProxyStatus.UNTESTED,
        latencyMs = 0,
        lastTestedIp = null
      )
    )

    // Profiles default to direct network unless user links their verified working proxy
    val defaultProfiles = FingerprintPresets.getAllDefaultPresets().map { profile ->
      profile.copy(proxyId = null)
    }

    _proxies.value = defaultProxies
    _profiles.value = defaultProfiles
    _selectedProfileIds.value = setOfNotNull(defaultProfiles.firstOrNull()?.id)
  }

  fun setSelectedTab(tab: Int) {
    _selectedTab.value = tab
  }

  fun toggleProfileSelection(profileId: String) {
    _selectedProfileIds.update { set ->
      if (set.contains(profileId)) {
        if (set.size > 1) set - profileId else set
      } else {
        if (set.size < 4) set + profileId else set
      }
    }
  }

  fun selectAllProfiles(select: Boolean) {
    if (select) {
      _selectedProfileIds.value = _profiles.value.take(4).map { it.id }.toSet()
    } else {
      _selectedProfileIds.value = setOfNotNull(_profiles.value.firstOrNull()?.id)
    }
  }

  // Profile Management
  fun addProfile(profile: BrowserProfile) {
    _profiles.update { it + profile }
  }

  fun addProfiles(newProfiles: List<BrowserProfile>) {
    _profiles.update { it + newProfiles }
  }

  fun updateProfile(profile: BrowserProfile) {
    _profiles.update { list -> list.map { if (it.id == profile.id) profile else it } }
  }

  fun deleteProfile(profileId: String) {
    _profiles.update { list -> list.filter { it.id != profileId } }
    _selectedProfileIds.update { it - profileId }
    closeWindowsForProfile(profileId)
    viewModelScope.launch {
      bookmarkRepository.deleteBookmarksByProfileId(profileId)
    }
  }

  fun cloneProfile(profileId: String) {
    val original = _profiles.value.find { it.id == profileId } ?: return
    val cloned = original.copy(
      id = java.util.UUID.randomUUID().toString(),
      name = "${original.name} (Copy)",
      createdAt = System.currentTimeMillis()
    )
    _profiles.update { it + cloned }
  }

  fun exportSingleProfileJson(profileId: String): String {
    val p = _profiles.value.find { it.id == profileId } ?: return "{}"
    val obj = org.json.JSONObject().apply {
      put("id", p.id)
      put("name", p.name)
      put("deviceModel", p.deviceModel)
      put("androidVersion", p.androidVersion)
      put("chromeVersion", p.chromeVersion)
      put("userAgent", p.userAgent)
      put("secChUa", p.secChUa)
      put("screenResolution", p.screenResolution)
      put("pixelRatio", p.pixelRatio)
      put("hardwareConcurrency", p.hardwareConcurrency)
      put("deviceMemoryGb", p.deviceMemoryGb)
      put("webGlVendor", p.webGlVendor)
      put("webGlRenderer", p.webGlRenderer)
      put("canvasNoiseEnabled", p.canvasNoiseEnabled)
      put("audioNoiseEnabled", p.audioNoiseEnabled)
      put("webrtcPolicy", p.webrtcPolicy)
      put("proxyId", p.proxyId ?: org.json.JSONObject.NULL)
      put("startupUrl", p.startupUrl)
      put("userScript", p.userScript)
      put("adBlockEnabled", p.adBlockEnabled)
      put("clearDataOnClose", p.clearDataOnClose)
      put("savedCookies", p.savedCookies)
      put("colorHex", p.colorHex)
    }
    return obj.toString(2)
  }

  fun exportProfilesJson(): String {
    val arr = JSONArray()
    _profiles.value.forEach { p ->
      val obj = JSONObject().apply {
        put("id", p.id)
        put("name", p.name)
        put("deviceModel", p.deviceModel)
        put("androidVersion", p.androidVersion)
        put("chromeVersion", p.chromeVersion)
        put("userAgent", p.userAgent)
        put("secChUa", p.secChUa)
        put("screenResolution", p.screenResolution)
        put("pixelRatio", p.pixelRatio)
        put("hardwareConcurrency", p.hardwareConcurrency)
        put("deviceMemoryGb", p.deviceMemoryGb)
        put("webGlVendor", p.webGlVendor)
        put("webGlRenderer", p.webGlRenderer)
        put("canvasNoiseEnabled", p.canvasNoiseEnabled)
        put("audioNoiseEnabled", p.audioNoiseEnabled)
        put("webrtcPolicy", p.webrtcPolicy)
        put("proxyId", p.proxyId ?: JSONObject.NULL)
        put("startupUrl", p.startupUrl)
        put("userScript", p.userScript)
        put("adBlockEnabled", p.adBlockEnabled)
        put("clearDataOnClose", p.clearDataOnClose)
        put("savedCookies", p.savedCookies)
        put("colorHex", p.colorHex)
      }
      arr.put(obj)
    }
    return arr.toString(2)
  }

  fun importProfilesJson(jsonStr: String): Int {
    val trimmed = jsonStr.trim()
    if (trimmed.isEmpty()) return 0
    val arr = try {
      if (trimmed.startsWith("[")) JSONArray(trimmed)
      else JSONArray().put(JSONObject(trimmed))
    } catch (_: Exception) {
      return 0
    }

    val importedList = mutableListOf<BrowserProfile>()
    for (i in 0 until arr.length()) {
      val obj = arr.getJSONObject(i)
      val profile = BrowserProfile(
        id = java.util.UUID.randomUUID().toString(),
        name = obj.optString("name", "Imported Profile"),
        deviceModel = obj.optString("deviceModel", "SM-S928B"),
        androidVersion = obj.optString("androidVersion", "Android 14"),
        chromeVersion = obj.optString("chromeVersion", "Chrome 131.0.6778.85"),
        userAgent = obj.optString("userAgent", "Mozilla/5.0 (Linux; Android 14; SM-S928B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.6778.85 Mobile Safari/537.36"),
        secChUa = obj.optString("secChUa", "\"Google Chrome\";v=\"131\", \"Chromium\";v=\"131\", \"Not_A Brand\";v=\"24\""),
        screenResolution = obj.optString("screenResolution", "1080x2400"),
        pixelRatio = obj.optDouble("pixelRatio", 2.625).toFloat(),
        hardwareConcurrency = obj.optInt("hardwareConcurrency", 8),
        deviceMemoryGb = obj.optInt("deviceMemoryGb", 8),
        webGlVendor = obj.optString("webGlVendor", "Qualcomm"),
        webGlRenderer = obj.optString("webGlRenderer", "Adreno (TM) 750"),
        canvasNoiseEnabled = obj.optBoolean("canvasNoiseEnabled", true),
        audioNoiseEnabled = obj.optBoolean("audioNoiseEnabled", true),
        webrtcPolicy = obj.optString("webrtcPolicy", "BLOCK_LEAK"),
        proxyId = if (obj.isNull("proxyId")) null else obj.optString("proxyId"),
        startupUrl = obj.optString("startupUrl", "https://www.google.com"),
        userScript = obj.optString("userScript", ""),
        adBlockEnabled = obj.optBoolean("adBlockEnabled", true),
        clearDataOnClose = obj.optBoolean("clearDataOnClose", true),
        savedCookies = obj.optString("savedCookies", ""),
        colorHex = obj.optLong("colorHex", 0xFF00E5FF)
      )
      importedList.add(profile)
    }

    if (importedList.isNotEmpty()) {
      _profiles.update { it + importedList }
    }
    return importedList.size
  }

  // Synchronizer / Multi-Browser Mirror Actions
  fun toggleSyncMode() {
    _isSyncMode.update { !it }
  }

  fun broadcastSyncNavigation(sourceWindowId: String, url: String) {
    if (!_isSyncMode.value) return
    val resolved = resolveQueryOrUrl(url)
    _floatingWindows.update { list ->
      list.map { w ->
        if (w.windowId != sourceWindowId && !w.isMinimized) {
          w.copy(currentUrl = resolved, inputSearchQuery = resolved)
        } else w
      }
    }
  }

  fun broadcastSyncScript(sourceWindowId: String, jsScript: String) {
    if (!_isSyncMode.value) return
    viewModelScope.launch {
      _syncScriptAction.emit(sourceWindowId to jsScript)
    }
  }

  fun broadcastSyncScroll(sourceWindowId: String, scrollYPercent: Float) {
    if (!_isSyncMode.value) return
    val script = """
      (function() {
        window.__isMirroringScroll = true;
        var maxScroll = Math.max(document.body.scrollHeight, document.documentElement.scrollHeight) - window.innerHeight;
        if (maxScroll > 0) {
          window.scrollTo({ top: maxScroll * $scrollYPercent, behavior: 'auto' });
        }
        setTimeout(function() { window.__isMirroringScroll = false; }, 150);
      })();
    """.trimIndent()
    broadcastSyncScript(sourceWindowId, script)
  }

  fun broadcastSyncClick(sourceWindowId: String, xRatio: Float, yRatio: Float) {
    if (!_isSyncMode.value) return
    val script = """
      (function() {
        var x = window.innerWidth * $xRatio;
        var y = window.innerHeight * $yRatio;
        var el = document.elementFromPoint(x, y);
        if (el) {
          el.dispatchEvent(new MouseEvent('mousedown', { bubbles: true, cancelable: true, clientX: x, clientY: y }));
          el.dispatchEvent(new MouseEvent('mouseup', { bubbles: true, cancelable: true, clientX: x, clientY: y }));
          el.dispatchEvent(new MouseEvent('click', { bubbles: true, cancelable: true, clientX: x, clientY: y }));
          if (el.focus) el.focus();
        }
      })();
    """.trimIndent()
    broadcastSyncScript(sourceWindowId, script)
  }

  fun syncReloadAll() {
    val active = _floatingWindows.value.filter { !it.isMinimized }
    active.forEach { w ->
      broadcastSyncScript("", "window.location.reload();")
    }
  }

  fun updateProfileUserScript(profileId: String, script: String) {
    _profiles.update { list ->
      list.map { if (it.id == profileId) it.copy(userScript = script) else it }
    }
  }

  fun updateProfileCookies(profileId: String, cookies: String) {
    _profiles.update { list ->
      list.map { if (it.id == profileId) it.copy(savedCookies = cookies) else it }
    }
  }

  // Proxy Management
  fun addProxy(proxy: ProxyConfig) {
    _proxies.update { it + proxy }
  }

  fun addProxies(newProxies: List<ProxyConfig>) {
    _proxies.update { it + newProxies }
  }

  fun updateProxy(proxy: ProxyConfig) {
    _proxies.update { list -> list.map { if (it.id == proxy.id) proxy else it } }
  }

  fun deleteProxy(proxyId: String) {
    _proxies.update { list -> list.filter { it.id != proxyId } }
    // Unlink proxy from profiles
    _profiles.update { list ->
      list.map { if (it.proxyId == proxyId) it.copy(proxyId = null) else it }
    }
  }

  fun testProxyConnection(proxyId: String) {
    val proxy = _proxies.value.find { it.id == proxyId } ?: return
    _proxies.update { list ->
      list.map { if (it.id == proxyId) it.copy(status = ProxyStatus.TESTING) else it }
    }
    viewModelScope.launch {
      val result = proxyManager.testProxy(proxy)
      _proxies.update { list ->
        list.map {
          if (it.id == proxyId) {
            it.copy(
              status = if (result.success) ProxyStatus.ACTIVE else ProxyStatus.FAILED,
              latencyMs = result.latencyMs,
              lastTestedIp = result.detectedIp ?: it.host
            )
          } else it
        }
      }
    }
  }

  fun rotateProxyIp(proxyId: String) {
    _proxies.update { list ->
      list.map {
        if (it.id == proxyId) {
          // Simulate instant proxy IP rotation
          val octet4 = (10..240).random()
          val newHost = it.host.substringBeforeLast(".") + ".$octet4"
          it.copy(
            host = newHost,
            lastRotatedTimestamp = System.currentTimeMillis(),
            lastTestedIp = newHost
          )
        } else it
      }
    }
    testProxyConnection(proxyId)
  }

  // Floating Multi-Browser Logic with Auto-Layout Side-by-Side & Grid
  fun launchSingleBrowser(profile: BrowserProfile) {
    val existing = _floatingWindows.value.find { it.profile.id == profile.id }
    if (existing != null) {
      restoreWindow(existing.windowId)
      bringToFront(existing.windowId)
      return
    }

    maxZIndex += 1f
    val startUrl = profile.startupUrl.ifBlank { "https://www.google.com" }
    val linkedProxy = profile.proxyId?.let { id -> _proxies.value.find { it.id == id } }
    val newWindow = FloatingBrowserInstance(
      profile = profile,
      currentUrl = startUrl,
      inputSearchQuery = startUrl,
      normX = 0.05f,
      normY = 0.05f,
      normWidth = 0.90f,
      normHeight = 0.88f,
      zIndex = maxZIndex,
      linkedProxy = linkedProxy
    )
    _floatingWindows.update { it + newWindow }
    applyProxyForWindow(profile)
    autoTileAllWindows()
  }

  fun launchSelectedProfilesSideBySide() {
    val selectedProfiles = _profiles.value.filter { _selectedProfileIds.value.contains(it.id) }
    if (selectedProfiles.isEmpty()) return
    launchMultiBrowsers(selectedProfiles)
  }

  fun launchMultiBrowsers(profilesToLaunch: List<BrowserProfile>) {
    val count = profilesToLaunch.size.coerceIn(1, 4)
    val newWindows = mutableListOf<FloatingBrowserInstance>()

    profilesToLaunch.take(count).forEachIndexed { index, profile ->
      maxZIndex += 1f
      val coords = calculateTiledPosition(index, count)
      val startUrl = profile.startupUrl.ifBlank { "https://www.google.com" }
      val linkedProxy = profile.proxyId?.let { id -> _proxies.value.find { it.id == id } }
      val window = FloatingBrowserInstance(
        profile = profile,
        currentUrl = startUrl,
        inputSearchQuery = startUrl,
        normX = coords.x,
        normY = coords.y,
        normWidth = coords.width,
        normHeight = coords.height,
        zIndex = maxZIndex,
        linkedProxy = linkedProxy
      )
      newWindows.add(window)
    }

    _floatingWindows.value = newWindows

    // Apply proxy of the topmost window
    profilesToLaunch.firstOrNull()?.let { applyProxyForWindow(it) }
  }

  fun autoTileAllWindows() {
    val activeWindows = _floatingWindows.value.filter { !it.isMinimized }
    val count = activeWindows.size
    if (count == 0) return

    _floatingWindows.update { list ->
      var activeIdx = 0
      list.map { window ->
        if (window.isMinimized) {
          window
        } else {
          val coords = calculateTiledPosition(activeIdx, count)
          activeIdx++
          window.copy(
            normX = coords.x,
            normY = coords.y,
            normWidth = coords.width,
            normHeight = coords.height,
            isMaximized = false
          )
        }
      }
    }
  }

  private data class WindowCoordinates(
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float
  )

  private fun calculateTiledPosition(index: Int, totalCount: Int): WindowCoordinates {
    return when (totalCount) {
      1 -> WindowCoordinates(x = 0.03f, y = 0.04f, width = 0.94f, height = 0.88f)
      2 -> {
        // 2 Browsers side-by-side!
        if (index == 0) {
          WindowCoordinates(x = 0.015f, y = 0.04f, width = 0.475f, height = 0.88f)
        } else {
          WindowCoordinates(x = 0.505f, y = 0.04f, width = 0.475f, height = 0.88f)
        }
      }
      3 -> {
        // Top 2 side-by-side, bottom 1 centered
        when (index) {
          0 -> WindowCoordinates(x = 0.015f, y = 0.03f, width = 0.475f, height = 0.44f)
          1 -> WindowCoordinates(x = 0.505f, y = 0.03f, width = 0.475f, height = 0.44f)
          else -> WindowCoordinates(x = 0.15f, y = 0.49f, width = 0.70f, height = 0.44f)
        }
      }
      4 -> {
        // 2x2 Grid (side-by-side top, side-by-side bottom)
        when (index) {
          0 -> WindowCoordinates(x = 0.015f, y = 0.03f, width = 0.475f, height = 0.435f)
          1 -> WindowCoordinates(x = 0.505f, y = 0.03f, width = 0.475f, height = 0.435f)
          2 -> WindowCoordinates(x = 0.015f, y = 0.485f, width = 0.475f, height = 0.435f)
          else -> WindowCoordinates(x = 0.505f, y = 0.485f, width = 0.475f, height = 0.435f)
        }
      }
      else -> WindowCoordinates(x = 0.05f, y = 0.05f, width = 0.90f, height = 0.85f)
    }
  }

  fun updateWindowPosition(windowId: String, deltaNormX: Float, deltaNormY: Float) {
    _floatingWindows.update { list ->
      list.map { window ->
        if (window.windowId == windowId && !window.isMaximized) {
          val newX = (window.normX + deltaNormX).coerceIn(0f, 1f - window.normWidth)
          val newY = (window.normY + deltaNormY).coerceIn(0f, 0.9f - window.normHeight)
          window.copy(normX = newX, normY = newY)
        } else window
      }
    }
  }

  fun updateWindowSize(windowId: String, newNormWidth: Float, newNormHeight: Float) {
    _floatingWindows.update { list ->
      list.map { window ->
        if (window.windowId == windowId && !window.isMaximized) {
          window.copy(
            normWidth = newNormWidth.coerceIn(0.25f, 0.98f),
            normHeight = newNormHeight.coerceIn(0.20f, 0.95f)
          )
        } else window
      }
    }
  }

  fun resizeWindow(windowId: String, deltaNormWidth: Float, deltaNormHeight: Float) {
    _floatingWindows.update { list ->
      list.map { window ->
        if (window.windowId == windowId && !window.isMaximized) {
          val newW = (window.normWidth + deltaNormWidth).coerceIn(0.25f, 1f - window.normX)
          val newH = (window.normHeight + deltaNormHeight).coerceIn(0.20f, 0.92f - window.normY)
          window.copy(normWidth = newW, normHeight = newH)
        } else window
      }
    }
  }

  fun bringToFront(windowId: String) {
    maxZIndex += 1f
    _floatingWindows.update { list ->
      list.map { window ->
        if (window.windowId == windowId) {
          applyProxyForWindow(window.profile)
          window.copy(zIndex = maxZIndex)
        } else window
      }
    }
  }

  fun minimizeWindow(windowId: String) {
    _floatingWindows.update { list ->
      list.map { if (it.windowId == windowId) it.copy(isMinimized = true) else it }
    }
  }

  fun restoreWindow(windowId: String) {
    maxZIndex += 1f
    _floatingWindows.update { list ->
      list.map {
        if (it.windowId == windowId) {
          it.copy(isMinimized = false, zIndex = maxZIndex)
        } else it
      }
    }
  }

  fun toggleMaximizeWindow(windowId: String) {
    _floatingWindows.update { list ->
      list.map {
        if (it.windowId == windowId) {
          it.copy(isMaximized = !it.isMaximized)
        } else it
      }
    }
  }

  fun closeWindow(windowId: String) {
    _floatingWindows.update { list -> list.filter { it.windowId != windowId } }
    autoTileAllWindows()
  }

  fun closeAllWindows() {
    _floatingWindows.value = emptyList()
  }

  private fun closeWindowsForProfile(profileId: String) {
    _floatingWindows.update { list -> list.filter { it.profile.id != profileId } }
  }

  // Universal Search & URL Navigation Handler
  // User Requirement: "ebong browser jokhon open hobe tokhon tar search bar e je kono kichu jano search kora jai"
  fun navigateBrowser(windowId: String, queryOrUrl: String) {
    val trimmed = queryOrUrl.trim()
    if (trimmed.isEmpty()) return

    val targetUrl = resolveQueryOrUrl(trimmed)

    _floatingWindows.update { list ->
      list.map {
        if (it.windowId == windowId) {
          it.copy(
            currentUrl = targetUrl,
            inputSearchQuery = targetUrl,
            isLoading = true
          )
        } else it
      }
    }
  }

  fun setWindowQueryInput(windowId: String, query: String) {
    _floatingWindows.update { list ->
      list.map {
        if (it.windowId == windowId) {
          it.copy(inputSearchQuery = query)
        } else it
      }
    }
  }

  fun updateWindowNavState(
    windowId: String,
    url: String,
    title: String,
    canGoBack: Boolean,
    canGoForward: Boolean,
    isLoading: Boolean
  ) {
    _floatingWindows.update { list ->
      list.map {
        if (it.windowId == windowId) {
          it.copy(
            currentUrl = url,
            inputSearchQuery = url,
            title = title.ifBlank { it.profile.name },
            canGoBack = canGoBack,
            canGoForward = canGoForward,
            isLoading = isLoading
          )
        } else it
      }
    }
  }

  fun resolveQueryOrUrl(input: String): String {
    val trimmed = input.trim()
    // Check if it's already an explicit URL scheme
    if (trimmed.startsWith("http://", ignoreCase = true) || trimmed.startsWith("https://", ignoreCase = true)) {
      return trimmed
    }

    // Check if input looks like a valid hostname / domain (contains '.' without spaces, e.g. "whoer.net", "google.com", "pixelscan.net", "m.youtube.com")
    val isDomainLike = !trimmed.contains(" ") && trimmed.contains(".") && trimmed.length > 3 &&
      !trimmed.startsWith(".") && !trimmed.endsWith(".")

    return if (isDomainLike) {
      "https://$trimmed"
    } else {
      // Treat as Search Query - encode and search Google
      val encoded = try {
        URLEncoder.encode(trimmed, "UTF-8")
      } catch (e: Exception) {
        trimmed.replace(" ", "+")
      }
      "https://www.google.com/search?q=$encoded"
    }
  }

  private fun applyProxyForWindow(profile: BrowserProfile) {
    val proxy = profile.proxyId?.let { id -> _proxies.value.find { it.id == id } }
    proxyManager.applyProxyToWebView(proxy)
  }

  // Room Database Bookmark Management
  fun saveBookmark(title: String, url: String, profileId: String? = null, onSaved: ((Long) -> Unit)? = null) {
    viewModelScope.launch {
      val id = bookmarkRepository.addBookmark(title, url, profileId)
      onSaved?.invoke(id)
    }
  }

  fun deleteBookmark(id: Long) {
    viewModelScope.launch {
      bookmarkRepository.deleteBookmarkById(id)
    }
  }

  fun deleteBookmarkByUrl(url: String) {
    viewModelScope.launch {
      bookmarkRepository.deleteBookmarkByUrl(url)
    }
  }

  // --- DATA MANAGEMENT & ZERO-TRACE PERMANENT PURGE ---

  /**
   * Permanently deletes multiple profiles at once (Select All or selected subset)
   * along with their associated bookmarks in Room database, session cookies, and closes active windows.
   */
  fun permanentlyDeleteProfiles(
    profileIds: Set<String>,
    deleteBookmarks: Boolean = true,
    wipeCookies: Boolean = true,
    onComplete: ((Int) -> Unit)? = null
  ) {
    if (profileIds.isEmpty()) {
      onComplete?.invoke(0)
      return
    }

    val count = profileIds.size
    // 1. Close all active floating browser windows associated with these profiles
    profileIds.forEach { closeWindowsForProfile(it) }

    // 2. Remove profiles from list
    _profiles.update { list -> list.filter { it.id !in profileIds } }
    _selectedProfileIds.update { it - profileIds }

    // 3. Remove associated bookmarks from Room Database
    viewModelScope.launch {
      if (deleteBookmarks) {
        try {
          bookmarkRepository.deleteBookmarksByProfileIds(profileIds.toList())
        } catch (_: Exception) {}
      }

      // 4. Flush cookie manager and web storage
      if (wipeCookies) {
        try {
          CookieManager.getInstance().flush()
          WebStorage.getInstance().deleteAllData()
        } catch (_: Exception) {}
      }

      onComplete?.invoke(count)
    }
  }

  /**
   * Cleans orphaned data: deletes any bookmarks in Room DB that belonged to profiles
   * that were previously deleted or no longer exist, and cleans lingering WebStorage.
   */
  fun cleanOrphanedData(onComplete: ((Int) -> Unit)? = null) {
    viewModelScope.launch {
      val validProfileIds = _profiles.value.map { it.id }
      val deletedBookmarksCount = try {
        bookmarkRepository.deleteOrphanedBookmarks(validProfileIds)
      } catch (_: Exception) {
        0
      }

      try {
        CookieManager.getInstance().flush()
        WebStorage.getInstance().deleteAllData()
      } catch (_: Exception) {}

      onComplete?.invoke(deletedBookmarksCount)
    }
  }

  /**
   * Wipes saved cookies for a specific profile.
   */
  fun clearProfileCookies(profileId: String) {
    _profiles.update { list ->
      list.map { if (it.id == profileId) it.copy(savedCookies = "") else it }
    }
  }

  /**
   * Wipes saved cookies for all profiles.
   */
  fun clearAllProfileCookies() {
    _profiles.update { list ->
      list.map { it.copy(savedCookies = "") }
    }
  }

  /**
   * Deep Clean All Browser Data:
   * Wipes all WebView disk/memory cache, cookies, WebStorage databases, and optionally bookmarks.
   */
  fun deepCleanAllBrowserData(context: Context, wipeBookmarks: Boolean = false, onComplete: (() -> Unit)? = null) {
    viewModelScope.launch {
      try {
        CookieManager.getInstance().removeAllCookies(null)
        CookieManager.getInstance().flush()
        WebStorage.getInstance().deleteAllData()
      } catch (_: Exception) {}

      try {
        context.cacheDir?.deleteRecursively()
      } catch (_: Exception) {}

      if (wipeBookmarks) {
        try {
          bookmarkRepository.deleteAllBookmarks()
        } catch (_: Exception) {}
      }

      // Empty cookies from profiles
      _profiles.update { list -> list.map { it.copy(savedCookies = "") } }

      onComplete?.invoke()
    }
  }

  /**
   * Factory Reset: Restores app to a fresh initial state.
   */
  fun factoryResetAll(context: Context, onComplete: (() -> Unit)? = null) {
    viewModelScope.launch {
      closeAllWindows()
      _selectedProfileIds.value = emptySet()

      try {
        bookmarkRepository.deleteAllBookmarks()
      } catch (_: Exception) {}

      try {
        CookieManager.getInstance().removeAllCookies(null)
        CookieManager.getInstance().flush()
        WebStorage.getInstance().deleteAllData()
        context.cacheDir?.deleteRecursively()
      } catch (_: Exception) {}

      // Re-initialize default clean profiles
      _profiles.value = FingerprintPresets.getAllDefaultPresets().map { it.copy(proxyId = null) }

      onComplete?.invoke()
    }
  }
}
