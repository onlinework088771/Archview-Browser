package com.example.stealthbrowser.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Router
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stealthbrowser.model.ProxyConfig
import com.example.stealthbrowser.model.ProxyStatus
import com.example.stealthbrowser.ui.theme.AmberPending
import com.example.stealthbrowser.ui.theme.CyanAccent
import com.example.stealthbrowser.ui.theme.DarkBorder
import com.example.stealthbrowser.ui.theme.DarkSurface
import com.example.stealthbrowser.ui.theme.DarkSurfaceElevated
import com.example.stealthbrowser.ui.theme.ElectricBlue
import com.example.stealthbrowser.ui.theme.EmeraldConnected
import com.example.stealthbrowser.ui.theme.RoseError
import com.example.stealthbrowser.ui.theme.TextMuted
import com.example.stealthbrowser.ui.theme.TextPrimary
import com.example.stealthbrowser.ui.theme.TextSecondary

@Composable
fun CompactProxyCard(
  proxy: ProxyConfig,
  linkedProfilesCount: Int,
  onTest: () -> Unit,
  onRotate: () -> Unit,
  onEdit: () -> Unit,
  onDelete: () -> Unit,
  modifier: Modifier = Modifier
) {
  val statusColor = when (proxy.status) {
    ProxyStatus.ACTIVE -> EmeraldConnected
    ProxyStatus.TESTING -> AmberPending
    ProxyStatus.FAILED -> RoseError
    ProxyStatus.UNTESTED -> TextMuted
  }

  Card(
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = DarkSurface),
    border = BorderStroke(1.dp, DarkBorder.copy(alpha = 0.5f)),
    modifier = modifier
      .fillMaxWidth()
      .testTag("proxy_card_${proxy.id}")
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 10.dp, vertical = 8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // 1. Country & Protocol Avatar (Matches Profile Card avatar size: 34dp)
      Box(
        modifier = Modifier
          .size(34.dp)
          .clip(RoundedCornerShape(8.dp))
          .background(ElectricBlue.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = proxy.countryCode,
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          color = CyanAccent
        )
      }

      Spacer(modifier = Modifier.width(8.dp))

      // 2. Middle Content (Proxy Name, Protocol Badge, Host:Port, Status)
      Column(
        modifier = Modifier.weight(1f)
      ) {
        // Line 1: Name and Protocol Badge
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(
            text = proxy.name,
            style = MaterialTheme.typography.titleSmall.copy(
              fontWeight = FontWeight.SemiBold,
              fontSize = 13.sp
            ),
            color = TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false)
          )

          Spacer(modifier = Modifier.width(6.dp))

          Surface(
            shape = RoundedCornerShape(4.dp),
            color = DarkBorder.copy(alpha = 0.5f)
          ) {
            Text(
              text = proxy.protocol.name,
              fontSize = 8.sp,
              fontWeight = FontWeight.Bold,
              color = CyanAccent,
              modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(2.dp))

        // Line 2: Monospace Host:Port, Status Dot, Ping
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(
            text = "${proxy.host}:${proxy.port}",
            fontFamily = FontFamily.Monospace,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = TextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )

          Spacer(modifier = Modifier.width(6.dp))

          // Status indicator
          Box(
            modifier = Modifier
              .size(5.dp)
              .clip(CircleShape)
              .background(statusColor)
          )

          Spacer(modifier = Modifier.width(3.dp))

          Text(
            text = when (proxy.status) {
              ProxyStatus.TESTING -> "Testing..."
              ProxyStatus.ACTIVE -> "${proxy.latencyMs ?: 0}ms"
              ProxyStatus.FAILED -> "Offline"
              ProxyStatus.UNTESTED -> "Untested"
            },
            fontSize = 9.sp,
            fontWeight = FontWeight.SemiBold,
            color = statusColor
          )

          if (linkedProfilesCount > 0) {
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "• $linkedProfilesCount profile${if (linkedProfilesCount > 1) "s" else ""}",
              fontSize = 9.sp,
              color = TextMuted
            )
          }
        }
      }

      Spacer(modifier = Modifier.width(4.dp))

      // 3. Compact Action Buttons (Ping, Rotate, Edit, Delete)
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(1.dp)
      ) {
        // Test Ping
        IconButton(
          onClick = onTest,
          modifier = Modifier.size(30.dp)
        ) {
          if (proxy.status == ProxyStatus.TESTING) {
            CircularProgressIndicator(
              modifier = Modifier.size(13.dp),
              strokeWidth = 1.8.dp,
              color = CyanAccent
            )
          } else {
            Icon(
              imageVector = Icons.Default.NetworkCheck,
              contentDescription = "Test Proxy",
              tint = CyanAccent,
              modifier = Modifier.size(15.dp)
            )
          }
        }

        // Rotate IP
        IconButton(
          onClick = onRotate,
          modifier = Modifier.size(30.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Autorenew,
            contentDescription = "Rotate IP",
            tint = AmberPending,
            modifier = Modifier.size(15.dp)
          )
        }

        // Edit
        IconButton(
          onClick = onEdit,
          modifier = Modifier.size(30.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Edit,
            contentDescription = "Edit Proxy",
            tint = TextSecondary,
            modifier = Modifier.size(14.dp)
          )
        }

        // Delete
        IconButton(
          onClick = onDelete,
          modifier = Modifier.size(30.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = "Delete Proxy",
            tint = TextMuted,
            modifier = Modifier.size(14.dp)
          )
        }
      }
    }
  }
}
