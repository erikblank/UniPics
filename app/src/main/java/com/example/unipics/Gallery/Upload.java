package com.example.unipics.Gallery;

public class Upload {
    private String id;
    private String imageUrl;


    //empty constructor needed
    public Upload(){

    }

    public Upload(String imageUrl){
        this.imageUrl = imageUrl;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String mImageUrl) {
        this.imageUrl = mImageUrl;
    }
}
