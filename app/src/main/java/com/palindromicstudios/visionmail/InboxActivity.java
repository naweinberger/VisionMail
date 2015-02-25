package com.palindromicstudios.visionmail;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.telephony.SmsMessage;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.palindromicstudios.testapplication.R;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import javax.security.auth.login.LoginException;

/**
 * Created by Natan on 2/24/2015.
 */
public class InboxActivity extends Activity {
    HashMap<Integer, ArrayList<Message>> conversations;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inbox);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        List<String> old = new ArrayList<String>();
        conversations = new HashMap<Integer, ArrayList<Message>>();
        List<Integer> keys = new ArrayList<Integer>();

        Cursor cursor = getContentResolver().query(Uri.parse("content://sms"), null, null, null, null);

        if (cursor.moveToFirst()) { // must check the result to prevent exception
            do {
                String msgData = "";
                Integer key = 0;
                HashMap<String, String> messageInfo = new HashMap<String, String>();
                Message message = null;
                message = new Message();
                message.setContent(cursor.getString(cursor.getColumnIndexOrThrow("body")));
                message.setPerson(cursor.getInt(cursor.getColumnIndexOrThrow("person")));
                message.setPhone(cursor.getString(cursor.getColumnIndexOrThrow("address")));
                message.setDate(getDate(cursor.getLong(cursor.getColumnIndexOrThrow("date"))));
                message.setType(cursor.getInt(cursor.getColumnIndexOrThrow("type")));
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
                conversationPreviews.add(msg);
            }
            ConversationsAdapter adapter = new ConversationsAdapter(this, conversationPreviews);
            recyclerView.setAdapter(adapter);


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
}
