package com.speakerz;

import android.app.Activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.speakerz.debug.D;
import com.speakerz.model.HostModel;
import com.speakerz.model.enums.EVT;
import com.speakerz.model.event.SongItemEventArgs;
import com.speakerz.model.network.HostNetwork;
import com.speakerz.model.network.event.BooleanEventArgs;
import com.speakerz.model.network.event.TextChangedEventArgs;
import com.speakerz.model.network.event.WirelessStatusChangedEventArgs;
import com.speakerz.util.Event;
import com.speakerz.util.EventArgs;
import com.speakerz.util.EventListener;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;

public class Create extends Activity {
    //REQUIRED_BEG MODEL_Declare
    SpeakerzService _service;
    boolean _isBounded;
    boolean _isRegisterRecieverConnected=false;
    ListView lvSongsList;
    ArrayAdapter<String> songListAdapter=null;
    private void subscribeModel(final HostModel model) {
        final Create selfActivity = this;
        songListAdapter=new ArrayAdapter<>(selfActivity.getApplicationContext(), android.R.layout.simple_list_item_1,_service.getModel().getSongList());
        lvSongsList.setAdapter(songListAdapter);

        //Basemodel Events
        _service.getModel().SongListChangedEvent.addListener(new EventListener<SongItemEventArgs>() {
            @Override
            public void action(final SongItemEventArgs args) {
                if(songListAdapter!=null) {
                    //must run on Ui thread:
                    
                    Runnable run=new Runnable() {
                        @Override
                        public void run() {
                            _service.getModel().getSongList().add(args.getSongRequestObject().getTitle()+" "+args.getSongRequestObject().getSender());
                            songListAdapter.notifyDataSetChanged();
                            D.log("dataset updated.");
                            D.log("empty?"+_service.getModel().getSongList().isEmpty());
                        }
                    };
                    RunnableFuture<Void> task = new FutureTask<>(run, null);
                    selfActivity.runOnUiThread(task);
                    try {
                        task.get(); // this will block until Runnable completes
                    } catch (InterruptedException | ExecutionException e) {
                        D.log("UiRefresh exception. "+e.getMessage());
                        // handle exception
                    }
                    D.log("Data recieved.", _service.getModel().getSongList().toString());
                }
            }
        });

        // Wireless changed event
        model.getNetwork().getReciever().WirelessStatusChanged.addListener(new EventListener<WirelessStatusChangedEventArgs>() {
            @Override
            public void action(WirelessStatusChangedEventArgs args) {
                _service.getTextValueStorage().autoConfigureTexts(selfActivity);
            }
        });


        model.getNetwork().TextChanged.addListener(new EventListener<TextChangedEventArgs>() {
            @Override
            public void action(TextChangedEventArgs args) {
                if(args.event()==EVT.update_discovery_status){
                _service.getTextValueStorage().setTextValue(R.id.discover_status, args.text());
                _service.getTextValueStorage().autoConfigureTexts(selfActivity);
                }
                if(args.event()==EVT.h_service_created){
                    _service.getTextValueStorage().setTextValue(R.id.h_service_status, args.text());
                    _service.getTextValueStorage().autoConfigureTexts(selfActivity);
                }
            }
        });
        model.getNetwork().GroupConnectionChangedEvent.addListener(new EventListener<BooleanEventArgs>() {
            @Override
            public void action(BooleanEventArgs args) {
                if (args.event() == EVT.host_group_creation) {
                    if (args.getValue())
                        ((TextView) findViewById(R.id.h_group_status)).setText("Group created");
                    else {
                        ((TextView) findViewById(R.id.h_group_status)).setText("Group creation Failed");
                    }
                }

            }
        });

    }

    private void initAndStart() {
        lvSongsList=(ListView) findViewById(R.id.lv_song_list_test);

        subscribeModel((HostModel) _service.getModel());
        _service.getTextValueStorage().autoConfigureTexts(this);
        _service.getModel().start();

        registerReceiver(_service.getModel().getNetwork().getReciever(), _service.getModel().getNetwork().getIntentFilter());
        _isRegisterRecieverConnected=true;
    }
    //REQUIRED_END MODEL_Declare

    Create selfActivity = this;
    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder binder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            SpeakerzService.LocalBinder localBinder = (SpeakerzService.LocalBinder) binder;
            _service = localBinder.getService();
            _isBounded = true;

            selfActivity.initAndStart();
            _service.ModelReadyEvent.addListener(new EventListener<BooleanEventArgs>() {
                @Override
                public void action(BooleanEventArgs args) {
                    D.log("create: initAndStart");
                    if(args.getValue())
                    {

                    }

                }
            });
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

    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService(connection);
        _isBounded = false;
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (_service != null)
            _service.getTextValueStorage().autoConfigureTexts(this);
        //a bánat tudja, hogy ez mit csinál, de kell
        if (_service != null) {
            if(!_isRegisterRecieverConnected) {
                registerReceiver(_service.getModel().getNetwork().getReciever(), _service.getModel().getNetwork().getIntentFilter());
                _isRegisterRecieverConnected=true;
            }
        }
        //D.log("main_onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (_service != null){
                if(_isRegisterRecieverConnected )
                { unregisterReceiver((_service.getModel().getNetwork().getReciever()));}
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);

        Button buttonBack = (Button) findViewById(R.id.back);
        buttonBack.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                // Intent Act2 = new Intent(getApplicationContext(),MainActivity.class);
                //  Act2.putExtra("Hello","Hello World");
                //  startActivity(Act2);
                finish();

            }

        });

        Button buttonMusicPlayer = (Button) findViewById(R.id.Musicplayer);
        buttonMusicPlayer.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent Act2 = new Intent(getApplicationContext(), MusicPlayer.class);
                Act2.putExtra("Hello", "Hello World");
                startActivity(Act2);

            }

        });

        Button startSession = (Button) findViewById(R.id.btn_start_session);
        startSession.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                ((HostModel) (_service.getModel())).startAdvertising();

            }

        });

    }
}