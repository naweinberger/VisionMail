package com.palindromicstudios.visionmail;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.palindromicstudios.testapplication.R;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Natan on 2/25/2015.
 */
public class ConversationActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inbox);

        Bundle bundle = getIntent().getBundleExtra("messages");
        ArrayList<Message> messages = bundle.getParcelableArrayList("messages");

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        MessagesAdapter messagesAdapter = new MessagesAdapter(messages);
        recyclerView.setAdapter(messagesAdapter);
    }
}
