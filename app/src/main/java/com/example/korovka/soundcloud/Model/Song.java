package com.example.korovka.soundcloud.Model;

import android.support.annotation.NonNull;

public class Song implements Comparable<Song> {

    private long id;
    private String title;
    private String artist;
    private String artworkUrl;
    private long duration;
    private String streamUrl;
    private int playbackCount;
    /**
     * Created by korovka on 2/22/17.
     */

    public Song(long id, String title, String artist, String artworkUrl, long duration, String streamUrl, int playbackCount) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.artworkUrl = artworkUrl;
        this.duration = duration;
        this.streamUrl = streamUrl;
        this.playbackCount = playbackCount;
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getArtworkUrl() {
        return artworkUrl;
    }

    public long getDuration() {
        return duration;
    }

    public String getStreamUrl() {
        return streamUrl;
    }

    public int getPlaybackCount() {
        return playbackCount;
    }

    @Override
    public int compareTo(@NonNull Song another) {
        return another.playbackCount - this.playbackCount;
    }
}
