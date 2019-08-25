package com.example.unipics.MainMenu.DatabaseFolder;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

@Database(entities = {Folder.class}, version = 1, exportSchema = false)
public abstract class FolderDatabase extends RoomDatabase {
    public abstract FolderDao folderDao();
}