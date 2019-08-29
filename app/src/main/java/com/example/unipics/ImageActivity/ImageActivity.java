package com.example.unipics.ImageActivity;

import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import com.example.unipics.R;
import com.squareup.picasso.Picasso;

import static com.example.unipics.Constants.KEY_IMAGE;

public class ImageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
        String uri = getIntent().getStringExtra(KEY_IMAGE);
        ImageView image = findViewById(R.id.imageView_fullscreenImage);
        Picasso.get().load(Uri.parse(uri)).into(image);
    }
}
