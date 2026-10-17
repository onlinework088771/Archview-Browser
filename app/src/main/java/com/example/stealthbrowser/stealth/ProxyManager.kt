package com.example.stealthbrowser.stealth

import android.content.Context
import android.util.Log
import androidx.webkit.ProxyConfig as AndroidWebKitProxyConfig
import androidx.webkit.ProxyController
import androidx.webkit.WebViewFeature
import com.example.stealthbrowser.model.ProxyConfig
import com.example.stealthbrowser.model.ProxyProtocol
import com.example.stealthbrowser.model.ProxyStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.InetSocketAddress
import java.net.Proxy
import java.util.concurrent.TimeUnit

class ProxyManager(private val context: Context) {

  private val TAG = "ProxyManager"

  fun applyProxyToWebView(proxy: ProxyConfig?) {
    if (!WebViewFeature.isFeatureSupported(WebViewFeature.PROXY_OVERRIDE)) {
      Log.w(TAG, "Proxy override not supported on this WebView version")
      return
    }

    val proxyController = ProxyController.getInstance()

    if (proxy == null) {
      proxyController.clearProxyOverride(context.mainExecutor) {
        Log.d(TAG, "Proxy cleared from WebView")
      }
      return
    }

    val scheme = when (proxy.protocol) {
      ProxyProtocol.HTTP -> "http"
      ProxyProtocol.HTTPS -> "https"
      ProxyProtocol.SOCKS5 -> "socks"
    }

    val proxyUrl = "$scheme://${proxy.host}:${proxy.port}"

    try {
      val proxyConfigBuilder = AndroidWebKitProxyConfig.Builder()
        .addProxyRule(proxyUrl)
        .addBypassRule("localhost")
        .addBypassRule("127.0.0.1")
        .build()

      proxyController.setProxyOverride(proxyConfigBuilder, context.mainExecutor) {
        Log.d(TAG, "Proxy applied successfully: $proxyUrl")
      }
    } catch (e: Exception) {
      Log.e(TAG, "Error applying proxy $proxyUrl", e)
    }
  }

  suspend fun testProxy(proxy: ProxyConfig): ProxyTestResult = withContext(Dispatchers.IO) {
    val startTime = System.currentTimeMillis()
    try {
      val javaProxyType = when (proxy.protocol) {
        ProxyProtocol.HTTP, ProxyProtocol.HTTPS -> Proxy.Type.HTTP
        ProxyProtocol.SOCKS5 -> Proxy.Type.SOCKS
      }
      val javaProxy = Proxy(javaProxyType, InetSocketAddress(proxy.host, proxy.port))

      val clientBuilder = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .proxy(javaProxy)

      if (!proxy.username.isNullOrEmpty() && !proxy.password.isNullOrEmpty()) {
        clientBuilder.proxyAuthenticator { _, response ->
          val credential = okhttp3.Credentials.basic(proxy.username, proxy.password)
          response.request.newBuilder()
            .header("Proxy-Authorization", credential)
            .build()
        }
      }

      val client = clientBuilder.build()
      val request = Request.Builder()
        .url("https://api.ipify.org?format=json")
        .build()

      client.newCall(request).execute().use { response ->
        val latency = System.currentTimeMillis() - startTime
        if (response.isSuccessful) {
          val body = response.body?.string() ?: ""
          val ip = if (body.contains("\"ip\":")) {
            body.substringAfter("\"ip\":\"").substringBefore("\"")
          } else {
            proxy.host
          }
          ProxyTestResult(success = true, latencyMs = latency, detectedIp = ip)
        } else {
          ProxyTestResult(success = false, latencyMs = latency, errorMessage = "HTTP ${response.code}")
        }
      }
    } catch (e: Exception) {
      val latency = System.currentTimeMillis() - startTime
      ProxyTestResult(success = false, latencyMs = latency, errorMessage = e.localizedMessage ?: "Connection failed")
    }
  }
}

data class ProxyTestResult(
  val success: Boolean,
  val latencyMs: Long,
  val detectedIp: String? = null,
  val errorMessage: String? = null
)
