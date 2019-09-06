package com.example.unipics.ImageActivity;


public class Note {
    public String noteText;

    //empty constructor needed for firebase database
    public Note (){

    }

    public Note (String noteText){
        this.noteText = noteText;
    }

    public String getNoteText() {
        return noteText;
    }

}
