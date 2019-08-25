package com.example.unipics.MainMenu.DatabaseFolder;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity
public class Folder {

    @PrimaryKey(autoGenerate = true)
    private int folderID;

    @ColumnInfo(name = "folder_name")
    private String folderName;

    public Folder(int folderID, String folderName){
        this.folderID = folderID;
        this.folderName = folderName;

    }

    public int getFolderID() {
        return folderID;
    }

    public String getFolderName() {
        return folderName;
    }

    public void setFolderID(int folderID) {
        this.folderID = folderID;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }
}
