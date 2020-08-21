package com.speakerz.model.network.threads.audio;

import android.content.Context;
import android.media.AudioManager;
import android.media.AudioTrack;

import android.media.MediaMetadataRetriever;

import android.os.Build;
import android.os.Environment;

import com.google.common.collect.ImmutableSet;
import com.speakerz.R;
import com.speakerz.debug.D;
import com.speakerz.model.network.threads.ClientSocketWrapper;
import com.speakerz.model.network.threads.audio.util.AUDIO;
import com.speakerz.model.network.threads.audio.util.AudioDecoderThread;
import com.speakerz.model.network.threads.audio.util.AudioMetaDto;
import com.speakerz.model.network.threads.audio.util.AudioMetaInfo;
import com.speakerz.model.network.threads.audio.util.YouTubeStreamAPI;
import com.speakerz.model.network.threads.audio.util.serializable.AudioPacket;
import com.speakerz.util.EventArgs1;
import com.speakerz.util.EventArgs2;
import com.speakerz.util.EventListener;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


import org.apache.commons.lang3.SerializationUtils;



public class ServerAudioMultiCastSocketThread extends Thread {

    public void playAudioStreamFromLocalStorage(final File file){

        synchronized(locker) {
            decoder.stop();
           // decoder.getAudioTrack().stop();
          //  decoder.getAudioTrack().release();

            D.log("SONG PLAYING");
            songpicked=true;
            currentFile=file;
            locker.notify();
        }
    }
    File currentFile=null;

    public void stopAudioStream() {
        decoder.stop();
    }

    public void pauseAudioStream() {

        decoder.isPlaying=false;
    }

    public void resumeAudioStream() {
        decoder.isPlaying=true;
    }

    public void shutdown() {
        decoder.stop();
        if(recieverSocket!=null)
        recieverSocket.close();
        for(ClientDatagramStruct ds: clients){
            if(ds!=null){
                ds.socket.close();
            }
        }
    }

    private class ClientDatagramStruct {
        public ClientDatagramStruct(DatagramSocket socket, InetAddress address, int port) {
            this.address = address;
            this.clientPort = port;
            this.socket = socket;

        }

        public DatagramSocket socket;
        public InetAddress address;
        public int clientPort;
    }

   // File currentMediaFile;
    private final List<ClientDatagramStruct> clients = Collections.synchronizedList(new LinkedList<ClientDatagramStruct>());
    AudioMetaDto recentAudioMetaDto=null;
    AudioDecoderThread decoder=new AudioDecoderThread();
    YouTubeStreamAPI yt=new YouTubeStreamAPI();


    private void init(){
        subscribeDecoderEvents();
    }


    Boolean songpicked=false;
    final Object locker=new Object();
    public void run() {
        // playWav();
       // currentMediaFile = getFileByResId(R.raw.tobu_wav, "target.wav");
        init();


        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                        while (!recieverSocket.isClosed()) {
                            synchronized(locker) {
                            D.log("waiting for audio pick.");
                            while (!songpicked) {
                                locker.wait();
                            }
                            songpicked = false;
                            D.log("got a new request");

                                D.log("MP3 IS PLAYING");
                                //getFileByResId(R.raw.tobu_wav,"target.wav")
                        }
                            Thread t=new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        decoder.startPlay(currentFile, AUDIO.MP3);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                            t.setPriority(Thread.MAX_PRIORITY);
                            t.start();
                    }

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //  yt.play("TW9d8vYrVFQ");
            }
        });
        t.setPriority(Thread.MAX_PRIORITY);
        t.start();
        Thread t2=new Thread(new Runnable() {
            @Override
            public void run() {
                acceptClients();
            }
        });
        t2.start();
    }

    public void sendAudioMeta(ClientDatagramStruct client) {
        AudioMetaDto dto = decoder.getAudioMeta();
        dto.port = currentClientPort;
        byte[] dtoBytes = null;
        dtoBytes = SerializationUtils.serialize(dto);

        DatagramPacket dtoDp = new DatagramPacket(dtoBytes, dtoBytes.length, client.address,8050);
        try {
            recieverSocket.send(dtoDp);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }



    ///
    private InetAddress address;

    private DatagramSocket recieverSocket;
    private boolean running;
    private byte[] buf = new byte[256];
    private Context context;


    public ServerAudioMultiCastSocketThread() {
        try {
            recieverSocket = new DatagramSocket(8050);
        } catch (SocketException e) {
            e.printStackTrace();
        }

    }

    File getFileByResId(int id, String targetFileName) {
        // D.log(path);
        InputStream fis = context.getResources().openRawResource(id);

        File file = new File(context.getFilesDir(), targetFileName);
        try (OutputStream outputStream = new FileOutputStream(file)) {
            copy(fis, outputStream);
            D.log("file readed");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            // handle exception here
        } catch (IOException e) {
            e.printStackTrace();
            // handle exception here
        }
        return file;
    }

    void copy(InputStream source, OutputStream target) throws IOException {
        byte[] buf = new byte[8192];
        int length;
        while ((length = source.read(buf)) > 0) {
            target.write(buf, 0, length);
        }
    }

    int currentClientPort = 8100;

    public void acceptClients() {

        while (!recieverSocket.isClosed()) {
            D.log("acccepting UDP clients...");
            DatagramPacket packet
                    = new DatagramPacket(buf, buf.length);
            try {
                recieverSocket.receive(packet);
                D.log("recieved (server)"+packet.getAddress().getHostAddress()+ " "+packet.getPort());
            } catch (IOException e) {
                e.printStackTrace();
            }
            ClientDatagramStruct newClient = null;
            try {
                //on the server side the client gets a designated port number.
                currentClientPort++;
                newClient = new ClientDatagramStruct(new DatagramSocket(currentClientPort), packet.getAddress(), currentClientPort);
            } catch (SocketException e) {
                e.printStackTrace();
            }

            //the new clients recieve the meta info from the server
            //and their designated port number to listen on for the audio data
            /*try {
                newClient.socket.setSoTimeout(4000);
            } catch (SocketException e) {
                e.printStackTrace();
            }*/
            clients.add(newClient);
            if(recentAudioMetaDto!=null)
            synchronized (clients) {
                D.log("client added");

                sendAudioMeta(newClient);
                waitForClientResponse(newClient);
            }


        }

    }

    private void waitForClientResponse(ClientDatagramStruct newClient) {
        D.log("acccepting UDP clients...");
        DatagramPacket packet
                = new DatagramPacket(buf, buf.length);
        try {
            newClient.socket.receive(packet);
            InetAddress address = packet.getAddress();
            int port = packet.getPort();
            D.log("recieved" + address.getHostAddress() + " :" + port);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void subscribeDecoderEvents(){
        decoder.MetaDtoReadyEvent.addListener(new EventListener<EventArgs1<AudioMetaDto>>() {
            @Override
            public void action(EventArgs1<AudioMetaDto> args) {
                synchronized (clients){
                    recentAudioMetaDto = args.arg1();
                    //sending tha packet to all the clients
                    Iterator it = clients.iterator();
                    while (it.hasNext()) {
                        ClientDatagramStruct tmpClient = (ClientDatagramStruct) it.next();
                        sendAudioMeta(tmpClient);
                        waitForClientResponse(tmpClient);
                        //  D.log("Packet:" + i + " sent to" + tmpClient.address.getHostAddress() + ":" + tmpClient.clientPort);
                    }
                }

                }

        });
        decoder.AudioTrackBufferUpdateEvent.addListener(new EventListener<EventArgs1<AudioPacket>>() {
            @Override
            public void action(EventArgs1<AudioPacket> args) {
                synchronized (clients) {
                    //sending tha packet to all the clients
                    Iterator it = clients.iterator();
                    DatagramPacket dp=null;
                    while (it.hasNext()) {
                        ClientDatagramStruct tmpClient = (ClientDatagramStruct) it.next();
                       byte[] data= SerializationUtils.serialize(args.arg1());
                        dp = new DatagramPacket(data, data.length, tmpClient.address, tmpClient.clientPort);
                        try {
                            if(!tmpClient.socket.isClosed())
                            tmpClient.socket.send(dp);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
    }

//GETTER & SETTER
    public void setAddress(InetAddress address) {
        this.address = address;
    }

    public InetAddress getAddress() {
        return address;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public Context getContext() {
        return context;
    }


}
  /* while (running) {
            DatagramPacket packet
                    = new DatagramPacket(buf, buf.length);
            try {
                socket.receive(packet);
                D.log("recieved (server)");
            } catch (IOException e) {
                e.printStackTrace();
            }

            InetAddress address = packet.getAddress();
            int port = packet.getPort();
            packet = new DatagramPacket(buf, buf.length, address, port);
            String received
                    = new String(packet.getData(), 0, packet.getLength());

            if (received.equals("end")) {
                running = false;
                continue;
            }
            try {

                socket.send(packet);
                D.log("sent (server)");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        socket.close();*/