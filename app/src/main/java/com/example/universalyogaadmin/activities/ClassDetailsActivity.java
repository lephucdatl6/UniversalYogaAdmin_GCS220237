package com.example.universalyogaadmin.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.universalyogaadmin.R;
import com.example.universalyogaadmin.database.DatabaseHelper;
import com.example.universalyogaadmin.models.YogaClass;

public class ClassDetailsActivity extends AppCompatActivity {

    private TextView tvDayOfWeek;
    private TextView tvTime;
    private TextView tvCapacity;
    private TextView tvDuration;
    private TextView tvPrice;
    private TextView tvType;
    private TextView tvDescription;
    private Button btnManageInstances;

    private DatabaseHelper databaseHelper;
    private YogaClass yogaClass;
    private long classId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class_details);

        // Set up back button in action bar
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("Class Details");
        }

        // Initialize views
        tvDayOfWeek = findViewById(R.id.tvDayOfWeek);
        tvTime = findViewById(R.id.tvTime);
        tvCapacity = findViewById(R.id.tvCapacity);
        tvDuration = findViewById(R.id.tvDuration);
        tvPrice = findViewById(R.id.tvPrice);
        tvType = findViewById(R.id.tvType);
        tvDescription = findViewById(R.id.tvDescription);
        btnManageInstances = findViewById(R.id.btnManageInstances);

        databaseHelper = new DatabaseHelper(this);

        // Get class ID from intent
        classId = getIntent().getLongExtra("CLASS_ID", -1);
        if (classId == -1) {
            Toast.makeText(this, "Error: Class not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadClassDetails();

        btnManageInstances.setOnClickListener(view -> {
            Intent intent = new Intent(ClassDetailsActivity.this, ManageInstancesActivity.class);
            intent.putExtra("CLASS_ID", classId);
            intent.putExtra("DAY_OF_WEEK", yogaClass.getDayOfWeek());
            startActivity(intent);
        });
    }

    private void loadClassDetails() {
        yogaClass = databaseHelper.getYogaClass(classId);
        if (yogaClass == null) {
            Toast.makeText(this, "Error: Class not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Display class details
        tvDayOfWeek.setText(yogaClass.getDayOfWeek());
        tvTime.setText(yogaClass.getTime());
        tvCapacity.setText(String.valueOf(yogaClass.getCapacity()));
        tvDuration.setText(String.valueOf(yogaClass.getDuration()) + " minutes");
        tvPrice.setText("£" + String.format("%.2f", yogaClass.getPrice()));
        tvType.setText(yogaClass.getType());
        tvDescription.setText(yogaClass.getDescription());

        if (yogaClass.getDescription() == null || yogaClass.getDescription().isEmpty()) {
            tvDescription.setText("No description available");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_class_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (id == R.id.action_edit) {
            Intent intent = new Intent(ClassDetailsActivity.this, EditClassActivity.class);
            intent.putExtra("CLASS_ID", classId);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_delete) {
            showDeleteConfirmationDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Class")
                .setMessage("Are you sure you want to delete this class? This will also delete all instances of this class.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    databaseHelper.deleteYogaClass(classId);
                    Toast.makeText(ClassDetailsActivity.this, "Class deleted successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadClassDetails();
    }
}