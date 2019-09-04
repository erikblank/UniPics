package com.example.unipics.Gallery;

import android.net.Uri;

import java.util.ArrayList;

public interface UploadListener {
    public void onProgress(int indexOfImagesUploaded);
    public void onResult(String result);
}
