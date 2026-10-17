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
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.DynamicFeed
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import com.example.stealthbrowser.model.BrowserProfile
import com.example.stealthbrowser.model.ProxyConfig
import com.example.stealthbrowser.ui.components.CompactProfileCard
import com.example.stealthbrowser.ui.theme.CyanAccent
import com.example.stealthbrowser.ui.theme.DarkBg
import com.example.stealthbrowser.data.BookmarkEntity
import com.example.stealthbrowser.ui.theme.DarkBorder
import com.example.stealthbrowser.ui.theme.DarkSurface
import com.example.stealthbrowser.ui.theme.DarkSurfaceElevated
import com.example.stealthbrowser.ui.theme.EmeraldConnected
import com.example.stealthbrowser.ui.theme.TextMuted
import com.example.stealthbrowser.ui.theme.TextPrimary
import com.example.stealthbrowser.ui.theme.TextSecondary

@Composable
fun ProfilesScreen(
  profiles: List<BrowserProfile>,
  proxies: List<ProxyConfig>,
  bookmarks: List<BookmarkEntity> = emptyList(),
  selectedProfileIds: Set<String>,
  onToggleSelect: (String) -> Unit,
  onSelectAll: (Boolean) -> Unit,
  onLaunchProfile: (BrowserProfile) -> Unit,
  onLaunchSelectedSideBySide: () -> Unit,
  onAddProfile: (BrowserProfile) -> Unit,
  onAddBulkProfiles: (List<BrowserProfile>) -> Unit,
  onUpdateProfile: (BrowserProfile) -> Unit,
  onDeleteProfile: (String) -> Unit,
  onCloneProfile: ((String) -> Unit)? = null,
  onExportBackup: (() -> String)? = null,
  onImportBackup: ((String) -> Int)? = null,
  onAddBookmarkToRoom: ((String, String, String?) -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  var showEditDialog by remember { mutableStateOf(false) }
  var showBulkDialog by remember { mutableStateOf(false) }
  var showBackupDialog by remember { mutableStateOf(false) }
  var backupImportText by remember { mutableStateOf("") }
  var isExportMode by remember { mutableStateOf(true) }
  var profileToEdit by remember { mutableStateOf<BrowserProfile?>(null) }
  val clipboardManager = LocalClipboardManager.current
  val context = LocalContext.current

  Box(modifier = modifier.fillMaxSize().background(DarkBg)) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 12.dp)
    ) {
      Spacer(modifier = Modifier.height(10.dp))

      // Compact Multi-Launch Bar
      Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
        border = BorderStroke(1.dp, CyanAccent.copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f, fill = false)
          ) {
            Box(
              modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(CyanAccent.copy(alpha = 0.15f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.GridView,
                contentDescription = null,
                tint = CyanAccent,
                modifier = Modifier.size(15.dp)
              )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Column {
              Text(
                text = "Multi-Launch",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
              )
              Text(
                text = "${selectedProfileIds.size}/4 Selected",
                fontSize = 9.sp,
                color = if (selectedProfileIds.isNotEmpty()) EmeraldConnected else TextMuted
              )
            }
          }

          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            // Bulk Create Pill
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = DarkSurface,
              border = BorderStroke(0.8.dp, DarkBorder),
              onClick = { showBulkDialog = true }
            ) {
              Text(
                text = "+Bulk",
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextSecondary,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
              )
            }

            // Select All / Deselect Pill
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = DarkSurface,
              border = BorderStroke(0.8.dp, DarkBorder),
              onClick = { onSelectAll(selectedProfileIds.size < 4) }
            ) {
              Text(
                text = if (selectedProfileIds.size == 4) "Clear" else "Select (4)",
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = CyanAccent,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
              )
            }

            // Compact Launch Button
            Button(
              onClick = onLaunchSelectedSideBySide,
              enabled = selectedProfileIds.isNotEmpty(),
              shape = RoundedCornerShape(6.dp),
              colors = ButtonDefaults.buttonColors(
                containerColor = CyanAccent,
                contentColor = DarkBg,
                disabledContainerColor = DarkBorder.copy(alpha = 0.6f),
                disabledContentColor = TextMuted
              ),
              contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
              modifier = Modifier
                .height(28.dp)
                .testTag("launch_multi_browser_btn")
            ) {
              Text(
                text = if (selectedProfileIds.isEmpty()) "Launch" else "Launch (${selectedProfileIds.size})",
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Profiles Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Browsing Profiles (${profiles.size})",
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
          color = TextPrimary
        )

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          // Backup & Export/Import JSON Button
          Surface(
            shape = RoundedCornerShape(6.dp),
            color = EmeraldConnected.copy(alpha = 0.15f),
            border = BorderStroke(1.dp, EmeraldConnected.copy(alpha = 0.4f)),
            onClick = {
              isExportMode = true
              showBackupDialog = true
            }
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
              Icon(Icons.Default.Backup, contentDescription = null, tint = EmeraldConnected, modifier = Modifier.size(11.dp))
              Spacer(modifier = Modifier.width(3.dp))
              Text(
                text = "Backup (JSON)",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = EmeraldConnected
              )
            }
          }

          // + Bulk Profiles Button
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
              Icon(Icons.Default.Add, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(11.dp))
              Spacer(modifier = Modifier.width(3.dp))
              Text(
                text = "+ Bulk",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = CyanAccent
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(6.dp))

      // Profiles List
      LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
      ) {
        items(profiles, key = { it.id }) { profile ->
          val linkedProxy = proxies.find { it.id == profile.proxyId }
          CompactProfileCard(
            profile = profile,
            linkedProxy = linkedProxy,
            isSelected = selectedProfileIds.contains(profile.id),
            onToggleSelect = { onToggleSelect(profile.id) },
            onLaunch = { onLaunchProfile(profile) },
            onEdit = {
              profileToEdit = profile
              showEditDialog = true
            },
            onClone = { onCloneProfile?.invoke(profile.id) },
            onDelete = { onDeleteProfile(profile.id) }
          )
        }
      }
    }

    // Floating Add Button
    FloatingActionButton(
      onClick = {
        profileToEdit = null
        showEditDialog = true
      },
      containerColor = CyanAccent,
      contentColor = DarkBg,
      shape = RoundedCornerShape(16.dp),
      modifier = Modifier
        .align(Alignment.BottomEnd)
        .padding(16.dp)
        .testTag("add_profile_fab")
    ) {
      Icon(Icons.Default.Add, contentDescription = "Add Profile")
    }
  }

  if (showEditDialog) {
    EditProfileDialog(
      profileToEdit = profileToEdit,
      availableProxies = proxies,
      savedBookmarks = bookmarks,
      onAddBookmarkToRoom = onAddBookmarkToRoom,
      onDismiss = { showEditDialog = false },
      onSave = { updated ->
        if (profileToEdit == null) onAddProfile(updated) else onUpdateProfile(updated)
        showEditDialog = false
      }
    )
  }

  if (showBulkDialog) {
    BulkProfileDialog(
      onDismiss = { showBulkDialog = false },
      onGenerateBulk = { newProfiles ->
        onAddBulkProfiles(newProfiles)
        showBulkDialog = false
      }
    )
  }

  // Profile Backup & Restore Dialog (JSON Export/Import)
  if (showBackupDialog) {
    AlertDialog(
      onDismissRequest = { showBackupDialog = false },
      title = {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Backup, contentDescription = null, tint = EmeraldConnected, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Profiles Backup & Restore", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
          }
        }
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          // Tab Switch: Export vs Import
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = if (isExportMode) CyanAccent.copy(alpha = 0.2f) else DarkSurfaceElevated,
              border = BorderStroke(1.dp, if (isExportMode) CyanAccent else DarkBorder),
              onClick = { isExportMode = true },
              modifier = Modifier.weight(1f)
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(vertical = 6.dp)
              ) {
                Icon(Icons.Default.FileDownload, contentDescription = null, tint = if (isExportMode) CyanAccent else TextMuted, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Export JSON", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isExportMode) CyanAccent else TextSecondary)
              }
            }

            Surface(
              shape = RoundedCornerShape(6.dp),
              color = if (!isExportMode) EmeraldConnected.copy(alpha = 0.2f) else DarkSurfaceElevated,
              border = BorderStroke(1.dp, if (!isExportMode) EmeraldConnected else DarkBorder),
              onClick = { isExportMode = false },
              modifier = Modifier.weight(1f)
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(vertical = 6.dp)
              ) {
                Icon(Icons.Default.FileUpload, contentDescription = null, tint = if (!isExportMode) EmeraldConnected else TextMuted, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Import JSON", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (!isExportMode) EmeraldConnected else TextSecondary)
              }
            }
          }

          if (isExportMode) {
            val exportJson = remember(profiles) { onExportBackup?.invoke() ?: "[]" }
            Text(
              text = "Export ${profiles.size} stealth profiles (including user scripts, cookies, hardware configs):",
              fontSize = 11.sp,
              color = TextSecondary
            )
            OutlinedTextField(
              value = exportJson,
              onValueChange = {},
              readOnly = true,
              maxLines = 7,
              modifier = Modifier
                .fillMaxWidth()
                .height(130.dp),
              colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CyanAccent,
                unfocusedBorderColor = DarkBorder,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
              )
            )
            Button(
              onClick = {
                clipboardManager.setText(AnnotatedString(exportJson))
                Toast.makeText(context, "Copied backup JSON to clipboard!", Toast.LENGTH_SHORT).show()
              },
              colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = DarkBg),
              modifier = Modifier.fillMaxWidth()
            ) {
              Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Copy Backup JSON to Clipboard", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
          } else {
            Text(
              text = "Paste your exported JSON profiles backup below to restore:",
              fontSize = 11.sp,
              color = TextSecondary
            )
            OutlinedTextField(
              value = backupImportText,
              onValueChange = { backupImportText = it },
              placeholder = { Text("Paste JSON array here...", fontSize = 10.sp) },
              maxLines = 7,
              modifier = Modifier
                .fillMaxWidth()
                .height(130.dp),
              colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = EmeraldConnected,
                unfocusedBorderColor = DarkBorder,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
              )
            )
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Button(
                onClick = {
                  val clip = clipboardManager.getText()?.text
                  if (!clip.isNullOrBlank()) {
                    backupImportText = clip
                    Toast.makeText(context, "Pasted from clipboard!", Toast.LENGTH_SHORT).show()
                  }
                },
                colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated, contentColor = CyanAccent),
                modifier = Modifier.weight(1f)
              ) {
                Text("Paste Clipboard", fontSize = 10.sp)
              }
              Button(
                onClick = {
                  if (backupImportText.isNotBlank()) {
                    val count = onImportBackup?.invoke(backupImportText) ?: 0
                    if (count > 0) {
                      Toast.makeText(context, "Successfully restored $count profiles!", Toast.LENGTH_SHORT).show()
                      showBackupDialog = false
                    } else {
                      Toast.makeText(context, "Invalid JSON format. Check format and try again.", Toast.LENGTH_SHORT).show()
                    }
                  }
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldConnected, contentColor = DarkBg),
                modifier = Modifier.weight(1f)
              ) {
                Text("Import Profiles", fontSize = 10.sp, fontWeight = FontWeight.Bold)
              }
            }
          }
        }
      },
      confirmButton = {},
      dismissButton = {
        OutlinedButton(onClick = { showBackupDialog = false }) {
          Text("Close", color = TextSecondary, fontSize = 11.sp)
        }
      },
      containerColor = DarkSurface
    )
  }
}
