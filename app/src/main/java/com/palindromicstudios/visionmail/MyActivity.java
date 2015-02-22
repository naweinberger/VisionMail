package com.palindromicstudios.visionmail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Bundle;
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
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
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
    static EditText to, subject, body;
    int counter = 0, cameraCounter = 0;
    ArrayList<String> emails;

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

        from = (Spinner) viewControl.findViewById(R.id.email_from_spinner);
        to = (EditText) viewControl.findViewById(R.id.email_to_edittext);
        subject = (EditText) viewControl.findViewById(R.id.email_subject_edittext);
        body = (EditText) viewControl.findViewById(R.id.email_body_edittext);

        emails = new ArrayList<String>();

        Pattern emailPattern = Patterns.EMAIL_ADDRESS; // API level 8+
        Account[] accounts = AccountManager.get(this).getAccounts();
        for (Account account : accounts) {
            if (emailPattern.matcher(account.name).matches()) {
                String possibleEmail = account.name;
                if (!emails.contains(possibleEmail)) {
                    emails.add(possibleEmail);
                }
            }
        }

        emails.add("natan.weinberger@mail.mcgill.ca");

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, android.R.id.text1, emails);
        from.setAdapter(adapter);

        subject.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (counter % 2 == 1) {
                    to.setText("naweinberger@gmail.com");
                    subject.setText("Secret message");
                    body.setText("This is a secret message from someone.");
                }

                else {
                    to.setText("naweinberger@gmail.com");
                    subject.setText("Anonymous tip");
                    body.setText("There appears to be a breaking-and-entering occuring right now on the 1000 block of Main Street.");
                }
                counter++;
                return false;
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

    private boolean validateInput() {
        if (isValidEmail(to.getText().toString())) {
            if (subject.getText().toString().isEmpty() && body.getText().toString().isEmpty()) {
                Toast.makeText(this, "You haven't entered a message.", Toast.LENGTH_SHORT).show();
                return false;
            }
            else {
                return true;
            }
        }
        else {
            Toast.makeText(this, "Please make sure that email address is valid.", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    public final static boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
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
                if (validateInput()) {
                    Toast.makeText(this, "Sending message", Toast.LENGTH_SHORT).show();
                    new MessageTask(emails.get(from.getSelectedItemPosition()), to.getText().toString(), subject.getText().toString(), body.getText().toString()).execute();
                }
                else {

                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private class MessageTask extends AsyncTask<Void, Void, String> {
        String from, to, subject, body;
        public MessageTask(String from, String to, String subject, String body) {
            this.from = from;
            this.to = to;
            this.subject = subject;
            this.body = body;
        }

        @Override
        protected String doInBackground(Void... params) {
            HttpClient httpclient = new DefaultHttpClient();
            // specify the URL you want to post to
            HttpPost httppost = new HttpPost("http://palindromicstudios.com/mailjet-apiv3-php-simple/mailjetapi.php");
            try {
                // create a list to store HTTP variables and their values
                List nameValuePairs = new ArrayList();
                // add an HTTP variable and value pair
                nameValuePairs.add(new BasicNameValuePair("from", from));
                nameValuePairs.add(new BasicNameValuePair("to", to));
                nameValuePairs.add(new BasicNameValuePair("subject", subject));
                nameValuePairs.add(new BasicNameValuePair("body", body));
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                // send the variable and value, in other words post, to the URL
                HttpResponse response = httpclient.execute(httppost);
                return EntityUtils.toString(response.getEntity());
            } catch (ClientProtocolException e) {
                // process execption
            } catch (IOException e) {
                // process execption
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            Log.e("MyActivity", result);
            if (result.equals("0")) {
                Toast.makeText(getApplicationContext(), "Message sent.", Toast.LENGTH_SHORT).show();
                MyActivity.to.setText("");
                MyActivity.subject.setText("");
                MyActivity.body.setText("");
            }
            else {
                Toast.makeText(getApplicationContext(), "An error occurred.", Toast.LENGTH_SHORT).show();
            }
        }
    }

}