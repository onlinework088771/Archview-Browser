package com.example.stealthbrowser.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stealthbrowser.model.ProxyConfig
import com.example.stealthbrowser.model.ProxyProtocol
import com.example.stealthbrowser.ui.theme.CyanAccent
import com.example.stealthbrowser.ui.theme.DarkBg
import com.example.stealthbrowser.ui.theme.DarkBorder
import com.example.stealthbrowser.ui.theme.DarkSurface
import com.example.stealthbrowser.ui.theme.DarkSurfaceElevated
import com.example.stealthbrowser.ui.theme.TextPrimary
import com.example.stealthbrowser.ui.theme.TextSecondary

@Composable
fun EditProxyDialog(
  proxyToEdit: ProxyConfig?,
  onDismiss: () -> Unit,
  onSave: (ProxyConfig) -> Unit
) {
  var name by remember { mutableStateOf(proxyToEdit?.name ?: "US Residential Proxy") }
  var protocol by remember { mutableStateOf(proxyToEdit?.protocol ?: ProxyProtocol.SOCKS5) }
  var host by remember { mutableStateOf(proxyToEdit?.host ?: "198.51.100.1") }
  var port by remember { mutableStateOf((proxyToEdit?.port ?: 1080).toString()) }
  var username by remember { mutableStateOf(proxyToEdit?.username ?: "") }
  var password by remember { mutableStateOf(proxyToEdit?.password ?: "") }
  var countryCode by remember { mutableStateOf(proxyToEdit?.countryCode ?: "US") }
  var rotationMinutes by remember { mutableStateOf((proxyToEdit?.rotationIntervalMinutes ?: 10).toString()) }
  var protocolDropdownExpanded by remember { mutableStateOf(false) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = if (proxyToEdit == null) "Add Proxy Node" else "Edit Proxy Node",
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        color = TextPrimary
      )
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        // Proxy Name
        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text("Proxy Name", fontSize = 11.sp) },
          singleLine = true,
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = CyanAccent,
            unfocusedBorderColor = DarkBorder,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary
          ),
          modifier = Modifier.fillMaxWidth()
        )

        // Protocol Selector
        Column {
          Text("Protocol:", fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.SemiBold)
          Spacer(modifier = Modifier.height(4.dp))
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = DarkSurfaceElevated,
            modifier = Modifier
              .fillMaxWidth()
              .clickable { protocolDropdownExpanded = true }
          ) {
            Text(
              text = protocol.name,
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = CyanAccent,
              modifier = Modifier.padding(10.dp)
            )

            DropdownMenu(
              expanded = protocolDropdownExpanded,
              onDismissRequest = { protocolDropdownExpanded = false },
              modifier = Modifier.background(DarkSurface)
            ) {
              ProxyProtocol.values().forEach { proto ->
                DropdownMenuItem(
                  text = { Text(proto.name, color = TextPrimary, fontSize = 12.sp) },
                  onClick = {
                    protocol = proto
                    protocolDropdownExpanded = false
                  }
                )
              }
            }
          }
        }

        // Host & Port Row
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          OutlinedTextField(
            value = host,
            onValueChange = { host = it },
            label = { Text("Host / IP", fontSize = 11.sp) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = CyanAccent,
              unfocusedBorderColor = DarkBorder,
              focusedTextColor = TextPrimary,
              unfocusedTextColor = TextPrimary
            ),
            modifier = Modifier.weight(2f)
          )

          OutlinedTextField(
            value = port,
            onValueChange = { port = it },
            label = { Text("Port", fontSize = 11.sp) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = CyanAccent,
              unfocusedBorderColor = DarkBorder,
              focusedTextColor = TextPrimary,
              unfocusedTextColor = TextPrimary
            ),
            modifier = Modifier.weight(1f)
          )
        }

        // Country Code & Rotation Interval
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          OutlinedTextField(
            value = countryCode,
            onValueChange = { countryCode = it.take(2).uppercase() },
            label = { Text("Country", fontSize = 11.sp) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = CyanAccent,
              unfocusedBorderColor = DarkBorder,
              focusedTextColor = TextPrimary,
              unfocusedTextColor = TextPrimary
            ),
            modifier = Modifier.weight(1f)
          )

          OutlinedTextField(
            value = rotationMinutes,
            onValueChange = { rotationMinutes = it },
            label = { Text("Rotate (Min)", fontSize = 11.sp) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = CyanAccent,
              unfocusedBorderColor = DarkBorder,
              focusedTextColor = TextPrimary,
              unfocusedTextColor = TextPrimary
            ),
            modifier = Modifier.weight(1f)
          )
        }

        // Optional Auth
        OutlinedTextField(
          value = username,
          onValueChange = { username = it },
          label = { Text("Username (Optional)", fontSize = 11.sp) },
          singleLine = true,
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = CyanAccent,
            unfocusedBorderColor = DarkBorder,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary
          ),
          modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
          value = password,
          onValueChange = { password = it },
          label = { Text("Password (Optional)", fontSize = 11.sp) },
          singleLine = true,
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = CyanAccent,
            unfocusedBorderColor = DarkBorder,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary
          ),
          modifier = Modifier.fillMaxWidth()
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val parsedPort = port.toIntOrNull() ?: 1080
          val parsedRotation = rotationMinutes.toIntOrNull() ?: 10
          val newProxy = (proxyToEdit ?: ProxyConfig(host = host, port = parsedPort, name = name)).copy(
            name = name.ifBlank { "Proxy $host" },
            protocol = protocol,
            host = host.ifBlank { "127.0.0.1" },
            port = parsedPort,
            username = username.ifBlank { null },
            password = password.ifBlank { null },
            countryCode = countryCode.ifBlank { "US" },
            rotationIntervalMinutes = parsedRotation
          )
          onSave(newProxy)
        },
        colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = DarkBg)
      ) {
        Text("Save Proxy", fontWeight = FontWeight.Bold, fontSize = 12.sp)
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
