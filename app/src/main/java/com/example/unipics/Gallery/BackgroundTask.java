package com.example.unipics.Gallery;


import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.util.Objects;

public class BackgroundTask extends AsyncTask<Uri, Integer, String> {
    private UploadListener uploadListener;
    private DatabaseReference databaseReference;

    public BackgroundTask(UploadListener uploadListener, DatabaseReference databaseReference) {
        this.uploadListener = uploadListener;
        this.databaseReference = databaseReference;

    }

    //upload images to firebase
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected String doInBackground(Uri... imageURIs) {
        //get id of the current user
        String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        //upload all images to firebase
        for (int i = 0; i < imageURIs.length; i++) {
            final StorageReference storageReference = FirebaseStorage.getInstance().getReference(userId).child(System.currentTimeMillis() + ".jpg");
            storageReference.putFile(imageURIs[i]).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw Objects.requireNonNull(task.getException());
                    }

                    // Continue with the task to get the download URL
                    return storageReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        //get the downloadUri out of task object, to upload to database
                        Uri downloadUri = task.getResult();
                        if (downloadUri != null){
                            Upload upload = new Upload(downloadUri.toString());
                            databaseReference.push().setValue(upload);
                        }

                    }
                }
            });
            //publish progress to set progressbar in GalleryActivity
            publishProgress(i);
        }
        return "";
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        uploadListener.onProgress();

    }


}
