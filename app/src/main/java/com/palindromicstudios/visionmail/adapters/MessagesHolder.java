package com.palindromicstudios.visionmail.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.palindromicstudios.testapplication.R;

/**
 * Created by Natan on 2/25/2015.
 */
public class MessagesHolder extends RecyclerView.ViewHolder {
    TextView name;
    LinearLayout container;
    public MessagesHolder(View v) {
        super(v);
        name = (TextView) v.findViewById(R.id.name);
        container = (LinearLayout) v.findViewById(R.id.container);
    }
}
