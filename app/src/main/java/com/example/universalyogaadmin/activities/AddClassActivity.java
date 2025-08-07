package com.example.universalyogaadmin.activities;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.example.universalyogaadmin.R;
import com.example.universalyogaadmin.database.DatabaseHelper;
import com.example.universalyogaadmin.models.YogaClass;
import com.example.universalyogaadmin.utils.ValidationUtils;

public class AddClassActivity extends AppCompatActivity {

    private Spinner spinnerDayOfWeek;
    private EditText etTime;
    private EditText etCapacity;
    private EditText etDuration;
    private EditText etPrice;
    private Spinner spinnerType;
    private EditText etDescription;
    private Button btnSave;
    private Button btnConfirm;

    private DatabaseHelper databaseHelper;
    private boolean isConfirmationMode = false;
    private YogaClass tempYogaClass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_class);

        // Set up back button in action bar
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("Add New Yoga Class");
        }

        databaseHelper = new DatabaseHelper(this);

        // Initialize views
        spinnerDayOfWeek = findViewById(R.id.spinnerDayOfWeek);
        etTime = findViewById(R.id.etTime);
        etCapacity = findViewById(R.id.etCapacity);
        etDuration = findViewById(R.id.etDuration);
        etPrice = findViewById(R.id.etPrice);
        spinnerType = findViewById(R.id.spinnerType);
        etDescription = findViewById(R.id.etDescription);
        btnSave = findViewById(R.id.btnSave);
        btnConfirm = findViewById(R.id.btnConfirm);

        // Set up spinners
        setupSpinners();

        // Set up button click listeners
        btnSave.setOnClickListener(view -> validateAndProceed());
        btnConfirm.setOnClickListener(view -> saveYogaClass());

        // Initial UI state
        updateUIState();
    }

    private void setupSpinners() {
        // Set up day of week spinner
        ArrayAdapter<CharSequence> dayAdapter = ArrayAdapter.createFromResource(this,
                R.array.days_of_week, android.R.layout.simple_spinner_item);
        dayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDayOfWeek.setAdapter(dayAdapter);

        // Set up class type spinner
        ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(this,
                R.array.class_types, android.R.layout.simple_spinner_item);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(typeAdapter);
    }

    private void validateAndProceed() {
        // Validate all required fields
        boolean isValid = true;

        isValid &= ValidationUtils.validateRequired(etTime, "Time is required");
        isValid &= ValidationUtils.validateTime(etTime, "Enter valid time (HH:MM)");
        isValid &= ValidationUtils.validateRequired(etCapacity, "Capacity is required");
        isValid &= ValidationUtils.validateNumber(etCapacity, "Enter valid number");
        isValid &= ValidationUtils.validateRequired(etDuration, "Duration is required");
        isValid &= ValidationUtils.validateNumber(etDuration, "Enter valid number");
        isValid &= ValidationUtils.validateRequired(etPrice, "Price is required");
        isValid &= ValidationUtils.validatePrice(etPrice, "Enter valid price");

        if (isValid) {
            // Create temporary yoga class object
            String dayOfWeek = spinnerDayOfWeek.getSelectedItem().toString();
            String time = etTime.getText().toString().trim();
            int capacity = Integer.parseInt(etCapacity.getText().toString().trim());
            int duration = Integer.parseInt(etDuration.getText().toString().trim());
            double price = Double.parseDouble(etPrice.getText().toString().trim());
            String type = spinnerType.getSelectedItem().toString();
            String description = etDescription.getText().toString().trim();

            tempYogaClass = new YogaClass(dayOfWeek, time, capacity, duration, price, type, description);

            // Switch to confirmation mode
            isConfirmationMode = true;
            updateUIState();
            Toast.makeText(this, "Please confirm the details", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveYogaClass() {
        long id = databaseHelper.insertYogaClass(tempYogaClass);
        if (id > 0) {
            Toast.makeText(this, "Yoga class saved successfully", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Error saving yoga class", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateUIState() {
        if (isConfirmationMode) {
            // Disable editing
            spinnerDayOfWeek.setEnabled(false);
            etTime.setEnabled(false);
            etCapacity.setEnabled(false);
            etDuration.setEnabled(false);
            etPrice.setEnabled(false);
            spinnerType.setEnabled(false);
            etDescription.setEnabled(false);

            // Show confirm button, hide save button
            btnSave.setVisibility(android.view.View.GONE);
            btnConfirm.setVisibility(android.view.View.VISIBLE);
        } else {
            // Enable editing
            spinnerDayOfWeek.setEnabled(true);
            etTime.setEnabled(true);
            etCapacity.setEnabled(true);
            etDuration.setEnabled(true);
            etPrice.setEnabled(true);
            spinnerType.setEnabled(true);
            etDescription.setEnabled(true);

            // Show save button, hide confirm button
            btnSave.setVisibility(android.view.View.VISIBLE);
            btnConfirm.setVisibility(android.view.View.GONE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (isConfirmationMode) {
            isConfirmationMode = false;
            updateUIState();
        } else {
            super.onBackPressed();
        }
    }
}