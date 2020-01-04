package com.punuo.sys.app.video.model;

import com.google.gson.annotations.SerializedName;
import com.punuo.sys.sdk.model.BaseModel;

import java.util.List;

/**
 * Created by han.chen.
 * Date on 2020-01-04.
 **/
public class MusicModel extends BaseModel {

    @SerializedName("basic_music_list")
    public List<String> mBasicMusicList;
}
