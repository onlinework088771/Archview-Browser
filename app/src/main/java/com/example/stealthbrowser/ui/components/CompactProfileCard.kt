package com.example.stealthbrowser.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FilledTonalButton
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stealthbrowser.model.BrowserProfile
import com.example.stealthbrowser.model.ProxyConfig
import com.example.stealthbrowser.ui.theme.CyanAccent
import com.example.stealthbrowser.ui.theme.DarkBorder
import com.example.stealthbrowser.ui.theme.DarkSurface
import com.example.stealthbrowser.ui.theme.DarkSurfaceElevated
import com.example.stealthbrowser.ui.theme.EmeraldConnected
import com.example.stealthbrowser.ui.theme.TextMuted
import com.example.stealthbrowser.ui.theme.TextPrimary
import com.example.stealthbrowser.ui.theme.TextSecondary

@Composable
fun CompactProfileCard(
  profile: BrowserProfile,
  linkedProxy: ProxyConfig?,
  isSelected: Boolean,
  onToggleSelect: () -> Unit,
  onLaunch: () -> Unit,
  onEdit: () -> Unit,
  onClone: (() -> Unit)? = null,
  onDelete: () -> Unit,
  modifier: Modifier = Modifier
) {
  val accentColor = Color(profile.colorHex)

  Card(
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (isSelected) DarkSurfaceElevated else DarkSurface
    ),
    border = BorderStroke(
      width = if (isSelected) 1.5.dp else 1.dp,
      color = if (isSelected) CyanAccent else DarkBorder.copy(alpha = 0.5f)
    ),
    modifier = modifier
      .fillMaxWidth()
      .testTag("profile_card_${profile.id}")
      .clickable { onToggleSelect() }
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 10.dp, vertical = 8.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      // Checkbox for batch launching
      Checkbox(
        checked = isSelected,
        onCheckedChange = { onToggleSelect() },
        colors = CheckboxDefaults.colors(
          checkedColor = CyanAccent,
          checkmarkColor = DarkSurface
        ),
        modifier = Modifier.size(28.dp)
      )

      Spacer(modifier = Modifier.width(6.dp))

      // Profile avatar/icon
      Box(
        modifier = Modifier
          .size(34.dp)
          .clip(RoundedCornerShape(8.dp))
          .background(accentColor.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.PhoneAndroid,
          contentDescription = null,
          tint = accentColor,
          modifier = Modifier.size(18.dp)
        )
      }

      Spacer(modifier = Modifier.width(8.dp))

      // Middle Details
      Column(
        modifier = Modifier.weight(1f)
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = profile.name,
            style = MaterialTheme.typography.titleSmall.copy(
              fontWeight = FontWeight.SemiBold,
              fontSize = 13.sp
            ),
            color = TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false)
          )
        }

        Spacer(modifier = Modifier.height(2.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
          // Android Version Badge
          Surface(
            shape = RoundedCornerShape(4.dp),
            color = DarkBorder.copy(alpha = 0.4f),
            modifier = Modifier.padding(end = 4.dp)
          ) {
            Text(
              text = profile.androidVersion.replace("Android ", "A"),
              fontSize = 9.sp,
              fontWeight = FontWeight.Medium,
              color = TextSecondary,
              modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
            )
          }

          // Proxy Badge
          if (linkedProxy != null) {
            Surface(
              shape = RoundedCornerShape(4.dp),
              color = EmeraldConnected.copy(alpha = 0.15f)
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
              ) {
                Box(
                  modifier = Modifier
                    .size(5.dp)
                    .clip(CircleShape)
                    .background(EmeraldConnected)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                  text = "${linkedProxy.countryCode} ${linkedProxy.protocol}",
                  fontSize = 9.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = EmeraldConnected
                )
              }
            }
          } else {
            Text(
              text = "Direct Network",
              fontSize = 9.sp,
              fontWeight = FontWeight.Normal,
              color = TextMuted
            )
          }

          // Feature Badges: AdBlock, Script, Cookies
          if (profile.adBlockEnabled) {
            Spacer(modifier = Modifier.width(4.dp))
            Surface(
              shape = RoundedCornerShape(4.dp),
              color = EmeraldConnected.copy(alpha = 0.12f)
            ) {
              Text(
                text = "🛡️ AdBlock",
                fontSize = 8.sp,
                fontWeight = FontWeight.SemiBold,
                color = EmeraldConnected,
                modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp)
              )
            }
          }

          if (profile.userScript.isNotBlank()) {
            Spacer(modifier = Modifier.width(4.dp))
            Surface(
              shape = RoundedCornerShape(4.dp),
              color = CyanAccent.copy(alpha = 0.12f)
            ) {
              Text(
                text = "⚡ Script",
                fontSize = 8.sp,
                fontWeight = FontWeight.SemiBold,
                color = CyanAccent,
                modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp)
              )
            }
          }

          if (profile.savedCookies.isNotBlank()) {
            Spacer(modifier = Modifier.width(4.dp))
            Surface(
              shape = RoundedCornerShape(4.dp),
              color = Color(0xFFFFD700).copy(alpha = 0.15f)
            ) {
              Text(
                text = "🍪 Cookies",
                fontSize = 8.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFFFFD700),
                modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp)
              )
            }
          }
        }
      }

      // Action buttons
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(1.dp)
      ) {
        // 1-Tap Clone / Duplicate Profile Button
        if (onClone != null) {
          IconButton(
            onClick = onClone,
            modifier = Modifier.size(28.dp)
          ) {
            Icon(
              imageVector = Icons.Default.ContentCopy,
              contentDescription = "Duplicate Profile",
              tint = CyanAccent,
              modifier = Modifier.size(15.dp)
            )
          }
        }

        IconButton(
          onClick = onLaunch,
          modifier = Modifier.size(30.dp)
        ) {
          Icon(
            imageVector = Icons.Default.OpenInBrowser,
            contentDescription = "Launch Floating",
            tint = CyanAccent,
            modifier = Modifier.size(17.dp)
          )
        }

        IconButton(
          onClick = onEdit,
          modifier = Modifier.size(28.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Edit,
            contentDescription = "Edit Profile",
            tint = TextSecondary,
            modifier = Modifier.size(15.dp)
          )
        }

        IconButton(
          onClick = onDelete,
          modifier = Modifier.size(28.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = "Delete Profile",
            tint = TextMuted,
            modifier = Modifier.size(15.dp)
          )
        }
      }
    }
  }
}
