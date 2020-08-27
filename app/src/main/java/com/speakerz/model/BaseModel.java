package com.speakerz.model;

import android.content.Context;

import com.speakerz.debug.D;
import com.speakerz.model.enums.MP_EVT;
import com.speakerz.model.network.BaseNetwork;
import com.speakerz.model.network.Serializable.ChannelObject;
import com.speakerz.model.network.Serializable.body.Body;
import com.speakerz.model.network.Serializable.body.controller.GetSongListBody;
import com.speakerz.model.network.Serializable.body.controller.PutNameChangeRequestBody;
import com.speakerz.model.network.Serializable.body.controller.PutSongRequestBody;
import com.speakerz.model.network.Serializable.body.controller.content.SongItem;
import com.speakerz.model.network.Serializable.enums.SUBTYPE;
import com.speakerz.model.network.Serializable.enums.TYPE;
import com.speakerz.model.network.WifiBroadcastReciever;
import com.speakerz.model.network.event.PermissionCheckEventArgs;
import com.speakerz.model.network.event.WirelessStatusChangedEventArgs;
import com.speakerz.util.Event;
import com.speakerz.util.EventArgs;
import com.speakerz.util.EventArgs1;
import com.speakerz.util.EventArgs2;
import com.speakerz.util.EventArgs3;
import com.speakerz.util.EventListener;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;


public abstract class BaseModel {
    public abstract void start();
    public abstract void stop();

    public String NickName = "placeholder";
    public ArrayList<String> NickNames;
    public HashMap<InetAddress,String> NickNames2;

    MusicPlayerModel musicPlayerModel;
    public Event<PermissionCheckEventArgs> PermissionCheckEvent;

    public volatile Event<EventArgs> SongQueueUpdatedEvent=new Event<>();
    public volatile Event<EventArgs1<Body>> MusicPlayerActionEvent=new Event<>();
    public volatile Event<EventArgs1<Body>> MetaInfoReceivedEvent=new Event<>();
    public volatile Event<EventArgs1<Body>> NameChangeEvent=new Event<>();
    public Event<EventArgs1<String>> SongDownloadedEvent=new Event<>();



    public BaseModel(Context context, WifiBroadcastReciever reciever,Boolean isHost, Event<PermissionCheckEventArgs> PermissionCheckEvent){
        this.PermissionCheckEvent=PermissionCheckEvent;
        musicPlayerModel = new MusicPlayerModel(context,this.PermissionCheckEvent);
        musicPlayerModel.setHost(isHost);


        //inject Events to MusicPLayerModel

        musicPlayerModel.SongDownloadedEvent=SongDownloadedEvent;
        musicPlayerModel.MusicPlayerActionEvent=this.MusicPlayerActionEvent;

        musicPlayerModel.subscribeEventsFromModel();
        subscribeToNameChange();


        reciever.WirelessStatusChanged.addListener(new EventListener<WirelessStatusChangedEventArgs>() {
            @Override
            public void action(WirelessStatusChangedEventArgs args) {
            }
        });

    }


    public abstract BaseNetwork getNetwork();

  /*  public List<String> getSongList() {
        return songList;
    }*/

 //   protected List<String> songList=new ArrayList<>();

    public MusicPlayerModel getMusicPlayerModel(){return musicPlayerModel;}

    private Boolean AreUiEventsSubscribed=false;

    public Boolean getAreUiEventsSubscribed() {
        return AreUiEventsSubscribed;
    }

    public void setAreUiEventsSubscribed(Boolean areUiEventsSubscribed) {
        AreUiEventsSubscribed = areUiEventsSubscribed;
    }
    protected abstract void injectNetworkDependencies();

    public void subscribeToNameChange(){




    }


}
