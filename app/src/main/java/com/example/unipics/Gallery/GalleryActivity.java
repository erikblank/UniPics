package com.example.unipics.Gallery;

import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.unipics.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.example.unipics.Constants.KEY_FOLDER;

public class GalleryActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 101;
    private String imagePath;
    private StorageReference mStorageRef;
    private GridView gvGallery;
    private GalleryAdapter galleryAdapter;
    private List<Uri> imageList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        
        init();
        addPicture();


    }

    private void init() {
        String folderID = getIntent().getStringExtra(KEY_FOLDER);
        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        //path were the images are saved in firebase
        imagePath = userID + "/" + folderID + "/";
        mStorageRef = FirebaseStorage.getInstance().getReference(imagePath);

        gvGallery = findViewById(R.id.gv_gallery);
        imageList = new ArrayList<>();
        galleryAdapter = new GalleryAdapter(this, imageList);


    }

    private void addPicture() {
        FloatingActionButton fab = findViewById(R.id.fab_addImage);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddPictureDialog();
            }
        });
    }

    private void showAddPictureDialog() {
        AlertDialog.Builder pictureDialog = new AlertDialog.Builder(this);
        pictureDialog.setTitle("Select Action");
        String[] pictureDialogItems = {
                "Select photo from gallery",
                "Capture photo from camera" };
        pictureDialog.setItems(pictureDialogItems,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                choosePictureFromGallery();
                                break;
                            case 1:
                                //takePhotoFromCamera();
                                break;
                        }
                    }
                });
        pictureDialog.show();
    }

    private void choosePictureFromGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            Uri uri = data.getData();
            uploadToFirebase(uri);

        }
    }

    private void uploadToFirebase(Uri uri) {
        StorageReference imageRef = mStorageRef.child(System.currentTimeMillis() + "." + getFileExtension(uri));

        imageRef.putFile(uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Get a URL to the uploaded content
                        Toast.makeText(GalleryActivity.this, "Image uploaded successfully",
                                Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                        // ...
                        Toast.makeText(GalleryActivity.this, "Image upload failed",
                                Toast.LENGTH_LONG).show();
                    }
                });

    }

    //returns the extension of the image (like jpg, png)
    private String getFileExtension(Uri uri) {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }


}
