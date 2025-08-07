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
import com.example.universalyogaadmin.models.YogaClass;

import java.util.List;

public class ClassListAdapter extends ArrayAdapter<YogaClass> {

    private Context context;
    private List<YogaClass> yogaClasses;

    public ClassListAdapter(Context context, List<YogaClass> yogaClasses) {
        super(context, R.layout.item_yoga_class, yogaClasses);
        this.context = context;
        this.yogaClasses = yogaClasses;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItem = convertView;

        if (listItem == null) {
            listItem = LayoutInflater.from(context).inflate(R.layout.item_yoga_class, parent, false);
        }

        YogaClass currentClass = yogaClasses.get(position);

        TextView tvClassName = listItem.findViewById(R.id.tvClassName);
        TextView tvClassTime = listItem.findViewById(R.id.tvClassTime);
        TextView tvClassDuration = listItem.findViewById(R.id.tvClassDuration);

        tvClassName.setText(currentClass.getType());
        tvClassTime.setText(currentClass.getDayOfWeek() + " at " + currentClass.getTime());
        tvClassDuration.setText(currentClass.getDuration() + " minutes");

        return listItem;
    }
}