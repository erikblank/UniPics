package com.example.unipics.Gallery;

import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.example.unipics.BuildConfig;
import com.example.unipics.MainMenu.Folder;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.core.content.FileProvider;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Environment;
import android.provider.MediaStore;
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

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.example.unipics.Constants.KEY_FOLDER;
import static com.example.unipics.Constants.KEY_IMAGE;

public class GalleryActivity extends AppCompatActivity{

    private static final int PICK_IMAGE_REQUEST = 101;
    private static final int REQUEST_IMAGE_CAPTURE = 1;

    private String mCurrentPhotoPath;
    private Uri mCurrentPhotoUri;

    private FirebaseStorage mStorage;
    private StorageReference mStorageRef;
    private DatabaseReference mDatabaseRef;

    private GridView gvGallery;
    private GalleryAdapter galleryAdapter;
    private List<Upload> uploads;
    private ProgressBar mProgressBar;

    private Folder currentFolder;

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

    private void init() {
        currentFolder = (Folder) getIntent().getSerializableExtra(KEY_FOLDER);
        String folderID = currentFolder.getFolderId();
        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        //path were the images are saved in firebase
        String imagePath = userID + "/" + folderID + "/images";
        mStorageRef = FirebaseStorage.getInstance().getReference(imagePath);
        mDatabaseRef = FirebaseDatabase.getInstance().getReference(imagePath);
        mProgressBar = findViewById(R.id.progressBar_gallery);
        mStorage = FirebaseStorage.getInstance();

    }

    private void populateGridViewWithDataBase() {
        gvGallery = findViewById(R.id.gv_gallery);

        mDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                uploads = new ArrayList<>();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Upload upload = postSnapshot.getValue(Upload.class);
                    upload.setId(postSnapshot.getKey());
                    uploads.add(upload);
                }
                galleryAdapter = new GalleryAdapter(GalleryActivity.this, uploads);
                gvGallery.setAdapter(galleryAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(GalleryActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });


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

    private void onImageClicked() {
        gvGallery.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Upload upload = uploads.get(position);
                startImageActivity(upload);
            }
        });

        gvGallery.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                deleteSelectedItemFromRealtimeDatabaseAndStorage(position);
                return false;
            }
        });
    }

    private void deleteSelectedItemFromRealtimeDatabaseAndStorage(int position) {
        Upload upload = uploads.get(position);
        final String uploadID = upload.getId();
        StorageReference currentStoreRef = mStorage.getReferenceFromUrl(upload.getImageUrl());
        currentStoreRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                mDatabaseRef.child(uploadID).removeValue();
                Toast.makeText(GalleryActivity.this, "Item deleted", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startImageActivity(Upload upload) {
        Intent intent = new Intent(GalleryActivity.this, ImageActivity.class);
        String imageUri = upload.getImageUrl();
        intent.putExtra(KEY_IMAGE, imageUri);
        startActivity(intent);

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
                                takePhotoFromCamera();
                                break;
                        }
                    }
                });
        pictureDialog.show();
    }

    private void takePhotoFromCamera(){
        dispatchTakePictureIntent();
        galleryAddPic();


    }

    //starts the camera, provides an Uri for uploading to firebase and saves image on device
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                ex.printStackTrace();
                // Error occurred while creating the File
            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        BuildConfig.APPLICATION_ID + ".provider",
                        photoFile);
                mCurrentPhotoUri = photoURI;

                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    /**
     * Create file with current timestamp name
     * @throws IOException
     */
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
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

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            uploadToFirebase(mCurrentPhotoUri);
        }

    }

    private void uploadToFirebase(Uri uri) {
        mProgressBar.setVisibility(View.VISIBLE);
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
                        mProgressBar.setVisibility(View.GONE);
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
