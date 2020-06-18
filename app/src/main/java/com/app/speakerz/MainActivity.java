package com.app.speakerz;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.app.speakerz.App.App;
import com.app.speakerz.debug.D;
import com.app.speakerz.model.event.CommonModel_ViewEventHandler;
import com.example.speakerz.R;
/**REQUIRED means: it needs to be in every Activity.*/
public class MainActivity extends AppCompatActivity{
    //REQUIRED_BEG MODEL
    CommonModel_ViewEventHandler viewEventHandler;
    private void onCreateInit(){

        //Read the Textfield values from the Textstorage
        setTextValuesFromStorage();
        App.setWifiManager(((WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE)));
        App.setP2pWifiManager(((WifiP2pManager)getApplicationContext().getSystemService(Context.WIFI_P2P_SERVICE)));
        App.setChannel(this,getMainLooper(),null);
        App.initIntentFilter();
        //ez előtt a wifip2p és channel init kell
        App.initWifiBroadCastReciever();
        //Model létrehozás előtt beállítom a a wifistatus szövegét.

    }

    private void initModelAfterDecision(){
        initEventListener();
        App.addUpdateEventListener(viewEventHandler);
    }
    void initEventListener() {
        viewEventHandler = new CommonModel_ViewEventHandler(this);
    }
    void setTextValuesFromStorage(){
      //  ((TextView)findViewById(R.id.wifi_status)).setText(new String(App.getTextFromStorage(R.id.wifi_status)));
        //auto configure textfields (if there is an view component id present from the layout present in the textStorage, the StorageModule automatically sets the fields )
        App.autoConfigureTexts(this);
    }
    //REQUIRED_END MODEL


    @Override
    protected void onResume() {
        super.onResume();
        setTextValuesFromStorage();
        //a bánat tudja, hogy ez mit csinál, de kell
        registerReceiver(App.getWifiBroadcastReciever(),App.getIntentFilter());
        D.log("main_onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(App.getWifiBroadcastReciever());
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        //set the viewEventhandler to handle events from model



        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //REQUIRED_BEGIN MODEL
        onCreateInit();
        //REQUIRED_END MODEL

        D.log("oncreate_main");

        Button buttonJoin = (Button) findViewById(R.id.join);
        buttonJoin.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
               // App.initModel(false);
               // initModelAfterDecision();
                Intent Act2 = new Intent(getApplicationContext(),Join.class);
                //TODO: Set model for activity Join
                Act2.putExtra("Hello","Hello World");
                startActivity(Act2);


            }

        });

        Button buttonCreate = (Button) findViewById(R.id.create);
        buttonCreate.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
               // App.initModel(false);
              //  initModelAfterDecision();
                Intent Act2 = new Intent(getApplicationContext(),Create.class);
                //TODO: Set model for activity Create

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
