package com.palindromicstudios.visionmail.adapters;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.palindromicstudios.testapplication.R;
import com.palindromicstudios.visionmail.items.Message;

import java.text.NumberFormat;
import java.util.ArrayList;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesHolder> {
    private ArrayList<Message> items;
    private int lastPosition = -1;
    private NumberFormat numberFormat;

    public MessagesAdapter(ArrayList<Message> items) {
        this.items = items;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public void onBindViewHolder(MessagesHolder holder, final int position) {
        Message dItem = items.get(position);
        if (dItem.getType() == 1) {
            holder.name.setGravity(Gravity.LEFT);
            //holder.name.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
        }
        else {
            holder.name.setGravity(Gravity.RIGHT);
            //holder.name.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
        }
        holder.name.setText(dItem.getContent());

        if (position % 2 == 0) {
            holder.container.setBackgroundColor(Color.parseColor("#e4e4e4"));
        }
        else {
            holder.container.setBackgroundColor(Color.parseColor("#ffffff"));
        }

    }

    @Override
    public MessagesHolder onCreateViewHolder(ViewGroup viewGroup, int position) {
        View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.message_item_layout, viewGroup, false);
        return new MessagesHolder(itemView);
    }

    private void setAnimation(View viewToAnimate, int position)
    {
        // If the bound view wasn't previously displayed on screen, it's animated
        if (position > lastPosition)
        {
            Animation animation = AnimationUtils.loadAnimation(viewToAnimate.getContext(), android.R.anim.slide_in_left);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }


}


