package com.palindromicstudios.visionmail;


import android.app.Fragment;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.palindromicstudios.testapplication.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;


/**
 * A simple {@link android.app.Fragment} subclass.
 */
public class MessageThreadFragment extends Fragment {

    FrameLayout container;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_inbox, container, false);

        this.container = (FrameLayout) view.findViewById(R.id.inbox_container);

        //setAlpha(0.4f);

        Bundle bundle = getArguments();
        ArrayList<Message> messages = bundle.getParcelableArrayList("messages");

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);

        MessagesAdapter messagesAdapter = new MessagesAdapter(messages);
        recyclerView.setAdapter(messagesAdapter);

        view.setFocusableInTouchMode(true);
        view.requestFocus();

        view.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        ((MyActivity)getActivity()).returnToInbox();
                        return true;
                    }
                }
                return false;
            }
        });

        return view;
    }



    public void setAlpha(float alpha) {
        this.container.setAlpha(alpha);
    }

}
