# Android Interactive Screens & Content Grids Design Spec

**Date:** 2026-05-26
**Topic:** Interactive Settings, Gallery, and Templates Grids
**Goal:** Enhance the Android app with interactive navigation, a professional profile section, and visual grids for templates and rendered videos.

---

## 1. Architecture & Screens

### 1.1 Interactive Settings (`SettingsScreen.kt`)
- **New Section:** "Account" section at the top with a `48.dp` circular avatar (Manim Orange) and user details.
- **Interactivity:** All rows (Profile + Settings items) receive `Modifier.clickable` and a `ChevronRight` trailing icon.
- **Visuals:** Maintain Card-based grouping with `RoundedCornerShape(24.dp)`.

### 1.2 Templates Grid (`TemplatesScreen.kt`)
- **Layout:** `LazyVerticalGrid` (2 columns).
- **Cards:** `aspectRatio(0.85f)`, dark grey background (`0xFF1E1F22`), rounded corners (`24.dp`).
- **Icons:** Circular placeholder with `Icons.Rounded.Category` in Manim Orange.
- **Content:** Title + 2-line description for mathematical concepts.

### 1.3 Gallery Grid & Metadata (`GalleryScreen.kt`)
- **Layout:** `LazyVerticalGrid` (2 columns), `aspectRatio(16f/9f)` video cards.
- **States:**
    - **Empty State:** Centered layout with `Icons.Rounded.VideoLibrary` + "No animations yet".
    - **Populated State:** Mock list of rendered MP4s.
- **Interactive Metadata (Hover Reveal):** 
    - On tap, show a subtle black gradient at the bottom of the card.
    - Overlay white text with metadata (e.g., "1080p • 24MB • 13s").
    - The reveal should feel integrated, not covering the whole card.

---

## 2. Navigation Wiring (`MainActivity.kt`)

- **Replacements:** Swap `Box(Color.Black)` placeholders for `GalleryScreen` and `TemplatesScreen`.
- **Wiring:** All navigation callbacks (`onBackClick`, `onNavigateTo...`) are passed through the `NavHost`.

---

## 3. Data Model (Mock)

- **Templates:** List of Pairs (Name, Description).
- **Gallery:** List of Strings (Filenames) + associated mock metadata.

---

## 4. Testing & Validation

- **Navigation:** Test round-trip navigation: Studio -> Gallery -> Back.
- **Interactivity:** Verify ripple effects on all clickable rows and gallery cards.
- **Visuals:** Ensure the metadata overlay in the Gallery is legible against various card backgrounds.
