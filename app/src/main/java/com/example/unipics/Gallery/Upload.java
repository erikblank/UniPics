package com.example.unipics.Gallery;

import com.google.firebase.database.Exclude;

public class Upload{
    private String id;
    private String imageUrl;

    //empty constructor needed for database
    public Upload(){

    }

    //constructor to set imageUrl
    public Upload(String imageUrl){
        this.imageUrl = imageUrl;
    }

    @Exclude
    public String getId() {
        return id;
    }

    @Exclude
    public void setId(String id) {
        this.id = id;
    }

    public String getImageUrl() {
        return imageUrl;
    }


}
