package com.example.universalyogaadmin.activities;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.OnBackPressedCallback;

import com.example.universalyogaadmin.R;
import com.example.universalyogaadmin.adapters.InstanceListAdapter;
import com.example.universalyogaadmin.database.DatabaseHelper;
import com.example.universalyogaadmin.models.ClassInstance;
import com.example.universalyogaadmin.utils.ValidationUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ManageInstancesActivity extends AppCompatActivity {

    private ListView listViewInstances;
    private Button btnAddInstance;
    private DatabaseHelper databaseHelper;
    private InstanceListAdapter adapter;
    private List<ClassInstance> instances;
    private long classId;
    private String dayOfWeek;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_instances);

        // Set up back button in action bar
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("Manage Class Instances");
        }

        // Get class ID from intent
        classId = getIntent().getLongExtra("CLASS_ID", -1);
        dayOfWeek = getIntent().getStringExtra("DAY_OF_WEEK");

        if (classId == -1) {
            Toast.makeText(this, "Error: Class not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        listViewInstances = findViewById(R.id.listViewInstances);
        btnAddInstance = findViewById(R.id.btnAddInstance);
        databaseHelper = new DatabaseHelper(this);

        loadInstances();
        setupListeners();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });
    }

    private void loadInstances() {
        instances = databaseHelper.getInstancesByClassId(classId);
        adapter = new InstanceListAdapter(this, instances);
        listViewInstances.setAdapter(adapter);

        if (instances.isEmpty()) {
            Toast.makeText(this, "No instances found for this class", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupListeners() {
        btnAddInstance.setOnClickListener(view -> showAddInstanceDialog());

        listViewInstances.setOnItemClickListener((parent, view, position, id) -> {
            ClassInstance instance = instances.get(position);
            showInstanceOptionsDialog(instance);
        });
    }

    private void showAddInstanceDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_instance, null);
        final EditText etDate = dialogView.findViewById(R.id.etDate);
        final EditText etTeacher = dialogView.findViewById(R.id.etTeacher);
        final EditText etComments = dialogView.findViewById(R.id.etComments);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Add New Instance")
                .setView(dialogView)
                .setPositiveButton("Add", null)
                .setNegativeButton("Cancel", null)
                .setCancelable(false)
                .create();

        dialog.show();

        // Set the positive button click listener after the dialog is shown
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view -> {
            boolean isValid = true;

            etDate.setError(null);
            etTeacher.setError(null);

            // Check required fields
            if (etDate.getText().toString().trim().isEmpty()) {
                etDate.setError("Date is required");
                isValid = false;
            } else {
                // Validate date format and day of week match
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.UK);
                    sdf.setLenient(false);
                    Date date = sdf.parse(etDate.getText().toString().trim());

                    // Check if date matches day of week
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(date);

                    String[] daysOfWeek = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
                    int dayOfWeekValue = cal.get(Calendar.DAY_OF_WEEK) - 1; // Calendar.DAY_OF_WEEK starts from 1 (Sunday)

                    if (!daysOfWeek[dayOfWeekValue].equalsIgnoreCase(dayOfWeek)) {
                        etDate.setError("Date must be a " + dayOfWeek);
                        isValid = false;
                    }
                } catch (ParseException e) {
                    etDate.setError("Enter valid date (DD/MM/YYYY)");
                    isValid = false;
                }
            }

            if (etTeacher.getText().toString().trim().isEmpty()) {
                etTeacher.setError("Teacher name is required");
                isValid = false;
            }

            if (isValid) {
                String date = etDate.getText().toString().trim();
                String teacher = etTeacher.getText().toString().trim();
                String comments = etComments.getText().toString().trim();

                ClassInstance instance = new ClassInstance(classId, date, teacher, comments);
                long instanceId = databaseHelper.insertClassInstance(instance);

                if (instanceId > 0) {
                    Toast.makeText(ManageInstancesActivity.this,
                            "Instance added successfully", Toast.LENGTH_SHORT).show();
                    loadInstances();
                    dialog.dismiss();
                } else {
                    Toast.makeText(ManageInstancesActivity.this,
                            "Error adding instance", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void showInstanceOptionsDialog(ClassInstance instance) {
        String[] options = {"Edit", "Delete"};

        new AlertDialog.Builder(this)
                .setTitle("Instance Options")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        showEditInstanceDialog(instance);
                    } else if (which == 1) {
                        showDeleteConfirmationDialog(instance);
                    }
                })
                .show();
    }

    private void showEditInstanceDialog(ClassInstance instance) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_instance, null);
        EditText etDate = dialogView.findViewById(R.id.etDate);
        EditText etTeacher = dialogView.findViewById(R.id.etTeacher);
        EditText etComments = dialogView.findViewById(R.id.etComments);

        // Pre-fill with existing data
        etDate.setText(instance.getDate());
        etTeacher.setText(instance.getTeacher());
        etComments.setText(instance.getComments());

        new AlertDialog.Builder(this)
                .setTitle("Edit Instance")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    // Validate inputs
                    boolean isValid = true;
                    isValid &= ValidationUtils.validateRequired(etDate, "Date is required");
                    isValid &= ValidationUtils.validateDate(etDate, dayOfWeek, "Enter valid date (DD/MM/YYYY)");
                    isValid &= ValidationUtils.validateRequired(etTeacher, "Teacher name is required");

                    if (isValid) {
                        String date = etDate.getText().toString().trim();
                        String teacher = etTeacher.getText().toString().trim();
                        String comments = etComments.getText().toString().trim();

                        instance.setDate(date);
                        instance.setTeacher(teacher);
                        instance.setComments(comments);

                        int rowsAffected = databaseHelper.updateClassInstance(instance);

                        if (rowsAffected > 0) {
                            Toast.makeText(ManageInstancesActivity.this,
                                    "Instance updated successfully", Toast.LENGTH_SHORT).show();
                            loadInstances();
                        } else {
                            Toast.makeText(ManageInstancesActivity.this,
                                    "Error updating instance", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .setCancelable(false)
                .show();
    }

    private void showDeleteConfirmationDialog(ClassInstance instance) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Instance")
                .setMessage("Are you sure you want to delete this instance?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    databaseHelper.deleteClassInstance(instance.getId());
                    Toast.makeText(ManageInstancesActivity.this,
                            "Instance deleted successfully", Toast.LENGTH_SHORT).show();
                    loadInstances();
                })
                .setNegativeButton("Cancel", null)
                .setCancelable(false)
                .show();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getOnBackPressedDispatcher().onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
