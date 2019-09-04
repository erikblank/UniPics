package com.example.unipics.ImageActivity;

import android.annotation.SuppressLint;
import android.media.Image;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
    private String folderId;

    private DatabaseReference mDatabaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
        init();
        makeEditTextScrollable();
        enableSaveButton();
        saveNote();
        initNoteFromDB();
    }

    private void initNoteFromDB() {
        mDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    Note note = dataSnapshot.getValue(Note.class);
                    //note.setNoteId(dataSnapshot.getKey());
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

    private void saveNote() {
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String noteText = editText.getText().toString().trim();
                if (!noteText.isEmpty()){
                    Note note = new Note(noteText);
                    mDatabaseRef.setValue(note);
                }
            }
        });
    }

    //makes the save button visible
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

    private void init() {
        String uri = getIntent().getStringExtra(KEY_IMAGE);
        folderId = getIntent().getStringExtra(KEY_PATH_IMAGE);
        View bottomSheet = findViewById(R.id.bottom_sheet);
        mBottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        PhotoView photoView = findViewById(R.id.photo_view);
        Picasso.get().load(Uri.parse(uri)).into(photoView);
        editText = findViewById(R.id.editText_note);
        btnSave = findViewById(R.id.btn_saveNote);
        mDatabaseRef = FirebaseDatabase.getInstance().getReference(folderId + "/note");
    }

}
