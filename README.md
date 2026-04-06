# TeleCalc — Telegram client disguised as a calculator

A privacy-focused Telegram client for Android that hides behind a fully functional calculator. Enter your secret math expression to unlock Telegram.

## How It Works

1. **First launch** — Set a secret numeric code (e.g., `1234`)
2. **Every launch after** — The app opens as a Material You calculator
3. **To unlock Telegram** — Type any math expression whose result equals your secret code (e.g., `617 x 2 =` gives `1234`) and Telegram opens instantly
4. **To everyone else** — It looks and works like a normal calculator app

The app appears as "Calculator" in your launcher, app drawer, and recents screen with a calculator icon. Notifications are configured to hide message content by default.

## Features

### Calculator Decoy
- Material You styled calculator with light purple palette
- Fully functional basic calculator (+, -, x, /)
- Secret code stored as salted SHA-256 hash
- Instant transition to Telegram on correct code
- Re-entry required every time the app is reopened
- `FLAG_SECURE` prevents screenshots of the calculator screen
- Notification previews auto-disabled to prevent content leaking
- Change your secret code from the hidden debug menu (tap version number twice in Settings)

### Inherited from Forkgram
- `Delete for everyone` option enabled by default
- Original message date for forwarded messages
- Online colored status dots
- Quick share button for media in private chats
- PiP mode for YouTube's in-app player
- Forward messages without quoting the original sender
- Unlimited pinned chats
- Fast Forward with anonymous and no-text options
- Hidden proxy string, hidden account names in drawer
- And many more privacy and convenience features

## Downloads

Download the latest APK from [Releases](https://github.com/Novaturiente/TeleCalc/releases).

## Building

### Prerequisites
- JDK 17
- Android SDK (platform 35, build-tools 35.0.0)
- NDK 21.4.7075529 and 23.2.8568313
- cmake, go, gperf, meson, ninja, yasm

### Steps

1. Clone with submodules:
   ```bash
   git clone --recursive https://github.com/Novaturiente/TeleCalc.git
   ```

2. Set up `gradle.properties` with your [Telegram API credentials](https://core.telegram.org/api/obtaining_api_id):
   ```
   APP_ID=your_api_id
   APP_HASH=your_api_hash
   ```

3. Build native dependencies:
   ```bash
   cd TMessagesProj/jni
   export NDK=/path/to/ndk/21.4.7075529
   export NINJA_PATH=/usr/bin/ninja
   ./build_libvpx_clang.sh
   ./build_ffmpeg_clang.sh
   ./patch_ffmpeg.sh
   ./patch_boringssl.sh
   ./build_boringssl.sh
   ```

4. Build the APK:
   ```bash
   ./gradlew assembleAfatRelease
   ```

## Credits & Attribution

TeleCalc is built on top of open-source work by others:

- **[Telegram for Android](https://github.com/DrKLO/Telegram)** — the official Telegram client by Nikolai Kudashov, licensed under [GNU GPL v2](LICENSE)
- **[Forkgram](https://github.com/Forkgram/TelegramAndroid)** — an unofficial Telegram fork that adds privacy features and UI improvements, which TeleCalc is directly based on
- **[Telegram FOSS](https://github.com/Telegram-FOSS-Team/Telegram-FOSS)** — some features incorporated via Forkgram

TeleCalc adds the calculator decoy layer on top of Forkgram's codebase. All upstream features and fixes are preserved.

## License

This project is licensed under the [GNU General Public License v2.0](LICENSE), the same license as the original Telegram for Android source code.

As required by the GPL, the complete source code is available in this repository. If you distribute modified versions, you must also make your source code available under the same license.
