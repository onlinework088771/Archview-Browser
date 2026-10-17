package com.example.stealthbrowser.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import com.example.stealthbrowser.data.BookmarkEntity
import com.example.stealthbrowser.model.BrowserProfile
import com.example.stealthbrowser.model.ProxyConfig
import com.example.stealthbrowser.stealth.CookieHelper
import com.example.stealthbrowser.stealth.OperatingSystemType
import com.example.stealthbrowser.stealth.StealthProfileGenerator
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
fun EditProfileDialog(
  profileToEdit: BrowserProfile?,
  availableProxies: List<ProxyConfig>,
  savedBookmarks: List<BookmarkEntity> = emptyList(),
  onAddBookmarkToRoom: ((title: String, url: String, profileId: String?) -> Unit)? = null,
  onDismiss: () -> Unit,
  onSave: (BrowserProfile) -> Unit
) {
  var selectedOs by remember {
    mutableStateOf(
      if (profileToEdit?.userAgent?.contains("Windows") == true) OperatingSystemType.WINDOWS
      else if (profileToEdit?.userAgent?.contains("Macintosh") == true) OperatingSystemType.MAC
      else if (profileToEdit?.userAgent?.contains("Linux") == true && profileToEdit.userAgent.contains("Android") != true) OperatingSystemType.LINUX
      else OperatingSystemType.ANDROID
    )
  }

  var name by remember { mutableStateOf(profileToEdit?.name ?: "Samsung Galaxy S24 Ultra (Android 14)") }
  var deviceModel by remember { mutableStateOf(profileToEdit?.deviceModel ?: "SM-S928B") }
  var startupUrl by remember { mutableStateOf(profileToEdit?.startupUrl ?: "https://www.google.com") }
  var showAddBookmarkSection by remember { mutableStateOf(false) }
  var customBookmarkTitle by remember { mutableStateOf("") }
  var customBookmarkUrl by remember { mutableStateOf("") }
  var androidVersionInt by remember {
    val verStr = profileToEdit?.androidVersion?.filter { it.isDigit() }
    mutableStateOf(verStr?.toIntOrNull()?.coerceIn(11, 17) ?: 14)
  }
  var userAgent by remember {
    mutableStateOf(
      profileToEdit?.userAgent
        ?: "Mozilla/5.0 (Linux; Android 14; SM-S928B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.6778.85 Mobile Safari/537.36"
    )
  }
  var secChUa by remember {
    mutableStateOf(profileToEdit?.secChUa ?: "\"Google Chrome\";v=\"131\", \"Chromium\";v=\"131\", \"Not_A Brand\";v=\"24\"")
  }
  var selectedProxyId by remember { mutableStateOf(profileToEdit?.proxyId) }
  var canvasNoise by remember { mutableStateOf(profileToEdit?.canvasNoiseEnabled ?: true) }
  var audioNoise by remember { mutableStateOf(profileToEdit?.audioNoiseEnabled ?: true) }
  var proxyDropdownExpanded by remember { mutableStateOf(false) }

  var userScript by remember { mutableStateOf(profileToEdit?.userScript ?: "") }
  var adBlockEnabled by remember { mutableStateOf(profileToEdit?.adBlockEnabled ?: true) }
  var clearDataOnClose by remember { mutableStateOf(profileToEdit?.clearDataOnClose ?: true) }
  var savedCookies by remember { mutableStateOf(profileToEdit?.savedCookies ?: "") }
  var showCookieImportDialog by remember { mutableStateOf(false) }
  var tempCookieInput by remember { mutableStateOf("") }
  var showUserScriptSection by remember { mutableStateOf(profileToEdit?.userScript?.isNotBlank() == true) }
  val clipboardManager = LocalClipboardManager.current
  val context = LocalContext.current

  // Function to roll a random real phone/device for selected OS
  fun rollRandomDevice() {
    val generated = StealthProfileGenerator.generateProfile(selectedOs)
    name = generated.name
    deviceModel = generated.deviceModel
    userAgent = generated.userAgent
    secChUa = generated.secChUa
    val verInt = generated.androidVersion.filter { it.isDigit() }.toIntOrNull()
    if (verInt != null) androidVersionInt = verInt
  }

  // Regenerate matching User-Agent
  fun regenerateUserAgent() {
    val (newUa, newSec) = StealthProfileGenerator.generateMatchingUserAgent(
      osType = selectedOs,
      androidVersion = androidVersionInt,
      modelName = deviceModel
    )
    userAgent = newUa
    secChUa = newSec
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = if (profileToEdit == null) "Create Profile" else "Edit Profile",
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
          color = TextPrimary
        )

        // Randomize Button
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = CyanAccent.copy(alpha = 0.15f),
          border = androidx.compose.foundation.BorderStroke(1.dp, CyanAccent.copy(alpha = 0.4f)),
          modifier = Modifier.clickable { rollRandomDevice() }
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
          ) {
            Icon(Icons.Default.Autorenew, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(13.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Randomize Real Phone", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CyanAccent)
          }
        }
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        // 1. OS Selector: Android / Windows / macOS / Linux
        Text(
          text = "Select Operating System (OS):",
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
              border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (isSelected) CyanAccent else DarkBorder
              ),
              modifier = Modifier
                .weight(1f)
                .clickable {
                  selectedOs = os
                  rollRandomDevice()
                }
            ) {
              Text(
                text = os.displayName,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) DarkBg else TextSecondary,
                modifier = Modifier.padding(vertical = 7.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
              )
            }
          }
        }

        // 2. If Android, display Random Brand & Version 11-17
        if (selectedOs == OperatingSystemType.ANDROID) {
          Text(
            text = "Android Version (11 to 17):",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextSecondary
          )
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            (11..17).forEach { ver ->
              val isVerSelected = androidVersionInt == ver
              Surface(
                shape = RoundedCornerShape(6.dp),
                color = if (isVerSelected) CyanAccent.copy(alpha = 0.25f) else DarkSurfaceElevated,
                border = androidx.compose.foundation.BorderStroke(
                  1.dp,
                  if (isVerSelected) CyanAccent else DarkBorder.copy(alpha = 0.4f)
                ),
                modifier = Modifier
                  .weight(1f)
                  .clickable {
                    androidVersionInt = ver
                    regenerateUserAgent()
                  }
              ) {
                Text(
                  text = "v$ver",
                  fontSize = 10.sp,
                  fontWeight = if (isVerSelected) FontWeight.Bold else FontWeight.Normal,
                  color = if (isVerSelected) CyanAccent else TextSecondary,
                  modifier = Modifier.padding(vertical = 4.dp),
                  textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
              }
            }
          }
        }

        // Profile Name
        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text("Profile Name", fontSize = 11.sp) },
          singleLine = true,
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = CyanAccent,
            unfocusedBorderColor = DarkBorder,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary
          ),
          modifier = Modifier.fillMaxWidth()
        )

        // Device Model (Random Brand / Real Device)
        OutlinedTextField(
          value = deviceModel,
          onValueChange = {
            deviceModel = it
            regenerateUserAgent()
          },
          label = { Text("Device Model / Market Brand", fontSize = 11.sp) },
          singleLine = true,
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = CyanAccent,
            unfocusedBorderColor = DarkBorder,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary
          ),
          modifier = Modifier.fillMaxWidth()
        )

        // 3. User-Agent Generator Section with Generate Button
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .background(DarkSurfaceElevated, RoundedCornerShape(10.dp))
            .padding(8.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Matched Real User-Agent:",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = CyanAccent
            )
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = CyanAccent.copy(alpha = 0.15f),
              modifier = Modifier.clickable { regenerateUserAgent() }
            ) {
              Text(
                text = "Generate New UA",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = CyanAccent,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
              )
            }
          }
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = userAgent,
            fontFamily = FontFamily.Monospace,
            fontSize = 9.sp,
            color = TextSecondary,
            maxLines = 3,
            lineHeight = 12.sp
          )
        }

        // Proxy Assignment Dropdown
        Column {
          Text(
            text = "Linked Proxy:",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextSecondary
          )
          Spacer(modifier = Modifier.height(3.dp))
          val currentProxy = availableProxies.find { it.id == selectedProxyId }
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = DarkSurfaceElevated,
            modifier = Modifier
              .fillMaxWidth()
              .clickable { proxyDropdownExpanded = true }
          ) {
            Text(
              text = currentProxy?.let { "${it.name} (${it.countryCode} ${it.protocol})" } ?: "Direct Network (No Proxy - Recommended)",
              fontSize = 11.sp,
              fontWeight = FontWeight.Medium,
              color = if (currentProxy != null) CyanAccent else EmeraldConnected,
              modifier = Modifier.padding(10.dp)
            )

            DropdownMenu(
              expanded = proxyDropdownExpanded,
              onDismissRequest = { proxyDropdownExpanded = false },
              modifier = Modifier.background(DarkSurface)
            ) {
              DropdownMenuItem(
                text = { Text("Direct Network (No Proxy)", color = EmeraldConnected, fontSize = 11.sp) },
                onClick = {
                  selectedProxyId = null
                  proxyDropdownExpanded = false
                }
              )
              availableProxies.forEach { proxy ->
                DropdownMenuItem(
                  text = {
                    Text(
                      "${proxy.name} (${proxy.countryCode} - ${proxy.host}:${proxy.port})",
                      color = TextPrimary,
                      fontSize = 11.sp
                    )
                  },
                  onClick = {
                    selectedProxyId = proxy.id
                    proxyDropdownExpanded = false
                  }
                )
              }
            }
          }
        }

        // Bookmarks & Startup Homepage URL Section (Room DB backed)
        Card(
          shape = RoundedCornerShape(10.dp),
          colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
          border = BorderStroke(1.dp, CyanAccent.copy(alpha = 0.35f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.Bookmark,
                  contentDescription = null,
                  tint = CyanAccent,
                  modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "Startup URL & Bookmarks",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold,
                  color = CyanAccent
                )
              }
              Surface(
                shape = RoundedCornerShape(6.dp),
                color = if (showAddBookmarkSection) DarkBorder else EmeraldConnected.copy(alpha = 0.2f),
                border = BorderStroke(1.dp, if (showAddBookmarkSection) DarkBorder else EmeraldConnected.copy(alpha = 0.5f)),
                modifier = Modifier.clickable { showAddBookmarkSection = !showAddBookmarkSection }
              ) {
                Text(
                  text = if (showAddBookmarkSection) "Close Add" else "+ Add New to DB",
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (showAddBookmarkSection) TextSecondary else EmeraldConnected,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
              }
            }

            OutlinedTextField(
              value = startupUrl,
              onValueChange = { startupUrl = it },
              label = { Text("Startup Homepage URL", fontSize = 10.sp) },
              placeholder = { Text("https://www.google.com", fontSize = 11.sp) },
              singleLine = true,
              modifier = Modifier
                .fillMaxWidth()
                .testTag("profile_startup_url_input"),
              colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CyanAccent,
                unfocusedBorderColor = DarkBorder,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedLabelColor = CyanAccent
              )
            )

            // Room Database saved bookmarks quick-pick chips
            if (savedBookmarks.isNotEmpty()) {
              Text(
                text = "Tap to set Startup URL from Room DB:",
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextSecondary
              )
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                savedBookmarks.forEach { bm ->
                  val isSelected = startupUrl.equals(bm.url, ignoreCase = true)
                  Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (isSelected) CyanAccent.copy(alpha = 0.25f) else DarkBg,
                    border = BorderStroke(1.dp, if (isSelected) CyanAccent else DarkBorder),
                    modifier = Modifier.clickable { startupUrl = bm.url }
                  ) {
                    Row(
                      verticalAlignment = Alignment.CenterVertically,
                      modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
                    ) {
                      Icon(
                        imageVector = if (isSelected) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = null,
                        tint = if (isSelected) CyanAccent else TextMuted,
                        modifier = Modifier.size(11.dp)
                      )
                      Spacer(modifier = Modifier.width(4.dp))
                      Text(
                        text = bm.title.take(18),
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) CyanAccent else TextSecondary
                      )
                    }
                  }
                }
              }
            }

            // Inline Add Bookmark directly to Room DB
            if (showAddBookmarkSection) {
              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .background(DarkBg, RoundedCornerShape(8.dp))
                  .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                Text(
                  text = "Save Custom Bookmark to Room Database:",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = EmeraldConnected
                )
                OutlinedTextField(
                  value = customBookmarkTitle,
                  onValueChange = { customBookmarkTitle = it },
                  label = { Text("Title", fontSize = 10.sp) },
                  placeholder = { Text("e.g. My Target Portal", fontSize = 10.sp) },
                  singleLine = true,
                  modifier = Modifier.fillMaxWidth(),
                  colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = EmeraldConnected,
                    unfocusedBorderColor = DarkBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedLabelColor = EmeraldConnected
                  )
                )
                OutlinedTextField(
                  value = customBookmarkUrl,
                  onValueChange = { customBookmarkUrl = it },
                  label = { Text("Target URL", fontSize = 10.sp) },
                  placeholder = { Text("https://example.com", fontSize = 10.sp) },
                  singleLine = true,
                  modifier = Modifier.fillMaxWidth(),
                  colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = EmeraldConnected,
                    unfocusedBorderColor = DarkBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedLabelColor = EmeraldConnected
                  )
                )
                Button(
                  onClick = {
                    if (customBookmarkUrl.isNotBlank()) {
                      val formattedUrl = if (!customBookmarkUrl.startsWith("http://") && !customBookmarkUrl.startsWith("https://")) {
                        "https://$customBookmarkUrl"
                      } else customBookmarkUrl
                      val title = customBookmarkTitle.ifBlank { formattedUrl }
                      onAddBookmarkToRoom?.invoke(title, formattedUrl, profileToEdit?.id)
                      startupUrl = formattedUrl
                      customBookmarkTitle = ""
                      customBookmarkUrl = ""
                      showAddBookmarkSection = false
                    }
                  },
                  colors = ButtonDefaults.buttonColors(containerColor = EmeraldConnected, contentColor = DarkBg),
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Text("Save to Room DB & Set as Startup", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
              }
            }
          }
        }

        // 1. Cookie & Session Manager Section (Import/Export Netscape & JSON)
        Card(
          shape = RoundedCornerShape(10.dp),
          colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
          border = BorderStroke(1.dp, EmeraldConnected.copy(alpha = 0.35f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.Key,
                  contentDescription = null,
                  tint = EmeraldConnected,
                  modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "Cookie & Session Manager",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold,
                  color = EmeraldConnected
                )
              }
              val cookieCount = CookieHelper.countCookies(savedCookies)
              Surface(
                shape = RoundedCornerShape(6.dp),
                color = if (cookieCount > 0) EmeraldConnected.copy(alpha = 0.2f) else DarkBorder,
                border = BorderStroke(1.dp, if (cookieCount > 0) EmeraldConnected.copy(alpha = 0.5f) else DarkBorder)
              ) {
                Text(
                  text = if (cookieCount > 0) "$cookieCount Cookies Loaded" else "No Cookies",
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (cookieCount > 0) EmeraldConnected else TextMuted,
                  modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                )
              }
            }

            Text(
              text = "Import Netscape / JSON cookies to open this browser already logged into your accounts without OTP/2FA friction.",
              fontSize = 10.sp,
              color = TextSecondary,
              lineHeight = 14.sp
            )

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              // Import Cookies button
              Surface(
                shape = RoundedCornerShape(6.dp),
                color = EmeraldConnected.copy(alpha = 0.15f),
                border = BorderStroke(1.dp, EmeraldConnected.copy(alpha = 0.4f)),
                onClick = {
                  tempCookieInput = savedCookies
                  showCookieImportDialog = true
                },
                modifier = Modifier.weight(1f)
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.Center,
                  modifier = Modifier.padding(vertical = 6.dp)
                ) {
                  Icon(Icons.Default.ContentPaste, contentDescription = null, tint = EmeraldConnected, modifier = Modifier.size(12.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("Import Cookies", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = EmeraldConnected)
                }
              }

              // Export Cookies button
              Surface(
                shape = RoundedCornerShape(6.dp),
                color = CyanAccent.copy(alpha = 0.15f),
                border = BorderStroke(1.dp, CyanAccent.copy(alpha = 0.4f)),
                onClick = {
                  if (savedCookies.isNotBlank()) {
                    clipboardManager.setText(AnnotatedString(savedCookies))
                    Toast.makeText(context, "Cookies copied to clipboard!", Toast.LENGTH_SHORT).show()
                  } else {
                    Toast.makeText(context, "No saved cookies to export yet.", Toast.LENGTH_SHORT).show()
                  }
                },
                modifier = Modifier.weight(1f)
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.Center,
                  modifier = Modifier.padding(vertical = 6.dp)
                ) {
                  Icon(Icons.Default.ContentCopy, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(12.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("Export", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CyanAccent)
                }
              }

              // Clear Cookies
              if (savedCookies.isNotBlank()) {
                Surface(
                  shape = RoundedCornerShape(6.dp),
                  color = DarkBg,
                  border = BorderStroke(1.dp, DarkBorder),
                  onClick = {
                    savedCookies = ""
                    Toast.makeText(context, "Cookies cleared for this profile", Toast.LENGTH_SHORT).show()
                  }
                ) {
                  Box(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Clear", tint = TextMuted, modifier = Modifier.size(14.dp))
                  }
                }
              }
            }
          }
        }

        // 2. Custom Script / UserScript Injector Section
        Card(
          shape = RoundedCornerShape(10.dp),
          colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
          border = BorderStroke(1.dp, CyanAccent.copy(alpha = 0.35f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.Code,
                  contentDescription = null,
                  tint = CyanAccent,
                  modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "Custom UserScript Injector",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold,
                  color = CyanAccent
                )
              }
              Surface(
                shape = RoundedCornerShape(6.dp),
                color = if (userScript.isNotBlank()) CyanAccent.copy(alpha = 0.2f) else DarkBorder,
                border = BorderStroke(1.dp, if (userScript.isNotBlank()) CyanAccent.copy(alpha = 0.5f) else DarkBorder),
                modifier = Modifier.clickable { showUserScriptSection = !showUserScriptSection }
              ) {
                Text(
                  text = if (showUserScriptSection) "Hide" else if (userScript.isNotBlank()) "Active (Edit)" else "+ Expand",
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (userScript.isNotBlank()) CyanAccent else TextSecondary,
                  modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                )
              }
            }

            Text(
              text = "Custom JavaScript executes automatically inside this profile whenever a webpage finishes loading.",
              fontSize = 10.sp,
              color = TextSecondary,
              lineHeight = 14.sp
            )

            if (showUserScriptSection) {
              // Quick preset chips
              Text(
                text = "Quick Script Presets (Tap to insert):",
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextSecondary
              )
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                val presets = listOf(
                  "Bypass Popups" to "document.querySelectorAll('.modal,.popup,.overlay,#overlay').forEach(e=>e.remove()); document.body.style.overflow='auto';",
                  "Auto-fill Emails" to "document.querySelectorAll('input[type=\"email\"],input[name*=\"email\"]').forEach(i=>i.value='demo@business.com');",
                  "Force Dark Mode" to "document.documentElement.style.filter='invert(0.9) hue-rotate(180deg)';",
                  "Anti-Blur/Always Focus" to "window.onblur=null; document.hasFocus=function(){return true;};"
                )
                presets.forEach { (label, script) ->
                  Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = DarkBg,
                    border = BorderStroke(1.dp, DarkBorder),
                    modifier = Modifier.clickable {
                      userScript = if (userScript.isBlank()) script else "$userScript\n$script"
                    }
                  ) {
                    Text(
                      text = "+ $label",
                      fontSize = 9.sp,
                      fontWeight = FontWeight.SemiBold,
                      color = CyanAccent,
                      modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                  }
                }
              }

              OutlinedTextField(
                value = userScript,
                onValueChange = { userScript = it },
                label = { Text("JavaScript Payload", fontSize = 10.sp) },
                placeholder = { Text("// e.g. document.title = 'Stealth View';", fontSize = 10.sp) },
                maxLines = 6,
                modifier = Modifier
                  .fillMaxWidth()
                  .height(110.dp),
                colors = OutlinedTextFieldDefaults.colors(
                  focusedBorderColor = CyanAccent,
                  unfocusedBorderColor = DarkBorder,
                  focusedTextColor = TextPrimary,
                  unfocusedTextColor = TextPrimary,
                  focusedLabelColor = CyanAccent
                )
              )

              if (userScript.isNotBlank()) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.End
                ) {
                  Text(
                    text = "Clear Script",
                    fontSize = 10.sp,
                    color = TextMuted,
                    modifier = Modifier.clickable { userScript = "" }
                  )
                }
              }
            }
          }
        }

        // 3. Security & Anti-Detection Toggles Card
        Card(
          shape = RoundedCornerShape(10.dp),
          colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
          border = BorderStroke(1.dp, DarkBorder),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Text(
              text = "🛡️ Core Security & Privacy Controls",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = TextPrimary
            )

            // Built-in AdBlock & Anti-Tracker Engine
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text("Built-in AdBlock & Anti-Tracker", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                Text("Blocks 40+ ad networks, trackers & analytics (saves 50% proxy data)", fontSize = 9.sp, color = TextSecondary)
              }
              Switch(
                checked = adBlockEnabled,
                onCheckedChange = { adBlockEnabled = it },
                colors = SwitchDefaults.colors(checkedThumbColor = EmeraldConnected, checkedTrackColor = EmeraldConnected.copy(alpha = 0.5f))
              )
            }

            // Auto Clear Cache & History on Window Close
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text("Auto Clear Cache on Close", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                Text("Wipes cookies, cache & web storage when window is closed", fontSize = 9.sp, color = TextSecondary)
              }
              Switch(
                checked = clearDataOnClose,
                onCheckedChange = { clearDataOnClose = it },
                colors = SwitchDefaults.colors(checkedThumbColor = CyanAccent, checkedTrackColor = CyanAccent.copy(alpha = 0.5f))
              )
            }

            // Canvas Hash Randomization
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text("Canvas Hash Randomization", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                Text("Noise injected into HTML5 2D canvas readbacks", fontSize = 9.sp, color = TextSecondary)
              }
              Switch(
                checked = canvasNoise,
                onCheckedChange = { canvasNoise = it },
                colors = SwitchDefaults.colors(checkedThumbColor = CyanAccent)
              )
            }

            // AudioContext Noise Masking
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text("AudioContext Noise Masking", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                Text("Frequencies shifted to spoof audio fingerprint", fontSize = 9.sp, color = TextSecondary)
              }
              Switch(
                checked = audioNoise,
                onCheckedChange = { audioNoise = it },
                colors = SwitchDefaults.colors(checkedThumbColor = CyanAccent)
              )
            }
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val generated = StealthProfileGenerator.generateProfile(
            osType = selectedOs,
            customName = name.ifBlank { "$deviceModel Profile" },
            specificAndroidVersion = androidVersionInt,
            proxyId = selectedProxyId
          )
          val finalProfile = generated.copy(
            id = profileToEdit?.id ?: generated.id,
            deviceModel = deviceModel.ifBlank { generated.deviceModel },
            userAgent = userAgent.ifBlank { generated.userAgent },
            secChUa = secChUa.ifBlank { generated.secChUa },
            startupUrl = startupUrl.ifBlank { "https://www.google.com" },
            canvasNoiseEnabled = canvasNoise,
            audioNoiseEnabled = audioNoise,
            userScript = userScript,
            adBlockEnabled = adBlockEnabled,
            clearDataOnClose = clearDataOnClose,
            savedCookies = savedCookies
          )
          onSave(finalProfile)
        },
        colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = DarkBg)
      ) {
        Text("Save Profile", fontWeight = FontWeight.Bold, fontSize = 12.sp)
      }
    },
    dismissButton = {
      OutlinedButton(onClick = onDismiss) {
        Text("Cancel", color = TextSecondary, fontSize = 12.sp)
      }
    },
    containerColor = DarkSurface
  )

  // Sub-dialog: Import Cookies (Supports Netscape & JSON format)
  if (showCookieImportDialog) {
    AlertDialog(
      onDismissRequest = { showCookieImportDialog = false },
      title = {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Key, contentDescription = null, tint = EmeraldConnected, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Import Cookies (Netscape / JSON)", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
        }
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text(
            text = "Paste cookies in Netscape format (.txt) or EditThisCookie/J2TEAM JSON format below:",
            fontSize = 11.sp,
            color = TextSecondary
          )
          OutlinedTextField(
            value = tempCookieInput,
            onValueChange = { tempCookieInput = it },
            placeholder = { Text("Paste Netscape tab-delimited or [{\"name\":\"c_user\",...}] JSON here", fontSize = 10.sp) },
            maxLines = 8,
            modifier = Modifier
              .fillMaxWidth()
              .height(140.dp),
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
                  tempCookieInput = clip
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
                val sampleJson = """[{"name":"session_id","value":"st_9882103741","domain":"example.com","path":"/"}]"""
                tempCookieInput = sampleJson
              },
              colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated, contentColor = TextSecondary),
              modifier = Modifier.weight(1f)
            ) {
              Text("Sample JSON", fontSize = 10.sp)
            }
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            savedCookies = tempCookieInput.trim()
            showCookieImportDialog = false
            val count = CookieHelper.countCookies(savedCookies)
            Toast.makeText(context, "Saved $count cookies to profile!", Toast.LENGTH_SHORT).show()
          },
          colors = ButtonDefaults.buttonColors(containerColor = EmeraldConnected, contentColor = DarkBg)
        ) {
          Text("Apply Cookies", fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
      },
      dismissButton = {
        OutlinedButton(onClick = { showCookieImportDialog = false }) {
          Text("Cancel", color = TextSecondary, fontSize = 12.sp)
        }
      },
      containerColor = DarkSurface
    )
  }
}
