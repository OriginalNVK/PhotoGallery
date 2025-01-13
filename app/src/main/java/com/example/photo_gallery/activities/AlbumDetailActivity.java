package com.example.photo_gallery.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.photo_gallery.R;
import com.example.photo_gallery.adapters.DateGroupAdapter;
import com.example.photo_gallery.models.Album;
import com.example.photo_gallery.models.DateGroup;
import com.example.photo_gallery.models.ImageItem;
import com.example.photo_gallery.utils.AlbumManager;
import com.example.photo_gallery.utils.ImageGrouping;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AlbumDetailActivity extends AppCompatActivity {
    private RecyclerView albumImagesRecyclerView;
    private Toolbar toolbar;
    private TextView albumNameTitle;
    private ImageButton deleteAlbumButton;
    private List<ImageItem> albumImages;
    private AlbumManager albumManager;
    private String albumName;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.album_activity_detail);

        albumImagesRecyclerView = findViewById(R.id.album_images_recycler_view);
        toolbar = findViewById(R.id.toolbar);
        albumNameTitle = findViewById(R.id.album_name_title);
        deleteAlbumButton = findViewById(R.id.delete_album_button);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        albumName = getIntent().getStringExtra("ALBUM_NAME");
        albumNameTitle.setText(albumName);

        albumManager = new AlbumManager(this);

        loadAlbumImages();

        setupDeleteAlbumButton();
    }

    private void loadAlbumImages(){
        Album selectedAlbum =albumManager.getAlbumByName(albumName);

        if(selectedAlbum != null)
        {
            albumImages = selectedAlbum.getListImage().stream()
                    .sorted((i1, i2) -> Long.compare(i2.getDateTaken(), i1.getDateTaken()))
                    .collect(Collectors.toList());

            Map<String, List<ImageItem>> groupedMap = ImageGrouping.groupByDate(albumImages);
            List<DateGroup> dateGroups = new ArrayList<>();
            for(String date: groupedMap.keySet()){
                dateGroups.add(new DateGroup(date, groupedMap.get(date)));
            }

            dateGroups = dateGroups.stream()
                    .sorted((d1, d2) -> d2.getDate().compareTo(d1.getDate()))
                    .collect(Collectors.toList());

            ArrayList<String> imagePaths = albumImages.stream()
                    .map(ImageItem::getImagePath)
                    .collect(Collectors.toCollection(ArrayList::new));

            DateGroupAdapter dateGroupAdapter = new DateGroupAdapter(this, dateGroups, imagePath -> {
                Intent intent = new Intent(this, SoloImageActivity.class);
                intent.putExtra("IMAGE_PATHS", imagePaths);
                intent.putExtra("CURRENT_IMAGE_INDEX", imagePaths.indexOf(imagePath));
                startActivity(intent);
            });

            albumImagesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            albumImagesRecyclerView.setAdapter(dateGroupAdapter);
        }
    }

    private void setupDeleteAlbumButton(){
        if(albumName.equals("All"))
        {
            deleteAlbumButton.setEnabled(false);
            deleteAlbumButton.setAlpha(0.5f);
        }

        deleteAlbumButton.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Delete Album")
                    .setMessage("Are you sure you want to delete this album?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        albumManager.removeAlbum(albumName);
                        Toast.makeText(this, "Album deleted", Toast.LENGTH_SHORT).show();
                        onBackPressed();
                    })
                    .setNegativeButton("No", null)
                    .show();
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadAlbumImages();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        if(item.getItemId() == android.R.id.home){
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
