package com.example.unipics.MainMenu;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Toast;

import com.example.unipics.Authentification.LogInActivity;
import com.example.unipics.Gallery.GalleryActivity;
import com.example.unipics.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.util.ArrayList;
import java.util.List;

import static com.example.unipics.Constants.KEY_FOLDER;
import static com.example.unipics.Constants.TITLE_FOLDER_ACTIVITY;


public class FolderActivity extends AppCompatActivity {

    private DatabaseReference myRef;
    private GridView gridFolder;
    private List<Folder> folders;
    private FolderAdapter folderAdapter;


    String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folder);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(TITLE_FOLDER_ACTIVITY);
        setSupportActionBar(toolbar);
        init();
        populateGridViewWithDataBase();
        onFolderClicked();
    }

    private void init() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        myRef = database.getReference(userID);

    }
    //simple click on Folder will open the GalleryActivity
    private void onFolderClicked() {
        gridFolder.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Folder folder = folders.get(position);
                Intent intent = new Intent(FolderActivity.this, GalleryActivity.class);
                intent.putExtra(KEY_FOLDER, folder);
                startActivity(intent);
            }
        });
    }

    private void addDeleteDialog(final String folderID) {
        final AlertDialog dialogBuilder = new AlertDialog.Builder(this).create();
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_delete_folder, null);
        Button ok = dialogView.findViewById(R.id.btn_dialogDelete_ok);
        Button cancel = dialogView.findViewById(R.id.btn_dialogDelete_cancel);

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myRef.child(folderID).removeValue();
                dialogBuilder.dismiss();
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogBuilder.dismiss();
            }
        });

        dialogBuilder.setView(dialogView);
        dialogBuilder.show();

    }

    private void populateGridViewWithDataBase() {
        gridFolder = findViewById(R.id.gv_folders);
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                folders = new ArrayList<>();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Folder folder = postSnapshot.getValue(Folder.class);
                    String folderID = postSnapshot.getKey();
                    folder.setFolderId(folderID);
                    folders.add(folder);
                }
                folderAdapter = new FolderAdapter(FolderActivity.this, folders);
                gridFolder.setAdapter(folderAdapter);
                gridFolder.setEmptyView(findViewById(R.id.emptyElement_folder));
                registerForContextMenu(gridFolder);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(FolderActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


    }

    //inflates menu of activity
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_folder_activity, menu);
        return true;
    }

    // manages clicks on items of menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.icon_add_folder:
                addFolderDialog();
                return true;

            case R.id.item_logout:
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(FolderActivity.this, LogInActivity.class));
                finish();
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    //rd try
    private void addFolderDialog() {
        final AlertDialog dialogBuilder = new AlertDialog.Builder(this).create();
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_folder, null);
        final EditText editText = dialogView.findViewById(R.id.editText_folderName);
        Button ok = dialogView.findViewById(R.id.btn_createFolder);
        Button cancel = dialogView.findViewById(R.id.btn_cancelFolder);

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editText.length() == 0) {
                    editText.setError("Gebe einen Namen ein");
                    return;
                }
                if (editText.length() > 15) {
                    editText.setError("Name muss kürzer als 15 Zeichen sein");
                    return;
                }else{
                    String folderName = editText.getText().toString().trim();
                    for (Folder folder: folders){
                        if (folder.getFolderName().equals(folderName)){
                            editText.setError("A folder with this name already exists");
                            return;
                        }
                    }
                    Folder folder = new Folder(folderName);
                    myRef.push().setValue(folder);
                }
                dialogBuilder.dismiss();
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogBuilder.dismiss();
            }
        });

        dialogBuilder.setView(dialogView);
        dialogBuilder.show();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_folder, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch(item.getItemId()){
            case R.id.menu_folder_rename:
                addRenameDialog(folders.get(info.position).getFolderId());
                return true;
            case R.id.menu_folder_delete:
                addDeleteDialog(folders.get(info.position).getFolderId());
                return true;
            default:
                return super.onContextItemSelected(item);
        }

    }

    private void addRenameDialog(final String folderID) {
        final AlertDialog dialogBuilder = new AlertDialog.Builder(this).create();
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_rename_folder, null);
        Button ok = dialogView.findViewById(R.id.btn_rename_ok);
        Button cancel = dialogView.findViewById(R.id.btn_rename_cancel);
        final EditText newName = dialogView.findViewById(R.id.editText_rename);

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String name = newName.getText().toString().trim();
                        Folder folder = new Folder(name);
                        if (!name.isEmpty()){
                            dataSnapshot.getRef().child(folderID).child("folderName").setValue(name);
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                dialogBuilder.dismiss();
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogBuilder.dismiss();
            }
        });

        dialogBuilder.setView(dialogView);
        dialogBuilder.show();


    }
}
