package com.example.photo_gallery.models;

import java.text.SimpleDateFormat;

public class ImageItem {
    private final String imagePath;
    private final long dateToken;

    public ImageItem(String imagePath, long dateToken) {
        this.imagePath = imagePath;
        this.dateToken = dateToken;
    }

    public String getImagePath() {
        return imagePath;
    }

    public long getDateToken() {
        return dateToken;
    }

    public String getDate(){
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(new java.util.Date(dateToken));
    }

    public String getMonth(){
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMMM yyyy");
        return sdf.format(new java.util.Date(dateToken));
    }

    public String getYear(){
        java.text.SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
        return sdf.format(new java.util.Date(dateToken));
    }

    public int getImageId()
    {
        return 0;
    }
}
