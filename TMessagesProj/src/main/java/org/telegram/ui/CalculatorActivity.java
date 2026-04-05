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

        int i = expr.length() - 1;
        while (i >= 0 && (Character.isDigit(expr.charAt(i)) || expr.charAt(i) == '.')) {
            i--;
        }

        if (i >= 0 && expr.charAt(i) == '-' && (i == 0 || "+-*/".indexOf(expr.charAt(i - 1)) >= 0)) {
            currentExpression.deleteCharAt(i);
        } else {
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
            pos[0]++;
            double result = parseExpr(chars, pos);
            if (pos[0] < chars.length && chars[pos[0]] == ')') {
                pos[0]++;
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
