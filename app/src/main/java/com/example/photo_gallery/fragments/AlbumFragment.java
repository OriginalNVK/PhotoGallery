package com.example.photo_gallery.fragments;

import static android.app.PendingIntent.getActivity;
import static androidx.core.content.ContentProviderCompat.requireContext;
import static androidx.core.content.ContextCompat.startActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.photo_gallery.R;
import com.example.photo_gallery.activities.AlbumDetailActivity;
import com.example.photo_gallery.adapters.AlbumThumbnailAdapter;
import com.example.photo_gallery.models.Album;
import com.example.photo_gallery.models.ImageItem;
import com.example.photo_gallery.utils.AlbumManager;
import com.example.photo_gallery.utils.ImageFetcher;

import java.util.ArrayList;
import java.util.List;

public class AlbumFragment extends Fragment {
    private ImageButton addAlbumButton;
    private RecyclerView albumRecyclerView;

    private List<Album> albums;
    private AlbumManager albumManager;
    private AlbumThumbnailAdapter albumThumbnailAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle saveInstanceState){
        View view = inflater.inflate(R.layout.fragment_album, container, false);

        addAlbumButton = view.findViewById(R.id.add_album_button);
        albumRecyclerView = view.findViewById(R.id.album_recycler_view);

        albumManager = new AlbumManager(requireContext());
        albums = new ArrayList<>();

        setupAlbumRecyclerView();
        loadAlbums();

        addAlbumButton.setOnClickListener(v -> showAddAlbumDialog());
        return view;
    }

    private void setupAlbumRecyclerView(){
        albumThumbnailAdapter = new AlbumThumbnailAdapter(requireContext(), albums, album -> {
            Intent intent = new Intent(getActivity(), AlbumDetailActivity.class);
            intent.putExtra("ALBUM_NAME", album.getName());
            startActivity(intent);
        });

        albumRecyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        albumRecyclerView.setAdapter(albumThumbnailAdapter);
    }

    private void loadAlbums(){
        ImageFetcher.getAllImagesAsync(requireContext(), new ImageFetcher.FetchImagesCallback(){
            @Override
            public void onImagesFetched(List<ImageItem> listImages)
            {
              requireActivity().runOnUiThread(() -> {
                  albums.clear();
                  albumManager.removeAlbum("All");
                  Album allAlbum = new Album("All", listImages);
                  albumManager.addAlbum(allAlbum);

                  albums.addAll(albumManager.loadAlbums());
                  albumThumbnailAdapter.notifyDataSetChanged();
              });
            }

            @Override
            public void onError(Exception e){
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "Error loading images", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void showAddAlbumDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Add Album");

        final EditText input = new EditText(requireContext());
        builder.setView(input);

        builder.setPositiveButton("Ok", (dialog, which) -> {
            String albumName = input.getText().toString();
            if(albumName.isEmpty()){
                Toast.makeText(requireContext(), "Album name cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean albumExists = albums.stream().anyMatch(album ->  album.getName().equals(albumName));
            if(albumExists){
                Toast.makeText(requireContext(), "Album already exists", Toast.LENGTH_SHORT).show();
                return;
            }

            Album newAlbum = new Album(albumName, new ArrayList<>());
            albumManager.addAlbum(newAlbum);

            albums.add(newAlbum);
            albumThumbnailAdapter.notifyItemInserted(albums.size() - 1);
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    // Reload albums when fragment is resumed
    @Override
    public void onResume() {
        super.onResume();
        loadAlbums();
    }
}
