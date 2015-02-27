package com.palindromicstudios.visionmail;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Telephony;
import android.support.v4.app.NotificationCompat;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.text.TextUtils;
import android.widget.Toast;

import com.palindromicstudios.testapplication.R;
import com.palindromicstudios.visionmail.activities.MainActivity;

/**
 * Created by Natan on 2/26/2015.
 */
public class SmsReceiver extends BroadcastReceiver {

    Context context;
    final String MESSAGE_RECEIVED = "message_received";

    public void onReceive(Context context, Intent intent)
    {
        this.context = context;
        Bundle bundle = intent.getExtras();

        String phone = "";
        String body = "";

        Object[] messages = (Object[]) bundle.get("pdus");
        SmsMessage[] sms = new SmsMessage[messages.length];

        for (int n=0; n < messages.length; n++){
            sms[n] = SmsMessage.createFromPdu((byte[]) messages[n]);
        }

        for (SmsMessage msg : sms) {

            ContentValues values = new ContentValues();
            values.put("address", msg.getDisplayOriginatingAddress());//sender name
            phone = msg.getDisplayOriginatingAddress();
            body += msg.getDisplayMessageBody();
            values.put("body", msg.getDisplayMessageBody());
            values.put("type", 1);
            values.put("date", msg.getTimestampMillis());
            values.put("thread_id", getThreadId(msg.getDisplayOriginatingAddress()));
            values.put("read", 0);
            context.getContentResolver().insert(Uri.parse("content://sms/inbox"), values);

//            MainActivity.refresh("\nFrom: " + msg.getOriginatingAddress() + "\n" +
//                    "Message: " + msg.getMessageBody() + "\n");


        }

        Intent launchIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, launchIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .addAction(R.drawable.ic_launcher, "Launch", pendingIntent)
                        .setContentTitle(getContactName(context, phone))
                        .setContentText(body);

        // Create Notification Manager
        NotificationManager notificationmanager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        // Build Notification with Notification Manager
        notificationmanager.notify(0, mBuilder.build());


        context.sendBroadcast(new Intent(MESSAGE_RECEIVED));

    }
    private Long getThreadId(String phone) {

        Cursor cursor = context.getContentResolver().query(Uri.parse("content://sms"), new String[]{"thread_id"},
                "address=?", new String[]{phone}, null);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    return cursor.getLong(0);
                }
            } finally {
                cursor.close();
            }
        }
        return null;
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


        if (name.isEmpty()) {
            return phoneNumber;
        }
        return name;
    }
}