# Locus Security Audit & Comprehensive Code Documentation Plan

A comprehensive security hardening and complete documentation blueprint for the Locus Android application, ensuring zero client-side credential exposure and establishing production-grade KDoc commentary across the entire codebase.

### User Review & Critical Decisions

> [!IMPORTANT]
> The following architectural decisions were confirmed based on your input:
> - **Build Pipeline Hardening**: Standard release configuration without aggressive code shrinking, preserving stability, custom serialization, and diagnostic readability while eliminating secret embedding and build leakage.
> - **Zero-Credential Architecture**: Locus functions as a fully local client architecture with no cloud credentials embedded in the client APK. Secrets Gradle Plugin injection of `.env` will be sanitized so that no sensitive tokens (such as `GEMINI_API_KEY`) are bundled into client-side `BuildConfig` bytecode or production artifacts.
> - **Documentation Standard**: Comprehensive KDoc function headers with parameter descriptions, return value specifications, feature boundaries, and explanatory inline logic notes will be added to every class, function, and component.

- **Confirmed Decision 1**: Eliminate raw `.env` key exposure (`GEMINI_API_KEY` present in local development `.env`) from the client APK build system by configuring Secrets Gradle Plugin ignore lists and sanitizing environment configuration.
- **Confirmed Decision 2**: Preserve local-first tipping intelligence (`BrainRepository` offline asset loading and zero-permission telephony cell country detection) with zero external network leakage of user financial calculations or location.
- **Confirmed Decision 3**: Retain `MAJOR_CAPABILITY_SERVER_SIDE_GEMINI_API` in `metadata.json` for platform ecosystem compliance while preventing client-side compilation of raw API keys.

---

### 1. Overview & Core Concept

- **What It Does**: Conducts a complete, multi-layer security audit of the Locus codebase, build configuration, environment files, and compilation pipeline. Eliminates credential leakage vectors (notably `.env` API keys injected into `BuildConfig`), hardens app security postures (data extraction rules, backup configurations, intent filters), and enriches every function and component with structured, clear KDoc commentary.
- **Target Audience / Persona**: Users requiring a secure, private, and accessible tipping and currency calculation utility that never transmits or leaks financial or location telemetry, alongside software engineers maintaining and reviewing the codebase.
- **Key Value**: Guarantees zero credential exposure in distributed APKs/bundles, protects user privacy through offline-first design, and provides readable, maintainable code with clear documentation for every feature.

---

### 2. User Experience & Visual Design

- **Key User Flows**:
  - **Tipping & Split Calculation**: Seamless real-time tip calculation across standard, generous, and custom percentages, split counts, and round-up modes with instant visual and haptic feedback.
  - **Global Explorer & Brain Switching**: Offline-first automatic or manual regional tip norms (US, DE, JP, GB, UA, and fallback) loaded securely from local asset bundles without network calls or credentialed APIs.
  - **Senior Accessibility Mode**: High-contrast typography, enlarged interactive touch targets (>=48dp), spoken TTS summaries, and enhanced haptic confirmations.
  - **Tip History & Sharing**: Local encrypted/DataStore persistence of tip records, safe formatted receipt generation, and Android Sharesheet export.
- **Visual Identity & Theme**:
  - *Aesthetic Direction*: Material 3 Expressive with dynamic color integration, tonal elevation, and tactile micro-interactions.
  - *Color Palette & Tokens*: Adaptive M3 light and dark color schemes (`surface`, `surfaceContainerHigh`, `primary`, `secondary`, `tertiary`, `error`).
  - *Typography & Hierarchy*: Material 3 scale (HeadlineLarge, TitleMedium, BodyLarge, LabelSmall) optimized for readability across screen sizes and accessibility modes.
- **Interactive Feedback & Motion**:
  - Expressive spring-based animations, smooth bottom sheet transitions (`TipHistorySheet`), coach marks for discovery, and distinct tactile haptic patterns (`Light`, `Medium`, `Heavy`, `Success`, `Error`).

---

### 3. Key Product Decisions & Trade-Offs

- **Decision 1: Removal of Client-Side Secret Injection vs. Server-Side Routing**
  - *Chosen Approach*: Configure the Secrets Gradle Plugin to ignore `GEMINI_API_KEY` in the client build, clean `.env` key exposure from compiled artifacts, and enforce a pure client-side local intelligence architecture.
  - *Why*: The application performs all tipping logic, currency presets, and regional heuristics locally via `BrainRepository` asset files and `CountryDataRegistry`. Embedding API keys into the client APK creates unnecessary credential leakage without any client need.
  - *Alternatives Considered*: Injecting the key into `BuildConfig` (rejected due to reverse-engineering vulnerability in client APKs) or setting up a server proxy (unnecessary for purely offline tip calculation).
- **Decision 2: Comprehensive KDoc Standards across 27 Source Files**
  - *Chosen Approach*: Standardized KDoc blocks containing `@param`, `@return`, `@throws`, and feature architecture headers, coupled with inline explanatory comments on math operations, state flows, and accessibility hooks.
  - *Why*: Ensures maintainability, rapid onboarding, and eliminates ambiguity in financial formulas (e.g., tip split round-ups, tax pre/post-calculation, currency symbol formatting).
- **Decision 3: Manifest & Storage Security Hardening**
  - *Chosen Approach*: Verify `allowBackup` settings with explicit `dataExtractionRules` and `fullBackupContent`, ensure `exported="true"` is strictly reserved for the launcher activity, and maintain zero dangerous runtime permissions.
  - *Why*: Adheres to Android security best practices and Google Play Store policies.

---

### 4. Technical Architecture & Data Strategy (Technical Reference)

- **Architecture & Component Diagram**:

```
┌────────────────────────────────────────────────────────────────────────┐
│                          Locus Android App                             │
├────────────────────────────────────────────────────────────────────────┤
│                           UI Layer (M3)                                │
│  ┌───────────────────────┐  ┌─────────────────┐  ┌──────────────────┐  │
│  │     MainActivity      │  │ TipHistorySheet │  │ CoachMarkOverlay │  │
│  └───────────┬───────────┘  └────────┬────────┘  └─────────┬────────┘  │
│              │                       │                     │           │
├──────────────┼───────────────────────┼─────────────────────┼───────────┤
│              ▼                       ▼                     ▼           │
│                           State & ViewModel Layer                      │
│                      ┌───────────────────────────────┐                 │
│                      │        TippyViewModel         │                 │
│                      └───────┬───────────────┬───────┘                 │
├──────────────────────────────┼───────────────┼─────────────────────────┤
│                              ▼               ▼                         │
│                           Data & Domain Services                       │
│  ┌───────────────────┐  ┌─────────────────┐  ┌──────────────────────┐  │
│  │  BrainRepository  │  │ TipHistoryStore │  │ SeniorTtsManager     │  │
│  │  (Local Assets)   │  │ (DataStore)     │  │ & HapticService      │  │
│  └───────────────────┘  └─────────────────┘  └──────────────────────┘  │
├────────────────────────────────────────────────────────────────────────┤
│                         Security & Build Layer                         │
│  ┌──────────────────────────────────────────────────────────────────┐  │
│  │ - secrets-gradle-plugin: ignoreList configured (no key bundling) │  │
│  │ - Build pipeline: zero secrets in BuildConfig / assets           │  │
│  │ - Offline cell-tower country detection (zero location leak)      │  │
│  └──────────────────────────────────────────────────────────────────┘  │
└────────────────────────────────────────────────────────────────────────┘
```

- **Data Model & State**:
  - `CountryTippingModel`: Country metadata, tipping norms (standard/generous percentages), currency code, and cultural advice.
  - `TipTransaction`: Timestamped records containing bill amount, tip percentage, split count, total, and country tag.
  - `ServiceType`: Service category multiplier and tipping etiquette rules.
  - `TippyUiState`: StateFlow emitting current bill, tip rate, people count, senior mode state, active country, and calculated totals.

- **Component & Documentation Plan**:
  - **Build & Security Hardening**:
    - Update `app/build.gradle.kts`: Configure `secrets { ignoreList.add("key:.*") }` or sanitize `.env` properties to guarantee no sensitive keys are emitted into `BuildConfig.java`.
    - Sanitize `.env` and `.env.example`: Remove live credential values, document secure usage.
    - Inspect `AndroidManifest.xml` and resource files for any lingering debug or exposure flags.
  - **Comprehensive KDoc Commentary**:
    - `MainActivity.kt`: Document all Compose entry points, state collections, gesture detectors, accessibility wrappers, and adaptive layouts.
    - `TippyViewModel.kt`: Document mathematical calculation formulas, StateFlow updates, split distribution, and persistence triggers.
    - `BrainRepository.kt`: Document zero-permission TelephonyManager inspection and offline JSON asset loading.
    - `GlobalTippingRepository.kt`, `CountryDataRegistry.kt`, `CurrencyQuickBillRegistry.kt`: Document country catalogs, currency mappings, and localized fallback algorithms.
    - `TipHistoryDataStore.kt` & `OnboardingPreferences.kt`: Document local DataStore serialization, concurrency, and schema migrations.
    - `SeniorTtsManager.kt` & `HapticFeedbackService.kt`: Document audio/haptic feedback lifecycles, TextToSpeech engine management, and sensory accessibility adaptations.
    - `BillShareHelper.kt`, `LanguagePreferences.kt`, `LanguageResolver.kt`, `LocaleFormatter.kt`: Document internationalization, formatting rules, and system share intent builders.
    - `CoachMarkOverlay.kt`, `SaveTipDialog.kt`, `TipHistorySheet.kt`: Document modal UI dialogs, animations, and dismissal semantics.
    - `Theme.kt`, `Color.kt`, `Type.kt`: Document Material 3 theme typography scales, tonal palettes, and expressive surface configurations.

- **Verification & Audit Checklist**:
  - Run compile build verification via `compile_applet`.
  - Verify compiled `BuildConfig` and build outputs contain zero API keys, secrets, or credential tokens.
  - Verify clean compilation with zero warnings or errors.
