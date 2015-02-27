package com.palindromicstudios.visionmail.activities;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.MultiAutoCompleteTextView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.palindromicstudios.testapplication.R;
import com.palindromicstudios.visionmail.fragments.InboxFragment;
import com.palindromicstudios.visionmail.fragments.MessageThreadFragment;

public class MainActivity extends ActionBarActivity implements SurfaceHolder.Callback{

    Camera mCamera;
    SurfaceView surfaceView;
    SurfaceHolder surfaceHolder;
    boolean isPreviewing = false, cameraActive = true;
    LayoutInflater controlInflater = null;
    TextView textView;
    FrameLayout overlayBackground;
    Spinner from;
    static EditText subject, body;
    static MultiAutoCompleteTextView to;
    int counter = 0, cameraCounter = 0;
    ArrayList<Map<String, String>> mPeopleList;
    FrameLayout container;

    String selectedNumber = "";

    float currentAlpha;

    final String prefs_tag = "VisionMail";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("VisionMail");

        getWindow().setFormat(PixelFormat.UNKNOWN);
        surfaceView = (SurfaceView)findViewById(R.id.surfaceview);

        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        getFragmentManager().beginTransaction().add(R.id.container, new InboxFragment(), "inbox").commit();
        container = (FrameLayout) findViewById(R.id.container);

        currentAlpha = getSharedPreferences(prefs_tag, 0).getFloat("alpha_value", 0.8f);
        setAlpha(currentAlpha);

        mPeopleList = new ArrayList<Map<String, String>>();

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




    public void setAlpha(float alpha) {
        container.setAlpha(alpha);
    }

    public void replaceFragment(Fragment fragment) {
        replaceFragment(fragment, false);
    }
    public void replaceFragment(Fragment fragment, boolean addToBackstack) {
        if (addToBackstack) {
            getFragmentManager().beginTransaction().add(R.id.container, fragment).addToBackStack(null).commit();
        }
        else {
            getFragmentManager().beginTransaction().add(R.id.container, fragment).commit();
        }
    }

    public void showMessageThread(Fragment fragment) {
        getFragmentManager().beginTransaction().hide(getFragmentManager().findFragmentByTag("inbox")).commit();
        getFragmentManager().beginTransaction().add(R.id.container, fragment, "thread").addToBackStack(null).commit();
    }

    public void returnToInbox() {
        getFragmentManager().beginTransaction().remove(getFragmentManager().findFragmentByTag("thread")).commit();
        getFragmentManager().beginTransaction().show(getFragmentManager().findFragmentByTag("inbox")).commit();
        ((InboxFragment)getFragmentManager().findFragmentByTag("inbox")).refresh();
    }

    public void refresh() {
        if (getFragmentManager().findFragmentByTag("thread").isVisible()) {
            ((MessageThreadFragment)getFragmentManager().findFragmentByTag("inbox")).refresh();
        }
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
    public void onBackPressed() {
        if (getFragmentManager().findFragmentByTag("thread") != null && getFragmentManager().findFragmentByTag("thread").isVisible()) {
           //In this case, the edittext has focus and we want to go back to the inbox without popping the backstack, as it is already popped in the returnToInbox() method
           if (!((MessageThreadFragment)getFragmentManager().findFragmentByTag("thread")).isKeyboardShowing()) {
               returnToInbox();
           }
           //This will hide the keyboard in the message thread
            else {
               super.onBackPressed();
           }
        }

        //Not in a message thread
        else {
            super.onBackPressed();
        }
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
        try {
            mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
        } catch (Exception e) {
            startActivity(new Intent(MainActivity.this, MainActivity.class));
            finish();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        try {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
            isPreviewing = false;
        } catch (NullPointerException e) {
            Log.d("VisionMail", "Error destroying camera surface: " + e.toString());
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.alpha:
                Dialog yourDialog = new Dialog(MainActivity.this);
                LayoutInflater inflater = (LayoutInflater)MainActivity.this.getSystemService(LAYOUT_INFLATER_SERVICE);
                View layout = inflater.inflate(R.layout.dialog_seekbar, (ViewGroup)findViewById(R.id.dialog_container));
                yourDialog.setTitle("Adjust transparency");
                yourDialog.setContentView(layout);

                SeekBar seekBar = (SeekBar)layout.findViewById(R.id.seekbar);

                seekBar.setMax(100);
                seekBar.setProgress((int)(currentAlpha*100));

                SeekBar.OnSeekBarChangeListener yourSeekBarListener = new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        //add code here
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                        //add code here
                    }

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        float trueProgress= ((float)progress)/100;
                        setAlpha(trueProgress);
                        currentAlpha = trueProgress;


                    }
                };
                seekBar.setOnSeekBarChangeListener(yourSeekBarListener);

                yourDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        final SharedPreferences.Editor editor = getSharedPreferences(prefs_tag, 0).edit();
                        editor.putFloat("alpha_value", currentAlpha);
                        editor.commit();
                    }
                });
                yourDialog.show();
                break;

        }
        return super.onOptionsItemSelected(item);
    }

}