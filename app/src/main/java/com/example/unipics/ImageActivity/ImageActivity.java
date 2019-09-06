package com.example.unipics.ImageActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.Image;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.unipics.R;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import static com.example.unipics.Constants.KEY_IMAGE;
import static com.example.unipics.Constants.KEY_PATH_IMAGE;

public class ImageActivity extends AppCompatActivity {

    private EditText editText;
    private BottomSheetBehavior mBottomSheetBehavior;
    private Button btnSave;
    private DatabaseReference mDatabaseRef;

    private PhotoView photoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
        init();
        loadImageIntoPhotoView();
        makeEditTextScrollable();
        enableSaveButton();
        saveNote();
        initNoteFromDB();
    }

    private void init() {
        //init bottomSheet
        View bottomSheet = findViewById(R.id.bottom_sheet);
        //declare behavior of bottomsheet
        mBottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        //init chrisbanes photoView: https://github.com/chrisbanes/PhotoView
        photoView = findViewById(R.id.photo_view);
        //init views
        editText = findViewById(R.id.editText_note);
        btnSave = findViewById(R.id.btn_saveNote);
        //init database
        String folderId = getIntent().getStringExtra(KEY_PATH_IMAGE);
        mDatabaseRef = FirebaseDatabase.getInstance().getReference(folderId + "/note");
    }

    private void loadImageIntoPhotoView() {
        String uri = getIntent().getStringExtra(KEY_IMAGE);
        Picasso.get().load(Uri.parse(uri)).into(photoView);
    }

    //edittext is without this method not scrollable because the bottomsheet goes down and not the text
    @SuppressLint("ClickableViewAccessibility")
    private void makeEditTextScrollable() {
        editText.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                v.getParent().requestDisallowInterceptTouchEvent(true);
                if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP) {
                    v.getParent().requestDisallowInterceptTouchEvent(false);
                }
                return false;
            }
        });
    }

    //makes the save button visible, when bottom sheet is full expanded
    private void enableSaveButton() {
        mBottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View view, int i) {
                if (i == BottomSheetBehavior.STATE_EXPANDED){
                    btnSave.setVisibility(View.VISIBLE);
                }else{
                    btnSave.setVisibility(View.INVISIBLE);
                }
            }
            @Override
            public void onSlide(@NonNull View view, float v) {

            }
        });
    }

    private void saveNote() {
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String noteText = editText.getText().toString().trim();
                Note note = new Note(noteText);
                //save note to database
                mDatabaseRef.setValue(note);
                //collapse the bottomsheet
                mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                //focus out of edittext
                editText.clearFocus();
                Toast.makeText(ImageActivity.this, "Notiz gespeichert", Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void initNoteFromDB() {
        //if there is already a note, then get the note out of database and display it in the edittext
        mDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Note note = dataSnapshot.getValue(Note.class);
                    //note.setNoteId(dataSnapshot.getKey());
                    assert note != null;
                    String noteText = note.getNoteText();
                    editText.setText(noteText);
                    editText.setSelection(editText.getText().length());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
