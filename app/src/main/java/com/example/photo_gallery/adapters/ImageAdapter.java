package com.example.photo_gallery.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.Target;
import com.example.photo_gallery.R;
import com.example.photo_gallery.models.ImageItem;

import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {
    private final Context context;
    private final List<ImageItem> images;
    private final DateGroupAdapter.OnImageClickListener imageClickListener;

    public ImageAdapter(Context context, List<ImageItem> images, DateGroupAdapter.OnImageClickListener imageClickListener) {
        this.context = context;
        this.images = images;
        this.imageClickListener = imageClickListener; // Accept listener
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        ImageItem imageItem = images.get(position);
        holder.bind(imageItem);
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    public class ImageViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageView;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_view);

            // Set onClickListener to handle image clicks
            itemView.setOnClickListener(view -> {
                if (imageClickListener != null) {
                    int position = getBindingAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        imageClickListener.onImageClick(images.get(position).getImagePath());
                    }
                }
            });
        }

        public void bind(ImageItem imageItem) {
            Glide.with(context)
                    .load(imageItem.getImagePath())
                    .thumbnail(0.1f) // Load images first with low quality
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC) // Reasonable image cache
                    .override(Target.SIZE_ORIGINAL) // Resize image if necessary
                    .into(imageView);
        }
    }
}
