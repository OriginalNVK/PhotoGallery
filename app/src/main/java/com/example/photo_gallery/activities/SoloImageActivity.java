package com.example.photo_gallery.activities;

import android.app.PendingIntent;
import android.app.RecoverableSecurityException;
import android.app.WallpaperManager;
import android.content.Intent;
import android.content.IntentSender;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.view.GestureDetector;
import android.view.MotionEvent;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.Target;
import com.example.photo_gallery.R;
import com.example.photo_gallery.models.Album;
import com.example.photo_gallery.models.ImageItem;
import com.example.photo_gallery.utilities.AlbumManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class SoloImageActivity extends AppCompatActivity {
    private ImageView soloImageView;
    private TextView tvTitle;
    private Button backBtn, btnShare, setBackgroundBtn, addToAlbumBtn, deleteFromAlbumBtn;

    private ArrayList<String> imagePaths;
    private int currentIndex;

    private final int CROP_REQUEST_CODE = 1;
    private final int REQUEST_CODE_DELETE_IMAGE = 2;

    private Uri _croppedImageUri;

    private GestureDetector gestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.solo_image); // Ensure you have this layout

        soloImageView = findViewById(R.id.imgView_solo_image);
        tvTitle = findViewById(R.id.txtView_solo_image_title);
        backBtn = findViewById(R.id.btn_solo_back);
        setBackgroundBtn = findViewById(R.id.btn_solo_set_background);
        addToAlbumBtn = findViewById(R.id.btn_solo_add_to_album);
        deleteFromAlbumBtn = findViewById(R.id.btn_solo_delete_from_album);

        // Get the data from the intent
        imagePaths = getIntent().getStringArrayListExtra("IMAGE_PATHS");
        currentIndex = getIntent().getIntExtra("CURRENT_IMAGE_INDEX", -1);

        if (imagePaths != null && currentIndex != -1) {
            loadImage(currentIndex); // Load the current image
        }

        // Back button to finish the activity
        backBtn.setOnClickListener(view -> finish());

        // Set the background
        setBackgroundBtn.setOnClickListener(view -> startSettingWallpaper());

        // Add to album button
        addToAlbumBtn.setOnClickListener(view -> addToAlbum());

        // Delete from album button
        deleteFromAlbumBtn.setOnClickListener(view -> deleteFromAlbum());
        gestureDetector = new GestureDetector(this, new MyGestureListener());
        btnShare = findViewById(R.id.btn_share_image);
        btnShare.setOnClickListener(view -> shareImage());

    }

    private void shareImage() {
        BitmapDrawable bitmapDrawable = (BitmapDrawable) soloImageView.getDrawable();
        Bitmap bitmap = bitmapDrawable.getBitmap();
        shareImageAndText(bitmap);
    }

    private void shareImageAndText(Bitmap bitmap) {
        Uri uri = getImageToShare(bitmap);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.putExtra(Intent.EXTRA_SUBJECT, "image subject");
        intent.setType("image/*");
        startActivity(Intent.createChooser(intent, "Share Via"));
    }

    private Uri getImageToShare(Bitmap bitmap) {
        File folder = new File(getCacheDir(), "images");
        Uri uri = null;
        try {
            folder.mkdirs();
            File file = new File(folder, "image.jpg");

            FileOutputStream fileOutputStream = new FileOutputStream(file);

            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.close();

            uri = FileProvider.getUriForFile(this, "com.example.photo_gallery.fileprovider", file);

        } catch (Exception e) {
            Toast.makeText(this, "Failed to share image: " + e, Toast.LENGTH_LONG).show();
            throw new RuntimeException(e);
        }

        return uri;
    }

    private void loadImage(int index) {
        // Load the image using Glide
        String imagePath = imagePaths.get(index);

        Glide.with(this)
                .load(imagePath)
                .thumbnail(0.1f) // Load images first with low quality
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC) // Reasonable image cache
                .override(Target.SIZE_ORIGINAL) // Resize image if necessary
                .into(soloImageView);

        ImageItem imageItem = processImageItemFromUri(Uri.parse(imagePath));
        String tempTitle = "Date: " + imageItem.getDate();
        tvTitle.setText(tempTitle);
    }

    private void slideOutLeftAndLoadImage(int index) {
        Animation slideOutLeft = AnimationUtils.loadAnimation(this, R.anim.slide_out_left);
        Animation slideInLeft = AnimationUtils.loadAnimation(this, R.anim.slide_in_left);

        soloImageView.startAnimation(slideOutLeft);
        slideOutLeft.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                loadImage(index); // Load the new image
                soloImageView.startAnimation(slideInLeft); // Slide in the new image
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
    }

    private void slideOutRightAndLoadImage(int index) {
        Animation slideOutRight = AnimationUtils.loadAnimation(this, R.anim.slide_out_right);
        Animation slideInRight = AnimationUtils.loadAnimation(this, R.anim.slide_in_right);

        soloImageView.startAnimation(slideOutRight);
        slideOutRight.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                loadImage(index); // Load the new image
                soloImageView.startAnimation(slideInRight); // Slide in the new image
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
    }
    // Using android's built in crop feature to allow user to choose the frame/position
    private void startSettingWallpaper() {
        try {
            Uri imageUri = Uri.parse(imagePaths.get(currentIndex));

            Intent cropIntent = new Intent("com.android.camera.action.CROP");
            cropIntent.setDataAndType(imageUri, "image/*");
            cropIntent.putExtra("crop", "true");

            // Get the dimensions of the device's display
            DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
            int screenWidth = displayMetrics.widthPixels;
            int screenHeight = displayMetrics.heightPixels;
            float ratio = (float)screenWidth/ (float)screenHeight;

            //to handle different devices' screen type
            int smallerSideAspect = 1;
            int largerSideAspect = (int) Math.ceil(1 * 1.0 / ratio);
            if(screenWidth < screenHeight){
                cropIntent.putExtra("aspectX", smallerSideAspect);
                cropIntent.putExtra("aspectY", largerSideAspect);
            }
            else {
                cropIntent.putExtra("aspectX",largerSideAspect);
                cropIntent.putExtra("aspectY",smallerSideAspect);
            }

            cropIntent.putExtra("outputX", screenWidth);
            cropIntent.putExtra("outputY", screenHeight);
            cropIntent.putExtra("scale", true);
            cropIntent.putExtra("return-data",true);

            startActivityForResult(cropIntent, CROP_REQUEST_CODE);
        } catch (Exception e) {
            Log.e("SoloImageActivity", "Error while trying to crop the image", e);
            Toast.makeText(SoloImageActivity.this, "Failed to crop image: " + e, Toast.LENGTH_LONG).show();
            _croppedImageUri = Uri.parse(imagePaths.get(currentIndex));
            setWallpaper();
        }
    }

    private void setWallpaper(){
        if (_croppedImageUri != null) {
            try {
                WallpaperManager wallpaperManager = WallpaperManager.getInstance(getApplicationContext());
                Bitmap croppedBitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(_croppedImageUri));

                wallpaperManager.setBitmap(croppedBitmap,null,true,WallpaperManager.FLAG_SYSTEM);
                Toast.makeText(SoloImageActivity.this, "Wallpaper set successfully", Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                Log.e("SoloImageActivity", "Error setting wallpaper", e);
                Toast.makeText(SoloImageActivity.this, "Failed to set wallpaper: " + e, Toast.LENGTH_LONG).show();
            } finally {
                //delete the newly cropped image, whether setting image as wallpaper succeeds or not
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    if (deleteCroppedImage(_croppedImageUri)) {
                        Log.i("SoloImageActivity", "Cropped image deleted successfully");
                    }
                }
                else {
                    Toast.makeText(this,"Can't delete the cropped image",Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            Log.e("SoloImageActivity", "Cropped image URI is null");
        }

    }

        /**
         * Shows a dialog explaining why you're requesting permission to delete the image.
         *
         * @param croppedImageUri The Uri of the cropped image.
         */
        private void showCustomDeletionExplanationDialogAndRequestDeletionPermission(Uri croppedImageUri, PendingIntent intent) {
            // Show a dialog explaining why you're requesting permission to delete the image
            new AlertDialog.Builder(this)
                    .setTitle("Permission Request")
                    .setMessage("We need your permission to \ndelete the TEMPORARY cropped image (auto-created by us) \nto keep your gallery organized.\nPlease allow us to delete it for you.")
                    .setPositiveButton("OK", (dialog, which) -> {
                        try {
                            // Show the dialog to the user to confirm deletion
                            startIntentSenderForResult(
                                    intent.getIntentSender(),
                                    REQUEST_CODE_DELETE_IMAGE,
                                    null,
                                    0,
                                    0,
                                    0
                            );

                        } catch (IntentSender.SendIntentException sendIntentException) {
                            Log.e("Send intent exception", "Failed to send intent for deletion", sendIntentException);
                        }
                    })
                    .setCancelable(true)
                    .show();
        }

        /**
         * Deletes the cropped image using the ContentResolver.
         *
         * @param croppedImageUri The Uri of the cropped image.
         * @return true if the image was deleted successfully, false otherwise.
         */
        @RequiresApi(api = Build.VERSION_CODES.Q)
        private boolean deleteCroppedImage(Uri croppedImageUri) {
            try {

                // Check if the URI is a content URI or file URI
                if ("content".equals(croppedImageUri.getScheme())) {
                    Log.e("testing deleting image content",croppedImageUri.getScheme());
                    // Use ContentResolver to delete the content
                    int rowsDeleted = getContentResolver().delete(croppedImageUri, null, null);
                    return rowsDeleted > 0;
                }
                if ("file".equals(croppedImageUri.getScheme())) {
                    // Directly delete the file
                    File file = new File(croppedImageUri.getPath());
                    return file.exists() && file.delete();
                }
            } catch (RecoverableSecurityException e) {
                // can't delete directly with contentResolver, handle RecoverableSecurityException
                Log.e("RecoverableSecurityException", e.getMessage());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

                    // Request the user to confirm deletion through the system dialog
                    PendingIntent pendingIntent = e.getUserAction().getActionIntent();

                    // Show your custom explanation and trigger the system dialog
                    showCustomDeletionExplanationDialogAndRequestDeletionPermission(croppedImageUri, pendingIntent);
                }
            } catch (Exception e) {
                Log.e("SoloImageActivity", "Error deleting cropped image, an exception other than RecoverableSecurityException: ", e);
            }
            return false;
        }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Set the the cropped image as the wallpaper, then delete it
        if (requestCode == CROP_REQUEST_CODE && resultCode == RESULT_OK) {

            if(data == null){
                Toast.makeText(this,"Can't set the image to be the wallpaper",Toast.LENGTH_LONG).show();
                Log.e( "Setting Wallpaper Error","Data when setting wallpaper is null!!!");
                return;
            }

            // Extract the URI of the cropped image from the intent
            _croppedImageUri = data.getData();
            setWallpaper();
        }

        if (requestCode == REQUEST_CODE_DELETE_IMAGE) {
            if (resultCode == RESULT_OK) {
                try{
                    if (_croppedImageUri != null) {
                        getContentResolver().delete(_croppedImageUri, null, null);
                        Log.i("SoloImageActivity", "Image deleted successfully after user confirmation");
                    }
                }catch (Exception e){
                    Log.e("Exception occurred while trying to re-delete the image ",e.getMessage());
                }
            } else {
                Log.e("SoloImageActivity", "User denied deletion");
            }
        }
    }


    private void addToAlbum() {
        AlbumManager albumManager = new AlbumManager(this);
        List<Album> albums = albumManager.loadAlbums();
        List<String> albumNames = new ArrayList<>();

        for (Album album : albums) {
            if (album.getName().equals("All")) {
                continue;
            }
            albumNames.add(album.getName());
        }

        if (albumNames.isEmpty()) {
            Toast.makeText(this, "No albums found", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show dialog to select an album to add the image to
        new MaterialAlertDialogBuilder(this)
                .setTitle("Select an album")
                .setItems(albumNames.toArray(new String[0]), (dialog, which) -> {
                    try {
                        String selectedAlbumName = albumNames.get(which);
                        albumManager.addImageToAlbum(selectedAlbumName, imagePaths.get(currentIndex));
                        Toast.makeText(this, "Image added to album: " + selectedAlbumName, Toast.LENGTH_SHORT).show();
                    } catch (IllegalArgumentException e) {
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    private void deleteFromAlbum() {
        AlbumManager albumManager = new AlbumManager(this);
        List<String> albumNames = albumManager.getAlbumNames(imagePaths.get(currentIndex));

        if (albumNames.isEmpty()) {
            Toast.makeText(this, "This image is not in any album", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show dialog to select an album to delete the image from
        new MaterialAlertDialogBuilder(this)
                .setTitle("Select an album")
                .setItems(albumNames.toArray(new String[0]), (dialog, which) -> {
                    String selectedAlbumName = albumNames.get(which);
                    albumManager.removeImageFromAlbum(selectedAlbumName, imagePaths.get(currentIndex));
                    Toast.makeText(this, "Image deleted from album: " + selectedAlbumName, Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event) || gestureDetector.onTouchEvent(event);
    }

    // In the SimpleOnGestureListener subclass, override gestures that needs detecting.
    class MyGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDown(@NonNull MotionEvent event) {
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(@NonNull MotionEvent e) {
            return true;
        }

        @Override
        public void onLongPress(@NonNull MotionEvent e) {
        }

        @Override
        public boolean onDoubleTap(@NonNull MotionEvent e) {
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, @NonNull MotionEvent e2,
                                float distanceX, float distanceY) {
            return true;
        }

        @Override
        public boolean onFling( MotionEvent e1, MotionEvent e2,
                               float velocityX, float velocityY) {
            assert e1 != null;
            assert e2 != null;
            float diffX = e2.getX() - e1.getX();
            float diffY = e2.getY() - e1.getY();

            if (Math.abs(diffX) > Math.abs(diffY)) {
                if (Math.abs(diffX) > 100 && Math.abs(velocityX) > 100) {
                    if (diffX > 0) {
                        // Swipe right
                        if (currentIndex > 0) {
                            slideOutRightAndLoadImage(--currentIndex);
                        } else {
                            Toast.makeText(SoloImageActivity.this, "This is the first image", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // Swipe left
                        if (currentIndex < imagePaths.size() - 1) {
                            slideOutLeftAndLoadImage(++currentIndex);
                        } else {
                            Toast.makeText(SoloImageActivity.this, "This is the last image", Toast.LENGTH_SHORT).show();
                        }
                    }
                    return true;
                }
            }
            else{
                return false;
            }
            return false;
        }

    };


    public ImageItem processImageItemFromUri(Uri uri) {
        String filePath = null;
        long dateTaken = 0;

        if ("content".equals(uri.getScheme())) {
            // Use ContentResolver to retrieve metadata from the Uri
            Cursor cursor = getContentResolver().query(uri,
                    new String[]{MediaStore.Images.Media.DATA, MediaStore.Images.Media.DATE_TAKEN},
                    null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                int filePathColumn = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
                int dateColumn = cursor.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN);

                if (filePathColumn != -1) {
                    filePath = cursor.getString(filePathColumn);
                }
                if (dateColumn != -1) {
                    dateTaken = cursor.getLong(dateColumn);
                }
                cursor.close();
            }
        } else if ("file".equals(uri.getScheme())) {
            File file = new File(uri.getPath());
            if (!file.exists()){
                Log.e("File does not exist when processing image item from Uri path: ",uri.getPath());
                return null;
            }
            filePath = file.getAbsolutePath();

            // Retrieve date from file's last modified timestamp
            dateTaken = file.lastModified();
        }

        // Create an ImageItem instance
        if (filePath != null) {
            return new ImageItem(filePath, dateTaken);
        }

        return null; // Return null if the process fails
    }
}


