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

public class EditClassActivity extends AppCompatActivity {

    private Spinner spinnerDayOfWeek;
    private EditText etTime;
    private EditText etCapacity;
    private EditText etDuration;
    private EditText etPrice;
    private Spinner spinnerType;
    private EditText etDescription;
    private Button btnSave;

    private DatabaseHelper databaseHelper;
    private YogaClass yogaClass;
    private long classId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_class);

        // Set up back button in action bar
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("Edit Yoga Class");
        }

        databaseHelper = new DatabaseHelper(this);

        // Get class ID from intent
        classId = getIntent().getLongExtra("CLASS_ID", -1);
        if (classId == -1) {
            Toast.makeText(this, "Error: Class not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize views
        spinnerDayOfWeek = findViewById(R.id.spinnerDayOfWeek);
        etTime = findViewById(R.id.etTime);
        etCapacity = findViewById(R.id.etCapacity);
        etDuration = findViewById(R.id.etDuration);
        etPrice = findViewById(R.id.etPrice);
        spinnerType = findViewById(R.id.spinnerType);
        etDescription = findViewById(R.id.etDescription);
        btnSave = findViewById(R.id.btnSave);

        // Set up spinners
        setupSpinners();

        // Load class data
        loadClassData();

        // Set up button click listener
        btnSave.setOnClickListener(view -> validateAndSave());
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

    private void loadClassData() {
        yogaClass = databaseHelper.getYogaClass(classId);
        if (yogaClass == null) {
            Toast.makeText(this, "Error: Class not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Set values to UI components
        // Find day of week position in spinner
        ArrayAdapter<CharSequence> dayAdapter = (ArrayAdapter<CharSequence>) spinnerDayOfWeek.getAdapter();
        int dayPosition = 0;
        for (int i = 0; i < dayAdapter.getCount(); i++) {
            if (dayAdapter.getItem(i).toString().equals(yogaClass.getDayOfWeek())) {
                dayPosition = i;
                break;
            }
        }
        spinnerDayOfWeek.setSelection(dayPosition);

        // Find class type position in spinner
        ArrayAdapter<CharSequence> typeAdapter = (ArrayAdapter<CharSequence>) spinnerType.getAdapter();
        int typePosition = 0;
        for (int i = 0; i < typeAdapter.getCount(); i++) {
            if (typeAdapter.getItem(i).toString().equals(yogaClass.getType())) {
                typePosition = i;
                break;
            }
        }
        spinnerType.setSelection(typePosition);

        // Set other fields
        etTime.setText(yogaClass.getTime());
        etCapacity.setText(String.valueOf(yogaClass.getCapacity()));
        etDuration.setText(String.valueOf(yogaClass.getDuration()));
        etPrice.setText(String.valueOf(yogaClass.getPrice()));
        etDescription.setText(yogaClass.getDescription());
    }

    private void validateAndSave() {
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
            // Update yoga class object
            yogaClass.setDayOfWeek(spinnerDayOfWeek.getSelectedItem().toString());
            yogaClass.setTime(etTime.getText().toString().trim());
            yogaClass.setCapacity(Integer.parseInt(etCapacity.getText().toString().trim()));
            yogaClass.setDuration(Integer.parseInt(etDuration.getText().toString().trim()));
            yogaClass.setPrice(Double.parseDouble(etPrice.getText().toString().trim()));
            yogaClass.setType(spinnerType.getSelectedItem().toString());
            yogaClass.setDescription(etDescription.getText().toString().trim());

            // Save to database
            int rowsAffected = databaseHelper.updateYogaClass(yogaClass);
            if (rowsAffected > 0) {
                Toast.makeText(this, "Yoga class updated successfully", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Error updating yoga class", Toast.LENGTH_SHORT).show();
            }
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
}