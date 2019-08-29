package com.example.unipics.ImageActivity;

import android.net.Uri;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import com.example.unipics.R;
import com.github.chrisbanes.photoview.PhotoView;
import com.squareup.picasso.Picasso;

import static com.example.unipics.Constants.KEY_IMAGE;

public class ImageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
        String uri = getIntent().getStringExtra(KEY_IMAGE);

        PhotoView photoView = findViewById(R.id.photo_view);
        Picasso.get().load(Uri.parse(uri)).into(photoView);
    }
}
