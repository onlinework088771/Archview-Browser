package com.example.stealthbrowser.ui.components

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CropSquare
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Minimize
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.stealthbrowser.model.FloatingBrowserInstance
import com.example.stealthbrowser.stealth.StealthScriptGenerator
import com.example.stealthbrowser.ui.theme.CyanAccent
import com.example.stealthbrowser.ui.theme.DarkBg
import com.example.stealthbrowser.ui.theme.DarkBorder
import com.example.stealthbrowser.ui.theme.DarkSurface
import com.example.stealthbrowser.ui.theme.DarkSurfaceElevated
import com.example.stealthbrowser.ui.theme.ElectricBlue
import com.example.stealthbrowser.ui.theme.EmeraldConnected
import com.example.stealthbrowser.ui.theme.TextMuted
import com.example.stealthbrowser.ui.theme.TextPrimary
import com.example.stealthbrowser.ui.theme.TextSecondary

import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.runtime.LaunchedEffect
import android.webkit.GeolocationPermissions
import android.webkit.JavascriptInterface
import android.webkit.WebResourceResponse
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewCompat
import androidx.webkit.WebViewFeature
import com.example.stealthbrowser.data.BookmarkEntity
import com.example.stealthbrowser.stealth.AdBlockEngine
import com.example.stealthbrowser.stealth.CookieHelper
import com.example.stealthbrowser.stealth.GeoTimezoneResolver
import kotlinx.coroutines.flow.SharedFlow
import org.json.JSONObject

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun FloatingBrowserWindow(
  window: FloatingBrowserInstance,
  isFrontmost: Boolean = true,
  savedBookmarks: List<BookmarkEntity> = emptyList(),
  isSyncMode: Boolean = false,
  syncScriptAction: SharedFlow<Pair<String, String>>? = null,
  onSyncScroll: (Float) -> Unit = {},
  onSyncClick: (Float, Float) -> Unit = { _, _ -> },
  onSyncNavigate: (String) -> Unit = {},
  onSaveCookiesToProfile: ((String, String) -> Unit)? = null,
  onSaveBookmark: (title: String, url: String, profileId: String?) -> Unit = { _, _, _ -> },
  onDeleteBookmark: (id: Long) -> Unit = {},
  onDrag: (Float, Float) -> Unit,
  onResize: ((Float, Float) -> Unit)? = null,
  onFocus: () -> Unit,
  onNavigate: (String) -> Unit,
  onQueryChange: (String) -> Unit,
  onNavStateUpdate: (url: String, title: String, canBack: Boolean, canForward: Boolean, loading: Boolean) -> Unit,
  onAutoTile: () -> Unit,
  onMinimize: () -> Unit,
  onToggleMaximize: () -> Unit,
  onClose: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val clipboardManager = LocalClipboardManager.current
  val focusManager = LocalFocusManager.current
  var webViewRef by remember { mutableStateOf<WebView?>(null) }
  val accentColor = Color(window.profile.colorHex)
  var showBookmarksModal by remember { mutableStateOf(false) }
  var showCookieModal by remember { mutableStateOf(false) }
  var inPageCookieInput by remember { mutableStateOf("") }
  var inPageCookieFormatNetscape by remember { mutableStateOf(false) }
  var bookmarkSearchQuery by remember { mutableStateOf("") }

  val isCurrentUrlBookmarked = remember(savedBookmarks, window.currentUrl) {
    savedBookmarks.any {
      it.url.equals(window.currentUrl, ignoreCase = true) ||
      it.url.trimEnd('/').equals(window.currentUrl.trimEnd('/'), ignoreCase = true)
    }
  }
  val matchingBookmark = remember(savedBookmarks, window.currentUrl) {
    savedBookmarks.find {
      it.url.equals(window.currentUrl, ignoreCase = true) ||
      it.url.trimEnd('/').equals(window.currentUrl.trimEnd('/'), ignoreCase = true)
    }
  }

  var showStealthShieldDialog by remember { mutableStateOf(false) }

  val stealthScript = remember(window.profile, window.linkedProxy) {
    StealthScriptGenerator.buildStealthScript(window.profile, window.linkedProxy)
  }

  // Safe close handler that cleans cache, history, and storage if profile.clearDataOnClose is true
  val handleCloseWindow: () -> Unit = {
    if (window.profile.clearDataOnClose) {
      try {
        webViewRef?.apply {
          stopLoading()
          clearHistory()
          clearCache(true)
          clearFormData()
          clearSslPreferences()
          android.webkit.WebStorage.getInstance().deleteAllData()
        }
        android.webkit.CookieManager.getInstance().flush()
      } catch (_: Exception) {}
    }
    onClose()
  }

  // Synchronizer Mirror action receiver
  LaunchedEffect(syncScriptAction) {
    syncScriptAction?.collect { (sourceId, script) ->
      if (sourceId != window.windowId && !window.isMinimized) {
        webViewRef?.evaluateJavascript(script, null)
      }
    }
  }

  // Intercept Android system back gesture (swipe back) and hardware back button
  // When floating browser is open and user swipes back, it navigates back in browser history
  BackHandler(enabled = isFrontmost && !window.isMinimized) {
    val canBack = (webViewRef?.canGoBack() == true) || window.canGoBack
    if (canBack) {
      webViewRef?.goBack()
    } else {
      // If browser history has no further back entries, close the floating window cleanly
      handleCloseWindow()
    }
  }

  fun performNavigate(rawInput: String) {
    val trimmed = rawInput.trim()
    if (trimmed.isEmpty()) return

    val targetUrl = if (trimmed.startsWith("http://", ignoreCase = true) || trimmed.startsWith("https://", ignoreCase = true)) {
      trimmed
    } else if (!trimmed.contains(" ") && trimmed.contains(".") && trimmed.length > 3 && !trimmed.startsWith(".")) {
      "https://$trimmed"
    } else {
      val encoded = try {
        java.net.URLEncoder.encode(trimmed, "UTF-8")
      } catch (_: Exception) {
        trimmed.replace(" ", "+")
      }
      "https://www.google.com/search?q=$encoded"
    }

    focusManager.clearFocus()
    onQueryChange(targetUrl)
    onNavigate(targetUrl)
    try {
      webViewRef?.loadUrl(targetUrl)
    } catch (_: Exception) {}
    if (isSyncMode) {
      onSyncNavigate(targetUrl)
    }
  }

  fun navigateWithSync(targetUrl: String) {
    performNavigate(targetUrl)
  }

  // Ensure whenever window.currentUrl changes, WebView loads it immediately
  LaunchedEffect(window.currentUrl) {
    val target = window.currentUrl.trim()
    if (target.isNotBlank() && webViewRef?.url != target) {
      try {
        webViewRef?.loadUrl(target)
      } catch (_: Exception) {}
    }
  }

  // Helper to read Android system clipboard text reliably
  fun getSystemClipboardText(): String? {
    val clip = clipboardManager.getText()?.text
    if (!clip.isNullOrBlank()) return clip.trim()
    return try {
      val sysClip = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
      val item = sysClip?.primaryClip?.getItemAt(0)
      val text = item?.coerceToText(context)?.toString()
      if (!text.isNullOrBlank()) text.trim() else null
    } catch (_: Exception) {
      null
    }
  }

  Card(
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = DarkSurface),
    border = BorderStroke(1.5.dp, if (window.isMaximized) CyanAccent else accentColor.copy(alpha = 0.8f)),
    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
    modifier = modifier
      .testTag("floating_window_${window.windowId}")
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .background(DarkBg)
    ) {
      // 1. Sleek Draggable Window Title Bar (Strict 36dp height to prevent vertical stretching)
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .height(36.dp)
          .background(DarkSurfaceElevated)
          .pointerInput(Unit) {
            detectDragGestures(
              onDragStart = { onFocus() },
              onDrag = { change, dragAmount ->
                change.consume()
                onDrag(dragAmount.x, dragAmount.y)
              }
            )
          }
          .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        // Window Title & Device Profile Badge
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.weight(1f)
        ) {
          Box(
            modifier = Modifier
              .size(8.dp)
              .clip(CircleShape)
              .background(EmeraldConnected)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = window.profile.name,
            style = MaterialTheme.typography.titleSmall.copy(
              fontWeight = FontWeight.Bold,
              fontSize = 11.sp
            ),
            color = TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )

          // Secondary badges shown only when window is wide enough
          if (window.normWidth >= 0.45f || window.isMaximized) {
            Spacer(modifier = Modifier.width(6.dp))
            // 6-Layer Security Shield Badge
            Surface(
              shape = RoundedCornerShape(4.dp),
              color = EmeraldConnected.copy(alpha = 0.15f),
              border = BorderStroke(1.dp, EmeraldConnected.copy(alpha = 0.4f)),
              modifier = Modifier.clickable { showStealthShieldDialog = true }
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.Security,
                  contentDescription = "Security Shield",
                  tint = EmeraldConnected,
                  modifier = Modifier.size(9.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                  text = "Shielded",
                  fontSize = 8.sp,
                  fontWeight = FontWeight.Bold,
                  color = EmeraldConnected
                )
              }
            }

            // AdBlock badge
            if (window.profile.adBlockEnabled) {
              Spacer(modifier = Modifier.width(4.dp))
              Surface(
                shape = RoundedCornerShape(4.dp),
                color = EmeraldConnected.copy(alpha = 0.15f),
                border = BorderStroke(0.8.dp, EmeraldConnected.copy(alpha = 0.4f))
              ) {
                Text(
                  text = "🛡️ AdBlock",
                  fontSize = 8.sp,
                  fontWeight = FontWeight.Bold,
                  color = EmeraldConnected,
                  modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )
              }
            }

            // Cookie Manager quick-access button
            Spacer(modifier = Modifier.width(4.dp))
            Surface(
              shape = RoundedCornerShape(4.dp),
              color = Color(0xFFFFD700).copy(alpha = 0.15f),
              border = BorderStroke(0.8.dp, Color(0xFFFFD700).copy(alpha = 0.4f)),
              modifier = Modifier.clickable { showCookieModal = true }
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
              ) {
                Icon(Icons.Default.Key, contentDescription = "Cookies", tint = Color(0xFFFFD700), modifier = Modifier.size(9.dp))
                Spacer(modifier = Modifier.width(2.dp))
                Text("Cookies", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFD700))
              }
            }

            // Synchronizer Mirror Badge
            if (isSyncMode) {
              Spacer(modifier = Modifier.width(4.dp))
              Surface(
                shape = RoundedCornerShape(4.dp),
                color = EmeraldConnected.copy(alpha = 0.2f),
                border = BorderStroke(0.8.dp, EmeraldConnected)
              ) {
                Text(
                  text = "🔗 Mirror",
                  fontSize = 8.sp,
                  fontWeight = FontWeight.Bold,
                  color = EmeraldConnected,
                  modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )
              }
            }
          }
        }

        // Window Controls (Auto-Tile, Minimize, Maximize, Close)
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(1.dp)
        ) {
          // Auto-tile button
          IconButton(
            onClick = onAutoTile,
            modifier = Modifier.size(24.dp)
          ) {
            Icon(
              imageVector = Icons.Default.GridView,
              contentDescription = "Tile Side by Side",
              tint = CyanAccent,
              modifier = Modifier.size(13.dp)
            )
          }

          // Minimize
          IconButton(
            onClick = onMinimize,
            modifier = Modifier.size(24.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Minimize,
              contentDescription = "Minimize",
              tint = TextSecondary,
              modifier = Modifier.size(12.dp)
            )
          }

          // Maximize / Restore
          IconButton(
            onClick = onToggleMaximize,
            modifier = Modifier.size(24.dp)
          ) {
            Icon(
              imageVector = Icons.Default.CropSquare,
              contentDescription = "Maximize",
              tint = TextSecondary,
              modifier = Modifier.size(12.dp)
            )
          }

          // Close (with auto-clear cache & storage)
          IconButton(
            onClick = handleCloseWindow,
            modifier = Modifier.size(24.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Close",
              tint = TextMuted,
              modifier = Modifier.size(13.dp)
            )
          }
        }
      }

      // 2. Universal Search & URL Bar
      // User Requirement: "ebong browser jokhon open hobe tokhon tar search bar e je kono kichu jano search kora jai"
      Surface(
        color = DarkSurface,
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp, vertical = 4.dp)
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .height(32.dp)
              .clip(RoundedCornerShape(8.dp))
              .background(DarkBg)
              .padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            // Back button
            IconButton(
              onClick = { webViewRef?.goBack() },
              enabled = window.canGoBack,
              modifier = Modifier.size(22.dp)
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = if (window.canGoBack) TextPrimary else TextMuted,
                modifier = Modifier.size(13.dp)
              )
            }

            // Forward button
            IconButton(
              onClick = { webViewRef?.goForward() },
              enabled = window.canGoForward,
              modifier = Modifier.size(22.dp)
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Forward",
                tint = if (window.canGoForward) TextPrimary else TextMuted,
                modifier = Modifier.size(13.dp)
              )
            }

            // Reload / Go
            IconButton(
              onClick = { webViewRef?.reload() },
              modifier = Modifier.size(22.dp)
            ) {
              Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Reload",
                tint = CyanAccent,
                modifier = Modifier.size(13.dp)
              )
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Search Icon
            Icon(
              imageVector = Icons.Default.Search,
              contentDescription = null,
              tint = TextMuted,
              modifier = Modifier.size(13.dp)
            )

            Spacer(modifier = Modifier.width(4.dp))

            // Editable Search / URL input (Accepts ANY query or URL!)
            BasicTextField(
              value = window.inputSearchQuery,
              onValueChange = onQueryChange,
              singleLine = true,
              textStyle = TextStyle(
                color = TextPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
              ),
              cursorBrush = SolidColor(CyanAccent),
              keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Search
              ),
              keyboardActions = KeyboardActions(
                onSearch = {
                  focusManager.clearFocus()
                  performNavigate(window.inputSearchQuery)
                }
              ),
              modifier = Modifier
                .weight(1f)
                .testTag("search_input_${window.windowId}"),
              decorationBox = { innerTextField ->
                if (window.inputSearchQuery.isEmpty()) {
                  Text(
                    text = "Search or type URL...",
                    fontSize = 11.sp,
                    color = TextMuted
                  )
                }
                innerTextField()
              }
            )

            // Quick Paste button in search bar
            IconButton(
              onClick = {
                val clip = getSystemClipboardText()
                if (!clip.isNullOrBlank()) {
                  performNavigate(clip)
                  Toast.makeText(context, "Pasted URL & loading...", Toast.LENGTH_SHORT).show()
                } else {
                  Toast.makeText(context, "Clipboard is empty! Copy text first", Toast.LENGTH_SHORT).show()
                }
              },
              modifier = Modifier
                .size(22.dp)
                .testTag("search_paste_${window.windowId}")
            ) {
              Icon(
                imageVector = Icons.Default.ContentPaste,
                contentDescription = "Paste from clipboard",
                tint = CyanAccent,
                modifier = Modifier.size(13.dp)
              )
            }

            // Save / Bookmark current URL button
            IconButton(
              onClick = {
                if (isCurrentUrlBookmarked && matchingBookmark != null) {
                  onDeleteBookmark(matchingBookmark.id)
                  Toast.makeText(context, "Removed from Bookmarks", Toast.LENGTH_SHORT).show()
                } else {
                  val saveTitle = window.title.ifBlank {
                    try {
                      android.net.Uri.parse(window.currentUrl).host ?: window.currentUrl
                    } catch (_: Exception) {
                      window.currentUrl
                    }
                  }
                  onSaveBookmark(saveTitle, window.currentUrl, window.profile.id)
                  Toast.makeText(context, "⭐ Saved to Room Bookmarks!", Toast.LENGTH_SHORT).show()
                }
              },
              modifier = Modifier
                .size(22.dp)
                .testTag("search_bookmark_${window.windowId}")
            ) {
              Icon(
                imageVector = if (isCurrentUrlBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                contentDescription = if (isCurrentUrlBookmarked) "Remove Bookmark" else "Save to Bookmarks",
                tint = if (isCurrentUrlBookmarked) Color(0xFFFFD700) else CyanAccent,
                modifier = Modifier.size(14.dp)
              )
            }

            // Clear or Go button
            if (window.inputSearchQuery.isNotEmpty()) {
              IconButton(
                onClick = {
                  focusManager.clearFocus()
                  performNavigate(window.inputSearchQuery)
                },
                modifier = Modifier.size(22.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.Search,
                  contentDescription = "Search",
                  tint = CyanAccent,
                  modifier = Modifier.size(14.dp)
                )
              }
            }
          }

          // Quick Bookmark Test Chips & Fast Paste & Go (Dynamic Room DB backed)
          // Automatically hides when window is resized small to prevent giant empty top space!
          if ((window.normHeight >= 0.38f && window.normWidth >= 0.38f) || window.isMaximized) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(top = 2.dp),
              horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              // One-tap Paste & Go chip
              Surface(
                shape = RoundedCornerShape(4.dp),
                color = CyanAccent.copy(alpha = 0.2f),
                border = BorderStroke(1.dp, CyanAccent.copy(alpha = 0.5f)),
                onClick = {
                  val clip = getSystemClipboardText()
                  if (!clip.isNullOrBlank()) {
                    performNavigate(clip)
                    Toast.makeText(context, "Pasted & loading...", Toast.LENGTH_SHORT).show()
                  } else {
                    Toast.makeText(context, "Clipboard is empty! Copy URL first", Toast.LENGTH_SHORT).show()
                  }
                }
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                  Icon(
                    imageVector = Icons.Default.ContentPaste,
                    contentDescription = null,
                    tint = CyanAccent,
                    modifier = Modifier.size(10.dp)
                  )
                  Spacer(modifier = Modifier.width(3.dp))
                  Text(
                    text = "Paste & Go",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyanAccent
                  )
                }
              }

              // Bookmarks Drawer/Modal Chip
              Surface(
                shape = RoundedCornerShape(4.dp),
                color = Color(0xFFFFD700).copy(alpha = 0.15f),
                border = BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.5f)),
                onClick = { showBookmarksModal = true },
                modifier = Modifier.testTag("chip_bookmarks_${window.windowId}")
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                  Icon(
                    imageVector = Icons.Default.Bookmarks,
                    contentDescription = null,
                    tint = Color(0xFFFFD700),
                    modifier = Modifier.size(10.dp)
                  )
                  Spacer(modifier = Modifier.width(3.dp))
                  Text(
                    text = "⭐ Bookmarks (${savedBookmarks.size})",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFD700)
                  )
                }
              }

              // Room Database Bookmarks Chips
              val bookmarksToShow = savedBookmarks.filter { it.profileId == null || it.profileId == window.profile.id }
                .ifEmpty { savedBookmarks }

              bookmarksToShow.take(8).forEach { bm ->
                val isCurrent = window.currentUrl.equals(bm.url, ignoreCase = true)
                Surface(
                  shape = RoundedCornerShape(4.dp),
                  color = if (isCurrent) CyanAccent.copy(alpha = 0.25f) else DarkSurfaceElevated,
                  border = if (isCurrent) BorderStroke(1.dp, CyanAccent) else null,
                  onClick = {
                    performNavigate(bm.url)
                  }
                ) {
                  Text(
                    text = bm.title.take(15),
                    fontSize = 9.sp,
                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.SemiBold,
                    color = if (isCurrent) CyanAccent else TextSecondary,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                  )
                }
              }
            }
          }
        }
      }

      // Loading Progress Indicator
      if (window.isLoading) {
        LinearProgressIndicator(
          color = CyanAccent,
          trackColor = DarkSurfaceElevated,
          modifier = Modifier
            .fillMaxWidth()
            .height(2.dp)
        )
      }

      // 3. Android WebView with injected Anti-Detect Stealth Engine
      // Uses weight(1f) to ensure the webview expands dynamically and fills all remaining window height!
      Box(
        modifier = Modifier
          .weight(1f)
          .fillMaxWidth()
          .background(Color.White)
      ) {
        AndroidView(
          factory = { ctx ->
            WebView(ctx).apply {
              layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
              )
              isFocusable = true
              isFocusableInTouchMode = true
              isLongClickable = true
              isClickable = true
              setOnTouchListener { v, _ ->
                if (!v.hasFocus()) {
                  v.requestFocus()
                }
                false
              }

              settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                databaseEnabled = true
                useWideViewPort = true
                loadWithOverviewMode = true
                setSupportZoom(true)
                builtInZoomControls = true
                displayZoomControls = false
                cacheMode = WebSettings.LOAD_DEFAULT
                userAgentString = window.profile.userAgent
                javaScriptCanOpenWindowsAutomatically = true
                allowFileAccess = true
                allowContentAccess = true

                // Layer 5: Strip X-Requested-With header completely across all domains to remove Android WebView fingerprint
                if (WebViewFeature.isFeatureSupported(WebViewFeature.REQUESTED_WITH_HEADER_ALLOW_LIST)) {
                  try {
                    WebSettingsCompat.setRequestedWithHeaderOriginAllowList(this, emptySet())
                  } catch (_: Exception) {}
                }
              }

              // Pre-inject saved cookies for this profile before initial page load
              if (window.profile.savedCookies.isNotBlank()) {
                try {
                  CookieHelper.importCookies(window.currentUrl, window.profile.savedCookies)
                } catch (_: Exception) {}
              }

              // Synchronizer mirror bridge for scrolling and clicking
              val syncBridge = object {
                @JavascriptInterface
                fun onScroll(percent: Float) {
                  if (isSyncMode) {
                    onSyncScroll(percent)
                  }
                }

                @JavascriptInterface
                fun onClick(xRatio: Float, yRatio: Float) {
                  if (isSyncMode) {
                    onSyncClick(xRatio, yRatio)
                  }
                }
              }
              addJavascriptInterface(syncBridge, "StealthBridge")

              webViewClient = object : WebViewClient() {
                // AdBlock & Anti-Tracker Request Interceptor
                override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
                  if (window.profile.adBlockEnabled) {
                    val reqUrl = request?.url?.toString()
                    if (AdBlockEngine.shouldBlock(reqUrl)) {
                      return AdBlockEngine.createEmptyResponse()
                    }
                  }
                  return super.shouldInterceptRequest(view, request)
                }

                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                  super.onPageStarted(view, url, favicon)
                  view?.evaluateJavascript(stealthScript, null)
                  onNavStateUpdate(
                    url ?: "",
                    view?.title ?: "",
                    view?.canGoBack() ?: false,
                    view?.canGoForward() ?: false,
                    true
                  )
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                  super.onPageFinished(view, url)
                  view?.evaluateJavascript(stealthScript, null)

                  // Feature 2: Custom UserScript Injection
                  if (window.profile.userScript.isNotBlank()) {
                    val wrappedScript = """
                      (function() {
                        try {
                          ${window.profile.userScript}
                        } catch(e) {
                          console.error('[UserScript Error]', e);
                        }
                      })();
                    """.trimIndent()
                    view?.evaluateJavascript(wrappedScript, null)
                  }

                  // Feature 4: Synchronizer Mirror Event Listeners
                  val syncListenerScript = """
                    (function() {
                      if (window.__stealthSyncAttached) return;
                      window.__stealthSyncAttached = true;

                      window.addEventListener('scroll', function() {
                        if (window.__isMirroringScroll) return;
                        var max = Math.max(document.body.scrollHeight, document.documentElement.scrollHeight) - window.innerHeight;
                        var pct = max > 0 ? (window.scrollY / max) : 0;
                        if (window.StealthBridge && window.StealthBridge.onScroll) {
                          window.StealthBridge.onScroll(pct);
                        }
                      }, { passive: true });

                      document.addEventListener('click', function(e) {
                        if (window.StealthBridge && window.StealthBridge.onClick) {
                          var xr = e.clientX / window.innerWidth;
                          var yr = e.clientY / window.innerHeight;
                          window.StealthBridge.onClick(xr, yr);
                        }
                      }, true);
                    })();
                  """.trimIndent()
                  view?.evaluateJavascript(syncListenerScript, null)

                  onNavStateUpdate(
                    url ?: "",
                    view?.title ?: "",
                    view?.canGoBack() ?: false,
                    view?.canGoForward() ?: false,
                    false
                  )
                }

                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                  return false
                }
              }

              webChromeClient = object : WebChromeClient() {
                override fun onCreateWindow(
                  view: WebView?,
                  isDialog: Boolean,
                  isUserGesture: Boolean,
                  resultMsg: android.os.Message?
                ): Boolean {
                  if (window.profile.adBlockEnabled && !isUserGesture) {
                    return false // Block automated popups
                  }
                  return super.onCreateWindow(view, isDialog, isUserGesture, resultMsg)
                }

                override fun onReceivedTitle(view: WebView?, title: String?) {
                  super.onReceivedTitle(view, title)
                  onNavStateUpdate(
                    view?.url ?: "",
                    title ?: "",
                    view?.canGoBack() ?: false,
                    view?.canGoForward() ?: false,
                    false
                  )
                }

                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                  super.onProgressChanged(view, newProgress)
                  if (newProgress < 100) {
                    view?.evaluateJavascript(stealthScript, null)
                  }
                }

                override fun onGeolocationPermissionsShowPrompt(
                  origin: String?,
                  callback: GeolocationPermissions.Callback?
                ) {
                  callback?.invoke(origin, true, false)
                }
              }

              webViewRef = this

              // Pre-inject 7-layer stealth engine script at document start before web page DOM initializes
              if (WebViewFeature.isFeatureSupported(WebViewFeature.DOCUMENT_START_SCRIPT)) {
                try {
                  WebViewCompat.addDocumentStartJavaScript(this, stealthScript, setOf("*"))
                } catch (_: Exception) {}
              }

              loadUrl(window.currentUrl)
            }
          },
          update = { view ->
            webViewRef = view
            val target = window.currentUrl.trim()
            if (target.isNotBlank() && view.url != target) {
              view.loadUrl(target)
            }
          },
          modifier = Modifier.fillMaxSize()
        )

        // Floating Navigation Bar (Only displayed when window has enough space so it never blocks small windows)
        if ((window.normHeight >= 0.44f && window.normWidth >= 0.46f) || window.isMaximized) {
          Surface(
            shape = RoundedCornerShape(22.dp),
            color = DarkSurfaceElevated.copy(alpha = 0.94f),
            border = BorderStroke(1.dp, CyanAccent.copy(alpha = 0.5f)),
            shadowElevation = 8.dp,
            modifier = Modifier
              .align(Alignment.BottomCenter)
              .padding(bottom = 10.dp)
              .testTag("floating_nav_bar_${window.windowId}")
          ) {
          Row(
            modifier = Modifier
              .padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            // 1. Back button
            IconButton(
              onClick = { webViewRef?.goBack() },
              enabled = window.canGoBack,
              modifier = Modifier
                .size(36.dp)
                .testTag("floating_nav_back_${window.windowId}")
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = if (window.canGoBack) CyanAccent else TextMuted,
                modifier = Modifier.size(18.dp)
              )
            }

            // 2. Forward button
            IconButton(
              onClick = { webViewRef?.goForward() },
              enabled = window.canGoForward,
              modifier = Modifier
                .size(36.dp)
                .testTag("floating_nav_forward_${window.windowId}")
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Forward",
                tint = if (window.canGoForward) CyanAccent else TextMuted,
                modifier = Modifier.size(18.dp)
              )
            }

            // 3. Refresh button
            IconButton(
              onClick = { webViewRef?.reload() },
              modifier = Modifier
                .size(36.dp)
                .testTag("floating_nav_refresh_${window.windowId}")
            ) {
              Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Refresh",
                tint = CyanAccent,
                modifier = Modifier.size(18.dp)
              )
            }

            // 4. Smart Paste button
            IconButton(
              onClick = {
                val clipText = getSystemClipboardText()
                if (!clipText.isNullOrBlank()) {
                  val escaped = JSONObject.quote(clipText)
                  val jsPasteScript = """
                    (function() {
                      var el = document.activeElement;
                      if (el && (el.tagName === 'INPUT' || el.tagName === 'TEXTAREA' || el.isContentEditable)) {
                        var start = el.selectionStart || 0;
                        var end = el.selectionEnd || 0;
                        var val = el.value || '';
                        if (typeof el.value !== 'undefined') {
                          el.value = val.substring(0, start) + $escaped + val.substring(end);
                          el.selectionStart = el.selectionEnd = start + $escaped.length;
                        } else if (el.isContentEditable) {
                          document.execCommand('insertText', false, $escaped);
                        }
                        el.dispatchEvent(new Event('input', { bubbles: true }));
                        el.dispatchEvent(new Event('change', { bubbles: true }));
                        return 'pasted_into_input';
                      }
                      return 'no_input_focused';
                    })();
                  """.trimIndent()
                  webViewRef?.evaluateJavascript(jsPasteScript) { result ->
                    if (result == null || result == "\"no_input_focused\"" || result == "null") {
                      onQueryChange(clipText)
                      navigateWithSync(clipText)
                      Toast.makeText(context, "Pasted URL & loading...", Toast.LENGTH_SHORT).show()
                    } else {
                      Toast.makeText(context, "Pasted into web page field", Toast.LENGTH_SHORT).show()
                    }
                  }
                } else {
                  Toast.makeText(context, "Clipboard empty. Copy something first!", Toast.LENGTH_SHORT).show()
                }
              },
              modifier = Modifier
                .size(36.dp)
                .testTag("floating_nav_paste_${window.windowId}")
            ) {
              Icon(
                imageVector = Icons.Default.ContentPaste,
                contentDescription = "Paste from clipboard",
                tint = CyanAccent,
                modifier = Modifier.size(18.dp)
              )
            }

            // 5. Home button
            IconButton(
              onClick = { navigateWithSync("https://www.google.com") },
              modifier = Modifier
                .size(36.dp)
                .testTag("floating_nav_home_${window.windowId}")
            ) {
              Icon(
                imageVector = Icons.Default.Home,
                contentDescription = "Home",
                tint = EmeraldConnected,
                modifier = Modifier.size(18.dp)
              )
            }

            // 6. Bookmarks button
            IconButton(
              onClick = { showBookmarksModal = true },
              modifier = Modifier
                .size(36.dp)
                .testTag("floating_nav_bookmarks_${window.windowId}")
            ) {
              Icon(
                imageVector = if (isCurrentUrlBookmarked) Icons.Default.Bookmark else Icons.Default.Bookmarks,
                contentDescription = "Saved Bookmarks",
                tint = if (isCurrentUrlBookmarked) Color(0xFFFFD700) else CyanAccent,
                modifier = Modifier.size(18.dp)
              )
            }

            // 7. Cookie & Session Manager button
            IconButton(
              onClick = { showCookieModal = true },
              modifier = Modifier
                .size(36.dp)
                .testTag("floating_nav_cookie_${window.windowId}")
            ) {
              Icon(
                imageVector = Icons.Default.Key,
                contentDescription = "Cookie & Session Manager",
                tint = Color(0xFFFFD700),
                modifier = Modifier.size(18.dp)
              )
            }

            // 8. Clear Cache & History button
            IconButton(
              onClick = {
                try {
                  webViewRef?.apply {
                    clearHistory()
                    clearCache(true)
                    clearFormData()
                    clearSslPreferences()
                    android.webkit.WebStorage.getInstance().deleteAllData()
                    reload()
                  }
                  Toast.makeText(context, "🧹 Cache & History Cleared!", Toast.LENGTH_SHORT).show()
                } catch (_: Exception) {}
              },
              modifier = Modifier
                .size(36.dp)
                .testTag("floating_nav_clear_${window.windowId}")
            ) {
              Icon(
                imageVector = Icons.Default.CleaningServices,
                contentDescription = "Clear Cache & History",
                tint = TextSecondary,
                modifier = Modifier.size(18.dp)
              )
            }
          }
        }
      }

        // Interactive Resize Handle at bottom right
        if (!window.isMaximized && onResize != null) {
          Box(
            modifier = Modifier
              .align(Alignment.BottomEnd)
              .size(32.dp)
              .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                  change.consume()
                  onResize(dragAmount.x, dragAmount.y)
                }
              }
              .padding(4.dp),
            contentAlignment = Alignment.BottomEnd
          ) {
            Canvas(modifier = Modifier.size(16.dp)) {
              val strokeWidth = 2.dp.toPx()
              val color = CyanAccent.copy(alpha = 0.85f)
              // Draw subtle triangle resize grip lines
              drawLine(
                color = color,
                start = androidx.compose.ui.geometry.Offset(size.width * 0.4f, size.height),
                end = androidx.compose.ui.geometry.Offset(size.width, size.height * 0.4f),
                strokeWidth = strokeWidth
              )
              drawLine(
                color = color,
                start = androidx.compose.ui.geometry.Offset(size.width * 0.7f, size.height),
                end = androidx.compose.ui.geometry.Offset(size.width, size.height * 0.7f),
                strokeWidth = strokeWidth
              )
            }
          }
        }
      }
    }
  }

  // Room Database Saved Bookmarks Dialog
  if (showBookmarksModal) {
    AlertDialog(
      onDismissRequest = { showBookmarksModal = false },
      title = {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Bookmarks,
              contentDescription = null,
              tint = Color(0xFFFFD700),
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Saved Bookmarks",
              fontWeight = FontWeight.Bold,
              fontSize = 15.sp,
              color = TextPrimary
            )
          }
          Text(
            text = "Room DB (${savedBookmarks.size})",
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = CyanAccent,
            modifier = Modifier
              .background(CyanAccent.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
              .padding(horizontal = 6.dp, vertical = 2.dp)
          )
        }
      },
      text = {
        Column(
          modifier = Modifier.fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          // "+ Bookmark Current URL" banner
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = if (isCurrentUrlBookmarked) Color(0xFFFFD700).copy(alpha = 0.15f) else CyanAccent.copy(alpha = 0.15f),
            border = BorderStroke(
              1.dp,
              if (isCurrentUrlBookmarked) Color(0xFFFFD700).copy(alpha = 0.5f) else CyanAccent.copy(alpha = 0.5f)
            ),
            onClick = {
              if (isCurrentUrlBookmarked && matchingBookmark != null) {
                onDeleteBookmark(matchingBookmark.id)
                Toast.makeText(context, "Removed from Bookmarks", Toast.LENGTH_SHORT).show()
              } else {
                val saveTitle = window.title.ifBlank {
                  try {
                    android.net.Uri.parse(window.currentUrl).host ?: window.currentUrl
                  } catch (_: Exception) {
                    window.currentUrl
                  }
                }
                onSaveBookmark(saveTitle, window.currentUrl, window.profile.id)
                Toast.makeText(context, "⭐ Saved to Room Bookmarks!", Toast.LENGTH_SHORT).show()
              }
            },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("modal_save_current_url_${window.windowId}")
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = if (isCurrentUrlBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkAdd,
                contentDescription = null,
                tint = if (isCurrentUrlBookmarked) Color(0xFFFFD700) else CyanAccent,
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = if (isCurrentUrlBookmarked) "Bookmarked (Tap to Remove)" else "+ Save Current Page to Room DB",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (isCurrentUrlBookmarked) Color(0xFFFFD700) else CyanAccent
                )
                Text(
                  text = window.currentUrl,
                  fontSize = 10.sp,
                  color = TextSecondary,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
              }
            }
          }

          // Search bookmarks filter
          if (savedBookmarks.size > 3) {
            OutlinedTextField(
              value = bookmarkSearchQuery,
              onValueChange = { bookmarkSearchQuery = it },
              placeholder = { Text("Search bookmarks...", fontSize = 11.sp) },
              singleLine = true,
              modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
              colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CyanAccent,
                unfocusedBorderColor = DarkBorder,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
              )
            )
          }

          // Bookmarks List
          val filteredBookmarks = savedBookmarks.filter {
            bookmarkSearchQuery.isBlank() ||
              it.title.contains(bookmarkSearchQuery, ignoreCase = true) ||
              it.url.contains(bookmarkSearchQuery, ignoreCase = true)
          }

          if (filteredBookmarks.isEmpty()) {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
              contentAlignment = Alignment.Center
            ) {
              Text("No bookmarks found", fontSize = 11.sp, color = TextMuted)
            }
          } else {
            LazyColumn(
              modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 240.dp),
              verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              items(filteredBookmarks, key = { it.id }) { bm ->
                val isCurrent = window.currentUrl.equals(bm.url, ignoreCase = true)
                Surface(
                  shape = RoundedCornerShape(6.dp),
                  color = if (isCurrent) CyanAccent.copy(alpha = 0.15f) else DarkSurfaceElevated,
                  border = BorderStroke(1.dp, if (isCurrent) CyanAccent else DarkBorder),
                  onClick = {
                    performNavigate(bm.url)
                    showBookmarksModal = false
                  },
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                  ) {
                    Column(modifier = Modifier.weight(1f)) {
                      Text(
                        text = bm.title,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isCurrent) CyanAccent else TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                      )
                      Text(
                        text = bm.url,
                        fontSize = 9.sp,
                        color = TextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                      )
                    }

                    IconButton(
                      onClick = {
                        onDeleteBookmark(bm.id)
                        Toast.makeText(context, "Deleted bookmark", Toast.LENGTH_SHORT).show()
                      },
                      modifier = Modifier.size(26.dp)
                    ) {
                      Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = TextMuted,
                        modifier = Modifier.size(14.dp)
                      )
                    }
                  }
                }
              }
            }
          }
        }
      },
      confirmButton = {
        Button(
          onClick = { showBookmarksModal = false },
          colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = DarkBg)
        ) {
          Text("Done", fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
      },
      containerColor = DarkSurface
    )
  }

  // 6-Layer Security & Anti-Detection Inspector Dialog
  if (showStealthShieldDialog) {
    val geoInfo = remember(window.linkedProxy) { GeoTimezoneResolver.resolve(window.linkedProxy) }
    val proxyHost = window.linkedProxy?.host ?: "Direct Network (Virtual Loopback)"

    AlertDialog(
      onDismissRequest = { showStealthShieldDialog = false },
      title = {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Security,
              contentDescription = null,
              tint = EmeraldConnected,
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "7-Layer Stealth Engine",
              fontWeight = FontWeight.Bold,
              fontSize = 15.sp,
              color = TextPrimary
            )
          }
          Surface(
            shape = RoundedCornerShape(4.dp),
            color = EmeraldConnected.copy(alpha = 0.2f),
            border = BorderStroke(1.dp, EmeraldConnected)
          ) {
            Text(
              text = "100% Undetectable",
              fontSize = 9.sp,
              fontWeight = FontWeight.Bold,
              color = EmeraldConnected,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
          }
        }
      },
      text = {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          val features = listOf(
            Triple(
              "1. Native Code Masking (Anti-Tamper)",
              "Function.prototype.toString cloaking active. All hooked functions return native code signatures to bypass Pixelscan & CreepJS audits.",
              Icons.Default.Code
            ),
            Triple(
              "2. AudioContext Fingerprint Noise",
              "AudioBuffer, OfflineAudioContext & AnalyserNode have deterministic hardware micro-noise applied to defeat audio hash matching.",
              Icons.Default.Security
            ),
            Triple(
              "3. Canvas & WebGL Readback Shield",
              "Consistent seed-based noise on getImageData, toBlob, toDataURL & WebGL readPixels without animation frame flicker.",
              Icons.Default.FormatSize
            ),
            Triple(
              "4. Desktop Plugins & MimeTypes",
              "Authentic PluginArray (Chrome PDF Viewer, Internal PDF) & MimeTypeArray for desktop profiles to match real PC Chrome.",
              Icons.Default.Layers
            ),
            Triple(
              "5. WebView Footprint Erasure",
              "X-Requested-With header stripped across all web requests. Internal JS interfaces (StealthBridge) completely concealed from enumeration.",
              Icons.Default.CleaningServices
            ),
            Triple(
              "6. Geo, Timezone & Locale Alignment",
              "Spoofed to ${geoInfo.timezoneId} (${geoInfo.tzAbbreviation}) in ${geoInfo.city}, ${geoInfo.countryCode}. Languages & Accept-Language aligned to ${geoInfo.primaryLocale}.",
              Icons.Default.Schedule
            ),
            Triple(
              "7. WebRTC Zero-Leak & Media Privacy",
              "Local LAN/mDNS leaks scrubbed and replaced with proxy IP ($proxyHost). Media devices return unprompted empty labels.",
              Icons.Default.Sync
            )
          )

          features.forEach { (title, desc, icon) ->
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = DarkSurfaceElevated,
              border = BorderStroke(1.dp, DarkBorder),
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(
                modifier = Modifier.padding(8.dp),
                verticalAlignment = Alignment.Top
              ) {
                Box(
                  modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(EmeraldConnected.copy(alpha = 0.2f)),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(icon, contentDescription = null, tint = EmeraldConnected, modifier = Modifier.size(13.dp))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Text(title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("ACTIVE", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = EmeraldConnected)
                  }
                  Spacer(modifier = Modifier.height(2.dp))
                  Text(desc, fontSize = 9.sp, color = TextSecondary, lineHeight = 13.sp)
                }
              }
            }
          }
        }
      },
      confirmButton = {
        Button(
          onClick = { showStealthShieldDialog = false },
          colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = DarkBg)
        ) {
          Text("Got it", fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
      },
      containerColor = DarkSurface
    )
  }

  // In-Session Cookie & Session Manager Dialog (Netscape & JSON Import/Export)
  if (showCookieModal) {
    val currentHost = remember(window.currentUrl) {
      try { android.net.Uri.parse(window.currentUrl).host ?: window.currentUrl } catch (_: Exception) { window.currentUrl }
    }
    val rawCurrentCookies = remember(showCookieModal, window.currentUrl) {
      android.webkit.CookieManager.getInstance().getCookie(window.currentUrl) ?: ""
    }
    val sessionCookieCount = remember(rawCurrentCookies) {
      if (rawCurrentCookies.isBlank()) 0 else rawCurrentCookies.split(";").filter { it.contains("=") }.size
    }

    AlertDialog(
      onDismissRequest = { showCookieModal = false },
      title = {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Key, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Session & Cookie Manager", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
          }
          Surface(
            shape = RoundedCornerShape(4.dp),
            color = Color(0xFFFFD700).copy(alpha = 0.15f),
            border = BorderStroke(0.8.dp, Color(0xFFFFD700).copy(alpha = 0.4f))
          ) {
            Text(
              text = "$sessionCookieCount Live Cookies",
              fontSize = 9.sp,
              fontWeight = FontWeight.Bold,
              color = Color(0xFFFFD700),
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
          }
        }
      },
      text = {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Text(
            text = "Site: $currentHost",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = CyanAccent
          )

          // Export Section
          Text(
            text = "Export Current Active Cookies:",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
          )
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Button(
              onClick = {
                val json = CookieHelper.exportCookiesJson(window.currentUrl)
                clipboardManager.setText(AnnotatedString(json))
                Toast.makeText(context, "Copied JSON Cookies to clipboard!", Toast.LENGTH_SHORT).show()
              },
              colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = DarkBg),
              modifier = Modifier.weight(1f)
            ) {
              Text("Export JSON", fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
            Button(
              onClick = {
                val netscape = CookieHelper.exportCookiesNetscape(window.currentUrl)
                clipboardManager.setText(AnnotatedString(netscape))
                Toast.makeText(context, "Copied Netscape Cookies to clipboard!", Toast.LENGTH_SHORT).show()
              },
              colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated, contentColor = CyanAccent),
              modifier = Modifier.weight(1f)
            ) {
              Text("Export Netscape", fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
          }

          // Save Active Session Cookies to Profile permanently
          Button(
            onClick = {
              val json = CookieHelper.exportCookiesJson(window.currentUrl)
              if (json != "[]") {
                onSaveCookiesToProfile?.invoke(window.profile.id, json)
                Toast.makeText(context, "💾 Saved session cookies to '${window.profile.name}' profile!", Toast.LENGTH_SHORT).show()
              } else {
                Toast.makeText(context, "No cookies found on this page yet", Toast.LENGTH_SHORT).show()
              }
            },
            colors = ButtonDefaults.buttonColors(containerColor = EmeraldConnected, contentColor = DarkBg),
            modifier = Modifier.fillMaxWidth()
          ) {
            Text("💾 Save Session Cookies to Profile", fontSize = 11.sp, fontWeight = FontWeight.Bold)
          }

          Spacer(modifier = Modifier.height(4.dp))

          // Import & Inject Section
          Text(
            text = "Import & Inject Cookies into Live Window:",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
          )
          OutlinedTextField(
            value = inPageCookieInput,
            onValueChange = { inPageCookieInput = it },
            placeholder = { Text("Paste Netscape format or JSON cookies here...", fontSize = 10.sp) },
            maxLines = 6,
            modifier = Modifier
              .fillMaxWidth()
              .height(110.dp),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = EmeraldConnected,
              unfocusedBorderColor = DarkBorder,
              focusedTextColor = TextPrimary,
              unfocusedTextColor = TextPrimary
            )
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Button(
              onClick = {
                val clip = getSystemClipboardText()
                if (!clip.isNullOrBlank()) {
                  inPageCookieInput = clip
                  Toast.makeText(context, "Pasted from clipboard!", Toast.LENGTH_SHORT).show()
                } else {
                  Toast.makeText(context, "Clipboard empty", Toast.LENGTH_SHORT).show()
                }
              },
              colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated, contentColor = TextSecondary),
              modifier = Modifier.weight(1f)
            ) {
              Text("Paste Clipboard", fontSize = 10.sp)
            }
            Button(
              onClick = {
                if (inPageCookieInput.isNotBlank()) {
                  val count = CookieHelper.importCookies(window.currentUrl, inPageCookieInput)
                  if (count > 0) {
                    Toast.makeText(context, "Injected $count cookies! Reloading page...", Toast.LENGTH_SHORT).show()
                    webViewRef?.reload()
                    showCookieModal = false
                  } else {
                    Toast.makeText(context, "Could not parse cookies. Check format.", Toast.LENGTH_SHORT).show()
                  }
                }
              },
              colors = ButtonDefaults.buttonColors(containerColor = EmeraldConnected, contentColor = DarkBg),
              modifier = Modifier.weight(1f)
            ) {
              Text("Inject & Reload", fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
          }

          // Clear Session Action
          OutlinedButton(
            onClick = {
              try {
                android.webkit.CookieManager.getInstance().removeAllCookies(null)
                android.webkit.CookieManager.getInstance().flush()
                webViewRef?.apply {
                  clearCache(true)
                  clearHistory()
                  reload()
                }
                Toast.makeText(context, "Wiped all session cookies & reloaded clean", Toast.LENGTH_SHORT).show()
                showCookieModal = false
              } catch (_: Exception) {}
            },
            modifier = Modifier.fillMaxWidth()
          ) {
            Icon(Icons.Default.CleaningServices, contentDescription = null, tint = TextMuted, modifier = Modifier.size(13.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Clear Session Cookies & Reload", color = TextMuted, fontSize = 10.sp)
          }
        }
      },
      confirmButton = {
        Button(
          onClick = { showCookieModal = false },
          colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated, contentColor = TextPrimary)
        ) {
          Text("Done", fontSize = 11.sp)
        }
      },
      containerColor = DarkSurface
    )
  }

  DisposableEffect(window.windowId) {
    onDispose {
      webViewRef?.destroy()
    }
  }
}
