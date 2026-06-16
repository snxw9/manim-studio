# Update Navigation in MainActivity Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace gallery and templates navigation placeholders with real screens in `MainActivity.kt`.

**Architecture:** Update the `NavHost` in `MainActivity` to use the newly created `GalleryScreen` and `TemplatesScreen` composables instead of temporary `Box` placeholders.

**Tech Stack:** Kotlin, Jetpack Compose, Jetpack Navigation.

---

### Task 1: Update MainActivity Navigation

**Files:**
- Modify: `android/app/src/main/java/com/manimstudio/app/MainActivity.kt`

- [ ] **Step 1: Add imports for GalleryScreen and TemplatesScreen**

```kotlin
import com.manimstudio.app.ui.screens.GalleryScreen
import com.manimstudio.app.ui.screens.TemplatesScreen
```

- [ ] **Step 2: Replace Gallery placeholder in NavHost**

```kotlin
                        // ROUTE 4: Gallery
                        composable("gallery") {
                            GalleryScreen(onBackClick = { navController.popBackStack() })
                        }
```

- [ ] **Step 3: Replace Templates placeholder in NavHost**

```kotlin
                        // ROUTE 5: Templates
                        composable("templates") {
                            TemplatesScreen(onBackClick = { navController.popBackStack() })
                        }
```

- [ ] **Step 4: Verify syntax and basic compilation (optional/ideal if possible)**

Run: `./gradlew :app:assembleDebug` (if env supports it)

- [ ] **Step 5: Commit changes**

```bash
git add android/app/src/main/java/com/manimstudio/app/MainActivity.kt
git commit -m "feat: wire real Gallery and Templates screens in NavHost"
```
