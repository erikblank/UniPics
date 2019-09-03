package com.example.unipics.ImageActivity;

import com.google.firebase.database.Exclude;

public class Note {
    @Exclude
    public String noteId;
    public String noteText;

    public Note (){

    }

    public Note (String noteText){
        this.noteText = noteText;
    }

    public String getNoteId() {
        return noteId;
    }

    public String getNoteText() {
        return noteText;
    }

    public void setNoteId(String noteId) {
        this.noteId = noteId;
    }

    public void setNoteText(String noteText) {
        this.noteText = noteText;
    }
}
