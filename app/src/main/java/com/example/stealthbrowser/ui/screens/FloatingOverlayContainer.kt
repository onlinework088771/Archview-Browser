package com.example.stealthbrowser.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.stealthbrowser.data.BookmarkEntity
import com.example.stealthbrowser.model.FloatingBrowserInstance
import com.example.stealthbrowser.ui.components.FloatingBrowserDock
import com.example.stealthbrowser.ui.components.FloatingBrowserWindow
import kotlinx.coroutines.flow.SharedFlow

@Composable
fun FloatingOverlayContainer(
  windows: List<FloatingBrowserInstance>,
  savedBookmarks: List<BookmarkEntity> = emptyList(),
  isSyncMode: Boolean = false,
  syncScriptAction: SharedFlow<Pair<String, String>>? = null,
  onToggleSyncMode: () -> Unit = {},
  onSyncReloadAll: () -> Unit = {},
  onSyncScroll: (windowId: String, pct: Float) -> Unit = { _, _ -> },
  onSyncClick: (windowId: String, x: Float, y: Float) -> Unit = { _, _, _ -> },
  onSyncNavigate: (windowId: String, url: String) -> Unit = { _, _ -> },
  onSaveCookiesToProfile: ((profileId: String, cookies: String) -> Unit)? = null,
  onSaveBookmark: (title: String, url: String, profileId: String?) -> Unit = { _, _, _ -> },
  onDeleteBookmark: (id: Long) -> Unit = {},
  onDragWindow: (windowId: String, deltaNormX: Float, deltaNormY: Float) -> Unit,
  onResizeWindow: ((windowId: String, deltaNormWidth: Float, deltaNormHeight: Float) -> Unit)? = null,
  onFocusWindow: (String) -> Unit,
  onNavigateWindow: (windowId: String, queryOrUrl: String) -> Unit,
  onQueryChange: (windowId: String, query: String) -> Unit,
  onNavStateUpdate: (windowId: String, url: String, title: String, canBack: Boolean, canForward: Boolean, loading: Boolean) -> Unit,
  onAutoTile: () -> Unit,
  onMinimizeWindow: (String) -> Unit,
  onRestoreWindow: (String) -> Unit,
  onToggleMaximizeWindow: (String) -> Unit,
  onCloseWindow: (String) -> Unit,
  onCloseAllWindows: () -> Unit,
  modifier: Modifier = Modifier
) {
  BoxWithConstraints(
    modifier = modifier.fillMaxSize()
  ) {
    val containerWidthPx = with(LocalDensity.current) { maxWidth.toPx() }
    val containerHeightPx = with(LocalDensity.current) { maxHeight.toPx() }

    // Determine the topmost (frontmost) active window by highest zIndex
    val activeWindows = windows.filter { !it.isMinimized }
    val topWindowId = activeWindows.maxByOrNull { it.zIndex }?.windowId

    // Render active (non-minimized) floating windows
    activeWindows.forEach { window ->
      val windowWidth = if (window.isMaximized) maxWidth else maxWidth * window.normWidth
      val windowHeight = if (window.isMaximized) maxHeight - 48.dp else maxHeight * window.normHeight
      val offsetX = if (window.isMaximized) 0.dp else maxWidth * window.normX
      val offsetY = if (window.isMaximized) 0.dp else maxHeight * window.normY

      Box(
        modifier = Modifier
          .offset(x = offsetX, y = offsetY)
          .width(windowWidth)
          .height(windowHeight)
          .zIndex(window.zIndex)
      ) {
        FloatingBrowserWindow(
          window = window,
          isFrontmost = (window.windowId == topWindowId),
          savedBookmarks = savedBookmarks,
          isSyncMode = isSyncMode,
          syncScriptAction = syncScriptAction,
          onSyncScroll = { pct -> onSyncScroll(window.windowId, pct) },
          onSyncClick = { x, y -> onSyncClick(window.windowId, x, y) },
          onSyncNavigate = { url -> onSyncNavigate(window.windowId, url) },
          onSaveCookiesToProfile = onSaveCookiesToProfile,
          onSaveBookmark = onSaveBookmark,
          onDeleteBookmark = onDeleteBookmark,
          onDrag = { dx, dy ->
            val deltaNormX = if (containerWidthPx > 0) dx / containerWidthPx else 0f
            val deltaNormY = if (containerHeightPx > 0) dy / containerHeightPx else 0f
            onDragWindow(window.windowId, deltaNormX, deltaNormY)
          },
          onResize = { dw, dh ->
            val deltaNormW = if (containerWidthPx > 0) dw / containerWidthPx else 0f
            val deltaNormH = if (containerHeightPx > 0) dh / containerHeightPx else 0f
            onResizeWindow?.invoke(window.windowId, deltaNormW, deltaNormH)
          },
          onFocus = { onFocusWindow(window.windowId) },
          onNavigate = { query -> onNavigateWindow(window.windowId, query) },
          onQueryChange = { q -> onQueryChange(window.windowId, q) },
          onNavStateUpdate = { url, title, canBack, canForward, loading ->
            onNavStateUpdate(window.windowId, url, title, canBack, canForward, loading)
          },
          onAutoTile = onAutoTile,
          onMinimize = { onMinimizeWindow(window.windowId) },
          onToggleMaximize = { onToggleMaximizeWindow(window.windowId) },
          onClose = { onCloseWindow(window.windowId) },
          modifier = Modifier.fillMaxSize()
        )
      }
    }

    // Floating Browser Dock at the bottom
    if (windows.isNotEmpty()) {
      Box(
        modifier = Modifier
          .align(Alignment.BottomCenter)
          .zIndex(9999f)
      ) {
        FloatingBrowserDock(
          windows = windows,
          isSyncMode = isSyncMode,
          onToggleSyncMode = onToggleSyncMode,
          onSyncReloadAll = onSyncReloadAll,
          onRestoreWindow = onRestoreWindow,
          onAutoTile = onAutoTile,
          onCloseAll = onCloseAllWindows
        )
      }
    }
  }
}
