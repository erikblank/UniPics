package com.example.unipics.MainMenu;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.unipics.R;

import java.util.List;

public class FolderAdapter extends ArrayAdapter<Folder> {
    private List<Folder> folderList;
    private Context context;

    public FolderAdapter(Context context, List<Folder> folderList) {
        super(context, R.layout.folder_item, folderList);
        this.context = context;
        this.folderList = folderList;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        View v = convertView;

        if(v == null){
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = layoutInflater.inflate(R.layout.folder_item, null);
        }
        //get current folder with parameter position
        Folder folder = folderList.get(position);

        if (folder != null){
            //set name of folder to view
            TextView name = v.findViewById(R.id.textView_folderName);
            name.setText(folder.getFolderName());

        }
        return v;
    }
}


