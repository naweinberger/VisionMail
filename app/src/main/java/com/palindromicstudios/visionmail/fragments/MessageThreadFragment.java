package com.palindromicstudios.visionmail.fragments;


import android.app.Fragment;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.palindromicstudios.testapplication.R;
import com.palindromicstudios.visionmail.activities.MainActivity;
import com.palindromicstudios.visionmail.items.Message;
import com.palindromicstudios.visionmail.adapters.MessagesAdapter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Locale;


/**
 * A simple {@link android.app.Fragment} subclass.
 */
public class MessageThreadFragment extends Fragment {

    String threadId = "";
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_thread, container, false);


        Bundle bundle = getArguments();
        ArrayList<Message> messages = bundle.getParcelableArrayList("messages");

        threadId = String.valueOf(messages.get(0).getThread());

        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);

//        MessagesAdapter messagesAdapter = new MessagesAdapter(messages);
//        recyclerView.setAdapter(messagesAdapter);
//
//        recyclerView.scrollToPosition(messages.size() - 1);

        refresh();

        view.setFocusableInTouchMode(true);
        view.requestFocus();

        view.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        getFragmentManager().popBackStack();
                        ((MainActivity) getActivity()).returnToInbox();
                        return true;
                    }
                }
                return false;
            }
        });

        return view;
    }

    private void refresh() {
        if (!threadId.isEmpty()) {
            ArrayList<Message> updatedList = refreshMessages(threadId);
            if (updatedList != null) {
                MessagesAdapter messagesAdapter = new MessagesAdapter(updatedList);
                recyclerView.setAdapter(messagesAdapter);
                recyclerView.scrollToPosition(updatedList.size() - 1);
            }
            else {
                Toast.makeText(getActivity(), "Could not refresh messages.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public ArrayList<Message> refreshMessages(String threadId) {
        ArrayList<Message> result = new ArrayList<>();
        Uri uri = Uri.parse("content://sms/");
        Cursor cursor = getActivity().getContentResolver().query(uri, null, ("thread_id = " + threadId), null, "date ASC");

        if (cursor.moveToFirst()) { // must check the result to prevent exception
            do {
                String msgData = "";
                //This is the thread id
                Integer key = 0;
                Message message = new Message();
                message.setContent(cursor.getString(cursor.getColumnIndexOrThrow("body")));
                message.setPerson(cursor.getInt(cursor.getColumnIndexOrThrow("person")));
                message.setPhone(cursor.getString(cursor.getColumnIndexOrThrow("address")));
                message.setDate(getDate(cursor.getLong(cursor.getColumnIndexOrThrow("date"))));
                message.setType(cursor.getInt(cursor.getColumnIndexOrThrow("type")));
                //message.setName(getContactName(this, cursor.getString(cursor.getColumnIndexOrThrow("address"))));

                key = cursor.getInt(cursor.getColumnIndexOrThrow("thread_id"));
                message.setThread(key);

                // use msgData

                result.add(message);

                //ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, xs);
                //recyclerView.setAdapter(adapter);
            } while (cursor.moveToNext());

            return result;
        }
        return null;
    }

    private String getDate(long time) {
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(time);
        String date = DateFormat.format("MMM d", cal).toString();
        return date;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.thread, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.refresh:
                refresh();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
