package com.americavoice.backup.music.model;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Created by pj on 2/15/18.
 */

public class Song {

    private final String title;
    private String artist;
    private final String path;
    private final int duration;
    private final byte[] cover;
    private boolean selected;

    public Song(String title, String artist, String path, int duration, byte[] cover) {
        this.title = title;
        this.artist = artist;
        this.path = path;
        this.duration = duration;
        this.cover = cover;
    }

    public String getDuration() {
        return String.format(Locale.US, "%d : %d",
          TimeUnit.MILLISECONDS.toMinutes(duration),
          TimeUnit.MILLISECONDS.toSeconds(duration) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration)));
    }

    public String getArtist() {
        return artist;
    }

    public String getTitle() {
        return title;
    }

    public String getPath() {
        return path;
    }

    public byte[] getCover() {
        return cover;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
