package com.example.universalyogaadmin.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.example.universalyogaadmin.R;
import com.example.universalyogaadmin.adapters.ClassListAdapter;
import com.example.universalyogaadmin.database.DatabaseHelper;
import com.example.universalyogaadmin.models.YogaClass;

import java.util.List;

public class ViewClassesActivity extends AppCompatActivity {

    private ListView listViewClasses;
    private DatabaseHelper databaseHelper;
    private ClassListAdapter adapter;
    private List<YogaClass> yogaClasses;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_classes);

        // Set up back button in action bar
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("View All Yoga Classes");
        }

        listViewClasses = findViewById(R.id.listViewClasses);
        databaseHelper = new DatabaseHelper(this);

        setupListView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadYogaClasses();
    }

    private void setupListView() {
        loadYogaClasses();

        listViewClasses.setOnItemClickListener((parent, view, position, id) -> {
            YogaClass selectedClass = yogaClasses.get(position);
            Intent intent = new Intent(ViewClassesActivity.this, ClassDetailsActivity.class);
            intent.putExtra("CLASS_ID", selectedClass.getId());
            startActivity(intent);
        });
    }

    private void loadYogaClasses() {
        yogaClasses = databaseHelper.getAllYogaClasses();

        if (yogaClasses.isEmpty()) {
            Toast.makeText(this, "No yoga classes found", Toast.LENGTH_SHORT).show();
        }

        adapter = new ClassListAdapter(this, yogaClasses);
        listViewClasses.setAdapter(adapter);
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