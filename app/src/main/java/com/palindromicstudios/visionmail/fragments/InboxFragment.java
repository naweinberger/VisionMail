package com.palindromicstudios.visionmail.fragments;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.provider.ContactsContract;
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
import android.widget.FrameLayout;
import android.widget.Toast;

import com.palindromicstudios.testapplication.R;
import com.palindromicstudios.visionmail.adapters.ConversationsAdapter;
import com.palindromicstudios.visionmail.items.Message;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;


/**
 * A simple {@link Fragment} subclass.
 */
public class InboxFragment extends Fragment {

    public HashMap<Integer, ArrayList<Message>> conversations;
    FrameLayout container;

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    final String MESSAGE_RECEIVED = "message_received";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_inbox, container, false);

        this.container = (FrameLayout) view.findViewById(R.id.inbox_container);

        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);

        //Populate list of conversations
        refresh();

        view.setFocusableInTouchMode(true);
        view.requestFocus();

        view.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        getActivity().onBackPressed();
                        return true;
                    }
                }
                return false;
            }
        });

        getActivity().registerReceiver(broadcastReceiver, new IntentFilter(MESSAGE_RECEIVED));

        return view;
    }

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            refresh();
            refresh();
        }
    };

    public void refresh() {
        //A hashmap that stores each conversation thread according to a thread id
        conversations = new HashMap<Integer, ArrayList<Message>>();

        //A list containing all of the thread ids for iterating through the hashmap later
        List<Integer> keys = new ArrayList<Integer>();

        //Get all messages
        Cursor cursor = getActivity().getContentResolver().query(Uri.parse("content://sms"), null, null, null, null);

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
                message.setRead(cursor.getInt(cursor.getColumnIndexOrThrow("read")));
                //message.setName(getContactName(this, cursor.getString(cursor.getColumnIndexOrThrow("address"))));

                key = cursor.getInt(cursor.getColumnIndexOrThrow("thread_id"));
                message.setThread(key);

                // use msgData

                ArrayList<Message> temp;
                if (keys.contains(key)) {
                    temp = conversations.get(key);
                }
                else {
                    temp = new ArrayList<Message>();
                    keys.add(key);
                }

                if (temp == null) {
                    temp = new ArrayList<Message>();
                }

                try {
                    temp.add(message);
                    conversations.put(key, temp);
                } catch (NullPointerException e) {
                    Log.e("VisionMail", e.toString());
                }

                //ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, xs);
                //recyclerView.setAdapter(adapter);
            } while (cursor.moveToNext());

            List<Message> conversationPreviews = new ArrayList<Message>();

            for (Integer key : keys) {
                List<Message> tempList = conversations.get(key);
                Message msg = tempList.get(0);
                msg.setName(getContactName(getActivity(), msg.getPhone()));
                conversationPreviews.add(msg);
            }
            ConversationsAdapter adapter = new ConversationsAdapter(this, conversationPreviews);
            recyclerView.setAdapter(adapter);

//            for (int key : keys) {
//                Message first = conversations.get(key).get(0);
//                first.setName(first.getPhone());
//            }
//
//            adapter.notifyDataSetChanged();


        } else {
            // empty box, no SMS
        }
    }

    public String getContactName(Context context, String phoneNumber) {
        Uri personUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, phoneNumber);

        Cursor cur = context.getContentResolver().query(personUri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);

        String name = "";

        if (cur.moveToFirst()) {
            int nameIndex = cur.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME);

            name = cur.getString(nameIndex);
        }
        cur.close();



        return name;
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

    @Override
    public void onDetach() {
        getActivity().unregisterReceiver(broadcastReceiver);
        super.onDetach();
    }
}
