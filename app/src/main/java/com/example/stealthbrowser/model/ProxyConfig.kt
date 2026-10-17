package com.example.stealthbrowser.model

import java.util.UUID

enum class ProxyProtocol {
  HTTP,
  HTTPS,
  SOCKS5
}

enum class ProxyStatus {
  ACTIVE,
  TESTING,
  FAILED,
  UNTESTED
}

data class ProxyConfig(
  val id: String = UUID.randomUUID().toString(),
  val name: String,
  val protocol: ProxyProtocol = ProxyProtocol.SOCKS5,
  val host: String,
  val port: Int,
  val username: String? = null,
  val password: String? = null,
  val countryCode: String = "US",
  val city: String? = "New York",
  val rotationIntervalMinutes: Int = 10, // 0 = Manual rotation
  val lastRotatedTimestamp: Long = System.currentTimeMillis(),
  val status: ProxyStatus = ProxyStatus.UNTESTED,
  val latencyMs: Long? = null,
  val lastTestedIp: String? = null
)
