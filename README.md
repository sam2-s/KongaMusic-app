<div align="center">

<img src="app/src/main/res/mipmap-xxxhdpi/ic_launcher.png" width="96" height="96" alt="KongaMusic" />

# KongaMusic

**A slimmed, rebranded ArchiveTune fork — TUI-flavoured, updates-free, made to feel like home.**

</div>

KongaMusic (repository `sam2-s/KongaMusic`) is a fork of
[ArchiveTune](https://github.com/darkion-4/ArchiveTune). It keeps the rock-solid
Tidal / YouTube / multi-source playback engine, swaps in an animated full-color
**Terminal-User-Interface ASCII mode** for the now-playing screen, defaults to the
modern **liquid-glass dark** look, and **removes the entire app-update subsystem**
(labels, banners, badges, sheets, workers, services) so that the app never asks you
to update itself and never decides on its own. New-release *albums* from your
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

## 🙏 Credits & acknowledgements

Standing on the shoulders of a generous open-source community:

- **ArchiveTune** by [Rukamori](https://github.com/rukamori) — the upstream this fork tracks.
- **Metrolist** by [Mostafa Alagamy](https://github.com/mostafaalagamy/Metrolist) for the base framework.
- **SimpMusic** by [maxrave-dev](https://github.com/maxrave-dev/SimpMusic) for the player style and the lyrics API provider.
- **SpatialFlow** by [MythicalSHUB](https://github.com/MythicalSHUB/SpatialFlow) for the player style and the music haptics feature.
- **Looper** by [SthrNilshaaa](https://github.com/SthrNilshaaa/looper) for the Looper player design — its typography (Jost, by [indestructible-type](https://github.com/indestructible-type/Jost), SIL OFL), squiggly expressive slider, asymmetric transport pills and blurred-sleeve backdrop.
- **Vivi Music** by [vivizzz007](https://github.com/vivizzz007/vivi-music) for the Apple Music player morph animations, the JioSaavn integration, and the Listen Together server.
- **Muzo** by [Shashwat-CODING](https://github.com/Shashwat-CODING/Muzo) for the fonts API, Spotify Canvas, and Qobuz backup, as well as design inspiration.
- **BitChord** by [kushagrasinghx](https://github.com/kushagrasinghx/BitChord) for the player style.
- [BetterLyrics](https://better-lyrics.boidu.dev/) for word-by-word lyrics, unison and artwork provider support.
- **LastWave** by [Clash-Projects](https://github.com/Clash-Projects/LastWave-native) for the Last.fm stats design.
- [Material Color Utilities](https://github.com/material-foundation/material-color-utilities)
- [Read You](https://github.com/Ashinch/ReadYou) and [Seal](https://github.com/JunkFood02/Seal) for UI component inspiration.
- Translators, beta testers, contributors, and community members who continue to support the project.

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
