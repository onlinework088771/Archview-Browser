package com.example.stealthbrowser.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stealthbrowser.model.BrowserProfile
import com.example.stealthbrowser.stealth.OperatingSystemType
import com.example.stealthbrowser.stealth.StealthProfileGenerator
import com.example.stealthbrowser.ui.theme.CyanAccent
import com.example.stealthbrowser.ui.theme.DarkBg
import com.example.stealthbrowser.ui.theme.DarkBorder
import com.example.stealthbrowser.ui.theme.DarkSurface
import com.example.stealthbrowser.ui.theme.DarkSurfaceElevated
import com.example.stealthbrowser.ui.theme.TextSecondary
import kotlin.math.roundToInt

@Composable
fun BulkProfileDialog(
  onDismiss: () -> Unit,
  onGenerateBulk: (List<BrowserProfile>) -> Unit
) {
  var selectedOs by remember { mutableStateOf(OperatingSystemType.ANDROID) }
  var profileCount by remember { mutableFloatStateOf(4f) }
  var startupUrl by remember { mutableStateOf("https://www.google.com") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = "Bulk Profile Generator",
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        color = CyanAccent
      )
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        Text(
          text = "Generate multiple unique, undetectable profiles with randomized real hardware, authentic User-Agents & fingerprints.",
          fontSize = 11.sp,
          color = TextSecondary,
          lineHeight = 15.sp
        )

        // 1. Select OS: Android / Windows / macOS / Linux
        Text(
          text = "Select Platform / OS:",
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          color = CyanAccent
        )
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          OperatingSystemType.values().forEach { os ->
            val isSelected = selectedOs == os
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = if (isSelected) CyanAccent else DarkSurfaceElevated,
              border = BorderStroke(1.dp, if (isSelected) CyanAccent else DarkBorder),
              modifier = Modifier
                .weight(1f)
                .clickable { selectedOs = os }
            ) {
              Text(
                text = os.displayName,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) DarkBg else TextSecondary,
                modifier = Modifier.padding(vertical = 8.dp),
                textAlign = TextAlign.Center
              )
            }
          }
        }

        // 2. Count Slider (1 to 10 profiles)
        Column {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Number of Profiles to Generate:",
              fontSize = 11.sp,
              fontWeight = FontWeight.SemiBold,
              color = TextSecondary
            )
            Text(
              text = "${profileCount.roundToInt()} profiles",
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = CyanAccent
            )
          }

          Slider(
            value = profileCount,
            onValueChange = { profileCount = it },
            valueRange = 1f..10f,
            steps = 8,
            colors = SliderDefaults.colors(
              thumbColor = CyanAccent,
              activeTrackColor = CyanAccent,
              inactiveTrackColor = DarkBorder
            )
          )
        }

        Surface(
          shape = RoundedCornerShape(8.dp),
          color = DarkSurfaceElevated,
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(
            text = "⚡ Each profile will have unique Canvas noise, AudioContext hash, genuine device model, and matching Chrome version.",
            fontSize = 10.sp,
            color = TextSecondary,
            modifier = Modifier.padding(8.dp),
            lineHeight = 13.sp
          )
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val generated = StealthProfileGenerator.generateBulkProfiles(
            osType = selectedOs,
            count = profileCount.roundToInt()
          ).map { profile ->
            profile.copy(startupUrl = startupUrl.ifBlank { "https://www.google.com" })
          }
          onGenerateBulk(generated)
        },
        colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = DarkBg)
      ) {
        Text("Generate ${profileCount.roundToInt()} Profiles", fontWeight = FontWeight.Bold, fontSize = 12.sp)
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
