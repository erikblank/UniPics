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

        if(v == null){
            LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = layoutInflater.inflate(R.layout.gallery_item, null);
        }

        Upload upload = uploads.get(position);

        if (upload != null){

            ImageView imageView = v.findViewById(R.id.imageView_galleryItem);
            String uri = upload.getImageUrl();


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