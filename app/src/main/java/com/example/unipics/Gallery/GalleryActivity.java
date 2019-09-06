package com.example.unipics.Gallery;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.example.unipics.BuildConfig;
import com.example.unipics.MainMenu.Folder;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Environment;
import android.provider.MediaStore;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.Button;
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
import java.util.Objects;

import static com.example.unipics.Constants.KEY_FOLDER;
import static com.example.unipics.Constants.KEY_IMAGE;
import static com.example.unipics.Constants.KEY_PATH_IMAGE;
import static com.example.unipics.Constants.PICK_IMAGE_REQUEST;
import static com.example.unipics.Constants.REQUEST_IMAGE_CAPTURE;

public class GalleryActivity extends AppCompatActivity implements UploadListener{

    private String mCurrentPhotoPath;
    private Uri mCurrentPhotoUri;
    private String imagePath;

    //storage and database
    private FirebaseStorage mStorage;
    private StorageReference mStorageRef;
    private DatabaseReference mDatabaseRef;

    private GridView gvGallery;
    private GalleryAdapter galleryAdapter;
    private List<Upload> uploads;
    private ProgressBar mProgressBar;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        init();
        populateGridViewWithDataBase();
        addPicture();
        onImageClicked();


    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void init() {
        //get folder extra, to get the key for database and title for toolbar
        Folder currentFolder = (Folder) getIntent().getSerializableExtra(KEY_FOLDER);
        String folderID = currentFolder.getFolderId();
        String folderName = currentFolder.getFolderName();
        //init toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        //set toolbar title to current folder
        toolbar.setTitle(folderName);
        setSupportActionBar(toolbar);
        String userID = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        //path were the images are saved in firebase database
        imagePath = userID + "/" + folderID + "/images";
        //init storage and database
        mStorage = FirebaseStorage.getInstance();
        mStorageRef = mStorage.getReference(userID);
        mDatabaseRef = FirebaseDatabase.getInstance().getReference(imagePath);
        //init progressbar
        mProgressBar = findViewById(R.id.progressBar_gallery);
        //init gridView
        gvGallery = findViewById(R.id.gv_gallery);

    }

    private void populateGridViewWithDataBase() {
        //listen to changes in database and update gridView
        mDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //get all uploads out of database
                uploads = new ArrayList<>();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Upload upload = postSnapshot.getValue(Upload.class);
                    assert upload != null;
                    upload.setId(postSnapshot.getKey());
                    uploads.add(upload);
                }
                //init adapter
                galleryAdapter = new GalleryAdapter(GalleryActivity.this, uploads);
                gvGallery.setAdapter(galleryAdapter);
                //display text when gridView is empty
                gvGallery.setEmptyView(findViewById(R.id.emptyElement_gallery));
                //register for context menu to delete images
                registerForContextMenu(gvGallery);
                mProgressBar.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(GalleryActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                mProgressBar.setVisibility(View.INVISIBLE);
            }
        });


    }

    //set onClickListener to fab to open gallery or camera
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
        //create dialog
        final AlertDialog dialogBuilder = new AlertDialog.Builder(this).create();
        LayoutInflater inflater = this.getLayoutInflater();
        //inflate view
        @SuppressLint("InflateParams") View dialogView = inflater.inflate(R.layout.dialog_gallery_select_from, null);
        Button camera = dialogView.findViewById(R.id.btn_selectFromCamera);
        Button gallery = dialogView.findViewById(R.id.btn_selectFromGallery);
        Button cancel = dialogView.findViewById(R.id.btn_selectFromCancel);
        //open camera
        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePhotoFromCamera();
                dialogBuilder.dismiss();
            }
        });
        //choose picture from gallery
        gallery.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
            @Override
            public void onClick(View v) {
                //check for permission of reading storage
                if (ActivityCompat.checkSelfPermission(GalleryActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(GalleryActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
                    return;
                }
                choosePictureFromGallery();
                dialogBuilder.dismiss();
            }
        });
        //if cancel is clicked, close dialog
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogBuilder.dismiss();
            }
        });

        dialogBuilder.setView(dialogView);
        dialogBuilder.show();


    }

    private void takePhotoFromCamera(){
        dispatchTakePictureIntent();
        galleryAddPic();
    }

    //starts the camera, provides an Uri for uploading to firebase
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

    //create file with current timestamp name
    private File createImageFile() throws IOException {
        // Create an image file name
        @SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
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

    //save image taken from camera to device
    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    //start intent to open up gallery app on device
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void choosePictureFromGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        //allow option to select multiple images
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            // When an Image is picked
            if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                    && null != data) {
                // if just one image is picked
                if(data.getData()!=null){
                    Uri uri = data.getData();
                    uploadToFirebase(uri);
                } else {
                    //if multiple images are picked
                    if (data.getClipData() != null) {
                        ClipData mClipData = data.getClipData();
                        //array to save URIs of all images selected
                        Uri[] images = new Uri[mClipData.getItemCount()];
                        for (int i = 0; i < mClipData.getItemCount(); i++) {
                            ClipData.Item item = mClipData.getItemAt(i);
                            Uri uri = item.getUri();
                            images[i] = uri;
                        }
                        //upload all URIs in background
                        new BackgroundTask(GalleryActivity.this, mDatabaseRef).execute(images);
                    }
                }
            }
            //if image is from camera
            if(requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
                uploadToFirebase(mCurrentPhotoUri);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG)
                    .show();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    //interface-method of background task: listens to progress of background task
    @Override
    public void onProgress() {
        mProgressBar.setVisibility(View.VISIBLE);
    }

    private void uploadToFirebase(Uri uri) {
        mProgressBar.setVisibility(View.VISIBLE);
        //save images in storage with name as following: current time in millies + fileextension (jpg for example)
        final StorageReference imageRef = mStorageRef.child(System.currentTimeMillis() + "." + getFileExtension(uri));
        if (uri != null) {
            //upload image
            imageRef.putFile(uri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    //if not successfull
                    if (!task.isSuccessful()) {
                        throw Objects.requireNonNull(task.getException());
                    }
                    return imageRef.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    //iff successfull
                    if (task.isSuccessful()) {
                        //get downloadUri out of task object, to set in imageview
                        Uri downloadUri = task.getResult();
                        assert downloadUri != null;
                        Upload upload = new Upload(downloadUri.toString());
                        //create new directory to save images in realtime database
                        mDatabaseRef.push().setValue(upload);
                        Toast.makeText(GalleryActivity.this, "Image uploaded successfully",
                                Toast.LENGTH_LONG).show();
                    } else {
                        //if upload failes
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                            Toast.makeText(GalleryActivity.this, "upload failed: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                        }
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

    //start image activity when on image is clicked
    private void onImageClicked() {
        gvGallery.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //get current image
                Upload upload = uploads.get(position);
                startImageActivity(upload);
            }
        });
    }

    private void startImageActivity(Upload upload) {
        Intent intent = new Intent(GalleryActivity.this, ImageActivity.class);
        String imageUri = upload.getImageUrl();
        //put path of image as extra
        intent.putExtra(KEY_PATH_IMAGE, imagePath + "/" + upload.getId());
        //put Uri of image as extra
        intent.putExtra(KEY_IMAGE, imageUri);
        startActivity(intent);

    }

    //inflate context menu
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_gallery, menu);
    }

    //when on context menu item is clicked
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        if (item.getItemId() == R.id.menu_gallery_delete) {
            //delete image from storage and database
            deleteSelectedItemFromRealtimeDatabaseAndStorage(info.position);
            return true;
        }
        return super.onContextItemSelected(item);
    }

    private void deleteSelectedItemFromRealtimeDatabaseAndStorage(final int position) {
        //create dialog
        final AlertDialog dialogBuilder = new AlertDialog.Builder(this).create();
        //inflate view
        LayoutInflater inflater = this.getLayoutInflater();
        @SuppressLint("InflateParams") View dialogView = inflater.inflate(R.layout.dialog_delete_photo, null);
        //initialize views
        Button ok = dialogView.findViewById(R.id.btn_dialogDelete_ok);
        Button cancel = dialogView.findViewById(R.id.btn_dialogDelete_cancel);
        //set on click listeners
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //get current image
                Upload upload = uploads.get(position);
                final String uploadID = upload.getId();
                //get reference to storage from url
                StorageReference currentStoreRef = mStorage.getReferenceFromUrl(upload.getImageUrl());
                //delete image in storage
                currentStoreRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //if successful, delete image from database
                        mDatabaseRef.child(uploadID).removeValue();
                        Toast.makeText(GalleryActivity.this, "Foto gelöscht", Toast.LENGTH_SHORT).show();
                    }
                });
                dialogBuilder.dismiss();
            }
        });
        //if cancel is clicked, close dialog
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogBuilder.dismiss();
            }
        });

        dialogBuilder.setView(dialogView);
        dialogBuilder.show();
    }
}
