package com.example.unipics.MainMenu;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.example.unipics.MainMenu.DatabaseFolder.Folder;
import com.example.unipics.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;


public class CreateFolder extends AppCompatActivity implements View.OnClickListener {
    Button btnOkay, btnCancel;
    EditText editTextFolderName;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_folder);
        initViews();

    }

    //find views
    private void initViews() {
        editTextFolderName = findViewById(R.id.editText_folderName);
        btnCancel = findViewById(R.id.btn_cancelFolder);
        btnOkay = findViewById(R.id.btn_createFolder);
        btnOkay.setClickable(false);
        db = FirebaseFirestore.getInstance();

        btnOkay.setOnClickListener(this);
        btnCancel.setOnClickListener(this);


    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            //start FolderActivity and finish current Activity
            case R.id.btn_cancelFolder:
                startFolderActivity();
                break;
            case R.id.btn_createFolder:
                saveFolderNameInDB();
                startFolderActivity();
        }
    }

    private void startFolderActivity() {
        startActivity(new Intent(CreateFolder.this, FolderActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
        finish();
    }

    private void saveFolderNameInDB() {
        String folderName = editTextFolderName.getText().toString().trim();
        if (!folderName.isEmpty()){
            btnOkay.setClickable(true);
            Folder folder = new Folder();
            folder.setFolderName(folderName);
            String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
            CollectionReference dbFolder = db.collection(userID);
            dbFolder.add(folder)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            Toast.makeText(CreateFolder.this, "Folder Added", Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(CreateFolder.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });

        }
    }
}
