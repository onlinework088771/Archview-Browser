package com.example.stealthbrowser

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.stealthbrowser.stealth.FingerprintPresets
import com.example.stealthbrowser.viewmodel.MainViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Shakil Stealth Browser", appName)
  }

  @Test
  fun `universal search bar resolves url and text queries correctly`() {
    val app = ApplicationProvider.getApplicationContext<Application>()
    val viewModel = MainViewModel(app)

    // 1. Direct URLs with scheme
    assertEquals("https://whoer.net", viewModel.resolveQueryOrUrl("https://whoer.net"))

    // 2. Domain without scheme
    assertEquals("https://pixelscan.net", viewModel.resolveQueryOrUrl("pixelscan.net"))

    // 3. Search query
    val searchQuery = viewModel.resolveQueryOrUrl("what is my ip address")
    assertTrue(searchQuery.startsWith("https://www.google.com/search?q="))
    assertTrue(searchQuery.contains("what") && searchQuery.contains("ip"))
  }

  @Test
  fun `launching 2 to 4 browsers positions them side by side without stacking`() {
    val app = ApplicationProvider.getApplicationContext<Application>()
    val viewModel = MainViewModel(app)

    val profiles = FingerprintPresets.getAllDefaultPresets().take(2)
    viewModel.launchMultiBrowsers(profiles)

    val windows = viewModel.floatingWindows.value
    assertEquals(2, windows.size)

    val win1 = windows[0]
    val win2 = windows[1]

    // Verify left and right horizontal split (side-by-side)
    assertTrue("Win1 should be on left side", win1.normX < 0.2f)
    assertTrue("Win2 should be on right side", win2.normX >= 0.5f)
    assertTrue("Windows should share same vertical top alignment", win1.normY == win2.normY)
  }
}
