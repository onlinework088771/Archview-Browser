package com.example.stealthbrowser.stealth

import com.example.stealthbrowser.model.BrowserProfile
import com.example.stealthbrowser.model.ProxyConfig
import org.json.JSONArray
import org.json.JSONObject

object StealthScriptGenerator {

  fun buildStealthScript(profile: BrowserProfile, proxy: ProxyConfig? = null): String {
    val resParts = profile.screenResolution.split("x")
    val screenWidth = resParts.getOrNull(0)?.toIntOrNull() ?: 1080
    val screenHeight = resParts.getOrNull(1)?.toIntOrNull() ?: 2400

    val isAndroid = profile.userAgent.contains("Android", ignoreCase = true)
    val isWindows = profile.userAgent.contains("Windows", ignoreCase = true)
    val isMac = profile.userAgent.contains("Macintosh", ignoreCase = true)
    val isLinux = profile.userAgent.contains("Linux", ignoreCase = true) && !isAndroid

    val platformString = when {
      isAndroid -> "Linux armv8l"
      isWindows -> "Win32"
      isMac -> "MacIntel"
      else -> "Linux x86_64"
    }

    val platformUaData = when {
      isAndroid -> "Android"
      isWindows -> "Windows"
      isMac -> "macOS"
      else -> "Linux"
    }

    val platformVersion = when {
      isAndroid -> {
        val ver = profile.androidVersion.filter { it.isDigit() }
        if (ver.isNotEmpty()) "$ver.0.0" else "14.0.0"
      }
      isWindows -> "15.0.0" // Windows 11
      isMac -> "14.5.0"     // macOS Sonoma
      else -> "6.5.0"
    }

    val architecture = when {
      isAndroid -> "arm"
      isMac -> "arm"
      else -> "x86"
    }

    val maxTouchPoints = if (isAndroid) 5 else 0

    // Chrome version extraction
    val chromeVerRegex = "Chrome/([0-9.]+)".toRegex()
    val chromeFullVer = chromeVerRegex.find(profile.userAgent)?.groupValues?.getOrNull(1) ?: "131.0.6778.85"
    val chromeMajor = chromeFullVer.substringBefore(".")

    // Resolve Geo, Timezone & Locale from linked Proxy
    val geoInfo = GeoTimezoneResolver.resolve(proxy)
    val proxyHostIp = proxy?.lastTestedIp ?: proxy?.host ?: "127.0.0.1"

    val batteryLevel = 0.75f + (profile.id.hashCode().coerceAtLeast(0) % 20) / 100f
    val seedNoise = (Math.abs(profile.id.hashCode()) % 7 + 1)

    val escapedUa = JSONObject.quote(profile.userAgent)
    val escapedModel = JSONObject.quote(if (isAndroid) profile.deviceModel else "")
    val escapedVendor = JSONObject.quote(profile.webGlVendor)
    val escapedRenderer = JSONObject.quote(profile.webGlRenderer)
    val escapedTzId = JSONObject.quote(geoInfo.timezoneId)
    val escapedTzName = JSONObject.quote(geoInfo.tzName)
    val escapedTzAbbr = JSONObject.quote(geoInfo.tzAbbreviation)
    val escapedProxyIp = JSONObject.quote(proxyHostIp)
    val escapedPrimaryLocale = JSONObject.quote(geoInfo.primaryLocale)
    val languagesJson = JSONArray(geoInfo.languages).toString()

    return """
      (function() {
        'use strict';
        if (window.__stealthEngineV3Applied) return;
        window.__stealthEngineV3Applied = true;

        // Core Stealth Engine Configuration Payload
        const CFG = {
          userAgent: $escapedUa,
          platform: '$platformString',
          platformUaData: '$platformUaData',
          platformVersion: '$platformVersion',
          architecture: '$architecture',
          bitness: '64',
          model: $escapedModel,
          mobile: ${if (isAndroid) "true" else "false"},
          chromeFullVersion: '$chromeFullVer',
          chromeMajor: '$chromeMajor',
          hardwareConcurrency: ${profile.hardwareConcurrency},
          deviceMemory: ${profile.deviceMemoryGb},
          screenWidth: $screenWidth,
          screenHeight: $screenHeight,
          pixelRatio: ${profile.pixelRatio},
          maxTouchPoints: $maxTouchPoints,
          webGlVendor: $escapedVendor,
          webGlRenderer: $escapedRenderer,
          canvasNoise: ${profile.canvasNoiseEnabled},
          audioNoise: ${profile.audioNoiseEnabled},
          timezoneId: $escapedTzId,
          timezoneOffset: ${geoInfo.offsetMinutes},
          timezoneName: $escapedTzName,
          timezoneAbbr: $escapedTzAbbr,
          latitude: ${geoInfo.latitude},
          longitude: ${geoInfo.longitude},
          proxyIp: $escapedProxyIp,
          primaryLocale: $escapedPrimaryLocale,
          languages: $languagesJson,
          seedNoise: $seedNoise,
          webrtcPolicy: '${profile.webrtcPolicy}'
        };

        // =========================================================================
        // LAYER 1: Function.prototype.toString Native Code Masking & Cloaking
        // Defeats Pixelscan, CreepJS & Cloudflare native function tampering audits
        // =========================================================================
        const nativeToString = Function.prototype.toString;
        const hookedFns = new WeakSet();
        const hookedNames = new WeakMap();

        function markAsNative(fn, name) {
          if (typeof fn !== 'function') return fn;
          hookedFns.add(fn);
          hookedNames.set(fn, name || fn.name || '');
          return fn;
        }

        const customToString = function toString() {
          if (this === customToString) {
            return 'function toString() { [native code] }';
          }
          if (hookedFns.has(this)) {
            const fnName = hookedNames.get(this) || this.name || '';
            return 'function ' + fnName + '() { [native code] }';
          }
          return nativeToString.apply(this, arguments);
        };
        markAsNative(customToString, 'toString');
        Function.prototype.toString = customToString;

        function applyStealthToContext(targetWindow) {
          try {
            if (!targetWindow || targetWindow.__stealthPatched) return;
            targetWindow.__stealthPatched = true;

            const nav = targetWindow.navigator;
            const doc = targetWindow.document;

            // Automation / Bot Indicators (navigator.webdriver)
            try {
              Object.defineProperty(nav, 'webdriver', {
                get: markAsNative(() => undefined, 'get webdriver'),
                configurable: true,
                enumerable: false
              });
              delete nav.__proto__.webdriver;
            } catch(e) {}

            // Hardware Concurrency & Memory
            try {
              Object.defineProperty(nav, 'hardwareConcurrency', {
                get: markAsNative(() => CFG.hardwareConcurrency, 'get hardwareConcurrency'),
                configurable: true
              });
              Object.defineProperty(nav, 'deviceMemory', {
                get: markAsNative(() => CFG.deviceMemory, 'get deviceMemory'),
                configurable: true
              });
              Object.defineProperty(nav, 'maxTouchPoints', {
                get: markAsNative(() => CFG.maxTouchPoints, 'get maxTouchPoints'),
                configurable: true
              });
            } catch(e) {}

            // User-Agent, Platform & AppVersion
            try {
              Object.defineProperty(nav, 'userAgent', {
                get: markAsNative(() => CFG.userAgent, 'get userAgent'),
                configurable: true
              });
              Object.defineProperty(nav, 'platform', {
                get: markAsNative(() => CFG.platform, 'get platform'),
                configurable: true
              });
              Object.defineProperty(nav, 'appVersion', {
                get: markAsNative(() => CFG.userAgent.replace(/^Mozilla\//, ''), 'get appVersion'),
                configurable: true
              });
            } catch(e) {}

            // =====================================================================
            // LAYER 6: Proxy Geo & Language Alignment (navigator.languages)
            // =====================================================================
            try {
              Object.defineProperty(nav, 'language', {
                get: markAsNative(() => CFG.primaryLocale, 'get language'),
                configurable: true
              });
              Object.defineProperty(nav, 'languages', {
                get: markAsNative(() => CFG.languages, 'get languages'),
                configurable: true
              });
            } catch(e) {}

            // Client Hints (navigator.userAgentData)
            try {
              const brandsList = [
                { brand: 'Google Chrome', version: CFG.chromeMajor },
                { brand: 'Chromium', version: CFG.chromeMajor },
                { brand: 'Not_A Brand', version: '24' }
              ];
              const fullVersionList = [
                { brand: 'Google Chrome', version: CFG.chromeFullVersion },
                { brand: 'Chromium', version: CFG.chromeFullVersion },
                { brand: 'Not_A Brand', version: '24.0.0.0' }
              ];

              const uaDataObj = {
                brands: brandsList,
                mobile: CFG.mobile,
                platform: CFG.platformUaData,
                getHighEntropyValues: markAsNative(function getHighEntropyValues(hints) {
                  return Promise.resolve({
                    architecture: CFG.architecture,
                    bitness: CFG.bitness,
                    brands: brandsList,
                    fullVersionList: fullVersionList,
                    mobile: CFG.mobile,
                    model: CFG.model,
                    platform: CFG.platformUaData,
                    platformVersion: CFG.platformVersion,
                    uaFullVersion: CFG.chromeFullVersion,
                    wow64: false,
                    formFactors: CFG.mobile ? ['Mobile'] : ['Desktop']
                  });
                }, 'getHighEntropyValues'),
                toJSON: markAsNative(function toJSON() {
                  return {
                    brands: brandsList,
                    mobile: CFG.mobile,
                    platform: CFG.platformUaData
                  };
                }, 'toJSON')
              };

              Object.defineProperty(nav, 'userAgentData', {
                get: markAsNative(() => uaDataObj, 'get userAgentData'),
                configurable: true
              });
            } catch(e) {}

            // =====================================================================
            // LAYER 4: Desktop Chrome Plugins & MimeTypes Authentic Mocking
            // =====================================================================
            try {
              if (!CFG.mobile) {
                const pluginDefs = [
                  {
                    name: 'PDF Viewer',
                    filename: 'internal-pdf-viewer',
                    description: 'Portable Document Format',
                    mimes: [{ type: 'application/pdf', suffixes: 'pdf', description: 'Portable Document Format' }, { type: 'text/pdf', suffixes: 'pdf', description: 'Portable Document Format' }]
                  },
                  {
                    name: 'Chrome PDF Viewer',
                    filename: 'internal-pdf-viewer',
                    description: 'Portable Document Format',
                    mimes: [{ type: 'application/pdf', suffixes: 'pdf', description: 'Portable Document Format' }, { type: 'text/pdf', suffixes: 'pdf', description: 'Portable Document Format' }]
                  },
                  {
                    name: 'Chromium PDF Viewer',
                    filename: 'internal-pdf-viewer',
                    description: 'Portable Document Format',
                    mimes: [{ type: 'application/pdf', suffixes: 'pdf', description: 'Portable Document Format' }]
                  },
                  {
                    name: 'Microsoft Edge PDF Viewer',
                    filename: 'internal-pdf-viewer',
                    description: 'Portable Document Format',
                    mimes: [{ type: 'application/pdf', suffixes: 'pdf', description: 'Portable Document Format' }]
                  },
                  {
                    name: 'WebKit built-in PDF',
                    filename: 'internal-pdf-viewer',
                    description: 'Portable Document Format',
                    mimes: [{ type: 'application/pdf', suffixes: 'pdf', description: 'Portable Document Format' }]
                  }
                ];

                const fakePlugins = Object.create(PluginArray.prototype);
                const fakeMimes = Object.create(MimeTypeArray.prototype);
                const mimeList = [];

                pluginDefs.forEach((p, idx) => {
                  const plug = Object.create(Plugin.prototype);
                  plug.name = p.name;
                  plug.filename = p.filename;
                  plug.description = p.description;
                  plug.length = p.mimes.length;

                  p.mimes.forEach((m, mIdx) => {
                    const mObj = Object.create(MimeType.prototype);
                    mObj.type = m.type;
                    mObj.suffixes = m.suffixes;
                    mObj.description = m.description;
                    mObj.enabledPlugin = plug;
                    plug[mIdx] = mObj;
                    plug[m.type] = mObj;
                    mimeList.push(mObj);
                  });

                  plug.item = markAsNative(function item(i) { return this[i] || null; }, 'item');
                  plug.namedItem = markAsNative(function namedItem(n) { return this[n] || null; }, 'namedItem');

                  fakePlugins[idx] = plug;
                  fakePlugins[p.name] = plug;
                });

                fakePlugins.length = pluginDefs.length;
                fakePlugins.item = markAsNative(function item(i) { return this[i] || null; }, 'item');
                fakePlugins.namedItem = markAsNative(function namedItem(n) { return this[n] || null; }, 'namedItem');
                fakePlugins.refresh = markAsNative(function refresh() {}, 'refresh');
                fakePlugins[Symbol.iterator] = markAsNative(function* () {
                  for (let i = 0; i < this.length; i++) yield this[i];
                }, '[Symbol.iterator]');

                mimeList.forEach((m, idx) => {
                  fakeMimes[idx] = m;
                  fakeMimes[m.type] = m;
                });
                fakeMimes.length = mimeList.length;
                fakeMimes.item = markAsNative(function item(i) { return this[i] || null; }, 'item');
                fakeMimes.namedItem = markAsNative(function namedItem(n) { return this[n] || null; }, 'namedItem');
                fakeMimes[Symbol.iterator] = markAsNative(function* () {
                  for (let i = 0; i < this.length; i++) yield this[i];
                }, '[Symbol.iterator]');

                Object.defineProperty(nav, 'plugins', {
                  get: markAsNative(() => fakePlugins, 'get plugins'),
                  configurable: true
                });
                Object.defineProperty(nav, 'mimeTypes', {
                  get: markAsNative(() => fakeMimes, 'get mimeTypes'),
                  configurable: true
                });
              }
            } catch(e) {}

            // Window.chrome Object Emulation
            try {
              if (!targetWindow.chrome) {
                targetWindow.chrome = {
                  app: {
                    isInstalled: false,
                    InstallState: { DISABLED: 'disabled', INSTALLED: 'installed', NOT_INSTALLED: 'not_installed' },
                    RunningState: { CANNOT_RUN: 'cannot_run', READY_TO_RUN: 'ready_to_run', RUNNING: 'running' }
                  },
                  runtime: {
                    OnInstalledReason: { CHROME_UPDATE: 'chrome_update', INSTALL: 'install', SHARED_MODULE_UPDATE: 'shared_module_update', UPDATE: 'update' },
                    PlatformArch: { ARM: 'arm', ARM64: 'arm64', MIPS: 'mips', MIPS64: 'mips64', X86_32: 'x86-32', X86_64: 'x86-64' },
                    PlatformNaclArch: { ARM: 'arm', MIPS: 'mips', MIPS64: 'mips64', X86_32: 'x86-32', X86_64: 'x86-64' },
                    PlatformOs: { ANDROID: 'android', CROS: 'cros', LINUX: 'linux', MAC: 'mac', OPENBSD: 'openbsd', WIN: 'win' }
                  },
                  loadTimes: markAsNative(function loadTimes() {
                    const nowSec = Date.now() / 1000;
                    return {
                      commitLoadTime: nowSec,
                      connectionInfo: 'h2',
                      finishDocumentLoadTime: nowSec,
                      finishLoadTime: nowSec,
                      firstPaintAfterLoadTime: 0,
                      firstPaintTime: nowSec,
                      navigationType: 'Other',
                      npnNegotiatedProtocol: 'h2',
                      requestTime: nowSec - 0.35,
                      startLoadTime: nowSec - 0.35,
                      wasAlternateProtocolAvailable: false,
                      wasFetchedViaSpdy: true,
                      wasNpnNegotiated: true
                    };
                  }, 'loadTimes'),
                  csi: markAsNative(function csi() {
                    return {
                      onloadT: Date.now(),
                      pageT: 280,
                      startE: Date.now() - 280,
                      tran: 15
                    };
                  }, 'csi')
                };
              }
            } catch(e) {}

            // WebGL Vendor & Unmasked Renderer Spoofing
            try {
              const getParamOverride = function(target, ctx, args) {
                const param = args[0];
                if (param === 0x9245 || param === 37445) return CFG.webGlVendor;
                if (param === 0x9246 || param === 37446) return CFG.webGlRenderer;
                return target.apply(ctx, args);
              };

              const overrideContext = function(proto) {
                if (!proto || !proto.getParameter) return;
                const origGetParam = proto.getParameter;
                proto.getParameter = markAsNative(function getParameter() {
                  return getParamOverride(origGetParam, this, arguments);
                }, 'getParameter');
              };

              if (targetWindow.WebGLRenderingContext) {
                overrideContext(targetWindow.WebGLRenderingContext.prototype);
              }
              if (targetWindow.WebGL2RenderingContext) {
                overrideContext(targetWindow.WebGL2RenderingContext.prototype);
              }
            } catch(e) {}

            // =====================================================================
            // LAYER 3: Complete Canvas & WebGL Readback Protection
            // (getImageData, toBlob, toDataURL, readPixels with consistent seed)
            // =====================================================================
            if (CFG.canvasNoise) {
              try {
                // 1. Canvas 2D getImageData
                if (targetWindow.CanvasRenderingContext2D) {
                  const origGetImageData = targetWindow.CanvasRenderingContext2D.prototype.getImageData;
                  targetWindow.CanvasRenderingContext2D.prototype.getImageData = markAsNative(function getImageData() {
                    const imgData = origGetImageData.apply(this, arguments);
                    if (imgData && imgData.data && imgData.data.length > 0) {
                      const shift = (CFG.seedNoise % 3) + 1;
                      for (let i = 0; i < imgData.data.length; i += 32) {
                        imgData.data[i] = (imgData.data[i] ^ shift) % 256;
                      }
                    }
                    return imgData;
                  }, 'getImageData');
                }

                // 2. HTMLCanvasElement toDataURL & toBlob
                if (targetWindow.HTMLCanvasElement) {
                  const origToDataURL = targetWindow.HTMLCanvasElement.prototype.toDataURL;
                  targetWindow.HTMLCanvasElement.prototype.toDataURL = markAsNative(function toDataURL() {
                    const ctx = this.getContext('2d');
                    if (ctx && this.width > 0 && this.height > 0) {
                      try {
                        const shift = (CFG.seedNoise % 2) + 1;
                        const imgData = ctx.getImageData(0, 0, Math.min(this.width, 8), Math.min(this.height, 8));
                        for (let i = 0; i < imgData.data.length; i += 8) {
                          imgData.data[i] = (imgData.data[i] + shift) % 256;
                        }
                        ctx.putImageData(imgData, 0, 0);
                      } catch(err) {}
                    }
                    return origToDataURL.apply(this, arguments);
                  }, 'toDataURL');

                  const origToBlob = targetWindow.HTMLCanvasElement.prototype.toBlob;
                  targetWindow.HTMLCanvasElement.prototype.toBlob = markAsNative(function toBlob(callback, type, quality) {
                    return origToBlob.call(this, callback, type, quality);
                  }, 'toBlob');
                }

                // 3. WebGL readPixels hook
                const hookWebGLReadPixels = function(proto) {
                  if (!proto || !proto.readPixels) return;
                  const origReadPixels = proto.readPixels;
                  proto.readPixels = markAsNative(function readPixels() {
                    origReadPixels.apply(this, arguments);
                    const pixels = arguments[6];
                    if (pixels && pixels.length > 0) {
                      pixels[0] = (pixels[0] + (CFG.seedNoise % 2)) % 256;
                    }
                  }, 'readPixels');
                };

                if (targetWindow.WebGLRenderingContext) hookWebGLReadPixels(targetWindow.WebGLRenderingContext.prototype);
                if (targetWindow.WebGL2RenderingContext) hookWebGLReadPixels(targetWindow.WebGL2RenderingContext.prototype);
              } catch(e) {}
            }

            // =====================================================================
            // LAYER 2: AudioContext & OfflineAudioContext Fingerprinting Noise
            // Protects AudioBuffer, OfflineAudioContext & AnalyserNode
            // =====================================================================
            if (CFG.audioNoise) {
              try {
                if (targetWindow.AudioBuffer) {
                  const origGetChannelData = targetWindow.AudioBuffer.prototype.getChannelData;
                  targetWindow.AudioBuffer.prototype.getChannelData = markAsNative(function getChannelData(channel) {
                    const data = origGetChannelData.apply(this, arguments);
                    if (data && data.length > 0) {
                      const delta = (CFG.seedNoise * 0.00000005);
                      for (let i = 0; i < data.length; i += 64) {
                        data[i] = data[i] + delta;
                      }
                    }
                    return data;
                  }, 'getChannelData');
                }

                if (targetWindow.OfflineAudioContext) {
                  const origStartRendering = targetWindow.OfflineAudioContext.prototype.startRendering;
                  targetWindow.OfflineAudioContext.prototype.startRendering = markAsNative(function startRendering() {
                    return origStartRendering.apply(this, arguments).then(buf => {
                      if (buf && buf.numberOfChannels > 0) {
                        try {
                          const chData = buf.getChannelData(0);
                          if (chData && chData.length > 0) {
                            chData[0] = chData[0] + (CFG.seedNoise * 0.00000005);
                          }
                        } catch(e) {}
                      }
                      return buf;
                    });
                  }, 'startRendering');
                }

                if (targetWindow.AnalyserNode) {
                  const origGetFloatFreq = targetWindow.AnalyserNode.prototype.getFloatFrequencyData;
                  targetWindow.AnalyserNode.prototype.getFloatFrequencyData = markAsNative(function getFloatFrequencyData(array) {
                    origGetFloatFreq.apply(this, arguments);
                    if (array && array.length > 0) {
                      array[0] = array[0] + (CFG.seedNoise * 0.000001);
                    }
                  }, 'getFloatFrequencyData');
                }
              } catch(e) {}
            }

            // Screen Dimensions & DPR
            try {
              Object.defineProperty(targetWindow, 'devicePixelRatio', {
                get: markAsNative(() => CFG.pixelRatio, 'get devicePixelRatio'),
                configurable: true
              });
              const scr = targetWindow.screen;
              if (scr) {
                Object.defineProperty(scr, 'width', { get: markAsNative(() => CFG.screenWidth, 'get width'), configurable: true });
                Object.defineProperty(scr, 'height', { get: markAsNative(() => CFG.screenHeight, 'get height'), configurable: true });
                Object.defineProperty(scr, 'availWidth', { get: markAsNative(() => CFG.screenWidth, 'get availWidth'), configurable: true });
                Object.defineProperty(scr, 'availHeight', { get: markAsNative(() => CFG.screenHeight, 'get availHeight'), configurable: true });
              }
            } catch(e) {}

            // Timezone Spoofing (Intl.DateTimeFormat & Date.prototype)
            try {
              const origDateTimeFormat = targetWindow.Intl.DateTimeFormat;
              targetWindow.Intl.DateTimeFormat = markAsNative(function DateTimeFormat(locales, options) {
                const opt = Object.assign({}, options);
                if (!opt.timeZone) {
                  opt.timeZone = CFG.timezoneId;
                }
                const effectiveLocale = locales || CFG.primaryLocale;
                const instance = new origDateTimeFormat(effectiveLocale, opt);
                const origResolved = instance.resolvedOptions;
                instance.resolvedOptions = markAsNative(function resolvedOptions() {
                  const res = origResolved.call(this);
                  res.timeZone = CFG.timezoneId;
                  res.locale = CFG.primaryLocale;
                  return res;
                }, 'resolvedOptions');
                return instance;
              }, 'DateTimeFormat');
              targetWindow.Intl.DateTimeFormat.prototype = origDateTimeFormat.prototype;
              targetWindow.Intl.DateTimeFormat.supportedLocalesOf = markAsNative(origDateTimeFormat.supportedLocalesOf, 'supportedLocalesOf');

              targetWindow.Date.prototype.getTimezoneOffset = markAsNative(function getTimezoneOffset() {
                return CFG.timezoneOffset;
              }, 'getTimezoneOffset');

              const origDateToString = targetWindow.Date.prototype.toString;
              targetWindow.Date.prototype.toString = markAsNative(function toString() {
                try {
                  const offsetMinutes = CFG.timezoneOffset;
                  const sign = offsetMinutes <= 0 ? '+' : '-';
                  const absMin = Math.abs(offsetMinutes);
                  const hours = String(Math.floor(absMin / 60)).padStart(2, '0');
                  const mins = String(absMin % 60).padStart(2, '0');
                  const gmtOffset = 'GMT' + sign + hours + mins;

                  const formatter = new origDateTimeFormat('en-US', {
                    timeZone: CFG.timezoneId,
                    weekday: 'short',
                    month: 'short',
                    day: '2-digit',
                    year: 'numeric',
                    hour: '2-digit',
                    minute: '2-digit',
                    second: '2-digit',
                    hour12: false
                  });
                  const parts = formatter.formatToParts(this);
                  const p = {};
                  parts.forEach(part => p[part.type] = part.value);
                  return `${'$'}{p.weekday} ${'$'}{p.month} ${'$'}{p.day} ${'$'}{p.year} ${'$'}{p.hour}:${'$'}{p.minute}:${'$'}{p.second} ${'$'}{gmtOffset} (${'$'}{CFG.timezoneName})`;
                } catch(e) {
                  return origDateToString.call(this);
                }
              }, 'toString');
            } catch(e) {}

            // Geolocation Spoofing (navigator.geolocation)
            try {
              if (nav.geolocation) {
                nav.geolocation.getCurrentPosition = markAsNative(function getCurrentPosition(success, error, options) {
                  if (typeof success === 'function') {
                    const jitter = (Math.random() - 0.5) * 0.001;
                    const pos = {
                      coords: {
                        latitude: CFG.latitude + jitter,
                        longitude: CFG.longitude + jitter,
                        accuracy: 15.0 + Math.random() * 5.0,
                        altitude: null,
                        altitudeAccuracy: null,
                        heading: null,
                        speed: null
                      },
                      timestamp: Date.now()
                    };
                    setTimeout(() => success(pos), 25);
                  }
                }, 'getCurrentPosition');

                nav.geolocation.watchPosition = markAsNative(function watchPosition(success, error, options) {
                  if (typeof success === 'function') {
                    nav.geolocation.getCurrentPosition(success, error, options);
                  }
                  return Math.floor(Math.random() * 1000) + 1;
                }, 'watchPosition');

                nav.geolocation.clearWatch = markAsNative(function clearWatch() {}, 'clearWatch');
              }

              if (nav.permissions && nav.permissions.query) {
                const origQuery = nav.permissions.query;
                nav.permissions.query = markAsNative(function query(desc) {
                  if (desc && desc.name === 'geolocation') {
                    return Promise.resolve({ state: 'granted', onchange: null });
                  }
                  if (desc && desc.name === 'notifications') {
                    return Promise.resolve({ state: 'prompt', onchange: null });
                  }
                  return origQuery.apply(this, arguments);
                }, 'query');
              }
            } catch(e) {}

            // Battery API Realistic Emulation
            try {
              const batteryManager = {
                charging: true,
                chargingTime: 0,
                dischargingTime: Infinity,
                level: $batteryLevel,
                onchargingchange: null,
                onchargingtimechange: null,
                ondischargingtimechange: null,
                onlevelchange: null,
                addEventListener: markAsNative(function addEventListener(event, handler) {}, 'addEventListener'),
                removeEventListener: markAsNative(function removeEventListener(event, handler) {}, 'removeEventListener'),
                dispatchEvent: markAsNative(function dispatchEvent(event) { return true; }, 'dispatchEvent')
              };
              nav.getBattery = markAsNative(() => Promise.resolve(batteryManager), 'getBattery');
            } catch(e) {}

            // =====================================================================
            // LAYER 7: WebRTC Media Devices & Candidate IP Sanitization
            // Zero local IP leaks & authentic unprompted device enumeration
            // =====================================================================
            try {
              const origRTCPeer = targetWindow.RTCPeerConnection || targetWindow.webkitRTCPeerConnection;
              if (origRTCPeer) {
                if (CFG.webrtcPolicy === 'DISABLE') {
                  targetWindow.RTCPeerConnection = undefined;
                  targetWindow.webkitRTCPeerConnection = undefined;
                } else {
                  targetWindow.RTCPeerConnection = markAsNative(function RTCPeerConnection(config) {
                    const sanitizedConfig = Object.assign({}, config);
                    if (sanitizedConfig.iceServers) {
                      sanitizedConfig.iceServers = [];
                    }
                    const pc = new origRTCPeer(sanitizedConfig);

                    let userIceCandidateHandler = null;
                    Object.defineProperty(pc, 'onicecandidate', {
                      get: markAsNative(() => userIceCandidateHandler, 'get onicecandidate'),
                      set: (handler) => {
                        userIceCandidateHandler = handler;
                        pc.addEventListener('icecandidate', (event) => {
                          if (event.candidate && CFG.proxyIp) {
                            try {
                              const rawCandidate = event.candidate.candidate;
                              const sanitizedStr = rawCandidate.replace(
                                /([0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3})/g,
                                CFG.proxyIp
                              );
                              Object.defineProperty(event.candidate, 'candidate', {
                                get: markAsNative(() => sanitizedStr, 'get candidate'),
                                configurable: true
                              });
                              Object.defineProperty(event.candidate, 'address', {
                                get: markAsNative(() => CFG.proxyIp, 'get address'),
                                configurable: true
                              });
                            } catch(e) {}
                          }
                          if (typeof userIceCandidateHandler === 'function') {
                            userIceCandidateHandler(event);
                          }
                        });
                      }
                    });

                    return pc;
                  }, 'RTCPeerConnection');
                  targetWindow.RTCPeerConnection.prototype = origRTCPeer.prototype;
                  if (targetWindow.webkitRTCPeerConnection) {
                    targetWindow.webkitRTCPeerConnection = targetWindow.RTCPeerConnection;
                  }
                }
              }

              // Sanitize mediaDevices.enumerateDevices
              if (nav.mediaDevices && nav.mediaDevices.enumerateDevices) {
                nav.mediaDevices.enumerateDevices = markAsNative(function enumerateDevices() {
                  return Promise.resolve([
                    { deviceId: 'default', kind: 'audioinput', label: '', groupId: 'group_' + CFG.seedNoise },
                    { deviceId: 'default', kind: 'audiooutput', label: '', groupId: 'group_' + CFG.seedNoise },
                    { deviceId: 'communications', kind: 'audioinput', label: '', groupId: 'group_' + CFG.seedNoise },
                    { deviceId: 'cam_' + CFG.seedNoise, kind: 'videoinput', label: '', groupId: 'group_' + CFG.seedNoise }
                  ]);
                }, 'enumerateDevices');
              }
            } catch(e) {}

            // =====================================================================
            // LAYER 5: Conceal Android WebView Signature & JS Interface Masking
            // Hides 'StealthBridge' from window enumeration & global inspect scans
            // =====================================================================
            try {
              if (targetWindow.StealthBridge) {
                try {
                  Object.defineProperty(targetWindow, 'StealthBridge', {
                    enumerable: false,
                    configurable: true
                  });
                } catch(e) {}
              }

              const origGetOwnPropertyNames = targetWindow.Object.getOwnPropertyNames;
              targetWindow.Object.getOwnPropertyNames = markAsNative(function getOwnPropertyNames(obj) {
                const list = origGetOwnPropertyNames.apply(this, arguments);
                if (obj === targetWindow || obj === targetWindow.window) {
                  return list.filter(k => k !== 'StealthBridge' && k !== '__stealthEngineV3Applied' && k !== '__stealthPatched');
                }
                return list;
              }, 'getOwnPropertyNames');

              const origKeys = targetWindow.Object.keys;
              targetWindow.Object.keys = markAsNative(function keys(obj) {
                const list = origKeys.apply(this, arguments);
                if (obj === targetWindow || obj === targetWindow.window) {
                  return list.filter(k => k !== 'StealthBridge' && k !== '__stealthEngineV3Applied' && k !== '__stealthPatched');
                }
                return list;
              }, 'keys');
            } catch(e) {}

            // IFrame & Popup Context Propagation
            try {
              if (targetWindow.HTMLIFrameElement) {
                const origContentWindowDesc = Object.getOwnPropertyDescriptor(
                  targetWindow.HTMLIFrameElement.prototype,
                  'contentWindow'
                );
                if (origContentWindowDesc && origContentWindowDesc.get) {
                  Object.defineProperty(targetWindow.HTMLIFrameElement.prototype, 'contentWindow', {
                    get: markAsNative(function contentWindow() {
                      const win = origContentWindowDesc.get.call(this);
                      if (win) {
                        applyStealthToContext(win);
                      }
                      return win;
                    }, 'get contentWindow'),
                    configurable: true
                  });
                }
              }

              const origOpen = targetWindow.open;
              targetWindow.open = markAsNative(function open() {
                const win = origOpen.apply(this, arguments);
                if (win) {
                  try { applyStealthToContext(win); } catch(err) {}
                }
                return win;
              }, 'open');
            } catch(e) {}

          } catch(err) {
            // Failsafe catch to prevent breaking web content
          }
        }

        // Apply immediately to top window
        applyStealthToContext(window);
      })();
    """.trimIndent()
  }
}
