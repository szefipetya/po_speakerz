package com.speakerz.model.network;


import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;

import com.speakerz.debug.D;
import com.speakerz.model.enums.EVT;
import com.speakerz.model.enums.PERM;
import com.speakerz.model.network.event.HostAddressEventArgs;
import com.speakerz.model.network.event.PermissionCheckEventArgs;
import com.speakerz.model.network.event.TextChangedEventArgs;
import com.speakerz.model.network.event.channel.ConnectionUpdatedEventArgs;
import com.speakerz.model.network.threads.ClientControllerSocketThread;
import com.speakerz.model.network.threads.ClientSocketWrapper;
import com.speakerz.util.EventArgs;
import com.speakerz.util.EventListener;

import java.util.ArrayList;
import java.util.List;

public class DeviceNetwork extends BaseNetwork {

ClientSocketWrapper clientSocketWrapper=new ClientSocketWrapper();
    public DeviceNetwork(WifiBroadcastReciever reciever) {
        super(reciever);
        reciever.HostAddressAvailableEvent.addListener(new EventListener<HostAddressEventArgs>() {
            @Override
            public void action(HostAddressEventArgs args) {
                if(!args.isHost()) {
                    clientSocketWrapper.controllerSocket = new ClientControllerSocketThread(args.getAddress());
                    clientSocketWrapper.controllerSocket.start();
                    clientSocketWrapper.controllerSocket.ConnectionUpdatedEvent.addListener(new EventListener<ConnectionUpdatedEventArgs>() {
                        @Override
                        public void action(ConnectionUpdatedEventArgs args) {
                            //a nézet a network ConnectionEventjére van csak feliratkozva.
                            ConnectionUpdatedEvent.invoke(args);
                        }
                    });
                }
            }
        });
    }

    public void discoverPeers() {
        reciever.discoverPeers(new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                TextChanged.invoke(new TextChangedEventArgs(this, EVT.update_discovery_status, "Discovering..."));
            }

            @Override
            public void onFailure(int i) {
                TextChanged.invoke(new TextChangedEventArgs(this, EVT.update_discovery_status, "Discovering init failed..."));
            }
        });

        peers = new ArrayList<>();
        peerListListener = new WifiP2pManager.PeerListListener() {
            @Override
            public void onPeersAvailable(WifiP2pDeviceList peerList) {

                D.log("Peers available");
                //if the saved list is outdated, replace it with the fresh devices

                    //D.log("Peers and not equals" + peerList.getDeviceList().size());
                    peers.clear();
                    peers.addAll(peerList.getDeviceList());

                    deviceNames.clear();
                    devices = new WifiP2pDevice[peerList.getDeviceList().size()];

                    int index = 0;
                    for (WifiP2pDevice device : peerList.getDeviceList()) {
                        deviceNames.add(device.deviceName);
                        devices[index] = device;
                        index++;
                        D.log("device found: " + device.deviceName);
                    }

                    ListChanged.invoke(new EventArgs(this));

                if (peers.size() == 0) {
                    TextChanged.invoke(new TextChangedEventArgs(this, EVT.update_discovery_status, "No devices found"));
                } else {
                    TextChanged.invoke(new TextChangedEventArgs(this, EVT.update_discovery_status, "Found some devices"));
                }
            }
        };
        reciever.setPeerListListener(peerListListener);
    }

    public List<String> getDeviceNames() {
        return deviceNames;
    }

    public ClientSocketWrapper getClientSocketWrapper() {
        return clientSocketWrapper;
    }

    private WifiP2pDevice hostDevice = null;
    private WifiP2pConfig hostConnectionConfig = null;

    /**
     * This function connects a client to a host by giving an index.
     * We can use that index to find the device in the devices list
     * @param i this param descibed the index of the selected device in the deviceList
     */
    public void connect(int i) {
        hostDevice = devices[i];
        hostConnectionConfig = new WifiP2pConfig();
        hostConnectionConfig.deviceAddress = hostDevice.deviceAddress;
        // make sure, this device does not become a groupowner
        hostConnectionConfig.wps.setup = WpsInfo.PBC;
        hostConnectionConfig.groupOwnerIntent=0;

        //send an invoke to the service, to check the FINE_LOCATION access permission
        PermissionCheckEvent.invoke(new PermissionCheckEventArgs(this, PERM.connectionPermission,Manifest.permission.ACCESS_FINE_LOCATION,PackageManager.PERMISSION_GRANTED));



    }
    @SuppressLint("MissingPermission")
    public void connectWithPermissionGranted(){
        hostConnectionConfig.groupOwnerIntent=0;
        reciever.getWifiP2pManager().connect(reciever.getChannel(), hostConnectionConfig, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                TextChanged.invoke(new TextChangedEventArgs(this,EVT.update_host_name,hostDevice.deviceName));
                //D.log("from deviceNetwork: success,  new host: "+hostDevice.deviceName);

            }

            @Override
            public void onFailure(int i) {
                TextChanged.invoke(new TextChangedEventArgs(this,EVT.update_host_name_failed,""));
            }
        });
    }

    public WifiP2pDevice getHostDevice() {
        return hostDevice;
    }
}
