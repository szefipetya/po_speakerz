package com.speakerz;

import androidx.appcompat.app.AlertDialog;
import android.app.Activity;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.StrictMode;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import com.speakerz.SpeakerzService.LocalBinder;
import com.speakerz.debug.D;
import com.speakerz.model.DeviceModel;
import com.speakerz.model.HostModel;
import com.speakerz.model.event.CommonModel_ViewEventHandler;
import com.speakerz.R;

import org.json.JSONException;

/**REQUIRED means: it needs to be in every Activity.*/
public class MainActivity extends Activity {
    //REQUIRED_BEG MODEL
    SpeakerzService _service;
    boolean _isBounded;

    CommonModel_ViewEventHandler viewEventHandler;

    //REQUIRED_END MODEL


    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder binder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            LocalBinder localBinder = (LocalBinder) binder;
            _service =  localBinder.getService();
            _isBounded = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            _isBounded = false;
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        // Bind to LocalService
        Intent intent = new Intent(this, SpeakerzService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
        if(_service!=null)
        _service.getTextValueStorage().autoConfigureTexts(this);
        else{
            //D.log("err: MainActivity : service is null");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService(connection);
        _isBounded = false;
        D.log("main.onStop");
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(_service!=null)
            _service.getTextValueStorage().autoConfigureTexts(this);
        //a bánat tudja, hogy ez mit csinál, de kell

        //D.log("main_onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();

    }
    private void initAndStart(boolean isHost) {
        Intent intent = new Intent(this, SpeakerzService.class);
        intent.putExtra("isHost",isHost);
        this.startService(intent);
    }


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        //set the viewEventhandler to handle events from model
        super.onCreate(savedInstanceState);
        //need the policy to send data below API 28
        checkDataSendingPolicy();


        setContentView(R.layout.activity_main);

        //D.log("oncreate_main");

        Button buttonJoin = (Button) findViewById(R.id.join);
        buttonJoin.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                initAndStart(false);
                Intent Act2 = new Intent(getApplicationContext(),Join.class);
                Act2.putExtra("Hello","Hello World");
                startActivity(Act2);


            }

        });

        Button buttonCreate = (Button) findViewById(R.id.create);
        buttonCreate.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                initAndStart(true);
                Intent Act2 = new Intent(getApplicationContext(),Create.class);
                Act2.putExtra("Hello","Hello World");
                startActivity(Act2);

            }

        });


        Button buttonOptions = (Button) findViewById(R.id.options);
        buttonOptions.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                Intent Act2 = new Intent(getApplicationContext(),Options.class);
                Act2.putExtra("Hello","Hello World");
                startActivity(Act2);

            }

        });

        statusCheck();
        //ez Android 8.0 felett kell
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    1001);
            //After this point you wait for callback in onRequestPermissionsResult(int, String[], int[]) overriden method

        }else{
           // getScanningResults();
            //do something, permission was previously granted; or legacy device
        }

    }

    private void checkDataSendingPolicy() {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
    }


    //ujproba
    public void statusCheck() {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();

        }
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }





}