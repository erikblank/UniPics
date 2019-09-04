package com.example.unipics.Gallery;


import android.net.Uri;
import android.os.AsyncTask;
import androidx.annotation.NonNull;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;

public class BackgroundTask extends AsyncTask<Uri, Integer, String> {
    private UploadListener uploadListener;
    private DatabaseReference databaseReference;

    public BackgroundTask(UploadListener uploadListener, DatabaseReference databaseReference) {
        this.uploadListener = uploadListener;
        this.databaseReference = databaseReference;

    }


    @Override
    protected String doInBackground(Uri... imageURIs) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        for (int i = 0; i < imageURIs.length; i++) {
            final StorageReference storageReference = FirebaseStorage.getInstance().getReference(userId).child(System.currentTimeMillis() + ".jpg");
            storageReference.putFile(imageURIs[i]).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    // Continue with the task to get the download URL
                    return storageReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        Upload upload = new Upload(downloadUri.toString());
                        databaseReference.push().setValue(upload);

                    } else {
                        // Handle failures
                        // ...

                    }
                }
            });
            publishProgress(i);
            //publishProgress((i*100) / imageURIs.length);
        }
        return "";
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        uploadListener.onProgress();

    }


}
