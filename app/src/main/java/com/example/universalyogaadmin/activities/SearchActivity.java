package com.example.universalyogaadmin.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.example.universalyogaadmin.R;
import com.example.universalyogaadmin.adapters.ClassListAdapter;
import com.example.universalyogaadmin.adapters.InstanceListAdapter;
import com.example.universalyogaadmin.database.DatabaseHelper;
import com.example.universalyogaadmin.models.ClassInstance;
import com.example.universalyogaadmin.models.YogaClass;

import java.util.List;

public class SearchActivity extends AppCompatActivity {

    private RadioGroup radioGroupSearchType;
    private RadioButton radioTeacher;
    private RadioButton radioDay;
    private EditText etTeacherSearch;
    private Spinner spinnerDaySearch;
    private ListView listViewSearchResults;
    private TextView tvNoResults;

    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // Set up back button in action bar
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("Search Classes");
        }

        // Initialize views
        radioGroupSearchType = findViewById(R.id.radioGroupSearchType);
        radioTeacher = findViewById(R.id.radioTeacher);
        radioDay = findViewById(R.id.radioDay);
        etTeacherSearch = findViewById(R.id.etTeacherSearch);
        spinnerDaySearch = findViewById(R.id.spinnerDaySearch);
        listViewSearchResults = findViewById(R.id.listViewSearchResults);
        tvNoResults = findViewById(R.id.tvNoResults);

        databaseHelper = new DatabaseHelper(this);

        // Set up day spinner
        ArrayAdapter<CharSequence> dayAdapter = ArrayAdapter.createFromResource(this,
                R.array.days_of_week, android.R.layout.simple_spinner_item);
        dayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDaySearch.setAdapter(dayAdapter);

        setupListeners();
        updateUIForSearchType();
    }

    private void setupListeners() {
        radioGroupSearchType.setOnCheckedChangeListener((group, checkedId) -> {
            updateUIForSearchType();
            clearSearchResults();

            if (checkedId == R.id.radioDay) {
                String selectedDay = spinnerDaySearch.getSelectedItem().toString();
                searchByDay(selectedDay);
            }
        });

        etTeacherSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (radioTeacher.isChecked() && s.length() >= 2) {
                    searchByTeacher(s.toString());
                } else if (s.length() == 0) {
                    clearSearchResults();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        spinnerDaySearch.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (radioDay.isChecked()) {
                    String day = parent.getItemAtPosition(position).toString();
                    searchByDay(day);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        listViewSearchResults.setOnItemClickListener((parent, view, position, id) -> {
            if (radioTeacher.isChecked()) {
                ClassInstance instance = (ClassInstance) parent.getItemAtPosition(position);
                YogaClass yogaClass = databaseHelper.getYogaClass(instance.getClassId());
                if (yogaClass != null) {
                    Intent intent = new Intent(SearchActivity.this, ClassDetailsActivity.class);
                    intent.putExtra("CLASS_ID", yogaClass.getId());
                    startActivity(intent);
                }
            } else if (radioDay.isChecked()) {
                YogaClass yogaClass = (YogaClass) parent.getItemAtPosition(position);
                Intent intent = new Intent(SearchActivity.this, ClassDetailsActivity.class);
                intent.putExtra("CLASS_ID", yogaClass.getId());
                startActivity(intent);
            }
        });
    }

    private void updateUIForSearchType() {
        if (radioTeacher.isChecked()) {
            etTeacherSearch.setVisibility(View.VISIBLE);
            spinnerDaySearch.setVisibility(View.GONE);
        } else if (radioDay.isChecked()) {
            etTeacherSearch.setVisibility(View.GONE);
            spinnerDaySearch.setVisibility(View.VISIBLE);
        }
    }

    private void searchByTeacher(String teacherName) {
        List<ClassInstance> instances = databaseHelper.searchInstancesByTeacher(teacherName);

        if (instances.isEmpty()) {
            showNoResults();
        } else {
            hideNoResults();
            InstanceListAdapter adapter = new InstanceListAdapter(this, instances);
            listViewSearchResults.setAdapter(adapter);
        }
    }

    private void searchByDay(String dayOfWeek) {
        List<YogaClass> classes = databaseHelper.searchClassesByDay(dayOfWeek);

        if (classes.isEmpty()) {
            showNoResults();
        } else {
            hideNoResults();
            ClassListAdapter adapter = new ClassListAdapter(this, classes);
            listViewSearchResults.setAdapter(adapter);
        }
    }

    private void clearSearchResults() {
        listViewSearchResults.setAdapter(null);
        showNoResults();
    }

    private void showNoResults() {
        tvNoResults.setVisibility(View.VISIBLE);
        listViewSearchResults.setVisibility(View.GONE);
    }

    private void hideNoResults() {
        tvNoResults.setVisibility(View.GONE);
        listViewSearchResults.setVisibility(View.VISIBLE);
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