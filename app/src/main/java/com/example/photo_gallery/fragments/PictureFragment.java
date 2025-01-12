package com.example.photo_gallery.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.photo_gallery.adapters.DateGroupAdapter;

import com.example.photo_gallery.databinding.FragmentPictureBinding;

import java.util.ArrayList;
import java.util.List;

public class PictureFragment extends Fragment {
    private FragmentPictureBinding binding;
    private List<ImageView> imageList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
        binding = FragmentPictureBinding.inflate(inflater, container, false);

        RecyclerView recyclerView = binding.recyclerView;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        imageList = new ArrayList<>();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item,
                new String[]{"Decrease", "Increase"});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerFilter.setAdapter(adapter);

        binding.spinnerFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
        @Override
        public void onItemSelected(AdapterView<?> parent, View view,  int position, long id)
        {
            RecyclerView.Adapter<?> groupAdapter = recyclerView.getAdapter();
            if(groupAdapter == null)
            {
                Toast.makeText(getContext(), "groupAdapter is null", Toast.LENGTH_SHORT).show();
                return;
            }
            if(!(groupAdapter instanceof DateGroupAdapter))
            {
                Toast.makeText(getContext(), "GroupAdapter is not instance of DateGroupAdapter", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        });
    }
}
