package com.example.unipics.ImageActivity;

import android.content.Intent;
import android.net.Uri;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.example.unipics.NoteActivity.NoteActivity;
import com.example.unipics.R;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.squareup.picasso.Picasso;

import static com.example.unipics.Constants.KEY_IMAGE;

public class ImageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
        String uri = getIntent().getStringExtra(KEY_IMAGE);

        View bottomSheet = findViewById(R.id.bottom_sheet);

        BottomSheetBehavior mBottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        PhotoView photoView = findViewById(R.id.photo_view);
        Picasso.get().load(Uri.parse(uri)).into(photoView);

    }
}
