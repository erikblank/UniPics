package com.example.unipics.Gallery;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.example.unipics.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class GalleryAdapter extends ArrayAdapter<Upload> {
    private List<Upload> uploads;
    private Context mContext;

    public GalleryAdapter(Context context, List<Upload> uploads) {
        super(context, R.layout.gallery_item, uploads);
        this.mContext = context;
        this.uploads = uploads;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        View v = convertView;
        //inflate view
        if(v == null){
            LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = layoutInflater.inflate(R.layout.gallery_item, null);
        }

        Upload upload = uploads.get(position);

        if (upload != null){
            ImageView imageView = v.findViewById(R.id.imageView_galleryItem);
            String uri = upload.getImageUrl();
            /*picasso is used to load the image url to the imageView
             *fit adjusts the picture to the size of the imageView
             *placeholder loads an picture which is placed while loading the real picture
             *centerCrop fill the whole imageView
             */
            Picasso.get()
                    .load(uri)
                    .fit()
                    .placeholder(R.drawable.logoperf)
                    .centerCrop()
                    .into(imageView);

        }
        return v;
    }



}