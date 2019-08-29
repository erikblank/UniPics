package com.example.unipics.Gallery;

import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.core.content.FileProvider;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.unipics.ImageActivity.ImageActivity;
import com.example.unipics.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.List;

import static com.example.unipics.Constants.KEY_FOLDER;
import static com.example.unipics.Constants.KEY_IMAGE;

public class GalleryActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 101;

    private StorageReference mStorageRef;
    private DatabaseReference mDatabaseRef;

    private GridView gvGallery;
    private GalleryAdapter galleryAdapter;
    private List<Upload> uploads;
    private ProgressBar mProgressCircle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        
        init();
        populateGridViewWithDataBase();
        addPicture();
        onImageClicked();


    }

    private void onImageClicked() {
        gvGallery.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Upload upload = uploads.get(position);
                startImageActivity(upload);
            }
        });
    }

    private void startImageActivity(Upload upload) {
        Intent intent = new Intent(GalleryActivity.this, ImageActivity.class);
        String imageUri = upload.getImageUrl();
        intent.putExtra(KEY_IMAGE, imageUri);
        startActivity(intent);

    }

    private void populateGridViewWithDataBase() {
        gvGallery = findViewById(R.id.gv_gallery);

        mDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                uploads = new ArrayList<>();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Upload upload = postSnapshot.getValue(Upload.class);
                    uploads.add(upload);
                }
                galleryAdapter = new GalleryAdapter(GalleryActivity.this, uploads);
                gvGallery.setAdapter(galleryAdapter);
                mProgressCircle.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(GalleryActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                mProgressCircle.setVisibility(View.INVISIBLE);
            }
        });


    }

    private void init() {
        String folderID = getIntent().getStringExtra(KEY_FOLDER);
        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        //path were the images are saved in firebase
        String imagePath = userID + "/" + folderID + "/images";
        mStorageRef = FirebaseStorage.getInstance().getReference(imagePath);
        mDatabaseRef = FirebaseDatabase.getInstance().getReference(imagePath);
        mProgressCircle = findViewById(R.id.progress_updateGallery);

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
        final StorageReference imageRef = mStorageRef.child(System.currentTimeMillis() + "." + getFileExtension(uri));
        if (uri != null) {
            imageRef.putFile(uri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();

                    }
                    return imageRef.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        Upload upload = new Upload(downloadUri.toString());
                        mDatabaseRef.push().setValue(upload);
                        Toast.makeText(GalleryActivity.this, "Image uploaded successfully",
                                Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(GalleryActivity.this, "upload failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

    }

    //returns the extension of the image (like jpg, png)
    private String getFileExtension(Uri uri) {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }


}
