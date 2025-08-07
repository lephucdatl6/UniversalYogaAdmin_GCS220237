package com.example.universalyogaadmin.utils;

import android.text.TextUtils;
import android.widget.EditText;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ValidationUtils {

    public static boolean validateRequired(EditText editText, String errorMessage) {
        String text = editText.getText().toString().trim();
        if (TextUtils.isEmpty(text)) {
            editText.setError(errorMessage);
            return false;
        }
        return true;
    }

    public static boolean validateNumber(EditText editText, String errorMessage) {
        String text = editText.getText().toString().trim();
        try {
            Integer.parseInt(text);
            return true;
        } catch (NumberFormatException e) {
            editText.setError(errorMessage);
            return false;
        }
    }

    public static boolean validatePrice(EditText editText, String errorMessage) {
        String text = editText.getText().toString().trim();
        try {
            double price = Double.parseDouble(text);
            if (price <= 0) {
                editText.setError("Price must be greater than 0");
                return false;
            }
            return true;
        } catch (NumberFormatException e) {
            editText.setError(errorMessage);
            return false;
        }
    }

    public static boolean validateTime(EditText editText, String errorMessage) {
        String text = editText.getText().toString().trim();
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.UK);
            sdf.setLenient(false);
            sdf.parse(text);
            return true;
        } catch (ParseException e) {
            editText.setError(errorMessage);
            return false;
        }
    }

    public static boolean validateDate(EditText editText, String dayOfWeek, String errorMessage) {
        String text = editText.getText().toString().trim();
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.UK);
            sdf.setLenient(false);
            Date date = sdf.parse(text);

            // Check if date matches day of week
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);

            String[] daysOfWeek = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
            int dayOfWeekValue = cal.get(Calendar.DAY_OF_WEEK) - 1; // Calendar.DAY_OF_WEEK starts from 1 (Sunday)

            if (!daysOfWeek[dayOfWeekValue].equalsIgnoreCase(dayOfWeek)) {
                editText.setError("Date must be a " + dayOfWeek);
                return false;
            }

            return true;
        } catch (ParseException e) {
            editText.setError(errorMessage);
            return false;
        }
    }
}