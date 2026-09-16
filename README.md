<div align="center">

<img src="app/src/main/res/mipmap-xxxhdpi/ic_launcher.png" width="96" height="96" alt="KongaMusic" />

# KongaMusic

**Terminal-flavoured. Updates-free. Yours.**

</div>

KongaMusic is a slim MusicTune-flavoured player. It keeps a rock-solid
Tidal / YouTube / multi-source playback engine, swaps in an animated full-color
**Terminal-User-Interface ASCII mode** for the now-playing screen, defaults to the
modern **liquid-glass dark** look, and has **no app-update subsystem at all**
— no labels, banners, badges, sheets, workers, or services. It never asks you to
update itself, and it never decides on its own. New-release *albums* from your
subscribed artists still get a friendly notification — that's a music feature, not an
update nag.

---

## ✨ Highlights

| | |
|---|---|
| 🎨 **TUI ASCII Now Playing** | Optional animated full-color ASCII-art mode for the player, complete with liquid glass and frosted variants |
| 🪟 **Liquid Glass by default** | Glazed translucent frosted surfaces, subtle motion, pure-black dark theme — on out of the box |
| 🔇 **No update prompts, ever** | The update-check subsystem is fully removed — no popups, no badges, no system-tray toasts, no "check for updates" screen |
| 🎤 **Multi-source playback** | Tidal / YouTube / Deezer / Qobuz / Spotify(extended metadata) with automatic source resolution and live Telegram streaming |
| 📻 **Broadcast + connect** | Chromecast, Bluetooth, LAN casting, Now Playing / listening insights widgets |
| 🔒 **Your data stays local** | No ads, no telemetry; everything is delivered through your own multi-source chain |

---

## 🖼️ Screenshots

*Player TUI mode, liquid-glass settings, and the redesigned About screen.*

```
Coming soon — screenshots will live here.
```

> ⚠️ Screenshots require an emulator/device connection primarily for verification; the
> APK you can install right away from **[Releases](#-releases)**.

---

## ⚙️ Defaults at a glance

KongaMusic ships opinionated so it feels right on day one:

- **Theme** – liquid glass **ON**, dark **ON**, pure-black **ON**, dynamic color **OFF**
- **Player** – default style `bitchord` (animated mini-cover TUI canvas)
- **Mini-player background** – `frosted` glass
- **Update prompts** – all removed (no prompts, banners, badges, or update sheet)

---

## 📦 Releases

Grab the latest release APK from the
**[Releases](https://github.com/sam2-s/KongaMusic/releases)** page.

| Variant | Package | Status |
|---|---|---|
| GMS Mobile Arm64 | `moe.kongamusic` | ✅ Latest — `KongaMusic 15.0.1 (142)` |

The APK is signed with the repo's release keystore (kept private; never committed).

---

## 🛠️ Building from source

**Requirements**

- JDK 21+
- Android SDK (compileSdk 37, build-tools 37)
- Gradle 9.6.1 (wrapper included)

**Clone**

```bash
git clone https://github.com/sam2-s/KongaMusic.git
cd KongaMusic
```

**Build a debug APK**

```bash
./gradlew assembleGmsMobileArm64Debug
```

**Run the fork contract tests**

```bash
./gradlew :app:testGmsMobileArm64DebugUnitTest
```

Output APK lands in `app/build/outputs/apk/`.

---

## 📁 Project layout

```
app/src/main/kotlin/moe/kongamusic/
├── ui/        # all screens, player TUI layer, settings, About
├── playback/  # multi-source resolution, dimensions/decryptors
├── utils/     # Telegram streaming, notifications, new-release check
└── viewmodels/ # UI ViewModels
```

---

## 🧩 Tech

- **Language:** Kotlin
- **UI:** Jetpack Compose (Material 3, dynamic color, adaptive icon)
- **Playback:** Media3 / ExoPlayer with multi-source `SchemeRoutingDataSource`
- **Streaming:** Tidal, YouTube, Deezer, Qobuz, Spotify-enrich, Telegram live
- **Build:** Gradle Kotlin DSL, flavor matrix `gms/foss × mobile/tv × universal/arm64/x86_64`
- **Signing:** release keystore kept out of version control

---

## 📄 License

[GPL-3.0](./LICENSE). Free as in freedom — no ads, no tracking, no strings.

---

<p align="center">
  made with ❤️ by <a href="https://github.com/sam2-s">sam2-s</a>
</p>
