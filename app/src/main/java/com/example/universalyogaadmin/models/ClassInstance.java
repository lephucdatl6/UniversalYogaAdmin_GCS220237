package com.example.universalyogaadmin.models;

public class ClassInstance {
    private long id;
    private long classId;
    private String date;
    private String teacher;
    private String comments;

    // Constructor with ID (for database retrieval)
    public ClassInstance(long id, long classId, String date, String teacher, String comments) {
        this.id = id;
        this.classId = classId;
        this.date = date;
        this.teacher = teacher;
        this.comments = comments;
    }

    // Constructor without ID (for new instance creation)
    public ClassInstance(long classId, String date, String teacher, String comments) {
        this.classId = classId;
        this.date = date;
        this.teacher = teacher;
        this.comments = comments;
    }

    // Getters and Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getClassId() {
        return classId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTeacher() {
        return teacher;
    }

    public void setTeacher(String teacher) {
        this.teacher = teacher;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    @Override
    public String toString() {
        return date + " - " + teacher;
    }
}