# Calculator Decoy Feature — Design Spec

## Overview

The app disguises itself as a Material You calculator. It launches as a fully functional basic calculator. When the user types any math expression whose result equals their secret code and presses `=`, the app instantly transitions to Telegram. To anyone else, it looks and behaves like a normal calculator.

## Requirements

- **Always on**: The calculator is always the first screen. There is no toggle.
- **First-launch setup**: On first open, a setup screen asks the user to pick a secret numeric code (entered twice for confirmation).
- **Unlock method**: Any math expression whose evaluated result matches the stored secret code triggers the transition to Telegram.
- **Calculator style**: Material You / colorful (light purple palette, rounded buttons).
- **Basic operations**: `+`, `-`, `×`, `÷` with proper operator precedence. No advanced functions.
- **App identity**: App name is "Calculator", launcher icon is a calculator icon. All Telegram icon-switcher aliases are removed.
- **Instant transition**: On correct code, Telegram appears immediately with no visible transition animation.
- **Secret code management**: Changeable via Forkgram debug menu (long-press version number twice in Settings).
- **Re-entry required**: Every time the app is re-opened from the launcher, the calculator is shown. The user must enter the code again.

## Architecture: New Launcher Activity

### Approach

Create a new `CalculatorActivity` that becomes the sole launcher activity. It intercepts the app before `LaunchActivity` ever runs. On correct code entry, it starts `LaunchActivity` and finishes itself. Telegram code is completely untouched — `LaunchActivity` has no knowledge of the calculator.

### Why this approach

- Clean separation: calculator is standalone, Telegram code untouched
- Telegram never loads until unlocked (no background memory use, no premature notifications)
- Easy to maintain across upstream Telegram updates
- Activity transition made invisible with `overridePendingTransition(0, 0)`

## Components

### 1. CalculatorActivity

**File:** `TMessagesProj/src/main/java/org/telegram/ui/CalculatorActivity.java`

- Registered in `AndroidManifest.xml` as the sole launcher activity
- `launchMode="singleTask"`
- Custom Material You calculator theme
- `FLAG_SECURE` set on the window

**onCreate flow:**
1. Check if `setup_complete` exists in `calculator_prefs` SharedPreferences
2. If no → show setup screen (see section 2)
3. If yes → show calculator UI

**Calculator UI:**
- Display area at top: current expression and result
- 4×4 button grid: `C`, `()`, `%`, `÷`, `7`, `8`, `9`, `×`, `4`, `5`, `6`, `−`, `1`, `2`, `3`, `+`, `+/-`, `0`, `.`, `=`
- Expression evaluation: simple stack-based evaluator (~50 lines) supporting `+`, `-`, `×`, `÷` with operator precedence

**On `=` press:**
1. Evaluate the expression
2. Hash the integer portion of the result with the stored salt using SHA-256
3. Compare against stored hash in `calculator_prefs`
4. If match → `startActivity(new Intent(this, LaunchActivity.class))`, `finish()`, `overridePendingTransition(0, 0)`
5. If no match → display result normally

**Back button:** Exits the app. Never reveals Telegram.

### 2. First-Launch Setup Screen

Shown inside `CalculatorActivity` when `setup_complete` is false.

**UI:**
- Centered layout
- Title: "Set your secret code"
- Subtitle: "Enter a number. Any math expression that equals this number will unlock the app."
- Numeric input field
- "Confirm" button
- Second screen: "Re-enter your secret code" (typo prevention)

**Storage:**
- Secret code stored as SHA-256 hash with random salt
- SharedPreferences file: `calculator_prefs`
- Keys: `secret_hash` (String), `secret_salt` (String), `setup_complete` (boolean)

### 3. App Identity Changes

**App name:**
- `TMessagesProj_App/src/main/res/values/strings.xml`: `app_name` → `"Calculator"`

**App icon:**
- Replace launcher icon resources in all density buckets (`mdpi` through `xxxhdpi`)
- Material You style calculator icon (adaptive icon with vector foreground)

**Manifest changes:**
- Remove all activity-aliases (DefaultIcon, OriginalIcon, VintageIcon, AquaIcon, PremiumIcon, TurboIcon, NoxIcon)
- Add `CalculatorActivity` with MAIN/LAUNCHER intent filter
- `LaunchActivity` remains in manifest but with no LAUNCHER intent filter (started only internally)

**Recents screen:**
- Shows "Calculator" as task label (inherited from CalculatorActivity)

### 4. Secret Code Management (Hidden Settings)

**Location:** Forkgram debug menu (long-press version number twice in Settings)

**New menu entry:** "Change calculator code"

**Dialog flow:**
1. "Enter current code" → verify against stored hash
2. "Enter new code"
3. "Confirm new code"
4. Save new SHA-256 hash + salt to `calculator_prefs`

### 5. App Lifecycle & Security

**Re-opening the app:**
- `CalculatorActivity` is the launcher, so it always intercepts
- Every re-open from launcher shows the calculator — code must be re-entered
- No grace period

**Notifications:**
- Force-disable notification content previews on first setup
- Notifications show "New message" without sender or text
- User can adjust from within Telegram's notification settings once unlocked

**FLAG_SECURE:**
- Set on `CalculatorActivity` window to prevent screenshots/screen recordings of the calculator and the transition

**Back button:**
- On calculator: exits app
- Never reveals Telegram

## Data Storage

**SharedPreferences file:** `calculator_prefs` (separate from all Telegram prefs)

| Key | Type | Description |
|-----|------|-------------|
| `setup_complete` | boolean | Whether first-launch setup has been completed |
| `secret_hash` | String | SHA-256 hash of the secret code |
| `secret_salt` | String | Random salt used for hashing |

## Secret Code Matching

- Expression result is evaluated as a double
- For comparison, the result is truncated to an integer (e.g. `1234.0` → `1234`)
- The integer is converted to a string, concatenated with the stored salt, and SHA-256 hashed
- The hash is compared against the stored `secret_hash`

## Files to Create

- `TMessagesProj/src/main/java/org/telegram/ui/CalculatorActivity.java` — launcher activity with calculator UI, setup screen, and expression evaluator

## Files to Modify

- `TMessagesProj/src/main/AndroidManifest.xml` — remove icon aliases, add CalculatorActivity as launcher, remove LAUNCHER filter from LaunchActivity
- `TMessagesProj_App/src/main/res/values/strings.xml` — change app_name to "Calculator"
- Launcher icon resources — replace with calculator icon
- Forkgram debug menu (in Settings) — add "Change calculator code" entry
- `ApplicationLoader.java` or notification setup — force-disable notification previews on first setup
