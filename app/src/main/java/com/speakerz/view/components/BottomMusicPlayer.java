package com.speakerz.view.components;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.speakerz.R;
import com.speakerz.debug.D;
import com.speakerz.model.MusicPlayerModel;
import com.speakerz.model.Song;
import com.speakerz.util.EventArgs;
import com.speakerz.util.EventArgs1;
import com.speakerz.util.EventArgs2;
import com.speakerz.util.EventListener;
import com.speakerz.view.ExtendedPlayerActivity;

public class BottomMusicPlayer {
    AppCompatActivity activity;
    ImageView albumArt;
    TextView titleSongTV;
    TextView detailsTV;
    ImageButton playButton;
    SeekBar seekBar;
    MusicPlayerModel mpModel = null;

    // Event listeners
    final EventListener<EventArgs2<Integer, Integer>> playbackDurationChanged = new EventListener<EventArgs2<Integer, Integer>>() {
        @Override
        public void action(EventArgs2<Integer, Integer> args) {
            Integer current = args.arg1();
            Integer total = args.arg2();

            if(total != null)seekBar.setMax(total);
            if(current != null)seekBar.setProgress(current);
        }
    };


    final EventListener<EventArgs1<Boolean>> playbackStateChangedListener = new EventListener<EventArgs1<Boolean>>() {
        @Override
        public void action(EventArgs1<Boolean> args) {
            if(args.arg1()){
                // TODO configure seekbar when media is started

                D.log("state " + args.arg1() );
            }
            final boolean _isPlaying = args.arg1();
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setPlayIcon(_isPlaying);
                }
            });
        }
    };

    final EventListener<EventArgs1<Song>> songChangedListener = new EventListener<EventArgs1<Song>>() {
        @Override
        public void action(EventArgs1<Song> args) {
            final Song song = args.arg1();
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    titleSongTV.setText(song.getTitle());
                    detailsTV.setText(song.getArtist());
                    //titleSongTV.setText(song.getTitle().length()>25? song.getTitle().substring(0,25)+"...":song.getTitle());
                   // detailsTV.setText(song.getArtist().length()>28?song.getArtist().substring(0,28)+"...":song.getArtist());
                            if(song.getSongCoverArt()!=null)
                                   albumArt.setImageBitmap(song.getSongCoverArt());
                            else{
                                albumArt.setImageResource(R.drawable.ic_twotone_music_note_24);
                            }
                }
            });
        }
    };


    final SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            try{
                if(mpModel == null) return;
                if(fromUser) {
                    // TODO SEEK
                }
                else{
                    seekBar.setProgress(progress);
                }
            }
            catch (Exception e) {}
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {}

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {}
    };

    View.OnClickListener openExtendedPlayer=(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent Act2 = new Intent(activity, ExtendedPlayerActivity.class);
            activity.startActivity(Act2);

        }
    });

    public BottomMusicPlayer(AppCompatActivity activity) {
        this.activity = activity;

        titleSongTV= (TextView)activity.findViewById(R.id.titleSong);
        titleSongTV.setOnClickListener(openExtendedPlayer);

        detailsTV=(TextView)activity.findViewById(R.id.details);
        detailsTV.setOnClickListener(openExtendedPlayer);

        seekBar = (SeekBar) activity.findViewById(R.id.playerSeekBar);
        seekBar.setOnSeekBarChangeListener(seekBarChangeListener);

        playButton = (ImageButton) activity.findViewById(R.id.button_pause_play);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mpModel.togglePause();
                setPlayIcon(mpModel.isPlaying());
            }
        });
        albumArt = (ImageView) activity.findViewById(R.id.imageAlbum);
    }


    private void setPlayIcon(boolean isPlaying){
        playButton.setImageResource(isPlaying ? R.drawable.ic_pause : R.drawable.ic_m_playbutton);
    }

    public void initModel(MusicPlayerModel model) {
       // if(mpModel != null) throw new RuntimeException("A MusicPlayerModel is already registered");
        mpModel = model;
        setPlayIcon(mpModel.isPlaying());

        mpModel.playbackDurationChanged.addListener(playbackDurationChanged);
        mpModel.playbackStateChanged.addListener(playbackStateChangedListener);
        mpModel.songChangedEvent.addListener(songChangedListener);

        seekBar.setMax(mpModel.getCurrentPlayingTotalTime());
        seekBar.setProgress(mpModel.getCurrentPlayingCurrentTime());

    }

    public void releaseModel() {
        if(mpModel != null){
            mpModel.playbackDurationChanged.removeListener(playbackDurationChanged);
            mpModel.playbackStateChanged.removeListener(playbackStateChangedListener);
            mpModel.songChangedEvent.removeListener(songChangedListener);

            mpModel = null;
        }
    }
}
