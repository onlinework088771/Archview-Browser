package com.example.stealthbrowser.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stealthbrowser.model.ProxyConfig
import com.example.stealthbrowser.model.ProxyProtocol
import com.example.stealthbrowser.model.ProxyStatus
import com.example.stealthbrowser.ui.theme.CyanAccent
import com.example.stealthbrowser.ui.theme.DarkBg
import com.example.stealthbrowser.ui.theme.DarkBorder
import com.example.stealthbrowser.ui.theme.DarkSurface
import com.example.stealthbrowser.ui.theme.DarkSurfaceElevated
import com.example.stealthbrowser.ui.theme.TextMuted
import com.example.stealthbrowser.ui.theme.TextPrimary
import com.example.stealthbrowser.ui.theme.TextSecondary

@Composable
fun BulkProxyUploadDialog(
  onDismiss: () -> Unit,
  onImportBulkProxies: (List<ProxyConfig>) -> Unit
) {
  var proxyText by remember {
    mutableStateOf(
      """# Paste your proxies below (one per line):
# Format: ip:port or ip:port:username:password or protocol://ip:port
104.28.19.45:1080
185.190.140.22:8080:myuser:mypass
socks5://146.70.12.90:1080
103.110.155.12:1080""".trimIndent()
    )
  }

  var parsedProxies by remember { mutableStateOf<List<ProxyConfig>>(emptyList()) }
  var errorMessage by remember { mutableStateOf<String?>(null) }

  fun parseProxies(text: String): List<ProxyConfig> {
    val lines = text.lines()
      .map { it.trim() }
      .filter { it.isNotEmpty() && !it.startsWith("#") }

    val list = mutableListOf<ProxyConfig>()
    lines.forEachIndexed { idx, rawLine ->
      try {
        var line = rawLine
        var protocol = ProxyProtocol.SOCKS5

        if (line.startsWith("socks5://", ignoreCase = true)) {
          protocol = ProxyProtocol.SOCKS5
          line = line.substringAfter("socks5://")
        } else if (line.startsWith("http://", ignoreCase = true)) {
          protocol = ProxyProtocol.HTTP
          line = line.substringAfter("http://")
        } else if (line.startsWith("https://", ignoreCase = true)) {
          protocol = ProxyProtocol.HTTPS
          line = line.substringAfter("https://")
        }

        // Support ip:port:user:pass or ip:port
        val parts = line.split(":")
        if (parts.size >= 2) {
          val host = parts[0].trim()
          val port = parts[1].trim().toIntOrNull() ?: 1080
          val username = if (parts.size >= 3) parts[2].trim() else null
          val password = if (parts.size >= 4) parts[3].trim() else null

          list.add(
            ProxyConfig(
              name = "Proxy #${idx + 1} ($host)",
              protocol = protocol,
              host = host,
              port = port,
              username = username,
              password = password,
              countryCode = listOf("US", "DE", "GB", "SG", "FR", "CA").random(),
              status = ProxyStatus.UNTESTED
            )
          )
        }
      } catch (e: Exception) {
        // Skip invalid line
      }
    }
    return list
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = "Bulk Proxy Upload",
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        color = CyanAccent
      )
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Text(
          text = "Paste multiple proxies (one per line). Supports IP:Port, IP:Port:User:Pass, or socks5://host:port",
          fontSize = 11.sp,
          color = TextSecondary,
          lineHeight = 14.sp
        )

        OutlinedTextField(
          value = proxyText,
          onValueChange = {
            proxyText = it
            errorMessage = null
          },
          label = { Text("Proxy List", fontSize = 11.sp) },
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = CyanAccent,
            unfocusedBorderColor = DarkBorder,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary
          ),
          textStyle = androidx.compose.ui.text.TextStyle(
            fontFamily = FontFamily.Monospace,
            fontSize = 10.sp
          ),
          modifier = Modifier
            .fillMaxWidth()
            .height(160.dp),
          maxLines = 10
        )

        Surface(
          shape = RoundedCornerShape(8.dp),
          color = DarkSurfaceElevated,
          modifier = Modifier.fillMaxWidth()
        ) {
          val count = parseProxies(proxyText).size
          Text(
            text = "Detected valid proxies: $count",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (count > 0) CyanAccent else TextMuted,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
          )
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val list = parseProxies(proxyText)
          if (list.isNotEmpty()) {
            onImportBulkProxies(list)
          } else {
            errorMessage = "No valid proxies found"
          }
        },
        colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = DarkBg)
      ) {
        Text("Import Proxies", fontWeight = FontWeight.Bold, fontSize = 12.sp)
      }
    },
    dismissButton = {
      OutlinedButton(onClick = onDismiss) {
        Text("Cancel", color = TextSecondary, fontSize = 12.sp)
      }
    },
    containerColor = DarkSurface
  )
}
