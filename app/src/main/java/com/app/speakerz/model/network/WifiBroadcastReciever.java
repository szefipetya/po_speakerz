package com.app.speakerz.model.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pManager;
import android.widget.Toast;

import com.app.speakerz.debug.D;
import com.app.speakerz.model.enums.EVT;
import com.app.speakerz.model.event.EventHandler;
import com.app.speakerz.model.event.UpdateEventManager;
import com.example.speakerz.R;


public class WifiBroadcastReciever extends BroadcastReceiver {
    UpdateEventManager updateEventManagerToNetwork;

    public void setWifiP2pManager(WifiP2pManager wifiP2pManager) {
        this.wifiP2pManager = wifiP2pManager;
    }

    private WifiP2pManager wifiP2pManager;
    private WifiP2pManager.Channel channel;
    public void setChannel(WifiP2pManager.Channel channel) {
        this.channel = channel;
    }



    public void setPeerListListener(WifiP2pManager.PeerListListener peerListListener) {
        this.peerListListener = peerListListener;
    }

    private WifiP2pManager.PeerListListener peerListListener;

    public WifiBroadcastReciever(WifiP2pManager manager, WifiP2pManager.Channel channel){
        this.wifiP2pManager=manager;
        this.channel=channel;
        updateEventManagerToNetwork =new UpdateEventManager();
    }

    public void addEventHandlerToUpdateManager(EventHandler evt){
        updateEventManagerToNetwork.addListener(evt);
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        String action=intent.getAction();
        if(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)){
            int state=intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE,-1);
            if(state==WifiP2pManager.WIFI_P2P_STATE_ENABLED){

                updateEventManagerToNetwork.updateAll(EVT.updateText, R.id.wifi_status,"wifi is on");
                Toast.makeText(context,"Wifi is on", Toast.LENGTH_SHORT).show();
            }
            else {
                updateEventManagerToNetwork.updateAll(EVT.updateText, R.id.wifi_status,"wifi is off");
                Toast.makeText(context,"Wifi is off", Toast.LENGTH_SHORT).show();
            }
        }
       else if(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)){
           if(wifiP2pManager!=null){
                wifiP2pManager.requestPeers(channel,peerListListener);
           }else D.log("err: wifip2pmanager was null");
           D.log("Peers changed action");
        }
       else if(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)){

        }
        else if(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)){

        }
    }


}