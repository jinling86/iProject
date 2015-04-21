package ca.uottawa.ljin027.iproject;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * This class is implemented for CSI5175 Bonus Assignment.
 * This class implements a Dialog fragment for prompting the report to users. The fond, style, size
 * of the text are customized for a better visual effect.
 *
 * @author Ling Jin
 * @version 1.0
 * @since 19/04/2015
 */
public class DialogReport extends DialogFragment {

    private String message;
    private int color;

    public void setContent(String message, int color) {
        this.message = message;
        this.color = color;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        final TextView textView = new TextView(getActivity());
        textView.setText(message);
        textView.setGravity(Gravity.LEFT);
        textView.setTextSize(10);
        Typeface typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD);
        textView.setTypeface(typeface);
        textView.setBackgroundColor(getResources().getColor(R.color.dialog_background));
        textView.setTextColor(getResources().getColor(color));
        textView.setPadding(50, 100, 50, 100);

        builder.setView(textView);

        return builder.create();
    }
}