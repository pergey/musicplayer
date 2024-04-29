package com.example.musicplayer5;

import java.util.ArrayList;


public class Song
{
    public String id;
    public String title;
    public String duration;

    public static class Thumbnails
    {
        public String url;
        public double width;
        public double height;
    }

    public ArrayList<Thumbnails> thumbnails;
    public String album;
    public ArrayList<String> artists;
    public boolean isExplicit;
    public String audio;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public ArrayList<Thumbnails> getThumbnails() {
        return thumbnails;
    }

    public void setThumbnails(ArrayList<Thumbnails> thumbnails) {
        this.thumbnails = thumbnails;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public ArrayList<String> getArtists() {
        return artists;
    }

    public void setArtists(ArrayList<String> artists) {
        this.artists = artists;
    }

    public boolean isExplicit() {
        return isExplicit;
    }

    public void setExplicit(boolean explicit) {
        isExplicit = explicit;
    }

    public String getAudio() {
        return audio;
    }

    public void setAudio(String audio) {
        this.audio = audio;
    }
}

