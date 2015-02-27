package com.palindromicstudios.visionmail.fragments;


import android.app.Activity;
import android.app.Fragment;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.telephony.SmsManager;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.palindromicstudios.testapplication.R;
import com.palindromicstudios.visionmail.activities.MainActivity;
import com.palindromicstudios.visionmail.items.Message;
import com.palindromicstudios.visionmail.adapters.MessagesAdapter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Locale;
import java.util.Objects;


/**
 * A simple {@link android.app.Fragment} subclass.
 */
public class MessageThreadFragment extends Fragment {

    String threadId = "";
    String phone = "";
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    final String MESSAGE_RECEIVED = "message_received";

    EditText messageEdittext;
    ImageButton messageSendBtn;

    boolean keyboardDown = true;

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
        final ArrayList<Message> messages = bundle.getParcelableArrayList("messages");

        threadId = String.valueOf(messages.get(0).getThread());
        phone = String.valueOf(messages.get(0).getPhone());

        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);

//        MessagesAdapter messagesAdapter = new MessagesAdapter(messages);
//        recyclerView.setAdapter(messagesAdapter);
//
//        recyclerView.scrollToPosition(messages.size() - 1);

        refresh();

        messageEdittext = (EditText) view.findViewById(R.id.message_edittext);
        messageSendBtn = (ImageButton) view.findViewById(R.id.message_send_btn);
        messageEdittext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                keyboardDown = false;
            }
        });

        messageSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String body = messageEdittext.getText().toString();
                if (!body.isEmpty()) {
                    if (!phone.isEmpty()) {
                        sendText(body, phone);
                    }
                    else {
                        Toast.makeText(getActivity(), "No recipient indicated.", Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    Log.i("VisionMail", "No message body.");
                }
            }
        });

        view.setFocusableInTouchMode(true);
        view.requestFocus();

        view.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {

                        //If the keyboard is not showing, then pressing back should take the user back to the inbox
                        if (keyboardDown) {
                            ((MainActivity) getActivity()).returnToInbox();
                        }
                        //If the keyboard is showing, then hide the keyboard
                        else {
                            keyboardDown = true;
                        }

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
        }
    };

    public boolean isKeyboardShowing() {
        return !keyboardDown;
    }

    public void refresh() {
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
        Cursor cursor = getActivity().getContentResolver().query(uri, null, ("thread_id=" + threadId), null, "date ASC");

        ArrayList<String> idsUnread = new ArrayList<String>();

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

                if (cursor.getInt(cursor.getColumnIndex("read")) == 0) {
                    String messageId = cursor.getString(cursor.getColumnIndex("_id"));
                    idsUnread.add(messageId);
                }


                //message.setName(getContactName(this, cursor.getString(cursor.getColumnIndexOrThrow("address"))));

                key = cursor.getInt(cursor.getColumnIndexOrThrow("thread_id"));
                message.setThread(key);

                // use msgData

                result.add(message);

                //ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, xs);
                //recyclerView.setAdapter(adapter);
            } while (cursor.moveToNext());

            cursor.close();

            Object[] ids = idsUnread.toArray();
            for (Object id : ids) {
                Log.d("UNREAD", (String)id);
                ContentValues values = new ContentValues();
                values.put("read",true);
                int rowsUpdated = getActivity().getContentResolver().update(Uri.parse("content://sms/"),values, "_id=?", new String[]{id + ""});
                Log.d("ROWS_UPDATED", "" + rowsUpdated);
            }

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

    private void sendText(String message, String phoneNumber) {
        String sent = "SMS_SENT";

        PendingIntent sentPI = PendingIntent.getBroadcast(getActivity(), 0,
                new Intent(sent), 0);

        //---when the SMS has been sent---
        getActivity().registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                if (getResultCode() == Activity.RESULT_OK) {
                    Toast.makeText(getActivity(), "SMS sent.", Toast.LENGTH_SHORT).show();
                    messageEdittext.setText("");

                } else {
                    Toast.makeText(getActivity(), "Sending failed.",
                            Toast.LENGTH_SHORT).show();
                }
            }
        }, new IntentFilter(sent));

        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, message, sentPI, null);
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
