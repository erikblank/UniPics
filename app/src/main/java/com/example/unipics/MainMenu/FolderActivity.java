package com.example.unipics.MainMenu;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import com.example.unipics.Authentification.LogInActivity;
import com.example.unipics.Gallery.GalleryActivity;
import com.example.unipics.MainMenu.DatabaseFolder.Folder;
import com.example.unipics.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.List;

import static com.example.unipics.Constants.DB_FOLDER;
import static com.example.unipics.Constants.KEY_FOLDER;


public class FolderActivity extends AppCompatActivity {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    GridView gridFolder;
    List<Folder> folderList;
    FolderAdapter folderAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folder);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        initFolderDB();
        init();
        onFolderClicked();
    }

    //simple click on Folder will open the GalleryActivity
    private void onFolderClicked() {
        gridFolder.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(FolderActivity.this, GalleryActivity.class);
                intent.putExtra(KEY_FOLDER, folderList.get(position).getId());
                startActivity(intent);
            }
        });
    }

    //get all folder stored in the database and sets ID
    private void initFolderDB() {
        folderList = new ArrayList<>();
        db = FirebaseFirestore.getInstance();
        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db.collection(DB_FOLDER + userID).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if(!queryDocumentSnapshots.isEmpty()){
                            List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                            for(DocumentSnapshot d : list){
                                Folder folder = d.toObject(Folder.class);
                                folder.setId(d.getId());
                                folderList.add(folder);
                            }
                            folderAdapter.notifyDataSetChanged();
                        }
                    }
                });
    }

    private void init() {
        gridFolder = findViewById(R.id.gv_folders);
        folderAdapter = new FolderAdapter(this, folderList);
        gridFolder.setAdapter(folderAdapter);
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
                //start CreateFolderActivity and add flag for no animation
                startActivity(new Intent(FolderActivity.this, CreateFolder.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
                finish();
                return true;

            case R.id.item_logout:
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(FolderActivity.this, LogInActivity.class));
                finish();
                return true;

        }
        return super.onOptionsItemSelected(item);
    }
}
