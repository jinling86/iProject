package ca.uottawa.ljin027.iproject;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by ljin027 on 16/04/2015.
 */
public class FragmentPicker extends DialogFragment
        implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {
    public static final boolean PICK_TIME = true;
    public static final boolean PICK_DATE = false;

    private EditText mEditText;
    private EditText mView_DueDate;
    private EditText mView_DueTime;
    private boolean mPickType;
    private String mOrgTimeString;
    private String mOrgDateString;
    private Date mUpperBound;
    private Date mLowerBound;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if(mPickType == PICK_TIME) {
            final Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);
            return new TimePickerDialog(getActivity(), this, hour, minute, true);
        }
        else {
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }
    }

    public void setListener(
            EditText editText,
            boolean pickType,
            String timeOrDateString,
            Date upperBound,
            Date lowerBound,
            EditText dueDate,
            EditText dueTime) {
        mEditText = editText;
        mPickType = pickType;
        if(pickType == PICK_TIME) {
            mOrgTimeString = mEditText.getText().toString();
            mOrgDateString = timeOrDateString;
            mUpperBound = upperBound;
            mLowerBound = lowerBound;
        }
        else {
            mOrgDateString = mEditText.getText().toString();
            mOrgTimeString = timeOrDateString;
            mUpperBound = upperBound;
            mLowerBound = lowerBound;
        }
        mView_DueDate = dueDate;
        mView_DueTime = dueTime;
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        String newDateString = Project.getDateString(year, month, day);
        Date newDate = Project.getDate(newDateString + mOrgTimeString);
        if(newDate.before(mLowerBound) || newDate.after(mUpperBound)) {
            mEditText.setText(mOrgDateString);
            Toast.makeText(getActivity(), "Date range error!", Toast.LENGTH_LONG).show();
        }
        else {
            mEditText.setText(newDateString);
            adjustDueDate(newDate);
        }

    }

    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        String newTimeString = Project.getTimeString(hourOfDay, minute);
        Date newDate = Project.getDate(mOrgDateString + newTimeString);
        if(newDate.before(mLowerBound) || newDate.after(mUpperBound)) {
            mEditText.setText(mOrgTimeString);
            Toast.makeText(getActivity(), "Time range error!", Toast.LENGTH_LONG).show();
        }
        else {
            mEditText.setText(newTimeString);
            adjustDueDate(newDate);
        }
    }

    private void adjustDueDate(Date startTime) {
        if(mView_DueDate != null && mView_DueTime != null) {
            Date dueDate = Project.getDate(
                    mView_DueDate.getText().toString() + mView_DueTime.getText().toString());
            if(dueDate.before(startTime)) {
                dueDate.setTime(startTime.getTime() + Project.TIME_OFFSET);
                if(dueDate.after(mUpperBound))
                    dueDate = mUpperBound;
                mView_DueDate.setText(Project.getDateString(dueDate));
                mView_DueTime.setText(Project.getTimeString(dueDate));
            }
        }
    }
}