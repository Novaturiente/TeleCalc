# Calculator Decoy Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Disguise the Telegram app as a Material You calculator that unlocks into Telegram when the user enters a math expression whose result matches their secret code.

**Architecture:** New `CalculatorActivity` becomes the sole launcher activity. It shows a calculator UI and, on correct code entry, starts the existing `LaunchActivity` with no visible transition. First launch shows a setup screen to pick the secret code. The secret code can be changed from the Forkgram debug menu.

**Tech Stack:** Java, Android SDK, SharedPreferences, SHA-256 hashing, Android adaptive icons (vector drawables)

---

## File Structure

| File | Action | Responsibility |
|------|--------|---------------|
| `TMessagesProj/src/main/java/org/telegram/ui/CalculatorActivity.java` | Create | Launcher activity: calculator UI, setup screen, expression evaluator, unlock logic |
| `TMessagesProj/src/main/res/layout/activity_calculator.xml` | Create | Calculator UI layout |
| `TMessagesProj/src/main/res/layout/activity_calculator_setup.xml` | Create | First-launch setup layout |
| `TMessagesProj/src/main/res/values/colors_calculator.xml` | Create | Material You purple palette colors |
| `TMessagesProj/src/main/res/drawable/calc_button_number.xml` | Create | Ripple drawable for number buttons |
| `TMessagesProj/src/main/res/drawable/calc_button_operator.xml` | Create | Ripple drawable for operator buttons |
| `TMessagesProj/src/main/res/drawable/calc_button_equals.xml` | Create | Ripple drawable for equals button |
| `TMessagesProj/src/main/res/drawable/ic_calc_foreground.xml` | Create | Vector drawable calculator icon foreground |
| `TMessagesProj/src/main/res/drawable/ic_calc_background.xml` | Create | Solid color background for adaptive icon |
| `TMessagesProj/src/main/res/mipmap-anydpi-v26/ic_calc_launcher.xml` | Create | Adaptive icon definition |
| `TMessagesProj/src/main/res/mipmap-anydpi-v26/ic_calc_launcher_round.xml` | Create | Round adaptive icon definition |
| `TMessagesProj/src/main/AndroidManifest.xml` | Modify | Remove icon aliases, add CalculatorActivity as launcher |
| `TMessagesProj/src/main/res/values/strings.xml` | Modify | Change AppName to "Calculator" |
| `TMessagesProj/src/main/java/org/telegram/ui/SettingsActivity.java` | Modify | Add "Change calculator code" to debug menu |

---

### Task 1: Create Calculator Color Resources

**Files:**
- Create: `TMessagesProj/src/main/res/values/colors_calculator.xml`

- [ ] **Step 1: Create the color resource file**

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <color name="calc_background">#F5F0FF</color>
    <color name="calc_display_text">#49454F</color>
    <color name="calc_number_bg">#E6E0E9</color>
    <color name="calc_number_text">#1C1B1F</color>
    <color name="calc_operator_bg">#E8DEF8</color>
    <color name="calc_operator_text">#6750A4</color>
    <color name="calc_equals_bg">#6750A4</color>
    <color name="calc_equals_text">#FFFFFF</color>
    <color name="calc_ripple">#1F6750A4</color>
    <color name="calc_setup_accent">#6750A4</color>
</resources>
```

Write this file to `TMessagesProj/src/main/res/values/colors_calculator.xml`.

- [ ] **Step 2: Create button ripple drawables**

Write `TMessagesProj/src/main/res/drawable/calc_button_number.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<ripple xmlns:android="http://schemas.android.com/apk/res/android"
    android:color="@color/calc_ripple">
    <item>
        <shape android:shape="rectangle">
            <solid android:color="@color/calc_number_bg" />
            <corners android:radius="24dp" />
        </shape>
    </item>
</ripple>
```

Write `TMessagesProj/src/main/res/drawable/calc_button_operator.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<ripple xmlns:android="http://schemas.android.com/apk/res/android"
    android:color="@color/calc_ripple">
    <item>
        <shape android:shape="rectangle">
            <solid android:color="@color/calc_operator_bg" />
            <corners android:radius="24dp" />
        </shape>
    </item>
</ripple>
```

Write `TMessagesProj/src/main/res/drawable/calc_button_equals.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<ripple xmlns:android="http://schemas.android.com/apk/res/android"
    android:color="#3DFFFFFF">
    <item>
        <shape android:shape="rectangle">
            <solid android:color="@color/calc_equals_bg" />
            <corners android:radius="24dp" />
        </shape>
    </item>
</ripple>
```

- [ ] **Step 3: Commit**

```bash
git add TMessagesProj/src/main/res/values/colors_calculator.xml \
       TMessagesProj/src/main/res/drawable/calc_button_number.xml \
       TMessagesProj/src/main/res/drawable/calc_button_operator.xml \
       TMessagesProj/src/main/res/drawable/calc_button_equals.xml
git commit -m "feat: add calculator decoy color resources and button drawables"
```

---

### Task 2: Create Calculator Layouts

**Files:**
- Create: `TMessagesProj/src/main/res/layout/activity_calculator.xml`
- Create: `TMessagesProj/src/main/res/layout/activity_calculator_setup.xml`

- [ ] **Step 1: Create the calculator layout**

Write `TMessagesProj/src/main/res/layout/activity_calculator.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:background="@color/calc_background">

    <!-- Display area -->
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_weight="1"
        android:orientation="vertical"
        android:gravity="end|bottom"
        android:padding="24dp">

        <TextView
            android:id="@+id/calc_expression"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:gravity="end"
            android:textSize="24sp"
            android:textColor="#9E9E9E"
            android:maxLines="2"
            android:ellipsize="start"
            android:fontFamily="sans-serif-light" />

        <TextView
            android:id="@+id/calc_result"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:gravity="end"
            android:text="0"
            android:textSize="48sp"
            android:textColor="@color/calc_display_text"
            android:maxLines="1"
            android:ellipsize="start"
            android:fontFamily="sans-serif" />
    </LinearLayout>

    <!-- Button grid -->
    <GridLayout
        android:id="@+id/calc_grid"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:columnCount="4"
        android:rowCount="5"
        android:padding="8dp"
        android:useDefaultMargins="false">

        <!-- Row 1: C () % ÷ -->
        <!-- Row 2: 7 8 9 × -->
        <!-- Row 3: 4 5 6 − -->
        <!-- Row 4: 1 2 3 + -->
        <!-- Row 5: +/- 0 . = -->
        <!-- Buttons are created programmatically in CalculatorActivity -->

    </GridLayout>

</LinearLayout>
```

- [ ] **Step 2: Create the setup layout**

Write `TMessagesProj/src/main/res/layout/activity_calculator_setup.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:gravity="center"
    android:padding="32dp"
    android:background="@color/calc_background">

    <TextView
        android:id="@+id/setup_title"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="Set your secret code"
        android:textSize="24sp"
        android:textColor="@color/calc_display_text"
        android:fontFamily="sans-serif-medium" />

    <TextView
        android:id="@+id/setup_subtitle"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginTop="12dp"
        android:text="Enter a number. Any math expression that equals this number will unlock the app."
        android:textSize="14sp"
        android:textColor="#9E9E9E"
        android:gravity="center"
        android:fontFamily="sans-serif" />

    <EditText
        android:id="@+id/setup_code_input"
        android:layout_width="200dp"
        android:layout_height="wrap_content"
        android:layout_marginTop="32dp"
        android:gravity="center"
        android:inputType="numberSigned"
        android:textSize="32sp"
        android:textColor="@color/calc_display_text"
        android:backgroundTint="@color/calc_setup_accent"
        android:fontFamily="sans-serif-medium" />

    <Button
        android:id="@+id/setup_confirm_button"
        android:layout_width="200dp"
        android:layout_height="56dp"
        android:layout_marginTop="24dp"
        android:text="Confirm"
        android:textSize="16sp"
        android:textColor="@color/calc_equals_text"
        android:background="@drawable/calc_button_equals"
        android:fontFamily="sans-serif-medium" />

</LinearLayout>
```

- [ ] **Step 3: Commit**

```bash
git add TMessagesProj/src/main/res/layout/activity_calculator.xml \
       TMessagesProj/src/main/res/layout/activity_calculator_setup.xml
git commit -m "feat: add calculator and setup screen layouts"
```

---

### Task 3: Create CalculatorActivity — Setup Screen

**Files:**
- Create: `TMessagesProj/src/main/java/org/telegram/ui/CalculatorActivity.java`

- [ ] **Step 1: Create CalculatorActivity with setup logic**

Write `TMessagesProj/src/main/java/org/telegram/ui/CalculatorActivity.java`:

```java
package org.telegram.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.telegram.messenger.R;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.ArrayList;

public class CalculatorActivity extends Activity {

    private static final String PREFS_NAME = "calculator_prefs";
    private static final String KEY_SETUP_COMPLETE = "setup_complete";
    private static final String KEY_SECRET_HASH = "secret_hash";
    private static final String KEY_SECRET_SALT = "secret_salt";

    private TextView expressionView;
    private TextView resultView;
    private StringBuilder currentExpression = new StringBuilder();
    private boolean lastPressWasEquals = false;

    // Setup state
    private String pendingCode = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE);

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        if (prefs.getBoolean(KEY_SETUP_COMPLETE, false)) {
            showCalculator();
        } else {
            showSetup();
        }
    }

    // ========================
    // SETUP SCREEN
    // ========================

    private void showSetup() {
        setContentView(R.layout.activity_calculator_setup);

        TextView title = findViewById(R.id.setup_title);
        TextView subtitle = findViewById(R.id.setup_subtitle);
        EditText codeInput = findViewById(R.id.setup_code_input);
        Button confirmButton = findViewById(R.id.setup_confirm_button);

        confirmButton.setOnClickListener(v -> {
            String input = codeInput.getText().toString().trim();
            if (input.isEmpty()) {
                Toast.makeText(this, "Please enter a number", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                Integer.parseInt(input);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Please enter a valid number", Toast.LENGTH_SHORT).show();
                return;
            }

            if (pendingCode == null) {
                // First entry
                pendingCode = input;
                title.setText("Confirm your secret code");
                subtitle.setText("Re-enter the same number to confirm.");
                codeInput.setText("");
                confirmButton.setText("Set Code");
            } else {
                // Confirmation
                if (pendingCode.equals(input)) {
                    saveSecretCode(input);
                    showCalculator();
                } else {
                    Toast.makeText(this, "Codes don't match. Try again.", Toast.LENGTH_SHORT).show();
                    pendingCode = null;
                    title.setText("Set your secret code");
                    subtitle.setText("Enter a number. Any math expression that equals this number will unlock the app.");
                    codeInput.setText("");
                    confirmButton.setText("Confirm");
                }
            }
        });
    }

    // ========================
    // SECRET CODE HASHING
    // ========================

    private void saveSecretCode(String code) {
        String salt = generateSalt();
        String hash = hashCode(code, salt);
        getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_SECRET_HASH, hash)
                .putString(KEY_SECRET_SALT, salt)
                .putBoolean(KEY_SETUP_COMPLETE, true)
                .apply();
    }

    private String generateSalt() {
        byte[] saltBytes = new byte[16];
        new SecureRandom().nextBytes(saltBytes);
        StringBuilder sb = new StringBuilder();
        for (byte b : saltBytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private String hashCode(String code, String salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest((code + salt).getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private boolean checkSecretCode(String code) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String storedHash = prefs.getString(KEY_SECRET_HASH, "");
        String storedSalt = prefs.getString(KEY_SECRET_SALT, "");
        String inputHash = hashCode(code, storedSalt);
        return inputHash.equals(storedHash);
    }

    // ========================
    // CALCULATOR UI
    // ========================

    private void showCalculator() {
        setContentView(R.layout.activity_calculator);
        expressionView = findViewById(R.id.calc_expression);
        resultView = findViewById(R.id.calc_result);

        GridLayout grid = findViewById(R.id.calc_grid);
        grid.removeAllViews();

        String[][] buttons = {
                {"C", "()", "%", "\u00F7"},
                {"7", "8", "9", "\u00D7"},
                {"4", "5", "6", "\u2212"},
                {"1", "2", "3", "+"},
                {"+/-", "0", ".", "="}
        };

        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int padding = (int) (8 * getResources().getDisplayMetrics().density);
        int spacing = (int) (6 * getResources().getDisplayMetrics().density);
        int buttonSize = (screenWidth - padding * 2 - spacing * 3) / 4;
        int buttonHeight = (int) (buttonSize * 0.75f);

        for (int row = 0; row < buttons.length; row++) {
            for (int col = 0; col < buttons[row].length; col++) {
                String label = buttons[row][col];
                TextView btn = new TextView(this);
                btn.setText(label);
                btn.setGravity(Gravity.CENTER);
                btn.setTextSize(20);

                if (label.equals("=")) {
                    btn.setBackgroundResource(R.drawable.calc_button_equals);
                    btn.setTextColor(getResources().getColor(R.color.calc_equals_text));
                } else if (isOperator(label)) {
                    btn.setBackgroundResource(R.drawable.calc_button_operator);
                    btn.setTextColor(getResources().getColor(R.color.calc_operator_text));
                } else {
                    btn.setBackgroundResource(R.drawable.calc_button_number);
                    btn.setTextColor(getResources().getColor(R.color.calc_number_text));
                }

                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = buttonSize;
                params.height = buttonHeight;
                params.setMargins(spacing / 2, spacing / 2, spacing / 2, spacing / 2);
                params.rowSpec = GridLayout.spec(row);
                params.columnSpec = GridLayout.spec(col);
                btn.setLayoutParams(params);
                btn.setClickable(true);

                btn.setOnClickListener(v -> onButtonClick(label));
                grid.addView(btn);
            }
        }
    }

    private boolean isOperator(String label) {
        return label.equals("C") || label.equals("()") || label.equals("%")
                || label.equals("\u00F7") || label.equals("\u00D7")
                || label.equals("\u2212") || label.equals("+")
                || label.equals("+/-");
    }

    private void onButtonClick(String label) {
        switch (label) {
            case "C":
                currentExpression.setLength(0);
                resultView.setText("0");
                expressionView.setText("");
                lastPressWasEquals = false;
                break;
            case "=":
                onEquals();
                break;
            case "+/-":
                toggleSign();
                break;
            case "()":
                handleParentheses();
                break;
            default:
                if (lastPressWasEquals) {
                    // After equals, start a new expression if user types a number
                    if (!isOperatorChar(label)) {
                        currentExpression.setLength(0);
                    }
                    lastPressWasEquals = false;
                }
                currentExpression.append(normalizeOperator(label));
                expressionView.setText(currentExpression.toString());
                break;
        }
    }

    private String normalizeOperator(String label) {
        switch (label) {
            case "\u00F7": return "/";
            case "\u00D7": return "*";
            case "\u2212": return "-";
            default: return label;
        }
    }

    private boolean isOperatorChar(String label) {
        return label.equals("+") || label.equals("\u2212")
                || label.equals("\u00D7") || label.equals("\u00F7");
    }

    private void toggleSign() {
        String expr = currentExpression.toString();
        if (expr.isEmpty()) return;

        // Find the last number in the expression and negate it
        int i = expr.length() - 1;
        while (i >= 0 && (Character.isDigit(expr.charAt(i)) || expr.charAt(i) == '.')) {
            i--;
        }

        if (i >= 0 && expr.charAt(i) == '-' && (i == 0 || "+-*/".indexOf(expr.charAt(i - 1)) >= 0)) {
            // Remove the negative sign
            currentExpression.deleteCharAt(i);
        } else {
            // Insert a negative sign
            currentExpression.insert(i + 1, "-");
        }
        expressionView.setText(currentExpression.toString());
    }

    private void handleParentheses() {
        String expr = currentExpression.toString();
        int openCount = 0, closeCount = 0;
        for (char c : expr.toCharArray()) {
            if (c == '(') openCount++;
            if (c == ')') closeCount++;
        }

        if (openCount > closeCount && expr.length() > 0) {
            char last = expr.charAt(expr.length() - 1);
            if (Character.isDigit(last) || last == ')') {
                currentExpression.append(')');
            } else {
                currentExpression.append('(');
            }
        } else {
            currentExpression.append('(');
        }
        expressionView.setText(currentExpression.toString());
    }

    // ========================
    // EXPRESSION EVALUATION
    // ========================

    private void onEquals() {
        String expr = currentExpression.toString();
        if (expr.isEmpty()) return;

        try {
            double result = evaluate(expr);
            String resultStr;
            if (result == (long) result) {
                resultStr = String.valueOf((long) result);
            } else {
                resultStr = String.valueOf(result);
            }
            resultView.setText(resultStr);
            expressionView.setText(expr + "=");
            lastPressWasEquals = true;
            currentExpression.setLength(0);
            currentExpression.append(resultStr);

            // Check if result matches secret code
            String intResult = String.valueOf((long) result);
            if (checkSecretCode(intResult)) {
                unlockTelegram();
            }
        } catch (Exception e) {
            resultView.setText("Error");
            currentExpression.setLength(0);
            lastPressWasEquals = false;
        }
    }

    /**
     * Simple recursive descent parser for arithmetic expressions.
     * Supports: +, -, *, /, parentheses, unary minus, %
     *
     * Grammar:
     *   expr    = term (('+' | '-') term)*
     *   term    = factor (('*' | '/') factor)*
     *   factor  = '-' factor | atom '%'? | atom
     *   atom    = NUMBER | '(' expr ')'
     */
    private double evaluate(String expr) {
        final char[] chars = expr.toCharArray();
        final int[] pos = {0};

        double result = parseExpr(chars, pos);
        if (pos[0] != chars.length) {
            throw new RuntimeException("Unexpected character at position " + pos[0]);
        }
        return result;
    }

    private double parseExpr(char[] chars, int[] pos) {
        double result = parseTerm(chars, pos);
        while (pos[0] < chars.length) {
            char op = chars[pos[0]];
            if (op == '+') {
                pos[0]++;
                result += parseTerm(chars, pos);
            } else if (op == '-') {
                pos[0]++;
                result -= parseTerm(chars, pos);
            } else {
                break;
            }
        }
        return result;
    }

    private double parseTerm(char[] chars, int[] pos) {
        double result = parseFactor(chars, pos);
        while (pos[0] < chars.length) {
            char op = chars[pos[0]];
            if (op == '*') {
                pos[0]++;
                result *= parseFactor(chars, pos);
            } else if (op == '/') {
                pos[0]++;
                result /= parseFactor(chars, pos);
            } else {
                break;
            }
        }
        return result;
    }

    private double parseFactor(char[] chars, int[] pos) {
        if (pos[0] < chars.length && chars[pos[0]] == '-') {
            pos[0]++;
            return -parseFactor(chars, pos);
        }
        double result = parseAtom(chars, pos);
        if (pos[0] < chars.length && chars[pos[0]] == '%') {
            pos[0]++;
            result /= 100.0;
        }
        return result;
    }

    private double parseAtom(char[] chars, int[] pos) {
        if (pos[0] < chars.length && chars[pos[0]] == '(') {
            pos[0]++; // skip '('
            double result = parseExpr(chars, pos);
            if (pos[0] < chars.length && chars[pos[0]] == ')') {
                pos[0]++; // skip ')'
            }
            return result;
        }

        int start = pos[0];
        while (pos[0] < chars.length && (Character.isDigit(chars[pos[0]]) || chars[pos[0]] == '.')) {
            pos[0]++;
        }
        if (start == pos[0]) {
            throw new RuntimeException("Expected number at position " + pos[0]);
        }
        return Double.parseDouble(new String(chars, start, pos[0] - start));
    }

    // ========================
    // UNLOCK
    // ========================

    private void unlockTelegram() {
        Intent intent = new Intent(this, LaunchActivity.class);
        startActivity(intent);
        overridePendingTransition(0, 0);
        // Don't finish() — CalculatorActivity stays alive so it intercepts
        // when the user returns to the app via the launcher icon.
        // Reset calculator state so it's ready for re-entry.
        currentExpression.setLength(0);
        lastPressWasEquals = false;
        if (resultView != null) {
            resultView.setText("0");
        }
        if (expressionView != null) {
            expressionView.setText("");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Every time the activity resumes (including returning from Telegram),
        // show a clean calculator. The user must enter the code again.
        if (getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getBoolean(KEY_SETUP_COMPLETE, false)) {
            currentExpression.setLength(0);
            lastPressWasEquals = false;
            if (resultView != null) {
                resultView.setText("0");
                expressionView.setText("");
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        // When launched again via launcher icon while already alive,
        // Android calls onNewIntent. Reset calculator to force re-entry.
        currentExpression.setLength(0);
        lastPressWasEquals = false;
        if (resultView != null) {
            resultView.setText("0");
            expressionView.setText("");
        }
    }
}
```

- [ ] **Step 2: Verify the file compiles conceptually**

Review the file for consistency: all referenced layout IDs (`calc_expression`, `calc_result`, `calc_grid`, `setup_title`, `setup_subtitle`, `setup_code_input`, `setup_confirm_button`) match the layout XMLs from Task 2. All drawable references (`calc_button_equals`, `calc_button_operator`, `calc_button_number`) match Task 1. All color references match Task 1.

- [ ] **Step 3: Commit**

```bash
git add TMessagesProj/src/main/java/org/telegram/ui/CalculatorActivity.java
git commit -m "feat: add CalculatorActivity with setup screen, calculator UI, and expression evaluator"
```

---

### Task 4: Create Calculator App Icon

**Files:**
- Create: `TMessagesProj/src/main/res/drawable/ic_calc_foreground.xml`
- Create: `TMessagesProj/src/main/res/drawable/ic_calc_background.xml`
- Create: `TMessagesProj/src/main/res/mipmap-anydpi-v26/ic_calc_launcher.xml`
- Create: `TMessagesProj/src/main/res/mipmap-anydpi-v26/ic_calc_launcher_round.xml`

- [ ] **Step 1: Create the icon foreground vector drawable**

Write `TMessagesProj/src/main/res/drawable/ic_calc_foreground.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">

    <!-- Calculator body (rounded rectangle) -->
    <path
        android:fillColor="#FFFFFF"
        android:pathData="M30,28h48c2.2,0 4,1.8 4,4v44c0,2.2 -1.8,4 -4,4H30c-2.2,0 -4,-1.8 -4,-4V32C26,29.8 27.8,28 30,28z" />

    <!-- Display area -->
    <path
        android:fillColor="#E8DEF8"
        android:pathData="M32,32h44c1.1,0 2,0.9 2,2v12c0,1.1 -0.9,2 -2,2H32c-1.1,0 -2,-0.9 -2,-2V34C30,32.9 30.9,32 32,32z" />

    <!-- Button dots row 1 -->
    <circle android:fillColor="#6750A4" android:cx="38" android:cy="56" android:r="3" />
    <circle android:fillColor="#6750A4" android:cx="50" android:cy="56" android:r="3" />
    <circle android:fillColor="#6750A4" android:cx="62" android:cy="56" android:r="3" />
    <circle android:fillColor="#6750A4" android:cx="74" android:cy="56" android:r="3" />

    <!-- Button dots row 2 -->
    <circle android:fillColor="#6750A4" android:cx="38" android:cy="68" android:r="3" />
    <circle android:fillColor="#6750A4" android:cx="50" android:cy="68" android:r="3" />
    <circle android:fillColor="#6750A4" android:cx="62" android:cy="68" android:r="3" />
    <circle android:fillColor="#6750A4" android:cx="74" android:cy="68" android:r="3" />
</vector>
```

- [ ] **Step 2: Create the icon background**

Write `TMessagesProj/src/main/res/drawable/ic_calc_background.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android">
    <solid android:color="#E8DEF8" />
</shape>
```

- [ ] **Step 3: Create adaptive icon definitions**

Write `TMessagesProj/src/main/res/mipmap-anydpi-v26/ic_calc_launcher.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@drawable/ic_calc_background" />
    <foreground android:drawable="@drawable/ic_calc_foreground" />
</adaptive-icon>
```

Write `TMessagesProj/src/main/res/mipmap-anydpi-v26/ic_calc_launcher_round.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@drawable/ic_calc_background" />
    <foreground android:drawable="@drawable/ic_calc_foreground" />
</adaptive-icon>
```

- [ ] **Step 4: Commit**

```bash
git add TMessagesProj/src/main/res/drawable/ic_calc_foreground.xml \
       TMessagesProj/src/main/res/drawable/ic_calc_background.xml \
       TMessagesProj/src/main/res/mipmap-anydpi-v26/ic_calc_launcher.xml \
       TMessagesProj/src/main/res/mipmap-anydpi-v26/ic_calc_launcher_round.xml
git commit -m "feat: add Material You calculator adaptive icon"
```

---

### Task 5: Modify AndroidManifest — Wire Up CalculatorActivity as Launcher

**Files:**
- Modify: `TMessagesProj/src/main/AndroidManifest.xml`

- [ ] **Step 1: Remove all icon activity-aliases (lines 112–221)**

Delete the following activity-aliases from the manifest:
- `org.telegram.messenger.DefaultIcon` (lines 112–125)
- `org.telegram.messenger.OriginalIcon` (lines 127–141)
- `org.telegram.messenger.VintageIcon` (lines 143–157)
- `org.telegram.messenger.AquaIcon` (lines 159–173)
- `org.telegram.messenger.PremiumIcon` (lines 175–189)
- `org.telegram.messenger.TurboIcon` (lines 191–205)
- `org.telegram.messenger.NoxIcon` (lines 207–221)

- [ ] **Step 2: Add CalculatorActivity declaration before LaunchActivity**

Insert the following where the aliases were (before the LaunchActivity `<activity>` tag):

```xml
        <activity
            android:name="org.telegram.ui.CalculatorActivity"
            android:label="Calculator"
            android:icon="@mipmap/ic_calc_launcher"
            android:roundIcon="@mipmap/ic_calc_launcher_round"
            android:theme="@android:style/Theme.NoTitleBar"
            android:launchMode="singleTop"
            android:clearTaskOnLaunch="true"
            android:exported="true">

            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
                <category android:name="android.intent.category.MULTIWINDOW_LAUNCHER" />
            </intent-filter>
        </activity>
```

- [ ] **Step 3: Remove MAIN action from LaunchActivity**

In the LaunchActivity `<activity>` block (currently line 232–234), remove this intent-filter:

```xml
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
            </intent-filter>
```

LaunchActivity keeps all other intent filters (SEND, VIEW, etc.) so deep links and sharing still work.

- [ ] **Step 4: Commit**

```bash
git add TMessagesProj/src/main/AndroidManifest.xml
git commit -m "feat: set CalculatorActivity as launcher, remove icon aliases"
```

---

### Task 6: Change App Name

**Files:**
- Modify: `TMessagesProj/src/main/res/values/strings.xml` (line 3)

- [ ] **Step 1: Change AppName strings**

In `TMessagesProj/src/main/res/values/strings.xml`, change:

```xml
<string name="AppName">Fork Client</string>
<string name="AppNameBeta">Fork Client</string>
<string name="AppNameFdroid">Forkgram</string>
```

to:

```xml
<string name="AppName">Calculator</string>
<string name="AppNameBeta">Calculator</string>
<string name="AppNameFdroid">Calculator</string>
```

- [ ] **Step 2: Commit**

```bash
git add TMessagesProj/src/main/res/values/strings.xml
git commit -m "feat: change app name to Calculator"
```

---

### Task 7: Add "Change Calculator Code" to Debug Menu

**Files:**
- Modify: `TMessagesProj/src/main/java/org/telegram/ui/SettingsActivity.java`

- [ ] **Step 1: Add the menu item to the items array**

In `SettingsActivity.java`, inside `openDebugMenu()` at line 1435 (after the last item `SharedConfig.shadowsInSections ...`), add a new item before the closing `};`:

Find this line (line 1435):
```java
                (SharedConfig.frameMetricsEnabled ? "hide frame metrics" : "show frame metrics"),
                BuildVars.DEBUG_PRIVATE_VERSION ? (SharedConfig.shadowsInSections ? "disable shadows in settings" : "enable shadows in settings") : null
        };
```

Replace with:
```java
                (SharedConfig.frameMetricsEnabled ? "hide frame metrics" : "show frame metrics"),
                BuildVars.DEBUG_PRIVATE_VERSION ? (SharedConfig.shadowsInSections ? "disable shadows in settings" : "enable shadows in settings") : null,
                "Change calculator code"
        };
```

- [ ] **Step 2: Add the handler for the new menu item**

In the `builder.setItems` callback, after the `which == 40` handler (line 1740), add:

Find:
```java
            } else if (which == 40) {
                final SharedPreferences prefs = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE);
                prefs.edit().putBoolean("shadowsInSections", SharedConfig.shadowsInSections = !SharedConfig.shadowsInSections).apply();
            }
```

Replace with:
```java
            } else if (which == 40) {
                final SharedPreferences prefs = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE);
                prefs.edit().putBoolean("shadowsInSections", SharedConfig.shadowsInSections = !SharedConfig.shadowsInSections).apply();
            } else if (which == 41) {
                showChangeCalculatorCodeDialog();
            }
```

- [ ] **Step 3: Add the showChangeCalculatorCodeDialog method**

Add this method to `SettingsActivity.java` after the `openDebugMenu()` method (after line 1744):

```java
    private void showChangeCalculatorCodeDialog() {
        Context context = getParentActivity();
        if (context == null) return;

        SharedPreferences calcPrefs = context.getSharedPreferences("calculator_prefs", Context.MODE_PRIVATE);

        // Step 1: Verify current code
        AlertDialog.Builder verifyBuilder = new AlertDialog.Builder(context, resourceProvider);
        verifyBuilder.setTitle("Enter current code");

        final EditText verifyInput = new EditText(context);
        verifyInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_SIGNED);
        verifyInput.setPadding(dp(24), dp(8), dp(24), dp(8));
        verifyBuilder.setView(verifyInput);

        verifyBuilder.setPositiveButton(getString(R.string.OK), (dialog, w) -> {
            String input = verifyInput.getText().toString().trim();
            String storedHash = calcPrefs.getString("secret_hash", "");
            String storedSalt = calcPrefs.getString("secret_salt", "");

            String inputHash = hashCalculatorCode(input, storedSalt);
            if (!inputHash.equals(storedHash)) {
                Toast.makeText(context, "Wrong code", Toast.LENGTH_SHORT).show();
                return;
            }

            // Step 2: Enter new code
            AlertDialog.Builder newBuilder = new AlertDialog.Builder(context, resourceProvider);
            newBuilder.setTitle("Enter new code");

            final EditText newInput = new EditText(context);
            newInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_SIGNED);
            newInput.setPadding(dp(24), dp(8), dp(24), dp(8));
            newBuilder.setView(newInput);

            newBuilder.setPositiveButton(getString(R.string.OK), (dialog2, w2) -> {
                String newCode = newInput.getText().toString().trim();
                if (newCode.isEmpty()) {
                    Toast.makeText(context, "Code cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Step 3: Confirm new code
                AlertDialog.Builder confirmBuilder = new AlertDialog.Builder(context, resourceProvider);
                confirmBuilder.setTitle("Confirm new code");

                final EditText confirmInput = new EditText(context);
                confirmInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_SIGNED);
                confirmInput.setPadding(dp(24), dp(8), dp(24), dp(8));
                confirmBuilder.setView(confirmInput);

                confirmBuilder.setPositiveButton(getString(R.string.OK), (dialog3, w3) -> {
                    String confirmCode = confirmInput.getText().toString().trim();
                    if (!newCode.equals(confirmCode)) {
                        Toast.makeText(context, "Codes don't match", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Save new code
                    byte[] saltBytes = new byte[16];
                    new java.security.SecureRandom().nextBytes(saltBytes);
                    StringBuilder sb = new StringBuilder();
                    for (byte b : saltBytes) {
                        sb.append(String.format("%02x", b));
                    }
                    String newSalt = sb.toString();
                    String newHash = hashCalculatorCode(newCode, newSalt);

                    calcPrefs.edit()
                            .putString("secret_hash", newHash)
                            .putString("secret_salt", newSalt)
                            .apply();

                    Toast.makeText(context, "Calculator code updated", Toast.LENGTH_SHORT).show();
                });
                confirmBuilder.setNegativeButton(getString(R.string.Cancel), null);
                showDialog(confirmBuilder.create());
            });
            newBuilder.setNegativeButton(getString(R.string.Cancel), null);
            showDialog(newBuilder.create());
        });
        verifyBuilder.setNegativeButton(getString(R.string.Cancel), null);
        showDialog(verifyBuilder.create());
    }

    private String hashCalculatorCode(String code, String salt) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest((code + salt).getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
```

- [ ] **Step 4: Add required imports to SettingsActivity.java**

At the top of `SettingsActivity.java`, ensure these imports are present (add if missing):

```java
import android.widget.EditText;
 import android.widget.Toast;
```

- [ ] **Step 5: Commit**

```bash
git add TMessagesProj/src/main/java/org/telegram/ui/SettingsActivity.java
git commit -m "feat: add 'Change calculator code' option to debug menu"
```

---

### Task 8: Force-Disable Notification Previews on First Setup

**Files:**
- Modify: `TMessagesProj/src/main/java/org/telegram/ui/CalculatorActivity.java`

- [ ] **Step 1: Add notification preview disabling to saveSecretCode()**

In `CalculatorActivity.java`, modify the `saveSecretCode` method to also disable notification previews. Replace the existing `saveSecretCode` method:

```java
    private void saveSecretCode(String code) {
        String salt = generateSalt();
        String hash = hashCode(code, salt);
        getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_SECRET_HASH, hash)
                .putString(KEY_SECRET_SALT, salt)
                .putBoolean(KEY_SETUP_COMPLETE, true)
                .apply();

        // Disable notification previews to avoid leaking message content
        disableNotificationPreviews();
    }

    private void disableNotificationPreviews() {
        for (int i = 0; i < org.telegram.messenger.UserConfig.MAX_ACCOUNT_COUNT; i++) {
            SharedPreferences prefs = org.telegram.messenger.MessagesController.getNotificationsSettings(i);
            prefs.edit()
                    .putBoolean("EnablePreviewAll", false)
                    .putBoolean("EnablePreviewGroup", false)
                    .putBoolean("EnablePreviewChannel", false)
                    .apply();
        }
    }
```

- [ ] **Step 2: Commit**

```bash
git add TMessagesProj/src/main/java/org/telegram/ui/CalculatorActivity.java
git commit -m "feat: disable notification previews on calculator setup to prevent content leaking"
```

---

### Task 9: Build Verification

**Files:** None (verification only)

- [ ] **Step 1: Run a Gradle build to verify compilation**

```bash
cd /home/nova/personal/TelegramAndroid
./gradlew assembleAfatRelease 2>&1 | tail -30
```

If there are build variants, try:
```bash
./gradlew TMessagesProj:compileReleaseJavaWithJavac 2>&1 | tail -30
```

Expected: BUILD SUCCESSFUL. If there are errors, fix them before proceeding.

- [ ] **Step 2: If errors, fix and re-commit**

Common issues to check:
- Missing imports in `CalculatorActivity.java`
- Resource ID mismatches between layouts and Java code
- Manifest XML syntax errors
- Missing `dp()` utility method in SettingsActivity context (use `AndroidUtilities.dp()` if needed)

- [ ] **Step 3: Final commit if fixes were needed**

```bash
git add -A
git commit -m "fix: resolve build errors from calculator decoy feature"
```
