package com.palindromicstudios.visionmail.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.palindromicstudios.testapplication.R;

public class ConversationHolder extends RecyclerView.ViewHolder {
    TextView name, message, date;
    ImageButton delete;
    LinearLayout container;
    public ConversationHolder(View v) {
        super(v);
        name = (TextView) v.findViewById(R.id.name);
        message = (TextView) v.findViewById(R.id.message);
        date = (TextView) v.findViewById(R.id.time);
        container = (LinearLayout) v.findViewById(R.id.container);

    }
}