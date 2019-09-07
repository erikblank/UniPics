package com.example.unipics.MainMenu;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folder);
        init();
        populateGridViewWithDataBase();
        onFolderClicked();
    }

    private void init() {
        //set toolbar and give it a title
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(TITLE_FOLDER_ACTIVITY);
        setSupportActionBar(toolbar);

        //initialize databases
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        myRef = database.getReference(userID);

        //display message if there is no internet connection
        noInternetConnection();

        //initialize gridView
        gridFolder = findViewById(R.id.gv_folders);

    }

    //check if there is a network connection and display a message to the user if not
    private void noInternetConnection() {
        if (!isNetworkAvailable()){
            Toast.makeText(this, "Kein Internet...", Toast.LENGTH_LONG).show();
        }
    }

    //return a boolean, which is true if there is an internet connection
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    //get the data out of realtimedatabase and populates the gridview everytime when an entry is changed, added or removed
    private void populateGridViewWithDataBase() {
        //add listener to database
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                folders = new ArrayList<>();
                //get all folder out of the current direction of the database
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Folder folder = postSnapshot.getValue(Folder.class);
                    //get the folderID and set it to the object
                    String folderID = postSnapshot.getKey();
                    folder.setFolderId(folderID);
                    //add folder to list of all folders
                    folders.add(folder);
                }
                //initialize and set adapter to folder gridview
                folderAdapter = new FolderAdapter(FolderActivity.this, folders);
                gridFolder.setAdapter(folderAdapter);
                //if there are no items in the gridview, show another view instead
                gridFolder.setEmptyView(findViewById(R.id.emptyElement_folder));
                //register context menu to gridView to enable rename or delete function
                registerForContextMenu(gridFolder);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(FolderActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


    }

    //inflate contextMenu with custom menu_folder.xml file
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_folder, menu);
    }

    //add functions on contextMenuItems
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch(item.getItemId()){
            case R.id.menu_folder_rename:
                //folder.get(info.position) gets the current position of folder item
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
        //create dialog
        final AlertDialog dialogBuilder = new AlertDialog.Builder(this).create();
        //inflate the view
        LayoutInflater inflater = this.getLayoutInflater();
        //initialize views
        View dialogView = inflater.inflate(R.layout.dialog_rename_folder, null);
        Button ok = dialogView.findViewById(R.id.btn_rename_ok);
        Button cancel = dialogView.findViewById(R.id.btn_rename_cancel);
        final EditText newName = dialogView.findViewById(R.id.editText_rename);

        //set on click listener to ok and cancel button
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        //get newName out of edittext
                        String name = newName.getText().toString().trim();
                        //check if name is not empty
                        if (!name.isEmpty()){
                            //if it is not empty, change value in database
                            dataSnapshot.getRef().child(folderID).child("folderName").setValue(name);
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) { }
                });
                dialogBuilder.dismiss();
            }
        });

        //closes the dialog
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogBuilder.dismiss();
            }
        });

        dialogBuilder.setView(dialogView);
        dialogBuilder.show();
    }

    private void addDeleteDialog(final String folderID) {
        //create dialog
        final AlertDialog dialogBuilder = new AlertDialog.Builder(this).create();
        //inflate the view
        LayoutInflater inflater = this.getLayoutInflater();
        //initialize views
        View dialogView = inflater.inflate(R.layout.dialog_delete_folder, null);
        Button ok = dialogView.findViewById(R.id.btn_dialogDelete_ok);
        Button cancel = dialogView.findViewById(R.id.btn_dialogDelete_cancel);

        //delete item in database
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myRef.child(folderID).removeValue();
                dialogBuilder.dismiss();
            }
        });
        //if cancel is clicked close dialog
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogBuilder.dismiss();
            }
        });

        dialogBuilder.setView(dialogView);
        dialogBuilder.show();

    }

    //click on Folder will open the GalleryActivity
    private void onFolderClicked() {
        gridFolder.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //get folder clicked on with parameter position
                Folder folder = folders.get(position);
                startGalleryActivity(folder);
            }
        });
    }

    //start gallery activity, when on folder is clicked
    private void startGalleryActivity(Folder folder) {
        Intent intent = new Intent(FolderActivity.this, GalleryActivity.class);
        //put id from folder in extra
        intent.putExtra(KEY_FOLDER, folder);
        startActivity(intent);
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
                logOut();
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    private void logOut(){
        //logout from firebase and go to login activity
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(FolderActivity.this, LogInActivity.class));
        finish();
    }

    //create dialog to add folder and add them to database
    private void addFolderDialog() {
        //create dialog
        final AlertDialog dialogBuilder = new AlertDialog.Builder(this).create();
        //inflate the view
        LayoutInflater inflater = this.getLayoutInflater();
        //initialize views
        View dialogView = inflater.inflate(R.layout.dialog_add_folder, null);
        final EditText editText = dialogView.findViewById(R.id.editText_folderName);
        Button ok = dialogView.findViewById(R.id.btn_createFolder);
        Button cancel = dialogView.findViewById(R.id.btn_cancelFolder);

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //check if text is not empty
                if (editText.length() == 0) {
                    editText.setError("Gebe einen Namen ein");
                    editText.requestFocus();
                    return;
                }
                //name should be shorter then 15 characters to avoid visual bugs
                if (editText.length() > 15) {
                    editText.setError("Name muss kürzer als 15 Zeichen sein");
                    editText.requestFocus();
                    return;
                }else{
                    //folder should have another name than one of the existing
                    String folderName = editText.getText().toString().trim();
                    for (Folder folder: folders){
                        if (folder.getFolderName().equals(folderName)){
                            editText.setError("A folder with this name already exists");
                            editText.requestFocus();
                            return;
                        }
                    }
                    //add folder to database
                    Folder folder = new Folder(folderName);
                    //push adds a new directory to database
                    myRef.push().setValue(folder);
                }
                dialogBuilder.dismiss();
            }
        });

        //if cancel is clicked, close dialog
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
