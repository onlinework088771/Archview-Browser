package com.example.stealthbrowser

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stealthbrowser.ui.screens.DataManagementScreen
import com.example.stealthbrowser.ui.screens.FloatingOverlayContainer
import com.example.stealthbrowser.ui.screens.MultiBrowserOverviewScreen
import com.example.stealthbrowser.ui.screens.ProfilesScreen
import com.example.stealthbrowser.ui.screens.ProxiesScreen
import com.example.stealthbrowser.ui.theme.CyanAccent
import com.example.stealthbrowser.ui.theme.DarkBg
import com.example.stealthbrowser.ui.theme.DarkBorder
import com.example.stealthbrowser.ui.theme.DarkSurface
import com.example.stealthbrowser.ui.theme.DarkSurfaceElevated
import com.example.stealthbrowser.ui.theme.EmeraldConnected
import com.example.stealthbrowser.ui.theme.StealthBrowserTheme
import com.example.stealthbrowser.ui.theme.TextMuted
import com.example.stealthbrowser.ui.theme.TextPrimary
import com.example.stealthbrowser.ui.theme.TextSecondary
import com.example.stealthbrowser.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

  private val viewModel: MainViewModel by viewModels()

  @OptIn(ExperimentalMaterial3Api::class)
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    setContent {
      StealthBrowserTheme(darkTheme = true) {
        val profiles by viewModel.profiles.collectAsState()
        val proxies by viewModel.proxies.collectAsState()
        val bookmarks by viewModel.bookmarks.collectAsState()
        val floatingWindows by viewModel.floatingWindows.collectAsState()
        val selectedTab by viewModel.selectedTab.collectAsState()
        val selectedProfileIds by viewModel.selectedProfileIds.collectAsState()
        val isSyncMode by viewModel.isSyncMode.collectAsState()

        // System back navigation when no floating windows are open: returns to Profiles tab (tab 0)
        BackHandler(enabled = floatingWindows.isEmpty() && selectedTab != 0) {
          viewModel.setSelectedTab(0)
        }

        Scaffold(
          topBar = {
            CenterAlignedTopAppBar(
              title = {
                Text(
                  text = "Shakil Stealth Browser",
                  style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    letterSpacing = 0.5.sp
                  ),
                  color = CyanAccent
                )
              },
              navigationIcon = {
                IconButton(onClick = { viewModel.setSelectedTab(0) }) {
                  Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = "Security Shield",
                    tint = CyanAccent,
                    modifier = Modifier.size(22.dp)
                  )
                }
              },
              actions = {
                if (floatingWindows.isNotEmpty()) {
                  IconButton(
                    onClick = { viewModel.autoTileAllWindows() },
                    modifier = Modifier.testTag("top_bar_tile_btn")
                  ) {
                    BadgedBox(
                      badge = {
                        Badge(containerColor = EmeraldConnected) {
                          Text("${floatingWindows.size}", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                      }
                    ) {
                      Icon(
                        imageVector = Icons.Default.GridView,
                        contentDescription = "Auto Tile Windows Side by Side",
                        tint = CyanAccent,
                        modifier = Modifier.size(20.dp)
                      )
                    }
                  }
                }
              },
              colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = DarkSurface
              )
            )
          },
          bottomBar = {
            NavigationBar(
              containerColor = DarkSurface,
              modifier = Modifier.testTag("bottom_nav_bar")
            ) {
              // Profiles Tab
              NavigationBarItem(
                selected = selectedTab == 0,
                onClick = { viewModel.setSelectedTab(0) },
                icon = {
                  Icon(
                    imageVector = Icons.Default.PhoneAndroid,
                    contentDescription = "Profiles",
                    modifier = Modifier.size(20.dp)
                  )
                },
                label = {
                  Text(
                    text = "Profiles",
                    fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 11.sp
                  )
                },
                colors = NavigationBarItemDefaults.colors(
                  selectedIconColor = DarkBg,
                  selectedTextColor = CyanAccent,
                  indicatorColor = CyanAccent,
                  unselectedIconColor = TextSecondary,
                  unselectedTextColor = TextSecondary
                )
              )

              // Compact Proxies Tab
              NavigationBarItem(
                selected = selectedTab == 1,
                onClick = { viewModel.setSelectedTab(1) },
                icon = {
                  Icon(
                    imageVector = Icons.Default.Router,
                    contentDescription = "Proxies",
                    modifier = Modifier.size(20.dp)
                  )
                },
                label = {
                  Text(
                    text = "Proxies",
                    fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 11.sp
                  )
                },
                colors = NavigationBarItemDefaults.colors(
                  selectedIconColor = DarkBg,
                  selectedTextColor = CyanAccent,
                  indicatorColor = CyanAccent,
                  unselectedIconColor = TextSecondary,
                  unselectedTextColor = TextSecondary
                )
              )

              // Windows Tab
              NavigationBarItem(
                selected = selectedTab == 2,
                onClick = { viewModel.setSelectedTab(2) },
                icon = {
                  BadgedBox(
                    badge = {
                      if (floatingWindows.isNotEmpty()) {
                        Badge(containerColor = CyanAccent) {
                          Text("${floatingWindows.size}", fontSize = 9.sp, color = DarkBg, fontWeight = FontWeight.Bold)
                        }
                      }
                    }
                  ) {
                    Icon(
                      imageVector = Icons.Default.GridView,
                      contentDescription = "Windows",
                      modifier = Modifier.size(20.dp)
                    )
                  }
                },
                label = {
                  Text(
                    text = "Windows",
                    fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 11.sp
                  )
                },
                colors = NavigationBarItemDefaults.colors(
                  selectedIconColor = DarkBg,
                  selectedTextColor = CyanAccent,
                  indicatorColor = CyanAccent,
                  unselectedIconColor = TextSecondary,
                  unselectedTextColor = TextSecondary
                )
              )

              // Data Management Tab
              NavigationBarItem(
                selected = selectedTab == 3,
                onClick = { viewModel.setSelectedTab(3) },
                icon = {
                  Icon(
                    imageVector = Icons.Default.Storage,
                    contentDescription = "Data Management",
                    modifier = Modifier.size(20.dp)
                  )
                },
                label = {
                  Text(
                    text = "Data Mgmt",
                    fontWeight = if (selectedTab == 3) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 10.sp
                  )
                },
                colors = NavigationBarItemDefaults.colors(
                  selectedIconColor = DarkBg,
                  selectedTextColor = CyanAccent,
                  indicatorColor = CyanAccent,
                  unselectedIconColor = TextSecondary,
                  unselectedTextColor = TextSecondary
                )
              )
            }
          },
          containerColor = DarkBg
        ) { innerPadding ->
          Box(
            modifier = Modifier
              .fillMaxSize()
              .padding(innerPadding)
          ) {
            // Main Tab Content
            when (selectedTab) {
              0 -> ProfilesScreen(
                profiles = profiles,
                proxies = proxies,
                bookmarks = bookmarks,
                selectedProfileIds = selectedProfileIds,
                onToggleSelect = { viewModel.toggleProfileSelection(it) },
                onSelectAll = { viewModel.selectAllProfiles(it) },
                onLaunchProfile = { viewModel.launchSingleBrowser(it) },
                onLaunchSelectedSideBySide = { viewModel.launchSelectedProfilesSideBySide() },
                onAddProfile = { viewModel.addProfile(it) },
                onAddBulkProfiles = { viewModel.addProfiles(it) },
                onUpdateProfile = { viewModel.updateProfile(it) },
                onDeleteProfile = { viewModel.deleteProfile(it) },
                onCloneProfile = { viewModel.cloneProfile(it) },
                onExportBackup = { viewModel.exportProfilesJson() },
                onImportBackup = { viewModel.importProfilesJson(it) },
                onAddBookmarkToRoom = { title, url, profileId ->
                  viewModel.saveBookmark(title, url, profileId)
                }
              )
              1 -> ProxiesScreen(
                proxies = proxies,
                profiles = profiles,
                onTestProxy = { viewModel.testProxyConnection(it) },
                onRotateProxy = { viewModel.rotateProxyIp(it) },
                onAddProxy = { viewModel.addProxy(it) },
                onAddBulkProxies = { viewModel.addProxies(it) },
                onUpdateProxy = { viewModel.updateProxy(it) },
                onDeleteProxy = { viewModel.deleteProxy(it) }
              )
              2 -> MultiBrowserOverviewScreen(
                openWindows = floatingWindows,
                profiles = profiles,
                onAutoTile = { viewModel.autoTileAllWindows() },
                onBringToFront = { viewModel.bringToFront(it) },
                onCloseWindow = { viewModel.closeWindow(it) },
                onCloseAllWindows = { viewModel.closeAllWindows() },
                onLaunchProfile = { viewModel.launchSingleBrowser(it) }
              )
              3 -> DataManagementScreen(
                profiles = profiles,
                proxies = proxies,
                bookmarks = bookmarks,
                onDeleteProfilePermanently = { viewModel.deleteProfile(it) },
                onBatchDeleteProfilesPermanently = { ids, delBookmarks, wipeCookies ->
                  viewModel.permanentlyDeleteProfiles(ids, delBookmarks, wipeCookies)
                },
                onCleanOrphanedData = { onDone ->
                  viewModel.cleanOrphanedData { onDone() }
                },
                onClearProfileCookies = { viewModel.clearProfileCookies(it) },
                onClearAllCookies = { viewModel.clearAllProfileCookies() },
                onDeepCleanBrowserData = { ctx, wipeBookmarks ->
                  viewModel.deepCleanAllBrowserData(ctx, wipeBookmarks)
                },
                onFactoryResetAll = { ctx ->
                  viewModel.factoryResetAll(ctx)
                }
              )
            }

            // Layered Floating Browser Overlay (Side-by-Side / Grid Floating Multi-Windows)
            FloatingOverlayContainer(
              windows = floatingWindows,
              savedBookmarks = bookmarks,
              isSyncMode = isSyncMode,
              syncScriptAction = viewModel.syncScriptAction,
              onToggleSyncMode = { viewModel.toggleSyncMode() },
              onSyncReloadAll = { viewModel.syncReloadAll() },
              onSyncScroll = { windowId, pct -> viewModel.broadcastSyncScroll(windowId, pct) },
              onSyncClick = { windowId, x, y -> viewModel.broadcastSyncClick(windowId, x, y) },
              onSyncNavigate = { windowId, url -> viewModel.broadcastSyncNavigation(windowId, url) },
              onSaveCookiesToProfile = { profileId, cookies -> viewModel.updateProfileCookies(profileId, cookies) },
              onSaveBookmark = { title, url, profileId ->
                viewModel.saveBookmark(title, url, profileId)
              },
              onDeleteBookmark = { id ->
                viewModel.deleteBookmark(id)
              },
              onDragWindow = { id, dx, dy -> viewModel.updateWindowPosition(id, dx, dy) },
              onResizeWindow = { id, dw, dh -> viewModel.resizeWindow(id, dw, dh) },
              onFocusWindow = { id -> viewModel.bringToFront(id) },
              onNavigateWindow = { id, query -> viewModel.navigateBrowser(id, query) },
              onQueryChange = { id, q -> viewModel.setWindowQueryInput(id, q) },
              onNavStateUpdate = { id, url, title, canBack, canForward, loading ->
                viewModel.updateWindowNavState(id, url, title, canBack, canForward, loading)
              },
              onAutoTile = { viewModel.autoTileAllWindows() },
              onMinimizeWindow = { id -> viewModel.minimizeWindow(id) },
              onRestoreWindow = { id -> viewModel.restoreWindow(id) },
              onToggleMaximizeWindow = { id -> viewModel.toggleMaximizeWindow(id) },
              onCloseWindow = { id -> viewModel.closeWindow(id) },
              onCloseAllWindows = { viewModel.closeAllWindows() }
            )
          }
        }
      }
    }
  }
}
