package com.example.universalyogaadmin.activities;

import android.os.Bundle;
import android.os.Looper;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.example.universalyogaadmin.R;
import com.example.universalyogaadmin.database.DatabaseHelper;
import com.example.universalyogaadmin.models.ClassInstance;
import com.example.universalyogaadmin.models.YogaClass;
import com.example.universalyogaadmin.utils.LogManager;
import com.example.universalyogaadmin.utils.NetworkUtils;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UploadActivity extends AppCompatActivity {
    private static final String TAG = "UploadActivity";

    private Button btnUpload;
    private ProgressBar progressBar;
    private TextView tvStatus;
    private DatabaseHelper databaseHelper;
    private ExecutorService executorService;
    private boolean isUploading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        FirebaseApp.initializeApp(this);

        // Set up back button in action bar
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("Upload to Firebase");
        }

        // Initialize views
        btnUpload = findViewById(R.id.btnUpload);
        progressBar = findViewById(R.id.progressBar);
        tvStatus = findViewById(R.id.tvStatus);

        databaseHelper = new DatabaseHelper(this);
        executorService = Executors.newSingleThreadExecutor();

        btnUpload.setOnClickListener(view -> {
            if (isUploading) {
                Toast.makeText(this, "Upload already in progress", Toast.LENGTH_SHORT).show();
                return;
            }

            if (NetworkUtils.isNetworkAvailable(this)) {
                uploadToFirebase();
            } else {
                Toast.makeText(this, "No network connection available", Toast.LENGTH_SHORT).show();
                tvStatus.setText("Error: No network connection available");
            }
        });
    }

    private void uploadToFirebase() {
        isUploading = true;
        // Update UI on main thread
        runOnUiThread(() -> {
            progressBar.setVisibility(View.VISIBLE);
            btnUpload.setEnabled(false);
            tvStatus.setText("Preparing to upload data to Firebase...");
        });

        executorService.execute(() -> {
            updateStatus("Retrieving data from local database...");

            // Get unsynced data
            List<YogaClass> yogaClasses = databaseHelper.getUnsyncedYogaClasses();
            updateStatus("Found " + yogaClasses.size() + " classes to upload");

            List<ClassInstance> allInstances = databaseHelper.getUnsyncedClassInstances();
            updateStatus("Found " + allInstances.size() + " class instances to upload");

            // Get deleted records
            List<Long> deletedClassIds = databaseHelper.getDeletedYogaClassIds();
            updateStatus("Found " + deletedClassIds.size() + " deleted classes to process");

            List<Long> deletedInstanceIds = databaseHelper.getDeletedClassInstanceIds();
            updateStatus("Found " + deletedInstanceIds.size() + " deleted class instances to process");

            if (yogaClasses.isEmpty() && allInstances.isEmpty() &&
                    deletedClassIds.isEmpty() && deletedInstanceIds.isEmpty()) {
                updateStatus("No data to upload");
                runOnUiThread(() -> finishUpload(true, "No data needed to be uploaded"));
                return;
            }

            // Prepare data for Firebase
            updateStatus("Preparing data for Firebase upload...");
            Map<String, Object> updates = new HashMap<>();

            // Add yoga classes
            for (YogaClass yogaClass : yogaClasses) {
                String key = String.valueOf(yogaClass.getId());

                Map<String, Object> yogaClassValues = new HashMap<>();
                yogaClassValues.put("id", yogaClass.getId());
                yogaClassValues.put("dayOfWeek", yogaClass.getDayOfWeek());
                yogaClassValues.put("time", yogaClass.getTime());
                yogaClassValues.put("capacity", yogaClass.getCapacity());
                yogaClassValues.put("duration", yogaClass.getDuration());
                yogaClassValues.put("price", yogaClass.getPrice());
                yogaClassValues.put("type", yogaClass.getType());
                yogaClassValues.put("description", yogaClass.getDescription());

                updates.put("/yogaClasses/" + key, yogaClassValues);
            }

            // Add class instances
            for (ClassInstance instance : allInstances) {
                String key = String.valueOf(instance.getId());

                Map<String, Object> instanceValues = new HashMap<>();
                instanceValues.put("id", instance.getId());
                instanceValues.put("classId", instance.getClassId());
                instanceValues.put("date", instance.getDate());
                instanceValues.put("teacher", instance.getTeacher());
                instanceValues.put("comments", instance.getComments());

                updates.put("/classInstances/" + key, instanceValues);
            }

            // Handle deletions
            for (Long deletedClassId : deletedClassIds) {
                updates.put("/yogaClasses/" + deletedClassId, null); // Set to null to delete in Firebase
            }

            for (Long deletedInstanceId : deletedInstanceIds) {
                updates.put("/classInstances/" + deletedInstanceId, null); // Set to null to delete in Firebase
            }

            // Perform the upload
            updateStatus("Uploading data to Firebase...");
            long startTime = System.currentTimeMillis();
            Log.d(TAG, "Starting Firebase upload at: " + startTime);

            DatabaseReference database = FirebaseDatabase.getInstance().getReference();

            Handler timeoutHandler = new Handler(Looper.getMainLooper());
            Runnable timeoutRunnable = () -> {
                Log.e(TAG, "Upload timeout");
                runOnUiThread(() -> finishUpload(false, "Upload timed out. Please check your connection and try again."));
            };
            timeoutHandler.postDelayed(timeoutRunnable, 15000);

            database.updateChildren(updates)
                    .addOnCompleteListener(task -> {
                        timeoutHandler.removeCallbacks(timeoutRunnable);

                        long endTime = System.currentTimeMillis();
                        Log.d(TAG, "Firebase upload completed at: " + endTime +
                                " (took " + (endTime - startTime) + "ms)");

                        final boolean success = task.isSuccessful();
                        final String resultMessage;

                        if (success) {
                            resultMessage = "Data successfully uploaded to Firebase!";
                            Log.d(TAG, "Firebase upload successful");
                            LogManager.addLogEntry("Synced with cloud");

                            executorService.execute(() -> {
                                Log.d(TAG, "Marking records as synced in SQLite");

                                // Mark updated classes as synced
                                for (YogaClass yogaClass : yogaClasses) {
                                    databaseHelper.markYogaClassAsSynced(yogaClass.getId());
                                }

                                // Mark updated instances as synced
                                for (ClassInstance instance : allInstances) {
                                    databaseHelper.markClassInstanceAsSynced(instance.getId());
                                }

                                // Clean up deleted records from tombstone tables
                                for (Long deletedClassId : deletedClassIds) {
                                    databaseHelper.clearDeletedYogaClassRecord(deletedClassId);
                                }

                                for (Long deletedInstanceId : deletedInstanceIds) {
                                    databaseHelper.clearDeletedClassInstanceRecord(deletedInstanceId);
                                }

                                // Then update UI on main thread
                                runOnUiThread(() -> finishUpload(true, resultMessage));
                            });
                        } else {
                            resultMessage = (task.getException() != null ? task.getException().getMessage() : "Unknown error");
                            Log.e(TAG, "Error in Firebase upload", task.getException());
                            LogManager.addLogEntry("Uploaded failed");


                            // Update UI on main thread
                            runOnUiThread(() -> finishUpload(false, resultMessage));
                        }
                    });
        });
    }

    private void updateStatus(final String message) {
        Log.d(TAG, "Status update: " + message);
        if (Looper.myLooper() == Looper.getMainLooper()) {
            tvStatus.setText(message);
        } else {
            runOnUiThread(() -> tvStatus.setText(message));
        }
    }

    private void finishUpload(final boolean success, final String message) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            Log.e(TAG, "finishUpload called from wrong thread! Using runOnUiThread to fix.");
            runOnUiThread(() -> finishUpload(success, message));
            return;
        }

        Log.d(TAG, "Finishing upload process: success=" + success + ", message=" + message);

        progressBar.setVisibility(View.GONE);
        btnUpload.setEnabled(true);
        tvStatus.setText(message);

        // Determine the toast message based on the status
        if (success && "No data needed to be uploaded".equals(message)) {
            Toast.makeText(this, "No data to upload", Toast.LENGTH_SHORT).show();
        } else if (success) {
            Toast.makeText(this, "Upload completed successfully", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Error during upload: " + message, Toast.LENGTH_LONG).show();
        }
        isUploading = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
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