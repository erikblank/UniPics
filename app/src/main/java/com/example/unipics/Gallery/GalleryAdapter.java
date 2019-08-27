package com.example.unipics.Gallery;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.example.unipics.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class GalleryAdapter extends ArrayAdapter<Uri> {
    private List<Uri> uriList;
    private Context mContext;

    public GalleryAdapter(Context context, List<Uri> uriList) {
        super(context, R.layout.gallery_item, uriList);
        this.mContext = context;
        this.uriList = uriList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        View v = convertView;

        if(v == null){
            LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = layoutInflater.inflate(R.layout.gallery_item, null);
        }

        Uri uri = uriList.get(position);



        if (uri != null){

            ImageView imageView = v.findViewById(R.id.imageView_galleryItem);

            Picasso.get()
                    .load(uri)
                    .fit()
                    .centerCrop()
                    .into(imageView);
        }
        return v;
    }



}