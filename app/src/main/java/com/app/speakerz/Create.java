package com.app.speakerz;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.app.speakerz.App.App;
import com.app.speakerz.model.DeviceModel;
import com.app.speakerz.model.HostModel;
import com.app.speakerz.model.event.CommonViewEventHandler;
import com.example.speakerz.R;

public class Create extends AppCompatActivity {
    //REQUIRED_BEG MODEL_Declare
    CommonViewEventHandler viewEventHandler;
    void initEventListener(){
        viewEventHandler=new CommonViewEventHandler(this);
    }
    void setTextValuesFromStorage(){
        ((TextView)findViewById(R.id.wifi_status)).setText(new String(App.getTextFromStorage(R.id.wifi_status)));
    }
    //REQUIRED_END MODEL_Declare

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);
        //REQUIRED_BEG MODEL
        initEventListener();
        viewEventHandler.toast("onCreate_Create");
        App.addUpdateEventListener(viewEventHandler);
        setTextValuesFromStorage();
        //REQUIRED_END MODEL

        Button buttonBack = (Button) findViewById(R.id.back);
        buttonBack.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
               // Intent Act2 = new Intent(getApplicationContext(),MainActivity.class);
              //  Act2.putExtra("Hello","Hello World");
              //  startActivity(Act2);
                finish();

            }

        });

        Button buttonMusicPlayer = (Button) findViewById(R.id.Musicplayer);
        buttonMusicPlayer.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                Intent Act2 = new Intent(getApplicationContext(),MusicPlayer.class);
                Act2.putExtra("Hello","Hello World");
                startActivity(Act2);

            }

        });



    }
}
