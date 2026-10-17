package com.example.stealthbrowser.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stealthbrowser.model.BrowserProfile
import com.example.stealthbrowser.model.ProxyConfig
import com.example.stealthbrowser.ui.components.CompactProxyCard
import com.example.stealthbrowser.ui.theme.AmberPending
import com.example.stealthbrowser.ui.theme.CyanAccent
import com.example.stealthbrowser.ui.theme.DarkBg
import com.example.stealthbrowser.ui.theme.DarkBorder
import com.example.stealthbrowser.ui.theme.DarkSurfaceElevated
import com.example.stealthbrowser.ui.theme.TextPrimary
import com.example.stealthbrowser.ui.theme.TextSecondary

@Composable
fun ProxiesScreen(
  proxies: List<ProxyConfig>,
  profiles: List<BrowserProfile>,
  onTestProxy: (String) -> Unit,
  onRotateProxy: (String) -> Unit,
  onAddProxy: (ProxyConfig) -> Unit,
  onAddBulkProxies: (List<ProxyConfig>) -> Unit,
  onUpdateProxy: (ProxyConfig) -> Unit,
  onDeleteProxy: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  var showEditDialog by remember { mutableStateOf(false) }
  var showBulkDialog by remember { mutableStateOf(false) }
  var proxyToEdit by remember { mutableStateOf<ProxyConfig?>(null) }

  Box(modifier = modifier.fillMaxSize().background(DarkBg)) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 12.dp)
    ) {
      Spacer(modifier = Modifier.height(10.dp))

      // Compact Proxy Management & Quick Actions Card
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
        border = BorderStroke(1.dp, DarkBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 8.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Column {
            Text(
              text = "Proxy Manager & Rotator",
              style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
              color = CyanAccent
            )
            Text(
              text = "HTTP • SOCKS5 • Auto-Rotation",
              fontSize = 10.sp,
              fontWeight = FontWeight.Medium,
              color = TextSecondary
            )
          }

          Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            // Bulk Upload Button
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = CyanAccent.copy(alpha = 0.15f),
              border = BorderStroke(1.dp, CyanAccent.copy(alpha = 0.4f)),
              onClick = { showBulkDialog = true }
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.CloudUpload,
                  contentDescription = null,
                  tint = CyanAccent,
                  modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                  text = "Bulk Upload",
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  color = CyanAccent
                )
              }
            }

            // Test all button
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = DarkBorder.copy(alpha = 0.5f),
              border = BorderStroke(1.dp, DarkBorder),
              onClick = { proxies.forEach { onTestProxy(it.id) } }
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.NetworkCheck,
                  contentDescription = null,
                  tint = CyanAccent,
                  modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                  text = "Test All",
                  fontSize = 10.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = CyanAccent
                )
              }
            }

            // Rotate all button
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = AmberPending.copy(alpha = 0.15f),
              border = BorderStroke(1.dp, AmberPending.copy(alpha = 0.4f)),
              onClick = { proxies.forEach { onRotateProxy(it.id) } }
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.Autorenew,
                  contentDescription = null,
                  tint = AmberPending,
                  modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                  text = "Rotate",
                  fontSize = 10.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = AmberPending
                )
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Section Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Configured Proxies (${proxies.size})",
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
          color = TextPrimary
        )

        Surface(
          shape = RoundedCornerShape(6.dp),
          color = CyanAccent.copy(alpha = 0.12f),
          border = BorderStroke(1.dp, CyanAccent.copy(alpha = 0.3f)),
          onClick = { showBulkDialog = true }
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
          ) {
            Icon(Icons.Default.CloudUpload, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(11.dp))
            Spacer(modifier = Modifier.width(3.dp))
            Text(
              text = "+ Bulk Import",
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              color = CyanAccent
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(6.dp))

      // Compact Proxy Cards List (matches the small profile card height!)
      LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
      ) {
        items(proxies, key = { it.id }) { proxy ->
          val linkedCount = profiles.count { it.proxyId == proxy.id }
          CompactProxyCard(
            proxy = proxy,
            linkedProfilesCount = linkedCount,
            onTest = { onTestProxy(proxy.id) },
            onRotate = { onRotateProxy(proxy.id) },
            onEdit = {
              proxyToEdit = proxy
              showEditDialog = true
            },
            onDelete = { onDeleteProxy(proxy.id) }
          )
        }
      }
    }

    // Floating Add Button
    FloatingActionButton(
      onClick = {
        proxyToEdit = null
        showEditDialog = true
      },
      containerColor = CyanAccent,
      contentColor = DarkBg,
      shape = RoundedCornerShape(16.dp),
      modifier = Modifier
        .align(Alignment.BottomEnd)
        .padding(16.dp)
        .testTag("add_proxy_fab")
    ) {
      Icon(Icons.Default.Add, contentDescription = "Add Proxy")
    }
  }

  if (showEditDialog) {
    EditProxyDialog(
      proxyToEdit = proxyToEdit,
      onDismiss = { showEditDialog = false },
      onSave = { updated ->
        if (proxyToEdit == null) onAddProxy(updated) else onUpdateProxy(updated)
        showEditDialog = false
      }
    )
  }

  if (showBulkDialog) {
    BulkProxyUploadDialog(
      onDismiss = { showBulkDialog = false },
      onImportBulkProxies = { list ->
        onAddBulkProxies(list)
        showBulkDialog = false
      }
    )
  }
}
