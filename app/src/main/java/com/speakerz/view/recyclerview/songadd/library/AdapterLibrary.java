package com.speakerz.view.recyclerview.songadd.library;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.speakerz.R;

import java.util.ArrayList;

public class AdapterLibrary extends RecyclerView.Adapter<AdapterLibrary.ViewHolderLibrary> {
    Context contextLibrary;
    ArrayList<libraryItem> listDevice;

    public AdapterLibrary(Context mContext, ArrayList<libraryItem> mList){
        contextLibrary = mContext;
        listDevice = mList;
    }

    @NonNull
    @Override
    public ViewHolderLibrary onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(contextLibrary).inflate(R.layout.item_song_import, parent, false);
        return new ViewHolderLibrary(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolderLibrary holder, int position) {
        libraryItem currentItem = listDevice.get(position);

        String imageURL = currentItem.getCoverImagePath();
        String songName = currentItem.getSongName();
        String artist = currentItem.getArtist();
        String songLengthTime = currentItem.getSongLengthTime();

        holder.songNameTextView.setText(songName);
        holder.songArtistTextView.setText(artist);
        holder.songLengthTimeTextView.setText(songLengthTime);
        holder.coverImageView.setImageResource(R.mipmap.ic_launcher_round);

    }

    @Override
    public int getItemCount() {
        return listDevice.size();
    }

    public class ViewHolderLibrary extends RecyclerView.ViewHolder{
        public ImageView coverImageView;
        public TextView songNameTextView;
        public TextView songArtistTextView;
        public TextView songLengthTimeTextView;

        public ViewHolderLibrary(@NonNull View itemView) {
            super(itemView);
            coverImageView = itemView.findViewById(R.id.coverImageView);
            songNameTextView = itemView.findViewById(R.id.songNameTextView);
            songArtistTextView = itemView.findViewById(R.id.songArtistTextView);
            songLengthTimeTextView = itemView.findViewById(R.id.songLengthTimeTextView);
        }
    }
}
