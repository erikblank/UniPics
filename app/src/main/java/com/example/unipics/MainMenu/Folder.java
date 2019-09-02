package com.example.unipics.MainMenu;



import com.google.firebase.database.Exclude;
import java.io.Serializable;

public class Folder implements Serializable{

    @Exclude
    private String folderId;
    private String folderName;

    //empty constructor needed for database
    public Folder (){

    }

    public Folder (String folderName){
        this.folderName = folderName;
    }

    public String getFolderName() {
        return folderName;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }

    public String getFolderId() {
        return folderId;
    }

    public void setFolderId(String folderId) {
        this.folderId = folderId;
    }


}
