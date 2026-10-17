package com.example.stealthbrowser.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.ViewColumn
import androidx.compose.material.icons.filled.Window
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stealthbrowser.model.FloatingBrowserInstance
import com.example.stealthbrowser.ui.theme.CyanAccent
import com.example.stealthbrowser.ui.theme.DarkBorder
import com.example.stealthbrowser.ui.theme.DarkSurfaceElevated
import com.example.stealthbrowser.ui.theme.EmeraldConnected
import com.example.stealthbrowser.ui.theme.TextMuted
import com.example.stealthbrowser.ui.theme.TextPrimary
import com.example.stealthbrowser.ui.theme.TextSecondary

@Composable
fun FloatingBrowserDock(
  windows: List<FloatingBrowserInstance>,
  isSyncMode: Boolean = false,
  onToggleSyncMode: () -> Unit = {},
  onSyncReloadAll: () -> Unit = {},
  onRestoreWindow: (String) -> Unit,
  onAutoTile: () -> Unit,
  onCloseAll: () -> Unit,
  modifier: Modifier = Modifier
) {
  AnimatedVisibility(
    visible = windows.isNotEmpty(),
    enter = slideInVertically { it },
    exit = slideOutVertically { it },
    modifier = modifier
  ) {
    Card(
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated.copy(alpha = 0.95f)),
      border = BorderStroke(1.dp, if (isSyncMode) EmeraldConnected else DarkBorder),
      elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
      modifier = Modifier
        .padding(horizontal = 12.dp, vertical = 6.dp)
        .testTag("floating_browser_dock")
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        // Quick Auto-Tile Button
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = CyanAccent.copy(alpha = 0.15f),
          border = BorderStroke(1.dp, CyanAccent.copy(alpha = 0.5f)),
          onClick = onAutoTile,
          modifier = Modifier.testTag("auto_tile_button")
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
          ) {
            Icon(
              imageVector = Icons.Default.GridView,
              contentDescription = "Tile Side by Side",
              tint = CyanAccent,
              modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "Tile (${windows.size})",
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              color = CyanAccent
            )
          }
        }

        Spacer(modifier = Modifier.width(6.dp))

        // Synchronizer / Mirror Action Toggle Button
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = if (isSyncMode) EmeraldConnected.copy(alpha = 0.25f) else DarkBorder.copy(alpha = 0.4f),
          border = BorderStroke(1.dp, if (isSyncMode) EmeraldConnected else DarkBorder),
          onClick = onToggleSyncMode,
          modifier = Modifier.testTag("sync_mode_toggle_btn")
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Sync,
              contentDescription = "Synchronizer Mirror Mode",
              tint = if (isSyncMode) EmeraldConnected else TextSecondary,
              modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = if (isSyncMode) "🔗 Sync ON" else "Sync",
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              color = if (isSyncMode) EmeraldConnected else TextSecondary
            )
          }
        }

        if (isSyncMode) {
          Spacer(modifier = Modifier.width(4.dp))
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = EmeraldConnected.copy(alpha = 0.15f),
            border = BorderStroke(1.dp, EmeraldConnected.copy(alpha = 0.3f)),
            onClick = onSyncReloadAll,
            modifier = Modifier.testTag("sync_reload_all_btn")
          ) {
            Box(modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)) {
              Icon(Icons.Default.Refresh, contentDescription = "Sync Reload All", tint = EmeraldConnected, modifier = Modifier.size(13.dp))
            }
          }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Open Window Chips
        Row(
          modifier = Modifier
            .weight(1f)
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          windows.forEach { window ->
            val accentColor = Color(window.profile.colorHex)
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = if (window.isMinimized) DarkBorder.copy(alpha = 0.5f) else accentColor.copy(alpha = 0.2f),
              border = BorderStroke(1.dp, if (window.isMinimized) DarkBorder else accentColor),
              onClick = { onRestoreWindow(window.windowId) }
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
              ) {
                Box(
                  modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(if (window.isMinimized) TextMuted else EmeraldConnected)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = window.profile.name.take(12),
                  fontSize = 10.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = if (window.isMinimized) TextSecondary else TextPrimary
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.width(6.dp))

        // Close All button
        IconButton(
          onClick = onCloseAll,
          modifier = Modifier.size(28.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Close All Windows",
            tint = TextMuted,
            modifier = Modifier.size(16.dp)
          )
        }
      }
    }
  }
}
