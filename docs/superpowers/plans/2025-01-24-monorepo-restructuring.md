# Monorepo Restructuring Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Restructure the manim-studio codebase into a clean monorepo with organized scripts, shared assets, Android scaffold, and GitHub Actions.

**Architecture:** Moving root scripts to a `scripts/` directory, creating a `shared/` directory for data used across components, setting up an `android/` scaffold, and organizing documentation in `docs/`.

**Tech Stack:** Bash/Batch scripts, JSON, Gradle (Android), GitHub Actions YAML.

---

### Task 1: Scripts Organization

**Files:**
- Create: `scripts/START.bat`
- Create: `scripts/STOP.bat`
- Create: `scripts/START.sh`
- Create: `scripts/STOP.sh`
- Modify: `START.bat`
- Modify: `STOP.bat`

- [ ] **Step 1: Create `scripts/START.bat`**

```batch
@echo off
setlocal
cd /d "%~dp0..\web"
npm run dev
endlocal
```

- [ ] **Step 2: Create `scripts/STOP.bat`**

```batch
@echo off
taskkill /F /IM node.exe /T
taskkill /F /IM python.exe /T
echo Stopped all services.
```

- [ ] **Step 3: Create `scripts/START.sh`**

```bash
#!/bin/bash
cd "$(dirname "$0")/../web"
npm run dev
```

- [ ] **Step 4: Create `scripts/STOP.sh`**

```bash
#!/bin/bash
pkill -f node
pkill -f python
echo "Stopped all services."
```

- [ ] **Step 5: Update root `START.bat`**

```batch
@echo off
call "%~dp0scripts\START.bat"
```

- [ ] **Step 6: Update root `STOP.bat`**

```batch
@echo off
call "%~dp0scripts\STOP.bat"
```

- [ ] **Step 7: Commit**

```bash
git add scripts/ root scripts
git commit -m "chore: move scripts to scripts directory and add shell scripts"
```

### Task 2: Shared Assets and Data

**Files:**
- Create: `shared/templates.json`
- Create: `shared/error-codes.json`
- Create: `shared/assets.json`

- [ ] **Step 1: Create `shared/templates.json`**
Extract templates from `web/lib/templates.ts`.

- [ ] **Step 2: Create `shared/error-codes.json`**
Extract error definitions from `web/lib/errorCodes.ts`.

- [ ] **Step 3: Create `shared/assets.json`**

```json
{
  "version": "1.0.0",
  "assets": []
}
```

- [ ] **Step 4: Commit**

```bash
git add shared/
git commit -m "feat: add shared templates, error codes, and assets"
```

### Task 3: Android Scaffold

**Files:**
- Create: `android/settings.gradle.kts`
- Create: `android/build.gradle.kts`
- Create: `android/app/build.gradle.kts`
- Create: `android/app/src/main/AndroidManifest.xml`
- Create: `android/app/src/main/java/com/manimstudio/app/MainActivity.kt`
- Create: `android/app/src/main/res/values/themes.xml`

- [ ] **Step 1: Create `android/settings.gradle.kts`**

```kotlin
rootProject.name = "ManimStudio"
include(":app")
```

- [ ] **Step 2: Create `android/build.gradle.kts`**

```kotlin
plugins {
    id("com.android.application") version "8.2.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.0" apply false
}
```

- [ ] **Step 3: Create `android/app/build.gradle.kts`**

```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.manimstudio.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.manimstudio.app"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}
```

- [ ] **Step 4: Create `android/app/src/main/AndroidManifest.xml`**

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <application
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="Manim Studio"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.ManimStudio">
        <activity
            android:name=".MainActivity"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
```

- [ ] **Step 5: Create `android/app/src/main/java/com/manimstudio/app/MainActivity.kt`**

```kotlin
package com.manimstudio.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
}
```

- [ ] **Step 6: Create `android/app/src/main/res/values/themes.xml`**

```xml
<resources>
    <style name="Theme.ManimStudio" parent="Theme.MaterialComponents.DayNight.NoActionBar" />
</resources>
```

- [ ] **Step 7: Commit**

```bash
git add android/
git commit -m "feat: add android scaffold"
```

### Task 4: GitHub Actions and Templates

**Files:**
- Create: `.github/workflows/engine-tests.yml`
- Create: `.github/workflows/web.yml`
- Create: `.github/workflows/android.yml`
- Create: `.github/ISSUE_TEMPLATE/bug_report.md`
- Create: `.github/ISSUE_TEMPLATE/feature_request.md`

- [ ] **Step 1: Create `.github/workflows/engine-tests.yml`**

```yaml
name: Engine Tests

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Set up Python
        uses: actions/setup-python@v4
        with:
          python-version: '3.11'
      - name: Install dependencies
        run: |
          cd engine
          pip install -r requirements.txt
          pip install pytest
      - name: Run tests
        run: |
          cd engine
          pytest
```

- [ ] **Step 2: Create `.github/workflows/web.yml`**

```yaml
name: Web CI

on: [push, pull_request]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Set up Node.js
        uses: actions/setup-node@v4
        with:
          node-version: '20'
      - name: Install dependencies
        run: |
          cd web
          npm install
      - name: Build
        run: |
          cd web
          npm run build
```

- [ ] **Step 3: Create `.github/workflows/android.yml`**

```yaml
name: Android CI

on: [push, pull_request]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
      - name: Grant execute permission for gradlew
        run: |
          cd android
          if [ -f gradlew ]; then chmod +x gradlew; fi
      - name: Build with Gradle
        run: |
          cd android
          if [ -f gradlew ]; then ./gradlew build; else echo "gradlew not found, skipping build"; fi
```

- [ ] **Step 4: Create `.github/ISSUE_TEMPLATE/bug_report.md`**

```markdown
---
name: Bug report
about: Create a report to help us improve
title: ''
labels: bug
assignees: ''
---

**Describe the bug**
A clear and concise description of what the bug is.

**To Reproduce**
Steps to reproduce the behavior.

**Expected behavior**
A clear and concise description of what you expected to happen.

**Screenshots**
If applicable, add screenshots to help explain your problem.

**Environment:**
 - OS: [e.g. Windows, macOS]
 - Browser [e.g. chrome, safari]
 - Engine Version [e.g. v1.0.0]
```

- [ ] **Step 5: Create `.github/ISSUE_TEMPLATE/feature_request.md`**

```markdown
---
name: Feature request
about: Suggest an idea for this project
title: ''
labels: enhancement
assignees: ''
---

**Is your feature request related to a problem? Please describe.**
A clear and concise description of what the problem is. Ex. I'm always frustrated when [...]

**Describe the solution you'd like**
A clear and concise description of what you want to happen.

**Describe alternatives you've considered**
A clear and concise description of any alternative solutions or features you've considered.

**Additional context**
Add any other context or screenshots about the feature request here.
```

- [ ] **Step 6: Commit**

```bash
git add .github/
git commit -m "feat: add github workflows and issue templates"
```

### Task 5: Root Files Update

**Files:**
- Modify: `.gitignore`
- Modify: `README.md`

- [ ] **Step 1: Update root `.gitignore`**

```text
# General
.env
.DS_Store
node_modules/
dist/
build/
.next/
out/

# Engine
engine/venv/
engine/__pycache__/
engine/media/
engine/media_cache/
engine/outputs/
engine/latex_cache/
engine/test_latex_dir/
*.log

# Android
.gradle/
android/app/build/
android/local.properties
*.iml

# Shared
shared/assets.json
```

- [ ] **Step 2: Update root `README.md`**

```markdown
# Manim Studio

A monorepo for Manim Studio, an interactive environment for creating mathematical animations with AI.

## Repository Structure

- `engine/`: Python-based rendering engine using Manim.
- `web/`: Next.js web application for the user interface.
- `android/`: Android application scaffold.
- `shared/`: Shared assets and data definitions.
- `scripts/`: Management and automation scripts.
- `docs/`: Project documentation.

## Getting Started

1.  **Clone the repository:**
    ```bash
    git clone https://github.com/your-org/manim-studio.git
    cd manim-studio
    ```

2.  **Start the services (Windows):**
    Double-click `START.bat` in the root directory.

3.  **Start the services (Mac/Linux):**
    ```bash
    ./scripts/START.sh
    ```

## License

MIT
```

- [ ] **Step 3: Commit**

```bash
git add .gitignore README.md
git commit -m "chore: update root gitignore and readme"
```

### Task 6: Documentation and Cleanup

**Files:**
- Create: `docs/CONTEXT.md`
- Delete: `MANIM_STUDIO_CONTEXT.md`

- [ ] **Step 1: Create `docs/CONTEXT.md`**
Merge content from `MANIM_STUDIO_CONTEXT.md` and `replit-ui/replit.md`. Update structure.

- [ ] **Step 2: Delete `MANIM_STUDIO_CONTEXT.md`**

- [ ] **Step 3: Commit**

```bash
git add docs/
git rm MANIM_STUDIO_CONTEXT.md
git commit -m "docs: consolidate documentation in docs/CONTEXT.md"
```

---
