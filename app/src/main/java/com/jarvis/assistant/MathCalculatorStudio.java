package com.jarvis.assistant;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/** Owner-invoked arithmetic helper. Expressions are evaluated locally without network access. */
public final class MathCalculatorStudio {
    private MathCalculatorStudio() { }

    public static void show(Activity activity) {
        EditText expression = new EditText(activity);
        expression.setSingleLine(true);
        expression.setHint("Example: (125 + 75) * 2 / 4");
        expression.setInputType(android.text.InputType.TYPE_CLASS_NUMBER
                | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
                | android.text.InputType.TYPE_NUMBER_FLAG_SIGNED
                | android.text.InputType.TYPE_CLASS_TEXT);
        TextView note = new TextView(activity);
        note.setText("Supports +  −  ×  ÷  ^ and parentheses. Calculations stay on this phone; no account or internet is used.");
        note.setPadding(8, 8, 8, 12);
        note.setGravity(Gravity.START);
        LinearLayout layout = new LinearLayout(activity);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(24, 8, 24, 8);
        layout.addView(note);
        layout.addView(expression);
        AlertDialog dialog = new AlertDialog.Builder(activity)
                .setTitle("JARVIS • Local Math Calculator")
                .setView(layout)
                .setNegativeButton("Close", null)
                .setPositiveButton("Calculate", null)
                .create();
        dialog.setOnShowListener(ignored -> {
            Button calculate = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            calculate.setOnClickListener(view -> {
                try {
                    double answer = MathExpressionRules.evaluate(expression.getText().toString());
                    String formatted = MathExpressionRules.format(answer);
                    new AlertDialog.Builder(activity).setTitle("Calculation result")
                            .setMessage(expression.getText().toString().trim() + "\n\n= " + formatted
                                    + "\n\nCalculated locally by JARVIS; no external service was used.")
                            .setPositiveButton("Done", null).show();
                } catch (IllegalArgumentException e) {
                    expression.setError(e.getMessage());
                    Toast.makeText(activity, e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        });
        dialog.show();
    }
}
