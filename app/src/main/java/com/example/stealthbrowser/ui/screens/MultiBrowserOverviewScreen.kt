package com.example.stealthbrowser.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ViewColumn
import androidx.compose.material.icons.filled.ViewQuilt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stealthbrowser.model.BrowserProfile
import com.example.stealthbrowser.model.FloatingBrowserInstance
import com.example.stealthbrowser.ui.theme.CyanAccent
import com.example.stealthbrowser.ui.theme.DarkBg
import com.example.stealthbrowser.ui.theme.DarkBorder
import com.example.stealthbrowser.ui.theme.DarkSurface
import com.example.stealthbrowser.ui.theme.DarkSurfaceElevated
import com.example.stealthbrowser.ui.theme.EmeraldConnected
import com.example.stealthbrowser.ui.theme.TextMuted
import com.example.stealthbrowser.ui.theme.TextPrimary
import com.example.stealthbrowser.ui.theme.TextSecondary

@Composable
fun MultiBrowserOverviewScreen(
  openWindows: List<FloatingBrowserInstance>,
  profiles: List<BrowserProfile>,
  onAutoTile: () -> Unit,
  onBringToFront: (String) -> Unit,
  onCloseWindow: (String) -> Unit,
  onCloseAllWindows: () -> Unit,
  onLaunchProfile: (BrowserProfile) -> Unit,
  modifier: Modifier = Modifier
) {
  Box(modifier = modifier.fillMaxSize().background(DarkBg)) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 12.dp)
    ) {
      Spacer(modifier = Modifier.height(10.dp))

      // Status Card
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
        border = BorderStroke(1.dp, CyanAccent.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(
              text = "Multi-Window Display Engine",
              style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
              color = CyanAccent
            )
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = EmeraldConnected.copy(alpha = 0.15f)
            ) {
              Text(
                text = "${openWindows.size} Active",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = EmeraldConnected,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(6.dp))

          Text(
            text = "Each window runs an isolated browsing session with custom Android hardware fingerprints, canvas noise injection, and independent proxy routing.",
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = TextSecondary
          )

          Spacer(modifier = Modifier.height(10.dp))

          // Auto Tile & Close Actions
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Button(
              onClick = onAutoTile,
              shape = RoundedCornerShape(8.dp),
              colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = DarkBg),
              modifier = Modifier.weight(1f)
            ) {
              Icon(Icons.Default.GridView, contentDescription = null, modifier = Modifier.size(14.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Tile Side-by-Side", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            if (openWindows.isNotEmpty()) {
              OutlinedButton(
                onClick = onCloseAllWindows,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, DarkBorder),
                modifier = Modifier.weight(1f)
              ) {
                Text("Close All", fontSize = 11.sp, color = TextSecondary)
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      Text(
        text = "Currently Open Windows (${openWindows.size})",
        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
        color = TextPrimary
      )

      Spacer(modifier = Modifier.height(6.dp))

      if (openWindows.isEmpty()) {
        Card(
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = DarkSurface),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Icon(
              imageVector = Icons.Default.OpenInBrowser,
              contentDescription = null,
              tint = TextMuted,
              modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = "No Floating Windows Open",
              fontSize = 13.sp,
              fontWeight = FontWeight.SemiBold,
              color = TextSecondary
            )
            Text(
              text = "Go to Profiles tab and select 2-4 profiles to open them side-by-side on your screen.",
              fontSize = 11.sp,
              color = TextMuted
            )
          }
        }
      } else {
        LazyColumn(
          modifier = Modifier.fillMaxSize(),
          verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          items(openWindows, key = { it.windowId }) { win ->
            val accentColor = Color(win.profile.colorHex)
            Card(
              shape = RoundedCornerShape(12.dp),
              colors = CardDefaults.cardColors(containerColor = DarkSurface),
              border = BorderStroke(1.dp, accentColor.copy(alpha = 0.5f)),
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = win.profile.name,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                  )
                  Text(
                    text = win.currentUrl,
                    fontSize = 10.sp,
                    color = CyanAccent,
                    maxLines = 1
                  )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                  Button(
                    onClick = { onBringToFront(win.windowId) },
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated)
                  ) {
                    Text("Focus", fontSize = 10.sp, color = TextPrimary)
                  }

                  IconButton(
                    onClick = { onCloseWindow(win.windowId) },
                    modifier = Modifier.size(30.dp)
                  ) {
                    Icon(
                      imageVector = Icons.Default.Close,
                      contentDescription = "Close",
                      tint = TextMuted,
                      modifier = Modifier.size(16.dp)
                    )
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}
