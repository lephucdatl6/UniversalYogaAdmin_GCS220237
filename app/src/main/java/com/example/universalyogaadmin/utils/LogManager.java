package com.example.universalyogaadmin.utils;

import android.content.Context;

import com.example.universalyogaadmin.models.LogEntry;
import com.example.universalyogaadmin.adapters.LogEntryAdapter;
import com.example.universalyogaadmin.database.DatabaseHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.List;

public class LogManager {
    public static final ArrayList<LogEntry> logList = new ArrayList<>();
    public static LogEntryAdapter logAdapter = null;
    private static DatabaseHelper dbHelper;

    // Call once in Application or MainActivity onCreate, before using logs
    public static void init(Context context) {
        dbHelper = new DatabaseHelper(context.getApplicationContext());
        loadLogsFromDatabase();
    }

    public static void addLogEntry(String message) {
        String time = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
        // Save to SQLite
        if (dbHelper != null) {
            dbHelper.insertLogEntry(message, time);
        }
        // Add to in-memory list (for UI)
        logList.add(0, new LogEntry(message, time));
        if (logAdapter != null) logAdapter.notifyDataSetChanged();
    }

    public static void loadLogsFromDatabase() {
        if (dbHelper != null) {
            List<LogEntry> logsFromDb = dbHelper.getAllLogEntries();
            logList.clear();
            logList.addAll(logsFromDb);
            if (logAdapter != null) logAdapter.notifyDataSetChanged();
        }
    }

    public static void clearAllLogs() {
        if (dbHelper != null) {
            dbHelper.clearAllLogs();
        }
        logList.clear();
        if (logAdapter != null) logAdapter.notifyDataSetChanged();
    }
}