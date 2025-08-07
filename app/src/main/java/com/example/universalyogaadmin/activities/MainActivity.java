package com.example.universalyogaadmin.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.universalyogaadmin.R;
import com.example.universalyogaadmin.adapters.LogEntryAdapter;
import com.example.universalyogaadmin.database.DatabaseHelper;
import com.example.universalyogaadmin.utils.LogManager;

public class MainActivity extends AppCompatActivity {

    private Button btnAddClass;
    private Button btnViewClasses;
    private Button btnSearch;
    private Button btnUpload;
    private DatabaseHelper databaseHelper;
    private ListView logListView;
    private TextView tvClearAll;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LogManager.init(this);

        databaseHelper = new DatabaseHelper(this);

        btnAddClass = findViewById(R.id.btnAddClass);
        btnViewClasses = findViewById(R.id.btnViewClasses);
        btnSearch = findViewById(R.id.btnSearch);
        btnUpload = findViewById(R.id.btnUpload);
        logListView = findViewById(R.id.logListView);
        tvClearAll = findViewById(R.id.tvClearAll);

        LogManager.logAdapter = new LogEntryAdapter(this, LogManager.logList);
        logListView.setAdapter(LogManager.logAdapter);

        setupClickListeners();

        // Clear All Log
        tvClearAll.setOnClickListener(view -> {
            LogManager.clearAllLogs();
        });
    }


    // Set up navigation and actions for each main button
    private void setupClickListeners() {
        btnAddClass.setOnClickListener(view -> {
            // Open AddClassActivity
            Intent intent = new Intent(MainActivity.this, AddClassActivity.class);
            startActivity(intent);
        });

        btnViewClasses.setOnClickListener(view -> {
            // Open ViewClassesActivity
            Intent intent = new Intent(MainActivity.this, ViewClassesActivity.class);
            startActivity(intent);
        });

        btnSearch.setOnClickListener(view -> {
            // Open SearchActivity
            Intent intent = new Intent(MainActivity.this, SearchActivity.class);
            startActivity(intent);
        });

        btnUpload.setOnClickListener(view -> {
            // Open UploadActivity
            Intent intent = new Intent(MainActivity.this, UploadActivity.class);
            startActivity(intent);
        });
    }

    // Inflate the menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    // Handle menu item selection
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_reset_database) {
            showResetConfirmationDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Show a confirmation dialog before resetting the database
    private void showResetConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Reset Database")
                .setMessage("Are you sure you want to reset the database? This will delete all data.")
                .setPositiveButton("Reset", (dialog, which) -> {
                    databaseHelper.resetDatabase();
                    Toast.makeText(MainActivity.this, "Database reset successfully", Toast.LENGTH_SHORT).show();
                    LogManager.addLogEntry("Database was reset");
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}