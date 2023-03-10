package com.speakerz.model;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;

public class Song implements Serializable {

    private int id = -1;



    private String data;
    private String title;
    private String album;
    private String artist;

    private int cursorIndex;
    private Long albumId;
    private String owner;
    private String duration;
    byte[] songCoverArt = null;

    public int getCursorIndex() {
        return cursorIndex;
    }

    public Song(int cursorIndex, String data, String title, String album, String artist, String owner, Long albumId) {
        this.cursorIndex=cursorIndex;
        this.data = data;
        this.title = title;
        this.album = album;
        this.artist = artist;
        this.owner = owner;
        this.albumId = albumId;
       duration= "0";
    }

    public void setAlbumId(Long albumId) {
        this.albumId = albumId;
    }


    public Bitmap getSongCoverArt() {
        if(songCoverArt!=null){
            return BitmapFactory.decodeByteArray(songCoverArt, 0, songCoverArt.length);

        }
        else return null;
    }

    public byte[] getSongCoverArtByte(){
        if(songCoverArt!=null) {
            return songCoverArt;
        }
        else return null;
    }

    public void setSongCoverArt(Bitmap songCoverArt1) {
        if(songCoverArt1!=null) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            songCoverArt1.compress(Bitmap.CompressFormat.JPEG, 30, stream);
            this.songCoverArt = stream.toByteArray();
        }
    }

    public Long getAlbumId() {
        return albumId;
    }

    public String getOwner() {
        return owner;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    @Override
    public String toString() {
        return title;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }
}