package com.speakerz.model.network.Serializable.body.audio.content;

import java.io.Serializable;
import java.util.Date;

public class AudioControlDto implements Serializable {
    public AudioControlDto(AUDIO_CONTROL flag) {
        this.flag = flag;
    }

    public AUDIO_CONTROL flag;
    public int number;
    public long timeInMilliSeconds;

}
