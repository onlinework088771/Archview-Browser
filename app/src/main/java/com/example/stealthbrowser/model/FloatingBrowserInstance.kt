package com.example.stealthbrowser.model

import java.util.UUID

enum class WindowLayoutMode {
  SIDE_BY_SIDE, // 2 side-by-side or multi-column
  GRID_2X2,     // 4 windows in 2x2 grid
  TILED,        // Auto calculated smart tile
  FREE_FLOAT    // Freely dragged anywhere
}

data class FloatingBrowserInstance(
  val windowId: String = UUID.randomUUID().toString(),
  val profile: BrowserProfile,
  val currentUrl: String = "https://www.google.com",
  val inputSearchQuery: String = "https://www.google.com",
  val title: String = "Shakil Stealth Browser",
  val isLoading: Boolean = false,
  val canGoBack: Boolean = false,
  val canGoForward: Boolean = false,
  // Normalized positioning (0f to 1f) relative to container width and height
  val normX: Float = 0.05f,
  val normY: Float = 0.05f,
  val normWidth: Float = 0.9f,
  val normHeight: Float = 0.85f,
  val isMinimized: Boolean = false,
  val isMaximized: Boolean = false,
  val zIndex: Float = 1f,
  val linkedProxy: ProxyConfig? = null
)
