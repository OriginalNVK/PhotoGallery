package com.example.photo_gallery.utils;

import com.example.photo_gallery.models.ImageItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImageGrouping {
    public static Map<String, List<ImageItem>> groupByDate(List<ImageItem> imageList)
    {
        Map<String, List<ImageItem>> imageByDate = new HashMap<>();
        for(ImageItem imageItem: imageList)
        {
            imageByDate.computeIfAbsent(imageItem.getDate(), k -> new ArrayList<>()).add(imageItem);
        }
        return imageByDate;
    }

    public static Map<String, List<ImageItem>> groupByMonth(List<ImageItem> imageList)
    {
        Map<String, List<ImageItem>> imageByMonth = new HashMap<>();
        for(ImageItem imageItem: imageList)
        {
            imageByMonth.computeIfAbsent(imageItem.getMonth(), k -> new ArrayList<>()).add(imageItem);
        }
        return imageByMonth;

    }

    public static Map<String, List<ImageItem>> groupByYear(List<ImageItem> imageList)
    {
        Map<String, List<ImageItem>> imageByYear = new HashMap<>();
        for(ImageItem imageItem: imageList)
        {
            imageByYear.computeIfAbsent(imageItem.getYear(), k -> new ArrayList<>()).add(imageItem);
        }

        return imageByYear;
    }
}
