package com.example.photo_gallery.models;

import java.util.ArrayList;
import java.util.List;

public class Album {
    private String name;
    private List<ImageItem> listImage;

    public Album(String name) {
        this.name = name;
        this.listImage = new ArrayList<>();
    }

    public Album(String name, List<ImageItem> images) {
        this.name = name;
        this.listImage = images;
    }

    public String getName() {
        return name;
    }

    public List<ImageItem> getListImage() {
        return listImage;
    }

    public void setImages(List<ImageItem> images){
        this.listImage = images;
    }

    public void addImage(ImageItem image) {listImage.add(image);}

    public void removeImage(ImageItem image){listImage.remove(image);}
}


