package com.palindromicstudios.visionmail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.MultiAutoCompleteTextView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.palindromicstudios.testapplication.R;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

public class MyActivity extends Activity implements SurfaceHolder.Callback{

    Camera mCamera;
    SurfaceView surfaceView;
    SurfaceHolder surfaceHolder;
    boolean isPreviewing = false, cameraActive = true;
    LayoutInflater controlInflater = null;
    TextView textView;
    LinearLayout overlayBackground;
    Spinner from;
    static EditText subject, body;
    static MultiAutoCompleteTextView to;
    int counter = 0, cameraCounter = 0;
    ArrayList<Map<String, String>> mPeopleList;

    String selectedNumber = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);

        getWindow().setFormat(PixelFormat.UNKNOWN);
        surfaceView = (SurfaceView)findViewById(R.id.surfaceview);

        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);



        controlInflater = LayoutInflater.from(getBaseContext());
        View viewControl = controlInflater.inflate(R.layout.overlay, null);
        LayoutParams layoutParamsControl
                = new LayoutParams(LayoutParams.FILL_PARENT,
                LayoutParams.FILL_PARENT);
//        textView = (TextView) viewControl.findViewById(R.id.textview);
//        textView.setText("Messages");

        overlayBackground = (LinearLayout) viewControl.findViewById(R.id.overlay_background);
        overlayBackground.getBackground().setAlpha(180);

        mPeopleList = new ArrayList<Map<String, String>>();

        to = (MultiAutoCompleteTextView) viewControl.findViewById(R.id.email_to_edittext);
        subject = (EditText) viewControl.findViewById(R.id.email_subject_edittext);
        body = (EditText) viewControl.findViewById(R.id.email_body_edittext);

        to.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
        to.setThreshold(1);

        PopulatePeopleList();

        SimpleAdapter mAdapter = new SimpleAdapter(this, mPeopleList, R.layout.contact_item_layout,
                new String[] { "Name", "Phone", "Type" }, new int[] {
                R.id.name, R.id.phone, R.id.type });
        to.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
        to.setAdapter(mAdapter);



        to.setOnItemClickListener(new AdapterView.OnItemClickListener() {


            public void onItemClick(AdapterView<?> av, View arg1, int index,
                                    long arg3) {
                Map<String, String> map = (Map<String, String>) av.getItemAtPosition(index);

                String name  = map.get("Name");
                String number = map.get("Phone");
                //mTxtPhoneNo.setText(""+name+"<"+number+">");
                to.setText(name);
                selectedNumber = map.get("Phone");

            }



        });




        this.addContentView(viewControl, layoutParamsControl);

        surfaceView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (cameraCounter % 3 == 0) {


                    cameraActive = false;

                    if (isPreviewing){
                        mCamera.stopPreview();
                        isPreviewing = false;
                    }

                    if (mCamera != null){
                        try {
                            surfaceDestroyed(surfaceHolder);
                            mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
                            mCamera.setDisplayOrientation(90);
                            mCamera.setPreviewDisplay(surfaceHolder);
                            mCamera.startPreview();
                            isPreviewing = true;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    overlayBackground.getBackground().setAlpha(180);
                }
                else if (cameraCounter % 3 == 2) {
                    cameraActive = true;


                    //Make the background slightly transparent
                    overlayBackground.getBackground().setAlpha(180);
                }

                else {
                    overlayBackground.getBackground().setAlpha(255);

                    //Preload the camera to avoid delay
                    if (isPreviewing){
                        mCamera.stopPreview();
                        isPreviewing = false;
                    }

                    if (mCamera != null){
                        try {
                            surfaceDestroyed(surfaceHolder);
                            mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
                            mCamera.setDisplayOrientation(90);
                            mCamera.setPreviewDisplay(surfaceHolder);
                            mCamera.startPreview();
                            isPreviewing = true;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                cameraCounter++;
                Log.i("COUNTER", cameraCounter + "");
                return false;
            }
        });

    }


    private void sendText() {
        String message = this.body.getText().toString();

        String sent = "SMS_SENT";

        PendingIntent sentPI = PendingIntent.getBroadcast(this, 0,
                new Intent(sent), 0);

        //---when the SMS has been sent---
        registerReceiver(new BroadcastReceiver(){
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                if(getResultCode() == Activity.RESULT_OK)
                {
                    Toast.makeText(getBaseContext(), "SMS sent.",
                            Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(getBaseContext(), "Sending failed.",
                            Toast.LENGTH_SHORT).show();
                }
            }
        }, new IntentFilter(sent));

        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(selectedNumber, null, message, sentPI, null);


    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
    }



    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (isPreviewing){
            mCamera.stopPreview();
            isPreviewing = false;
        }

        if (mCamera != null){
            try {
                mCamera.setDisplayOrientation(90);
                mCamera.setPreviewDisplay(surfaceHolder);
                mCamera.startPreview();
                isPreviewing = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;
        isPreviewing = false;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.my, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.send:
                //if (validateInput()) {
                    Toast.makeText(this, "Sending message", Toast.LENGTH_SHORT).show();
                    //new MessageTask(emails.get(from.getSelectedItemPosition()), to.getText().toString(), subject.getText().toString(), body.getText().toString()).execute();
                    sendText();
                //}
                //else {

                //}
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void PopulatePeopleList() {
        mPeopleList.clear();
        Cursor people = getContentResolver().query(
                ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        while (people.moveToNext()) {
            String contactName = people.getString(people
                    .getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
            String contactId = people.getString(people
                    .getColumnIndex(ContactsContract.Contacts._ID));
            String hasPhone = people
                    .getString(people
                            .getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));

            if ((Integer.parseInt(hasPhone) > 0)){
                // You know have the number so now query it like this
                Cursor phones = getContentResolver().query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = "+ contactId,
                        null, null);
                while (phones.moveToNext()){
                    //store numbers and display a dialog letting the user select which.
                    String phoneNumber = phones.getString(
                            phones.getColumnIndex(
                                    ContactsContract.CommonDataKinds.Phone.NUMBER));
                    String numberType = phones.getString(phones.getColumnIndex(
                            ContactsContract.CommonDataKinds.Phone.TYPE));
                    Map<String, String> NamePhoneType = new HashMap<String, String>();
                    NamePhoneType.put("Name", contactName);
                    NamePhoneType.put("Phone", formatPhoneNumber(phoneNumber));
                    if(numberType.equals("0"))
                        NamePhoneType.put("Type", "Work");
                    else
                    if(numberType.equals("1"))
                        NamePhoneType.put("Type", "Home");
                    else if(numberType.equals("2"))
                        NamePhoneType.put("Type",  "Mobile");
                    else
                        NamePhoneType.put("Type", "Other");
                    //Then add this map to the list.
                    mPeopleList.add(NamePhoneType);
                }
                //phones.close();
            }
        }
        //people.close();
        startManagingCursor(people);
    }

    private String formatPhoneNumber(String number) {
        if (number.substring(0, 2).equals("+1")) number = number.substring(2);
//        number = number.replace("-", "");
//        number = number.replace("(", "");
        number.replaceAll("[-()]", "");

        if (number.length() == 10) {
            return "(" + number.substring(0, 3) + ") " + number.substring(3, 6) + "-" + number.substring(6);
        }
        else return number;
    }


    private class Contact {
        String name, phone;

        public Contact(String name, String phone) {
            this.name = name;
            this.phone = phone;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String toString() {
            return getName() + " (" + getPhone() + ")";
        }
    }

}