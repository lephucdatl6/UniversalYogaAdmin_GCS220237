package com.example.universalyogaadmin.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.universalyogaadmin.R;
import com.example.universalyogaadmin.database.DatabaseHelper;
import com.example.universalyogaadmin.models.ClassInstance;
import com.example.universalyogaadmin.models.YogaClass;

import java.util.List;

public class InstanceListAdapter extends ArrayAdapter<ClassInstance> {

    private Context context;
    private List<ClassInstance> instances;
    private DatabaseHelper databaseHelper;

    public InstanceListAdapter(Context context, List<ClassInstance> instances) {
        super(context, R.layout.item_class_instance, instances);
        this.context = context;
        this.instances = instances;
        this.databaseHelper = new DatabaseHelper(context);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItem = convertView;

        if (listItem == null) {
            listItem = LayoutInflater.from(context).inflate(R.layout.item_class_instance, parent, false);
        }

        ClassInstance currentInstance = instances.get(position);

        TextView tvInstanceDate = listItem.findViewById(R.id.tvInstanceDate);
        TextView tvInstanceTeacher = listItem.findViewById(R.id.tvInstanceTeacher);
        TextView tvInstanceClassName = listItem.findViewById(R.id.tvInstanceClassName);

        // Set date and teacher
        tvInstanceDate.setText(currentInstance.getDate());
        tvInstanceTeacher.setText("Teacher: " + currentInstance.getTeacher());

        // Retrieve and set class name
        YogaClass yogaClass = databaseHelper.getYogaClass(currentInstance.getClassId());
        if (yogaClass != null) {
            tvInstanceClassName.setText("Classes: " + yogaClass.getType());
        } else {
            tvInstanceClassName.setText("Classes: Unknown");
        }

        return listItem;
    }
}