/**TO DO:**
 * Színeket rendberakni
 *
 * Szebb törlés gomb
 * Sötétíteni a hátteret

 */


package com.speakerz.view;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.MenuItem;
import com.speakerz.R;
import com.speakerz.SpeakerzService;
import com.speakerz.model.MusicPlayerModel;
import com.speakerz.model.network.Serializable.body.Body;
import com.speakerz.model.network.Serializable.body.controller.PutNameChangeRequestBody;
import com.speakerz.model.network.Serializable.body.controller.content.NameItem;
import com.speakerz.model.network.Serializable.enums.TYPE;
import com.speakerz.util.EventArgs1;
import com.speakerz.util.EventArgs2;
import com.speakerz.view.components.BottomMusicPlayer;
import com.speakerz.view.components.NameChangeDialog;
import com.speakerz.view.components.TopMenu;
import com.speakerz.view.recyclerview.RecyclerView_FAB;

import java.util.ArrayList;

public class PlayerRecyclerActivity extends AppCompatActivity implements NameChangeDialog.NameChangeDialogListener{
    RecyclerView_FAB recyclerViewFab;
    TopMenu menu;

    public MusicPlayerModel getModel() {
        return model;
    }

    BottomMusicPlayer bottomPlayer;
    AppCompatActivity self=this;


    MusicPlayerModel model = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Initialize View
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recycler_view);

        // Configure toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // Configure RecyclerView
        recyclerViewFab=new RecyclerView_FAB(self);


        // Configure menu
        menu=new TopMenu(self);

        // Configure BottomPlayer
        bottomPlayer = new BottomMusicPlayer(self);
    }

    private ServiceConnection srvConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder binder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            SpeakerzService.LocalBinder localBinder = (SpeakerzService.LocalBinder) binder;

            // Bind model
            model = localBinder.getService().getModel().getMusicPlayerModel();

            // Register model event handlers
            bottomPlayer.initModel(model);
            recyclerViewFab.initModel(model);

            menu.setModel(model.getModel());
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            bottomPlayer.releaseModel();
            recyclerViewFab.releaseModel();

            model = null;
        }
    };

    @Override
    protected void onStop() {
        super.onStop();
        // Bind to LocalService
        unbindService(srvConn);
    }

    protected void onStart() {
        super.onStart();

        // Bind to LocalService
        Intent intent = new Intent(this, SpeakerzService.class);
        bindService(intent, srvConn, Context.BIND_AUTO_CREATE);
    }

    // David's stuff
    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
       return this.menu.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return this.menu.onOptionsItemSelected(item);
    }


    @Override
    public void applyTexts(String username) {
        System.out.println(username+"outside");
        NameItem item = new NameItem(username,"en",model.getModel().deviceID);
        PutNameChangeRequestBody body1 = new PutNameChangeRequestBody(null,item);
        model.getModel().NameChangeEvent.invoke(new EventArgs2<Body, TYPE>(null,body1,TYPE.NAME));
    }
}