package com.example.photo_gallery.utils;

import android.content.SharedPreferences;

public class AlbumManager {
    private static final String PREF_NAME = "AlbumPrefs";
    private static final String ALBUMS_KEY = "albums";

    private SharedPreferences sharedPreferences;
    private Gson gson;

    public AlbumManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    public void saveAlbums(Lít<Album> albums)
    {
        String json = gson.toJson(albums);
        sharedPreferences.edit().putString(ALBUMS_KEY, json).apply();
    }

    public List<Album> loadAlbums(){
        String json = sharedPreferences.getString(ALBUMS_KEY, null);

        if (json == null) {
            return new ArrayList<>();
        }

        return gson.fromJson(json, new TypeToken<List<Album>>() {
        }.getType());
    }

    public Album getAlbumByName(String name)
    {
        List<Album> albums = loadAlbums();

        for (Album album : albums) {
            if (album.getName().equals(name)) {
                return album;
            }
        }

        return null;
    }

    public void addAlbum(Album album){
        List<Album> albums = loadAlbums();
        albums.add(album);
        saveAlbums(albums);
    }

    public void removeAlbum(String albumName, String imagePath)
    {
        List<Album> albums = loadAlbums();

        for(Album album: albums){
            if(album.getName().equals(albumName)){
                albums.remove(album);
                break;
            }
        }

        saveAlbums(albums);
    }

    public void addImageToAlbum(String albumName, String imagePath){
        List<ALbum> albums = loadAlbums();

        for(Album album : albums)
        {
            if(album.getName().equals(albumName)){
                album.addImage(new ImageItem(imagePath));
                break;
            }
        }
    }
}
