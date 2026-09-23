# AI Shorts Factory 🎬🚀

> **Autonomous AI-powered vertical video repurposing engine built with Kotlin & Jetpack Compose.**  
> Turn long podcasts, keynote speeches, and YouTube videos into high-engagement vertical Shorts, Reels, and TikToks with automated speaker diarization, AI hook detection, and kinetic subtitles.

[![Android CI & Build](https://github.com/TaufikMir1/ai-shorts-factory/actions/workflows/android-ci.yml/badge.svg)](https://github.com/TaufikMir1/ai-shorts-factory/actions/workflows/android-ci.yml)
[![Vercel Deployment](https://img.shields.io/badge/Vercel-Deployed-black?logo=vercel)](https://vercel.com)
[![Platform](https://img.shields.io/badge/Platform-Android%2015-brightgreen.svg)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0-blue.svg)](https://kotlinlang.org)
[![UI](https://img.shields.io/badge/UI-Jetpack%20Compose%20M3-purple.svg)](https://developer.android.com/jetpack/compose)
[![Database](https://img.shields.io/badge/Storage-Room%20SQLite-orange.svg)](https://developer.android.com/training/data-storage/room)

---

## 🌟 Key Features

- **⚡ Direct YouTube Link Importer**:
  - Ingests standard YouTube links, Shorts, youtu.be shortlinks, and live stream URLs.
  - Automatically sanitizes incoming mobile share text and resolves metadata via oEmbed.
- **🎙️ 10-Stage Neural Processing Pipeline**:
  - Acoustic analysis, voice activity detection, and vocal isolation.
  - Neural speech-to-text with word-level forced alignment.
  - Speaker diarization to identify transitions and conversational hooks.
- **🎯 Virality Scoring Engine**:
  - Analyzes hook strength, narrative flow, and engagement probability (0-100%).
  - Automatically identifies candidate 15-60s clips ideal for YouTube Shorts, Instagram Reels, and TikTok.
- **📐 Intelligent 9:16 Video Framing**:
  - *Smart Face Crop*: Centers active speaker with AI face tracking.
  - *Center Speaker*: Locks onto central subject.
  - *Blurred 16:9 Background*: Preserves full landscape source with aesthetic vertical fill.
- **✨ Kinetic Animated Subtitles**:
  - Multiple viral typography styles: **Hormozi Pop** (yellow/green dynamic highlights), **MrBeast Impact**, **Kinetic Wave**, and **Minimalist Clean**.
- **📦 Publishing Content Kit & 1080x1920 MP4 Export**:
  - Generates SEO-optimized titles, descriptions, and viral hashtag lists.
  - One-tap clipboard copy actions for creator convenience.
- **💾 Offline-First Local Room Database**:
  - Zero mandatory cloud dependencies; all projects, clips, transcripts, and render statuses persist in local SQLite.

---

## 🌐 Web Showcase & Vercel Deployment

This repository is **100% Vercel compatible**. It includes a zero-config web deployment setup (`vercel.json` + `public/index.html`) featuring an interactive web simulator of AI Shorts Factory:

1. **Deploy to Vercel via CLI**:
   ```bash
   npx vercel
   ```
2. **Deploy via GitHub**:
   - Push this repository to your GitHub account.
   - Go to [vercel.com/new](https://vercel.com/new) and import the repository.
   - Vercel will automatically detect `vercel.json` and serve the interactive web demo from the `public/` directory with zero build configuration required!

---

## 📱 Android Development & Building

### Prerequisites
- **Android Studio Ladybug (or newer)**
- **JDK 17** (Temurin or OpenJDK)
- **Android SDK Platform 35/36**

### Clone & Build
```bash
# Clone the repository
git clone https://github.com/TaufikMir1/ai-shorts-factory.git
cd ai-shorts-factory

# Run unit and Robolectric tests
gradle :app:testDebugUnitTest

# Assemble Debug APK
gradle :app:assembleDebug
```
The output APK will be generated at:
`app/build/outputs/apk/debug/app-debug.apk`

---

## 🔄 GitHub Actions CI/CD

This repository includes a preconfigured GitHub Actions workflow in `.github/workflows/android-ci.yml`:
- Runs automatically on every `push` and `pull_request` to `main`.
- Validates the Gradle build environment.
- Executes the entire unit and Robolectric test suite.
- Builds and uploads the Debug APK as a downloadable artifact.

---

## 🏗️ Architecture & Tech Stack

```
AI Shorts Factory
├── app/
│   ├── src/main/java/com/example/
│   │   ├── data/
│   │   │   ├── local/          # Room Database, Entities, DAOs (AppDatabase.kt)
│   │   │   ├── model/          # Data models & Segment serialization
│   │   │   ├── repository/     # ShortsRepository (Pipeline Orchestration)
│   │   │   ├── service/        # GeminiApiService & AI Logic
│   │   │   ├── util/           # YouTubeImportHelper & VideoUploadValidator
│   │   │   └── worker/         # VideoTranscriptionWorker (Speech-to-Text)
│   │   └── ui/
│   │       ├── components/     # YouTubeImportSection, DeviceMediaScanner
│   │       ├── screens/        # Dashboard, Create, Studio, Export, Library
│   │       ├── theme/          # Material 3 Color Schemes & Typography
│   │       └── MainApp.kt      # App Shell & Navigation
│   └── build.gradle.kts        # App Gradle Dependencies
├── public/                     # Static Web Showcase for Vercel
│   └── index.html              # Interactive Shorts Studio Simulator
├── .github/workflows/          # GitHub Actions CI/CD Pipeline
├── vercel.json                 # Vercel Deployment Configuration
├── package.json                # Project & Script Metadata
└── build.gradle.kts            # Root Gradle Build Configuration
```

---

## 📄 License
Distributed under the Apache-2.0 License. See `LICENSE` for details.
