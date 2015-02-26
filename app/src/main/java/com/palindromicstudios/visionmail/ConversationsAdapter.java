package com.palindromicstudios.visionmail;


import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.palindromicstudios.testapplication.R;

import java.util.ArrayList;
import java.util.List;

public class ConversationsAdapter extends RecyclerView.Adapter<ConversationHolder> {
    private Fragment fragment;
    private List<Message> items;
    private int lastPosition = -1;

    public ConversationsAdapter(Fragment fragment, List<Message> items) {
        this.items = items;
        this.fragment = fragment;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public void onBindViewHolder(ConversationHolder holder, final int position) {
        final Message dItem = items.get(position);
        if (!dItem.getName().isEmpty()) {
            holder.name.setText(dItem.getName());
        }
        else {
            holder.name.setText(dItem.getPhone());
        }
        holder.message.setText(dItem.getContent());
        holder.date.setText(dItem.getDate());

        if (position % 2 == 0) {
            holder.container.setBackgroundColor(Color.parseColor("#e4e4e4"));
        }
        else {
            holder.container.setBackgroundColor(Color.parseColor("#ffffff"));
        }

        holder.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<Message> messages = ((InboxFragment)fragment).conversations.get(dItem.getThread());
                Bundle bundle = new Bundle();
                bundle.putParcelableArrayList("messages", messages);
                Intent intent = new Intent(fragment.getActivity(), ConversationActivity.class);
                intent.putExtra("messages", bundle);
                fragment.getActivity().startActivity(intent);
            }
        });
    }

    @Override
    public ConversationHolder onCreateViewHolder(ViewGroup viewGroup, int position) {
        View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.conversation_item_layout, viewGroup, false);
        return new ConversationHolder(itemView);
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

