# Android UI/UX Refresh Design Spec

**Date:** 2026-05-26
**Topic:** Android UI/UX Refresh (Gemini-style)
**Goal:** Implement a modern, floating UI aesthetic for the Manim Studio Android app, including a refined input pill, cleaned-up sidebar, and a new card-based Settings screen with full navigation wiring.

---

## 1. Architecture & Components

### 1.1 Floating Input Pill (`FloatingPromptInput.kt`)
Refine the main prompt input to feel "lifted" and more substantial.
- **Outer Padding:** Increase `horizontal` and `bottom` padding to `24.dp`.
- **Inner Padding:** Increase internal padding to `12.dp` (all sides).
- **Shape:** `RoundedCornerShape(32.dp)`.
- **Background:** `Color(0xFF1E1F22)`.

### 1.2 Sidebar Cleanup (`StudioScreen.kt`)
Streamline the sidebar content and wire the settings entry point.
- **Items:** 
    - "New chat" (Edit icon)
    - "Search chats" (Search icon)
    - "Gallery" (VideoLibrary icon) - *Renamed from Videos*
    - "Templates" (GridView icon) - *Renamed from Library*
- **Removals:** Remove the "Notebooks" section entirely.
- **Spacing:** `32.dp` Spacer before the "Recent" section.
- **Settings Entry:** The bottom Profile/Settings row becomes clickable and triggers navigation to the Settings screen.

### 1.3 Gemini-Style Settings Screen (`SettingsScreen.kt`)
New screen featuring grouped cards on a pure black background.
- **Background:** `Color.Black` (AMOLED).
- **Cards:** `Color(0xFF1E1F22)` with `RoundedCornerShape(24.dp)`.
- **Sections:**
    - **AI Engine & Processing:** Default Provider, Offline Fallback, Thinking Level.
    - **Render Quality:** Resolution, Frame Rate, Aspect Ratio.
    - **Downloads & Storage:** Save Location, Clear Render Cache.
- **Accents:** Manim Orange (`0xFFFF8C00`) for section titles.

---

## 2. Navigation Flow (`MainActivity.kt`)

The `NavHost` in `MainActivity` will be updated to include the new Settings screen.

- **Route:** `"settings"`
- **Transitions:**
    - `StudioScreen` -> `SettingsScreen`: Via `navController.navigate("settings")`.
    - `SettingsScreen` -> `StudioScreen`: Via `navController.popBackStack()`.

---

## 3. Data & State

- **Settings State:** Initial implementation will focus on the UI/stateless components or simple local state. Full persistence integration (DataStore/Room) is out of scope for this UI refresh but the UI will be structured to accept state in the future.

---

## 4. Testing & Validation

### 4.1 Visual Verification
- Verify the input pill doesn't clip on smaller screens with the increased padding.
- Verify the sidebar scrolls correctly if "Recent" list is long.
- Verify the Settings screen is accessible and the "Back" button works.

### 4.2 Integration Verification
- Ensure `enableEdgeToEdge()` is respected by checking padding for status/navigation bars.
