package com.example.unipics.MainMenu.DatabaseFolder;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface FolderDao {

    @Query("SELECT * FROM folder")
    List<Folder> getAll();

    @Insert
    void insertAll(Folder... folders);

    @Delete
    void delete(Folder folder);


}
