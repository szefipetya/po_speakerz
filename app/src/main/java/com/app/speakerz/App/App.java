package com.app.speakerz.App;

import android.app.Application;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Looper;
import android.view.View;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.app.speakerz.MainActivity;
import com.app.speakerz.model.BaseModel;
import com.app.speakerz.model.DeviceModel;
import com.app.speakerz.model.HostModel;
import com.app.speakerz.model.event.Model_ViewEventHandler;
import com.app.speakerz.model.network.WifiBroadcastReciever;
import com.app.speakerz.viewModel.TextValueStorage;

public class App extends Application {
   private static BaseModel model;
   private static final TextValueStorage textValueStorage = new TextValueStorage();
    private static WifiManager wifiManager;
    private static WifiP2pManager wifiP2pManager;
    private static WifiP2pManager.Channel wifiP2pChannel;
    private static WifiBroadcastReciever reciever;
    public static IntentFilter intentFilter;

    public static void setChannel(MainActivity mainActivity, Looper mainLooper, WifiP2pManager.ChannelListener cl) {
        wifiP2pChannel=wifiP2pManager.initialize(mainActivity,mainLooper,cl);
    }

    public static void jStartDiscovering(AppCompatActivity activity, ListView lvPeersList) {
        if(model!=null){
            ((DeviceModel)model).discoverPeers(activity,lvPeersList);
        }
    }
    public static void hStartAdvertising() {
        if(model!=null){
            ((HostModel)model).startAdvertising();
        }
    }


    public static String[] jGetDeviceNames(){
        return  ((DeviceModel)model).getDeviceNames();
    }
    @Override
    public void onCreate() {
        super.onCreate();
        // Your methods here...
    }
    public static void initModel(boolean isHost){
        if (isHost){
            model=new HostModel();
            ((HostModel)model).init();
        }else {
            model = new DeviceModel();
            ((DeviceModel)model).init();
        }

        model.setTextValueStorageForViewUpdateEventManager(textValueStorage);
        model.setWifiManager(wifiManager);
        model.setWifiP2pManager(wifiP2pManager);
        model.setWifiP2pChannel(wifiP2pChannel);

        model.setIntentFilter(intentFilter);
        model.setWifiBroadcastReciever(reciever);
    }

    public static void initIntentFilter(){
        intentFilter=new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
    }
    public static void initWifiBroadCastReciever(){

       reciever=new WifiBroadcastReciever(wifiP2pManager,wifiP2pChannel);

    }

    public static void setWifiManagerForModel(WifiManager manager){

    }
    public static void startModel(){
        model.start();
    }
    public static void addUpdateEventListener(Model_ViewEventHandler handler){
        model.addUpdateEventListener(handler);

    }

    //auto configurates all the textfields if there is any element exists in the storage
    public static void autoConfigureTexts(AppCompatActivity act){
        textValueStorage.autoConfigureTexts(act);
    }
    //REQUIRED
    // FIXME SETTERS
    public static void setP2pWifiManager(WifiP2pManager manager){
        wifiP2pManager=manager;
    }
    public static void setWifiManager(WifiManager manager){
        wifiManager=manager;
    }

    //FIXME GETTERS
    public static String getTextFromStorage(Integer id){
           return textValueStorage.getTextValue(id);
    }
    public static WifiManager getWifiManager(){
        return wifiManager;
    }
     public static WifiBroadcastReciever getWifiBroadcastReciever(){
        return reciever;
    }
    public static IntentFilter getIntentFilter(){
        return intentFilter;
    }
}