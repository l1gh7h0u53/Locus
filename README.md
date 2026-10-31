# Locus 🌍📍

**Locus** is a vibe coded privacy-first, offline-first Global Cultural Tipping & Currency Calculator built for travelers, digital nomads, and everyday diners. By utilizing `TelephonyManager.getNetworkCountryISO`, Locus automatically detects your current country via cellular network signals—without requesting GPS permissions—and instantly provides local tipping customs, currencies, and etiquette guidelines.

[![Version](https://img.shields.io/badge/version-2.1.0-blue.svg)](https://github.com/l1gh7h0u53/locus/releases/tag/v2.1.0)
[![Kotlin](https://img.shields.io/badge/Kotlin-100%25-7F52FF.svg)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4.svg)](https://developer.android.com/jetpack/compose)
[![Material 3](https://img.shields.io/badge/Material%20Design-M3%20Expressive-7B1FA2.svg)](https://m3.material.io/)
[![License](https://img.shields.io/badge/license-MIT-green.svg)](LICENSE)

---

## 🌟 Key Features

### 1. 📍 Smart Offline Location Detection
* **Zero GPS Required**: Uses `getNetworkCountryISO` via cellular SIM/network ISO to determine your location safely and accurately without invasive GPS tracking.
* **Context-Aware Cultural Rules**: Automatically displays local tipping customs, rules, and default currencies relative to your current country.
* **Global Country Explorer**: Tap the passport emblem 6 times to unlock the hidden Explorer mode and manually browse tipping customs for any destination.
* **Extensive Global Coverage**: Built-in support for **192 countries** and **145 currencies**.

### 2. 🧮 Smart Tipping & Bill Splitting
* **Custom Tip Percentage**: Interactive slider supporting custom tips up to **50%**.
* **Service Categories**: Tailored tipping guidelines for various service types including restaurants, bars, food delivery, takeout, taxis, food trucks, and groceries.
* **Bill Splitting**: Effortlessly split totals and tips across multiple people in real-time.
* **Smart Rounding**: Instant **Round Up** and **Round Down** toggles to keep cash or card payments clean.
* **Quick-Add Increment Chips**: Speed up entry with `+$5`, `+$20`, and `+$50` increment buttons.
* **Text Share Summary**: Formats calculations into a clean text breakdown ready to share via SMS, messaging apps, or email.
* **Clear All Action**: Dedicated single-tap button to reset calculations instantly.

### 3. 👓 Senior & Accessibility Mode
* **High-Contrast Palette**: Designed with oversized touch targets and enlarged typography for effortless readability.
* **Tap-to-Listen (TTS)**: Built-in Text-to-Speech support reads aloud bill totals, split shares, and local tipping advice with a single tap.

### 4. 🎨 Modern Design & Customization
* **Dynamic Themes**: Full support for Light Mode, Dark Mode, and **Android Monet (Material You)** dynamic color schemes.
* **Interactive Feature Guide**: Includes a hands-on onboarding tutorial to walk users through core features on launch.

### 5. 🔒 100% Offline, Secure & Private
* **On-Device Storage**: Transaction history and preferences are saved locally on-device using **Android Jetpack DataStore**.
* **Flexible History Control**: Toggle save preferences (*Always*, *Never*, or *Prompt on Exit*) or clear history at any time.

---

## 🌐 Supported Languages

Locus is fully localized in **11 major world languages**:

* 🇬🇧 **English**
* 🇨🇳 **Mandarin Chinese** (简体中文)
* 🇮🇳 **Hindi** (हिन्दी)
* 🇪🇸 **Spanish** (Español)
* 🇫🇷 **French** (Français)
* 🇦🇪 / 🇸🇦 **Arabic** (العربية)
* 🇧🇩 **Bengali** (বাংলা)
* 🇵🇹 **Portuguese** (Português)
* 🇷🇺 **Russian** (Русский)
* 🇩🇪 **German** (Deutsch)
* 🇯🇵 **Japanese** (日本語)

---

## 📱 Interface Overview

* **Streamlined Header**: Features the branded passport logo with country ISO pill (e.g., `US`, `DE`, `JP`), a Senior Accessibility Mode quick toggle, and Settings gear.
* **Centralized Settings Bottom Sheet**:
  * **Appearance**: Light / Dark theme & Monet engine preferences.
  * **Tip History**: View, load, or clear saved calculations stored in DataStore.
  * **Language & Region**: Select supported localized languages or sync with system telephony settings.
  * **Accessibility**: Quick Senior Mode toggle and option to re-launch the interactive onboarding guide.
  * **Save Preferences**: Custom prompt options (*Always*, *Never*, or *Prompt on Exit*).

---

## 🛠️ Built With

* **Language**: Kotlin
* **UI Framework**: Jetpack Compose (Material 3)
* **Data Storage**: Jetpack DataStore
* **Architecture**: MVVM / Clean Architecture
* **Telephony Integration**: Android `TelephonyManager` (`getNetworkCountryISO`)
* **Accessibility**: Android TextToSpeech API

---

## 🚀 Getting Started

### Prerequisites
* Android 8.0 (API Level 26) or higher.
* Android Studio Ladybug (or newer).

### Installation & Building

1. **Clone the Repository:**
   ```bash
   git clone https://github.com/l1gh7h0u53/Locus.git
   cd Locus
   ```

2. **Open in Android Studio:**
   * Launch Android Studio.
   * Select **Open an existing project** and navigate to the cloned `Locus` directory.

3. **Build & Run:**
   * Sync Gradle files.
   * Build and run the project on an emulator or physical device:
     ```bash
     ./gradlew installDebug
     ```

---

## 🔒 Privacy & Permissions

* **No GPS Permissions:** Uses cellular SIM network ISO (`TelephonyManager.getNetworkCountryISO`) to identify the country without tracking fine or coarse GPS coordinates.
* **100% Offline:** Operates entirely without an internet connection or external network requests.
* **Zero Telemetry:** No user tracking, analytics, or external API keys are used. All saved history stays local to your device.

---

## 📜 License

This project is licensed under the [MIT License](LICENSE).
