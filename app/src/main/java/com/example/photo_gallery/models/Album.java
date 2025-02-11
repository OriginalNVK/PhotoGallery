package com.example.photo_gallery.models;

import java.util.ArrayList;
import java.util.List;

public class Album {
    private String name;
    private List<ImageItem> images;

    public Album(String name) {
        this.name = name;
        this.images = new ArrayList<>();
    }

    public Album(String name, List<ImageItem> images) {
        this.name = name;
        this.images = images;
    }

    public String getName() {
        return name;
    }

    public List<ImageItem> getImages() {
        return images;
    }

    public void setImages(List<ImageItem> images) {
        this.images = images;
    }

    public void addImage(ImageItem image) {
        images.add(image);
    }

    public void removeImage(ImageItem image) {
        images.remove(image);
    }
}
