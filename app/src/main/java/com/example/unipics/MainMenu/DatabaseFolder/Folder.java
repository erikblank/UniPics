package com.example.unipics.MainMenu.DatabaseFolder;


import com.google.firebase.firestore.Exclude;

public class Folder {

    @Exclude
    private String id;
    private String folderName;
    private boolean isSelected = false;

    public String getFolderName() {
        return folderName;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
