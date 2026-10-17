package com.example.stealthbrowser.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stealthbrowser.data.BookmarkEntity
import com.example.stealthbrowser.model.BrowserProfile
import com.example.stealthbrowser.model.ProxyConfig
import com.example.stealthbrowser.ui.theme.CyanAccent
import com.example.stealthbrowser.ui.theme.DarkBg
import com.example.stealthbrowser.ui.theme.DarkBorder
import com.example.stealthbrowser.ui.theme.DarkSurface
import com.example.stealthbrowser.ui.theme.DarkSurfaceElevated
import com.example.stealthbrowser.ui.theme.EmeraldConnected
import com.example.stealthbrowser.ui.theme.TextMuted
import com.example.stealthbrowser.ui.theme.TextPrimary
import com.example.stealthbrowser.ui.theme.TextSecondary

private val DangerRed = Color(0xFFFF5252)
private val WarningOrange = Color(0xFFFF9100)
private val CookieGold = Color(0xFFFFD700)

@Composable
fun DataManagementScreen(
  profiles: List<BrowserProfile>,
  proxies: List<ProxyConfig>,
  bookmarks: List<BookmarkEntity>,
  onDeleteProfilePermanently: (String) -> Unit,
  onBatchDeleteProfilesPermanently: (Set<String>, deleteBookmarks: Boolean, wipeCookies: Boolean) -> Unit,
  onCleanOrphanedData: (() -> Unit) -> Unit,
  onClearProfileCookies: (String) -> Unit,
  onClearAllCookies: () -> Unit,
  onDeepCleanBrowserData: (Context, wipeBookmarks: Boolean) -> Unit,
  onFactoryResetAll: (Context) -> Unit
) {
  val context = LocalContext.current

  // Selection state for batch actions
  var selectedProfileIds by remember { mutableStateOf<Set<String>>(emptySet()) }
  var searchQuery by remember { mutableStateOf("") }

  // Dialog triggers
  var showBatchDeleteDialog by remember { mutableStateOf(false) }
  var showSingleDeleteDialogForId by remember { mutableStateOf<String?>(null) }
  var showCleanOrphansDialog by remember { mutableStateOf(false) }
  var showDeepCleanDialog by remember { mutableStateOf(false) }
  var showFactoryResetDialog by remember { mutableStateOf(false) }

  // Batch delete options
  var batchDeleteBookmarksOption by remember { mutableStateOf(true) }
  var batchWipeCookiesOption by remember { mutableStateOf(true) }

  // Filter profiles based on search
  val filteredProfiles = remember(profiles, searchQuery) {
    if (searchQuery.isBlank()) {
      profiles
    } else {
      profiles.filter {
        it.name.contains(searchQuery, ignoreCase = true) ||
          it.deviceModel.contains(searchQuery, ignoreCase = true) ||
          it.androidVersion.contains(searchQuery, ignoreCase = true)
      }
    }
  }

  // Calculate stats
  val validProfileIds = remember(profiles) { profiles.map { it.id }.toSet() }
  val orphanedBookmarksCount = remember(bookmarks, validProfileIds) {
    bookmarks.count { it.profileId != null && it.profileId !in validProfileIds }
  }
  val profilesWithCookiesCount = remember(profiles) {
    profiles.count { it.savedCookies.isNotBlank() }
  }
  val isAllSelected by remember(filteredProfiles, selectedProfileIds) {
    derivedStateOf {
      filteredProfiles.isNotEmpty() && selectedProfileIds.containsAll(filteredProfiles.map { it.id })
    }
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(DarkBg)
      .padding(horizontal = 12.dp, vertical = 8.dp)
  ) {
    // 1. Screen Header
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.Storage,
            contentDescription = null,
            tint = CyanAccent,
            modifier = Modifier.size(20.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Data Management",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
          )
        }
        Text(
          text = "Permanent data purge & zero-trace storage cleanup",
          fontSize = 10.sp,
          color = TextSecondary
        )
      }

      // Quick Orphan Cleaner button
      Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (orphanedBookmarksCount > 0) WarningOrange.copy(alpha = 0.2f) else DarkSurfaceElevated,
        border = BorderStroke(1.dp, if (orphanedBookmarksCount > 0) WarningOrange else DarkBorder),
        onClick = { showCleanOrphansDialog = true },
        modifier = Modifier.testTag("clean_orphans_quick_btn")
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
          Icon(
            imageVector = Icons.Default.CleaningServices,
            contentDescription = "Clean Residuals",
            tint = if (orphanedBookmarksCount > 0) WarningOrange else CyanAccent,
            modifier = Modifier.size(14.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = if (orphanedBookmarksCount > 0) "Clean ($orphanedBookmarksCount)" else "Clean Residue",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = if (orphanedBookmarksCount > 0) WarningOrange else TextPrimary
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    // 2. Storage Overview Stats Grid
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      // Card 1: Profiles
      StatCard(
        label = "Profiles",
        value = "${profiles.size}",
        subtitle = "Configured",
        accentColor = CyanAccent,
        modifier = Modifier.weight(1f)
      )

      // Card 2: Room Bookmarks
      StatCard(
        label = "Bookmarks",
        value = "${bookmarks.size}",
        subtitle = if (orphanedBookmarksCount > 0) "$orphanedBookmarksCount orphaned" else "Room DB",
        accentColor = if (orphanedBookmarksCount > 0) WarningOrange else EmeraldConnected,
        modifier = Modifier.weight(1f)
      )

      // Card 3: Saved Cookies
      StatCard(
        label = "Live Cookies",
        value = "$profilesWithCookiesCount",
        subtitle = "Active profiles",
        accentColor = CookieGold,
        modifier = Modifier.weight(1f)
      )

      // Card 4: Orphaned Data
      StatCard(
        label = "Residuals",
        value = "$orphanedBookmarksCount",
        subtitle = "Removed items",
        accentColor = if (orphanedBookmarksCount > 0) DangerRed else TextMuted,
        modifier = Modifier.weight(1f)
      )
    }

    Spacer(modifier = Modifier.height(10.dp))

    // 3. Quick Action Bar (Deep Clean & Wiping)
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      Surface(
        shape = RoundedCornerShape(8.dp),
        color = DarkSurface,
        border = BorderStroke(1.dp, CyanAccent.copy(alpha = 0.35f)),
        onClick = { showDeepCleanDialog = true },
        modifier = Modifier
          .weight(1f)
          .testTag("deep_clean_cache_btn")
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Center
        ) {
          Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(15.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Wipe WebView Cache", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = CyanAccent)
        }
      }

      Surface(
        shape = RoundedCornerShape(8.dp),
        color = DarkSurface,
        border = BorderStroke(1.dp, DangerRed.copy(alpha = 0.35f)),
        onClick = { showFactoryResetDialog = true },
        modifier = Modifier
          .weight(1f)
          .testTag("factory_reset_dialog_btn")
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Center
        ) {
          Icon(Icons.Default.DeleteForever, contentDescription = null, tint = DangerRed, modifier = Modifier.size(15.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Nuclear Wipe All", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = DangerRed)
        }
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    // 4. Batch Selection & Filter Bar
    Card(
      shape = RoundedCornerShape(10.dp),
      colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
      border = BorderStroke(1.dp, DarkBorder),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Search TextField
        OutlinedTextField(
          value = searchQuery,
          onValueChange = { searchQuery = it },
          placeholder = { Text("Filter profiles by name or device...", fontSize = 11.sp, color = TextMuted) },
          leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
          },
          trailingIcon = {
            if (searchQuery.isNotEmpty()) {
              IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.Clear, contentDescription = "Clear search", tint = TextMuted, modifier = Modifier.size(14.dp))
              }
            }
          },
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .height(46.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = CyanAccent,
            unfocusedBorderColor = DarkBorder,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,
            focusedContainerColor = DarkBg,
            unfocusedContainerColor = DarkBg
          )
        )

        // Select All & Batch Delete Row
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Select All Checkbox & Label
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable {
              selectedProfileIds = if (isAllSelected) {
                emptySet()
              } else {
                filteredProfiles.map { it.id }.toSet()
              }
            }
          ) {
            Checkbox(
              checked = isAllSelected,
              onCheckedChange = { checked ->
                selectedProfileIds = if (checked) {
                  filteredProfiles.map { it.id }.toSet()
                } else {
                  emptySet()
                }
              },
              colors = CheckboxDefaults.colors(
                checkedColor = CyanAccent,
                checkmarkColor = DarkBg,
                uncheckedColor = TextSecondary
              ),
              modifier = Modifier.size(28.dp).testTag("select_all_checkbox")
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = if (isAllSelected) "Deselect All" else "Select All (${filteredProfiles.size})",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = if (isAllSelected) CyanAccent else TextPrimary
            )
          }

          // Batch Action Buttons
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            if (selectedProfileIds.isNotEmpty()) {
              Surface(
                shape = RoundedCornerShape(4.dp),
                color = CyanAccent.copy(alpha = 0.15f),
                border = BorderStroke(0.8.dp, CyanAccent.copy(alpha = 0.4f))
              ) {
                Text(
                  text = "${selectedProfileIds.size} Selected",
                  fontSize = 9.sp,
                  fontWeight = FontWeight.Bold,
                  color = CyanAccent,
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                )
              }

              Button(
                onClick = { showBatchDeleteDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = DangerRed, contentColor = Color.White),
                shape = RoundedCornerShape(6.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                modifier = Modifier
                  .height(30.dp)
                  .testTag("batch_delete_permanently_btn")
              ) {
                Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(13.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Delete Selected", fontSize = 10.sp, fontWeight = FontWeight.Bold)
              }
            }
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // 5. Profiles List with Permanent Destruction Options
    if (filteredProfiles.isEmpty()) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f),
        contentAlignment = Alignment.Center
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Icon(Icons.Default.Storage, contentDescription = null, tint = TextMuted, modifier = Modifier.size(36.dp))
          Spacer(modifier = Modifier.height(8.dp))
          Text("No profiles match search query", color = TextSecondary, fontSize = 12.sp)
        }
      }
    } else {
      LazyColumn(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
      ) {
        items(filteredProfiles, key = { it.id }) { profile ->
          val isSelected = selectedProfileIds.contains(profile.id)
          val linkedBookmarksCount = remember(bookmarks, profile.id) {
            bookmarks.count { it.profileId == profile.id }
          }
          val linkedProxy = remember(proxies, profile.proxyId) {
            proxies.find { it.id == profile.proxyId }
          }

          ProfileDataCard(
            profile = profile,
            isSelected = isSelected,
            bookmarksCount = linkedBookmarksCount,
            linkedProxy = linkedProxy,
            onToggleSelect = {
              selectedProfileIds = if (isSelected) {
                selectedProfileIds - profile.id
              } else {
                selectedProfileIds + profile.id
              }
            },
            onClearCookies = {
              onClearProfileCookies(profile.id)
              Toast.makeText(context, "Cookies wiped for '${profile.name}'", Toast.LENGTH_SHORT).show()
            },
            onDeletePermanently = {
              showSingleDeleteDialogForId = profile.id
            }
          )
        }
      }
    }
  }

  // --- DIALOGS ---

  // 1. Batch Delete Confirmation Dialog
  if (showBatchDeleteDialog) {
    AlertDialog(
      onDismissRequest = { showBatchDeleteDialog = false },
      title = {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Warning, contentDescription = null, tint = DangerRed, modifier = Modifier.size(20.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Permanently Delete ${selectedProfileIds.size} Profiles?",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
          )
        }
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(
            text = "This will permanently destroy the selected profiles and close any running windows. Select additional data to purge:",
            fontSize = 12.sp,
            color = TextSecondary,
            lineHeight = 16.sp
          )

          // Checkbox: Delete Bookmarks in Room
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { batchDeleteBookmarksOption = !batchDeleteBookmarksOption }
          ) {
            Checkbox(
              checked = batchDeleteBookmarksOption,
              onCheckedChange = { batchDeleteBookmarksOption = it },
              colors = CheckboxDefaults.colors(checkedColor = DangerRed, checkmarkColor = Color.White)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Column {
              Text("Permanently Delete Room Bookmarks", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
              Text("Deletes all bookmarks saved under these profiles", fontSize = 9.sp, color = TextMuted)
            }
          }

          // Checkbox: Wipe Cookies & Storage
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { batchWipeCookiesOption = !batchWipeCookiesOption }
          ) {
            Checkbox(
              checked = batchWipeCookiesOption,
              onCheckedChange = { batchWipeCookiesOption = it },
              colors = CheckboxDefaults.colors(checkedColor = DangerRed, checkmarkColor = Color.White)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Column {
              Text("Purge Session Cookies & Storage", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
              Text("Cleans local storage, Web SQL and session data", fontSize = 9.sp, color = TextMuted)
            }
          }

          Box(
            modifier = Modifier
              .fillMaxWidth()
              .background(DangerRed.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
              .padding(8.dp)
          ) {
            Text(
              text = "⚠️ Warning: This permanent erasure cannot be undone. All fingerprint parameters will be discarded.",
              fontSize = 10.sp,
              color = DangerRed,
              fontWeight = FontWeight.Medium
            )
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            val count = selectedProfileIds.size
            onBatchDeleteProfilesPermanently(selectedProfileIds, batchDeleteBookmarksOption, batchWipeCookiesOption)
            selectedProfileIds = emptySet()
            showBatchDeleteDialog = false
            Toast.makeText(context, "Permanently purged $count profiles & associated data", Toast.LENGTH_SHORT).show()
          },
          colors = ButtonDefaults.buttonColors(containerColor = DangerRed, contentColor = Color.White)
        ) {
          Text("Permanently Destroy", fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        OutlinedButton(onClick = { showBatchDeleteDialog = false }) {
          Text("Cancel", fontSize = 11.sp, color = TextSecondary)
        }
      },
      containerColor = DarkSurface
    )
  }

  // 2. Single Profile Delete Dialog
  showSingleDeleteDialogForId?.let { profileId ->
    val targetProfile = profiles.find { it.id == profileId }
    AlertDialog(
      onDismissRequest = { showSingleDeleteDialogForId = null },
      title = {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.DeleteForever, contentDescription = null, tint = DangerRed, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text("Destroy '${targetProfile?.name}'?", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        }
      },
      text = {
        Text(
          text = "Permanently remove this profile along with all its saved bookmarks in the Room database, session cookies, and local caches.",
          fontSize = 11.sp,
          color = TextSecondary,
          lineHeight = 15.sp
        )
      },
      confirmButton = {
        Button(
          onClick = {
            onDeleteProfilePermanently(profileId)
            selectedProfileIds = selectedProfileIds - profileId
            showSingleDeleteDialogForId = null
            Toast.makeText(context, "Profile and associated data purged cleanly", Toast.LENGTH_SHORT).show()
          },
          colors = ButtonDefaults.buttonColors(containerColor = DangerRed, contentColor = Color.White)
        ) {
          Text("Purge Profile", fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        OutlinedButton(onClick = { showSingleDeleteDialogForId = null }) {
          Text("Cancel", fontSize = 11.sp, color = TextSecondary)
        }
      },
      containerColor = DarkSurface
    )
  }

  // 3. Clean Orphaned Data Dialog
  if (showCleanOrphansDialog) {
    AlertDialog(
      onDismissRequest = { showCleanOrphansDialog = false },
      title = {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.CleaningServices, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text("Purge Residual & Orphaned Data", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        }
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text(
            text = "When profiles are deleted, residual database bookmarks, stale storage keys, and disconnected cookies can remain in system memory.",
            fontSize = 11.sp,
            color = TextSecondary,
            lineHeight = 15.sp
          )
          Text(
            text = "Found $orphanedBookmarksCount orphaned bookmarks from removed profiles.",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (orphanedBookmarksCount > 0) WarningOrange else EmeraldConnected
          )
          Text(
            text = "This will flush the system CookieManager, clean orphaned bookmarks from Room, and wipe stale WebStorage.",
            fontSize = 10.sp,
            color = TextMuted
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            onCleanOrphanedData {
              Toast.makeText(context, "Orphaned data cleaned completely!", Toast.LENGTH_SHORT).show()
              showCleanOrphansDialog = false
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = DarkBg)
        ) {
          Text("Wipe Residuals Now", fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        OutlinedButton(onClick = { showCleanOrphansDialog = false }) {
          Text("Cancel", fontSize = 11.sp, color = TextSecondary)
        }
      },
      containerColor = DarkSurface
    )
  }

  // 4. Deep Clean WebView Cache Dialog
  if (showDeepCleanDialog) {
    var wipeRoomBookmarksAlso by remember { mutableStateOf(false) }
    AlertDialog(
      onDismissRequest = { showDeepCleanDialog = false },
      title = {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text("Wipe All WebView Cache & Storage", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        }
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(
            text = "This purges all HTTP caches, cookies, HTML5 local storage, and Web SQL databases from disk.",
            fontSize = 11.sp,
            color = TextSecondary
          )
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { wipeRoomBookmarksAlso = !wipeRoomBookmarksAlso }
          ) {
            Checkbox(
              checked = wipeRoomBookmarksAlso,
              onCheckedChange = { wipeRoomBookmarksAlso = it },
              colors = CheckboxDefaults.colors(checkedColor = DangerRed, checkmarkColor = Color.White)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text("Also wipe all saved bookmarks (${bookmarks.size})", fontSize = 11.sp, color = TextPrimary)
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            onDeepCleanBrowserData(context, wipeRoomBookmarksAlso)
            showDeepCleanDialog = false
            Toast.makeText(context, "WebView cache & cookies wiped clean", Toast.LENGTH_SHORT).show()
          },
          colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = DarkBg)
        ) {
          Text("Purge Cache", fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        OutlinedButton(onClick = { showDeepCleanDialog = false }) {
          Text("Cancel", fontSize = 11.sp, color = TextSecondary)
        }
      },
      containerColor = DarkSurface
    )
  }

  // 5. Nuclear Factory Reset Dialog
  if (showFactoryResetDialog) {
    AlertDialog(
      onDismissRequest = { showFactoryResetDialog = false },
      title = {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Warning, contentDescription = null, tint = DangerRed, modifier = Modifier.size(20.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text("Factory Reset (Zero-Trace Wipe)", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = DangerRed)
        }
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text(
            text = "Are you sure you want to perform a nuclear reset?",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
          )
          Text(
            text = "This will close all floating windows, wipe all bookmarks from Room, clear all cookies and cache, and restore default clean profiles. All custom profiles and data will be destroyed.",
            fontSize = 11.sp,
            color = TextSecondary,
            lineHeight = 15.sp
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            onFactoryResetAll(context)
            selectedProfileIds = emptySet()
            showFactoryResetDialog = false
            Toast.makeText(context, "App reset to pristine zero-trace state!", Toast.LENGTH_SHORT).show()
          },
          colors = ButtonDefaults.buttonColors(containerColor = DangerRed, contentColor = Color.White)
        ) {
          Text("Yes, Nuclear Reset", fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        OutlinedButton(onClick = { showFactoryResetDialog = false }) {
          Text("Cancel", fontSize = 11.sp, color = TextSecondary)
        }
      },
      containerColor = DarkSurface
    )
  }
}

@Composable
private fun StatCard(
  label: String,
  value: String,
  subtitle: String,
  accentColor: Color,
  modifier: Modifier = Modifier
) {
  Card(
    shape = RoundedCornerShape(8.dp),
    colors = CardDefaults.cardColors(containerColor = DarkSurface),
    border = BorderStroke(1.dp, DarkBorder),
    modifier = modifier
  ) {
    Column(
      modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text(label, fontSize = 9.sp, color = TextSecondary, maxLines = 1)
      Text(
        text = value,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = accentColor
      )
      Text(
        text = subtitle,
        fontSize = 8.sp,
        color = TextMuted,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    }
  }
}

@Composable
private fun ProfileDataCard(
  profile: BrowserProfile,
  isSelected: Boolean,
  bookmarksCount: Int,
  linkedProxy: ProxyConfig?,
  onToggleSelect: () -> Unit,
  onClearCookies: () -> Unit,
  onDeletePermanently: () -> Unit
) {
  Card(
    shape = RoundedCornerShape(8.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (isSelected) DarkSurfaceElevated else DarkSurface
    ),
    border = BorderStroke(
      1.dp,
      if (isSelected) CyanAccent else DarkBorder
    ),
    modifier = Modifier
      .fillMaxWidth()
      .testTag("profile_data_card_${profile.id}")
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 10.dp, vertical = 8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Checkbox
      Checkbox(
        checked = isSelected,
        onCheckedChange = { onToggleSelect() },
        colors = CheckboxDefaults.colors(
          checkedColor = CyanAccent,
          checkmarkColor = DarkBg,
          uncheckedColor = TextSecondary
        ),
        modifier = Modifier.size(28.dp)
      )

      Spacer(modifier = Modifier.width(6.dp))

      // Profile details
      Column(modifier = Modifier.weight(1f)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          // OS Icon
          Icon(
            imageVector = if (profile.androidVersion.contains("Windows") || profile.androidVersion.contains("macOS")) {
              Icons.Default.Laptop
            } else {
              Icons.Default.PhoneAndroid
            },
            contentDescription = null,
            tint = CyanAccent,
            modifier = Modifier.size(13.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = profile.name,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }

        Spacer(modifier = Modifier.height(3.dp))

        // Metadata Pills Row
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          // Bookmarks count pill
          Surface(
            shape = RoundedCornerShape(4.dp),
            color = if (bookmarksCount > 0) EmeraldConnected.copy(alpha = 0.15f) else DarkBg,
            border = BorderStroke(0.6.dp, if (bookmarksCount > 0) EmeraldConnected.copy(alpha = 0.3f) else DarkBorder)
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
            ) {
              Icon(Icons.Default.Bookmark, contentDescription = null, tint = if (bookmarksCount > 0) EmeraldConnected else TextMuted, modifier = Modifier.size(8.dp))
              Spacer(modifier = Modifier.width(2.dp))
              Text(
                text = "$bookmarksCount Bookmarks",
                fontSize = 8.sp,
                fontWeight = FontWeight.Medium,
                color = if (bookmarksCount > 0) EmeraldConnected else TextMuted
              )
            }
          }

          // Cookies status pill
          val hasCookies = profile.savedCookies.isNotBlank()
          Surface(
            shape = RoundedCornerShape(4.dp),
            color = if (hasCookies) CookieGold.copy(alpha = 0.15f) else DarkBg,
            border = BorderStroke(0.6.dp, if (hasCookies) CookieGold.copy(alpha = 0.3f) else DarkBorder)
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
            ) {
              Icon(Icons.Default.Key, contentDescription = null, tint = if (hasCookies) CookieGold else TextMuted, modifier = Modifier.size(8.dp))
              Spacer(modifier = Modifier.width(2.dp))
              Text(
                text = if (hasCookies) "Cookies Saved" else "No Cookies",
                fontSize = 8.sp,
                fontWeight = FontWeight.Medium,
                color = if (hasCookies) CookieGold else TextMuted
              )
            }
          }

          // Proxy Pill
          if (linkedProxy != null) {
            Surface(
              shape = RoundedCornerShape(4.dp),
              color = EmeraldConnected.copy(alpha = 0.12f)
            ) {
              Text(
                text = "${linkedProxy.countryCode} ${linkedProxy.protocol}",
                fontSize = 8.sp,
                color = EmeraldConnected,
                modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp)
              )
            }
          }
        }
      }

      // Action Buttons
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
      ) {
        if (profile.savedCookies.isNotBlank()) {
          IconButton(
            onClick = onClearCookies,
            modifier = Modifier.size(28.dp)
          ) {
            Icon(
              imageVector = Icons.Default.CleaningServices,
              contentDescription = "Clear Cookies",
              tint = CookieGold,
              modifier = Modifier.size(14.dp)
            )
          }
        }

        IconButton(
          onClick = onDeletePermanently,
          modifier = Modifier.size(28.dp)
        ) {
          Icon(
            imageVector = Icons.Default.DeleteForever,
            contentDescription = "Delete Profile Permanently",
            tint = DangerRed,
            modifier = Modifier.size(16.dp)
          )
        }
      }
    }
  }
}
