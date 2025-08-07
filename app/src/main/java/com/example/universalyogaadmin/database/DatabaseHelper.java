package com.example.universalyogaadmin.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.universalyogaadmin.models.ClassInstance;
import com.example.universalyogaadmin.models.YogaClass;
import com.example.universalyogaadmin.utils.LogManager;
import com.example.universalyogaadmin.models.LogEntry;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "DatabaseHelper";
    private static final String DATABASE_NAME = "universalyoga.db";
    private static final int DATABASE_VERSION = 2;

    // Table names
    private static final String TABLE_LOGS = "logs";
    private static final String TABLE_YOGA_CLASSES = "yoga_classes";
    private static final String TABLE_CLASS_INSTANCES = "class_instances";
    private static final String TABLE_DELETED_YOGA_CLASSES = "deleted_yoga_classes";
    private static final String TABLE_DELETED_CLASS_INSTANCES = "deleted_class_instances";

    // Common column names
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_LOG_ID = "id";

    // Yoga Class table columns
    private static final String COLUMN_DAY_OF_WEEK = "day_of_week";
    private static final String COLUMN_TIME = "time";
    private static final String COLUMN_CAPACITY = "capacity";
    private static final String COLUMN_DURATION = "duration";
    private static final String COLUMN_PRICE = "price";
    private static final String COLUMN_TYPE = "type";
    private static final String COLUMN_DESCRIPTION = "description";
    private static final String COLUMN_LAST_SYNC = "last_sync";

    // Class Instance table columns
    private static final String COLUMN_CLASS_ID = "class_id";
    private static final String COLUMN_DATE = "date";
    private static final String COLUMN_TEACHER = "teacher";
    private static final String COLUMN_COMMENTS = "comments";

    // Tombstone table column
    private static final String COLUMN_DELETED_AT = "deleted_at";

    // Log table columns
    private static final String COLUMN_LOG_MESSAGE = "message";
    private static final String COLUMN_LOG_TIMESTAMP = "timestamp";

    // Create table statements
    private static final String CREATE_TABLE_YOGA_CLASSES = "CREATE TABLE " + TABLE_YOGA_CLASSES + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_DAY_OF_WEEK + " TEXT NOT NULL,"
            + COLUMN_TIME + " TEXT NOT NULL,"
            + COLUMN_CAPACITY + " INTEGER NOT NULL,"
            + COLUMN_DURATION + " INTEGER NOT NULL,"
            + COLUMN_PRICE + " REAL NOT NULL,"
            + COLUMN_TYPE + " TEXT NOT NULL,"
            + COLUMN_DESCRIPTION + " TEXT,"
            + COLUMN_LAST_SYNC + " INTEGER DEFAULT 0"
            + ")";

    private static final String CREATE_TABLE_CLASS_INSTANCES = "CREATE TABLE " + TABLE_CLASS_INSTANCES + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_CLASS_ID + " INTEGER NOT NULL,"
            + COLUMN_DATE + " TEXT NOT NULL,"
            + COLUMN_TEACHER + " TEXT NOT NULL,"
            + COLUMN_COMMENTS + " TEXT,"
            + COLUMN_LAST_SYNC + " INTEGER DEFAULT 0,"
            + "FOREIGN KEY(" + COLUMN_CLASS_ID + ") REFERENCES " + TABLE_YOGA_CLASSES + "(" + COLUMN_ID + ")"
            + ")";

    // Create tombstone tables for tracking deletions
    private static final String CREATE_TABLE_DELETED_YOGA_CLASSES = "CREATE TABLE " + TABLE_DELETED_YOGA_CLASSES + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY,"
            + COLUMN_DELETED_AT + " INTEGER NOT NULL"
            + ")";

    private static final String CREATE_TABLE_DELETED_CLASS_INSTANCES = "CREATE TABLE " + TABLE_DELETED_CLASS_INSTANCES + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY,"
            + COLUMN_DELETED_AT + " INTEGER NOT NULL"
            + ")";

    private static final String CREATE_TABLE_LOGS = "CREATE TABLE " + TABLE_LOGS + " ("
            + COLUMN_LOG_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_LOG_MESSAGE + " TEXT NOT NULL, "
            + COLUMN_LOG_TIMESTAMP + " TEXT NOT NULL"
            + ");";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_LOGS);
        db.execSQL(CREATE_TABLE_YOGA_CLASSES);
        db.execSQL(CREATE_TABLE_CLASS_INSTANCES);
        db.execSQL(CREATE_TABLE_DELETED_YOGA_CLASSES);
        db.execSQL(CREATE_TABLE_DELETED_CLASS_INSTANCES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 3) {
            db.execSQL(CREATE_TABLE_LOGS);
            db.execSQL(CREATE_TABLE_DELETED_YOGA_CLASSES);
            db.execSQL(CREATE_TABLE_DELETED_CLASS_INSTANCES);
        } else {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOGS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_CLASS_INSTANCES);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_YOGA_CLASSES);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_DELETED_CLASS_INSTANCES);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_DELETED_YOGA_CLASSES);
            onCreate(db);
        }
    }

    // CRUD operations for Log Entries
    public long insertLogEntry(String message, String timestamp) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_LOG_MESSAGE, message);
        values.put(COLUMN_LOG_TIMESTAMP, timestamp);
        long id = db.insert(TABLE_LOGS, null, values);
        db.close();
        return id;
    }

    public List<LogEntry> getAllLogEntries() {
        List<LogEntry> logs = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_LOGS, null, null, null, null, null, COLUMN_LOG_ID + " DESC");
        if (cursor.moveToFirst()) {
            do {
                String message = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LOG_MESSAGE));
                String timestamp = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LOG_TIMESTAMP));
                logs.add(new LogEntry(message, timestamp));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return logs;
    }

    public void clearAllLogs() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_LOGS, null, null);
        db.close();
    }

    // CRUD operations for Yoga Classes
    public long insertYogaClass(YogaClass yogaClass) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_DAY_OF_WEEK, yogaClass.getDayOfWeek());
        values.put(COLUMN_TIME, yogaClass.getTime());
        values.put(COLUMN_CAPACITY, yogaClass.getCapacity());
        values.put(COLUMN_DURATION, yogaClass.getDuration());
        values.put(COLUMN_PRICE, yogaClass.getPrice());
        values.put(COLUMN_TYPE, yogaClass.getType());
        values.put(COLUMN_DESCRIPTION, yogaClass.getDescription());

        long id = db.insert(TABLE_YOGA_CLASSES, null, values);
        db.close();

        // Log creation
        if (id != -1) {
            LogManager.addLogEntry("Created class: " + yogaClass.getType() + " (ID:" + id + ")");
        }

        return id;
    }

    public YogaClass getYogaClass(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_YOGA_CLASSES, null, COLUMN_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null);

        YogaClass yogaClass = null;
        if (cursor != null && cursor.moveToFirst()) {
            try {
                yogaClass = new YogaClass(
                        cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DAY_OF_WEEK)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIME)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CAPACITY)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_DURATION)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_PRICE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TYPE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION))
                );
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "Error getting yoga class: " + e.getMessage());
            } finally {
                cursor.close();
            }
        }
        return yogaClass;
    }

    public List<YogaClass> getAllYogaClasses() {
        List<YogaClass> yogaClasses = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_YOGA_CLASSES;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                try {
                    YogaClass yogaClass = new YogaClass(
                            cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DAY_OF_WEEK)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIME)),
                            cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CAPACITY)),
                            cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_DURATION)),
                            cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_PRICE)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TYPE)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION))
                    );
                    yogaClasses.add(yogaClass);
                } catch (IllegalArgumentException e) {
                    Log.e(TAG, "Error in getAllYogaClasses: " + e.getMessage());
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return yogaClasses;
    }

    public int updateYogaClass(YogaClass yogaClass) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_DAY_OF_WEEK, yogaClass.getDayOfWeek());
        values.put(COLUMN_TIME, yogaClass.getTime());
        values.put(COLUMN_CAPACITY, yogaClass.getCapacity());
        values.put(COLUMN_DURATION, yogaClass.getDuration());
        values.put(COLUMN_PRICE, yogaClass.getPrice());
        values.put(COLUMN_TYPE, yogaClass.getType());
        values.put(COLUMN_DESCRIPTION, yogaClass.getDescription());
        values.put(COLUMN_LAST_SYNC, 0); // Mark for sync

        int rowsAffected = db.update(TABLE_YOGA_CLASSES, values, COLUMN_ID + "=?",
                new String[]{String.valueOf(yogaClass.getId())});
        db.close();

        if (rowsAffected > 0) {
            LogManager.addLogEntry("Edited class: " + yogaClass.getType() + " (ID:" + yogaClass.getId() + ")");
        }

        return rowsAffected;
    }

    public void deleteYogaClass(long id) {
        SQLiteDatabase db = this.getWritableDatabase();

        String classType = "";
        YogaClass yogaClass = getYogaClass(id);
        if (yogaClass != null) {
            classType = yogaClass.getType();
        }

        try {
            db.beginTransaction();

            // Format current time
            SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
            String formattedTime = timeFormatter.format(new Date());

            // Record this deletion in the tombstone table
            ContentValues tombstoneValues = new ContentValues();
            tombstoneValues.put(COLUMN_ID, id);
            tombstoneValues.put(COLUMN_DELETED_AT, formattedTime);
            db.insertWithOnConflict(TABLE_DELETED_YOGA_CLASSES, null, tombstoneValues,
                    SQLiteDatabase.CONFLICT_REPLACE);

            Cursor cursor = db.query(TABLE_CLASS_INSTANCES, new String[]{COLUMN_ID},
                    COLUMN_CLASS_ID + "=?", new String[]{String.valueOf(id)},
                    null, null, null);

            // Process each instance
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    try {
                        long instanceId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID));

                        // Mark instance as deleted in tombstone table
                        ContentValues instanceTombstoneValues = new ContentValues();
                        instanceTombstoneValues.put(COLUMN_ID, instanceId);
                        instanceTombstoneValues.put(COLUMN_DELETED_AT, formattedTime);
                        db.insertWithOnConflict(TABLE_DELETED_CLASS_INSTANCES, null, instanceTombstoneValues,
                                SQLiteDatabase.CONFLICT_REPLACE);

                        // Delete the instance
                        db.delete(TABLE_CLASS_INSTANCES, COLUMN_ID + "=?", new String[]{String.valueOf(instanceId)});
                    } catch (IllegalArgumentException e) {
                        Log.e(TAG, "Error in deleteYogaClass: " + e.getMessage());
                    }
                } while (cursor.moveToNext());
                cursor.close();
            }

            db.delete(TABLE_YOGA_CLASSES, COLUMN_ID + "=?", new String[]{String.valueOf(id)});
            db.setTransactionSuccessful();
        } finally {
            if (db.inTransaction()) {
                db.endTransaction();
            }
            db.close();
        }

        LogManager.addLogEntry("Deleted class: " + classType + " (ID:" + id + ")");
    }

    // CRUD operations for Class Instances
    public long insertClassInstance(ClassInstance instance) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_CLASS_ID, instance.getClassId());
        values.put(COLUMN_DATE, instance.getDate());
        values.put(COLUMN_TEACHER, instance.getTeacher());
        values.put(COLUMN_COMMENTS, instance.getComments());

        long id = db.insert(TABLE_CLASS_INSTANCES, null, values);
        db.close();

        // Log creation
        if (id != -1) {
            YogaClass yogaClass = getYogaClass(instance.getClassId());
            String classType = yogaClass != null ? yogaClass.getType() : "";
            LogManager.addLogEntry("Created class instance: " + classType + " (ID:" + id + ")");
        }

        return id;
    }

    public List<ClassInstance> getInstancesByClassId(long classId) {
        List<ClassInstance> instances = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_CLASS_INSTANCES, null, COLUMN_CLASS_ID + "=?",
                new String[]{String.valueOf(classId)}, null, null, COLUMN_DATE + " ASC");

        if (cursor.moveToFirst()) {
            do {
                try {
                    ClassInstance instance = new ClassInstance(
                            cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                            cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_CLASS_ID)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TEACHER)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COMMENTS))
                    );
                    instances.add(instance);
                } catch (IllegalArgumentException e) {
                    Log.e(TAG, "Error in getInstancesByClassId: " + e.getMessage());
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return instances;
    }

    public int updateClassInstance(ClassInstance instance) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_CLASS_ID, instance.getClassId());
        values.put(COLUMN_DATE, instance.getDate());
        values.put(COLUMN_TEACHER, instance.getTeacher());
        values.put(COLUMN_COMMENTS, instance.getComments());
        values.put(COLUMN_LAST_SYNC, 0); // Mark for sync

        int rowsAffected = db.update(TABLE_CLASS_INSTANCES, values, COLUMN_ID + "=?",
                new String[]{String.valueOf(instance.getId())});
        db.close();

        if (rowsAffected > 0) {
            YogaClass yogaClass = getYogaClass(instance.getClassId());
            String classType = yogaClass != null ? yogaClass.getType() : "";
            LogManager.addLogEntry("Edited class instance: " + classType + " (ID:" + instance.getId() + ")");
        }

        return rowsAffected;
    }

    public void deleteClassInstance(long id) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Try to get class type for logging
        String classType = "";
        long classId = -1;

        Cursor cursor = db.query(TABLE_CLASS_INSTANCES, new String[]{COLUMN_CLASS_ID},
                COLUMN_ID + "=?", new String[]{String.valueOf(id)}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            classId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_CLASS_ID));
            cursor.close();
        }

        if (classId != -1) {
            YogaClass yogaClass = getYogaClass(classId);
            if (yogaClass != null) {
                classType = yogaClass.getType();
            }
        }

        try {
            db.beginTransaction();

            SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
            String formattedTime = timeFormatter.format(new Date());

            // Record this deletion in the tombstone table
            ContentValues tombstoneValues = new ContentValues();
            tombstoneValues.put(COLUMN_ID, id);
            tombstoneValues.put(COLUMN_DELETED_AT, formattedTime);
            db.insertWithOnConflict(TABLE_DELETED_CLASS_INSTANCES, null, tombstoneValues,
                    SQLiteDatabase.CONFLICT_REPLACE);

            // Delete the instance
            db.delete(TABLE_CLASS_INSTANCES, COLUMN_ID + "=?", new String[]{String.valueOf(id)});
            db.setTransactionSuccessful();
        } finally {
            if (db.inTransaction()) {
                db.endTransaction();
            }
            db.close();
        }

        LogManager.addLogEntry("Deleted class instance: " + classType + " (ID:" + id + ")");
    }

    // Search methods
    public List<ClassInstance> searchInstancesByTeacher(String teacherName) {
        List<ClassInstance> instances = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT * FROM " + TABLE_CLASS_INSTANCES +
                " WHERE " + COLUMN_TEACHER + " LIKE '%" + teacherName + "%'" +
                " ORDER BY " + COLUMN_DATE + " ASC";

        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                try {
                    ClassInstance instance = new ClassInstance(
                            cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                            cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_CLASS_ID)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TEACHER)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COMMENTS))
                    );
                    instances.add(instance);
                } catch (IllegalArgumentException e) {
                    Log.e(TAG, "Error in searchInstancesByTeacher: " + e.getMessage());
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return instances;
    }

    public List<YogaClass> searchClassesByDay(String dayOfWeek) {
        List<YogaClass> yogaClasses = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT * FROM " + TABLE_YOGA_CLASSES +
                " WHERE " + COLUMN_DAY_OF_WEEK + " = '" + dayOfWeek + "'" +
                " ORDER BY " + COLUMN_TIME + " ASC";

        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                try {
                    YogaClass yogaClass = new YogaClass(
                            cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DAY_OF_WEEK)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIME)),
                            cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CAPACITY)),
                            cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_DURATION)),
                            cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_PRICE)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TYPE)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION))
                    );
                    yogaClasses.add(yogaClass);
                } catch (IllegalArgumentException e) {
                    Log.e(TAG, "Error in searchClassesByDay: " + e.getMessage());
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return yogaClasses;
    }

    // Methods for cloud synchronization
    public List<YogaClass> getUnsyncedYogaClasses() {
        List<YogaClass> yogaClasses = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT * FROM " + TABLE_YOGA_CLASSES +
                " WHERE " + COLUMN_LAST_SYNC + " = 0";

        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                try {
                    YogaClass yogaClass = new YogaClass(
                            cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DAY_OF_WEEK)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIME)),
                            cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CAPACITY)),
                            cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_DURATION)),
                            cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_PRICE)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TYPE)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION))
                    );
                    yogaClasses.add(yogaClass);
                } catch (IllegalArgumentException e) {
                    Log.e(TAG, "Error in getUnsyncedYogaClasses: " + e.getMessage());
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return yogaClasses;
    }

    public List<ClassInstance> getUnsyncedClassInstances() {
        List<ClassInstance> instances = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT * FROM " + TABLE_CLASS_INSTANCES +
                " WHERE " + COLUMN_LAST_SYNC + " = 0";

        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                try {
                    ClassInstance instance = new ClassInstance(
                            cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                            cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_CLASS_ID)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TEACHER)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COMMENTS))
                    );
                    instances.add(instance);
                } catch (IllegalArgumentException e) {
                    Log.e(TAG, "Error in getUnsyncedClassInstances: " + e.getMessage());
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return instances;
    }

    // New methods for handling deleted records
    public List<Long> getDeletedYogaClassIds() {
        List<Long> deletedIds = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT " + COLUMN_ID + " FROM " + TABLE_DELETED_YOGA_CLASSES;
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                try {
                    deletedIds.add(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                } catch (IllegalArgumentException e) {
                    Log.e(TAG, "Error in getDeletedYogaClassIds: " + e.getMessage());
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return deletedIds;
    }

    public List<Long> getDeletedClassInstanceIds() {
        List<Long> deletedIds = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT " + COLUMN_ID + " FROM " + TABLE_DELETED_CLASS_INSTANCES;
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                try {
                    deletedIds.add(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                } catch (IllegalArgumentException e) {
                    Log.e(TAG, "Error in getDeletedClassInstanceIds: " + e.getMessage());
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return deletedIds;
    }

    public void clearDeletedYogaClassRecord(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_DELETED_YOGA_CLASSES, COLUMN_ID + "=?",
                new String[]{String.valueOf(id)});
        db.close();
    }

    public void clearDeletedClassInstanceRecord(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_DELETED_CLASS_INSTANCES, COLUMN_ID + "=?",
                new String[]{String.valueOf(id)});
        db.close();
    }

    public void markYogaClassAsSynced(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_LAST_SYNC, 1);
        db.update(TABLE_YOGA_CLASSES, values, COLUMN_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
    }

    public void markClassInstanceAsSynced(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_LAST_SYNC, 1);
        db.update(TABLE_CLASS_INSTANCES, values, COLUMN_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
    }

    public void resetDatabase() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOGS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CLASS_INSTANCES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_YOGA_CLASSES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DELETED_CLASS_INSTANCES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DELETED_YOGA_CLASSES);
        onCreate(db);
        db.close();
    }

}