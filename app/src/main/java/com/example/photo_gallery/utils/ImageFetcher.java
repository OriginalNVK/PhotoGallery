package com.example.photo_gallery.utils;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.widget.Toast;

import com.example.photo_gallery.models.ImageItem;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ImageFetcher {
    private static final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public static void getAllImagesAsync(Context context, FetchImagesCallback callback)
    {
        executorService.execute(() -> {
            try{
                List<ImageItem> imageItems = new ArrayList<>();
                Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                String[] projection = {
                        MediaStore.Images.Media._ID,
                        MediaStore.Images.Media.DATE_TAKEN
                };
                String sortOrder = MediaStore.Images.Media.DATE_ADDED + "DESC";
                Cursor cursor = context.getContentResolver().query(uri, projection, null, null, sortOrder);

                if(cursor != null)
                {
                    while(cursor.moveToNext())
                    {
                        long id =cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID));
                        long dateToken = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN));

                        Uri contentUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
                        imageItems.add(new ImageItem(contentUri.toString(), dateToken));
                    }
                    cursor.close();
                }
                if(imageItems.isEmpty())
                {
                    showToastOnUiThread(context, "No Images Found");
                }

                callback.onImagesFetched(imageItems);
            }
            catch(Exception e)
            {
                callback.onError(e);
            }
        });
    }

    private static void showToastOnUiThread(Context context, String message)
    {
        new Handler(Looper.getMainLooper()).post(() -> {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        });
    }

    public interface FetchImagesCallback{
        void onImagesFetched(List<ImageItem> imageItems);
        void onError(Exception e);
    }
}
