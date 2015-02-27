package com.palindromicstudios.visionmail;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;

/**
 * Created by Natan on 2/26/2015.
 */
public class MmsReceiver extends BroadcastReceiver {

    final String MESSAGE_RECEIVED = "message_received";

    public void onReceive(Context context, Intent intent)
    {
        Bundle bundle = intent.getExtras();

        context.sendBroadcast(new Intent(MESSAGE_RECEIVED));

        Object[] messages = (Object[]) bundle.get("pdus");
        SmsMessage[] sms = new SmsMessage[messages.length];

        for (int n=0; n < messages.length; n++){
            sms[n] = SmsMessage.createFromPdu((byte[]) messages[n]);
        }

        for (SmsMessage msg : sms) {

//            MainActivity.refresh("\nFrom: " + msg.getOriginatingAddress() + "\n" +
//                    "Message: " + msg.getMessageBody() + "\n");
        }
    }
}