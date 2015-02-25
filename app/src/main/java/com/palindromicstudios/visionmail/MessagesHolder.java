package com.palindromicstudios.visionmail;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by Natan on 2/25/2015.
 */
public class MessagesHolder extends RecyclerView.ViewHolder {
    TextView name;
    public MessagesHolder(View v) {
        super(v);
        name = (TextView) v.findViewById(android.R.id.text1);
    }
}
