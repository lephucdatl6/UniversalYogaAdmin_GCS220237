package com.example.universalyogaadmin.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.universalyogaadmin.models.LogEntry;
import com.example.universalyogaadmin.R;

import java.util.List;

public class LogEntryAdapter extends ArrayAdapter<LogEntry> {
    public LogEntryAdapter(Context context, List<LogEntry> entries) {
        super(context, 0, entries);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LogEntry entry = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_log_entry, parent, false);
        }
        TextView tvTime = convertView.findViewById(R.id.tvTime);
        TextView tvMessage = convertView.findViewById(R.id.tvMessage);

        tvTime.setText(entry.getTimestamp());
        tvMessage.setText(entry.getMessage());
        return convertView;
    }
}