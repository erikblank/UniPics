package com.example.unipics.Gallery;

public class Upload {
    private String imageUrl;

    //empty constructor needed
    public Upload(){

    }

    public Upload(String imageUrl){
        this.imageUrl = imageUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String mImageUrl) {
        this.imageUrl = mImageUrl;
    }
}
