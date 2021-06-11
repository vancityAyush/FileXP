package com.ak11.filexp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileUtils;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ak11.filexp.databinding.ActivityMainBinding;
import com.ak11.filexp.databinding.DialogNewFolderBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.shashank.sony.fancytoastlib.FancyToast;

import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, View.OnClickListener, AdapterView.OnItemLongClickListener {

    ActivityMainBinding binding;

    static File currentPath;
    ListView listView;
    ListAdapter adapter;
    TextView txtCurrentDirectory;
    FloatingActionButton fabNewFolder;
    public final int REQUEST_CODE=1109; // Request code for Storage Permission




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        txtCurrentDirectory = binding.txtCurrentDIrectory;
        listView = binding.listView;
        fabNewFolder = binding.fabNewFolder;

        isStoragePermissionGranted();

//        currentPath = Environment.getExternalStorageDirectory();
//        txtCurrentDirectory.setText(currentPath.getAbsolutePath());
//        adapter = new ListAdapter(this,currentPath);
//        listView.setAdapter(adapter);
        updateList(Environment.getExternalStorageDirectory());

        listView.setOnItemClickListener(this);
        listView.setOnItemLongClickListener(this);



        txtCurrentDirectory.setOnClickListener(this);
        fabNewFolder.setOnClickListener(this);

    }

    public  void showToast(String message, int type){
        FancyToast.makeText(MainActivity.this,message, Toast.LENGTH_SHORT,type,false).show();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull  String[] permissions, @NonNull  int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==REQUEST_CODE && grantResults[0]== PackageManager.PERMISSION_GRANTED){
            showToast("Write Permission Granted!",FancyToast.SUCCESS);
        }else if(requestCode==REQUEST_CODE && grantResults[0] == PackageManager.PERMISSION_DENIED){
            showToast("Write Permission Not Granted!",FancyToast.ERROR);
        }
    }

    public boolean isStoragePermissionGranted(){
        if(Build.VERSION.SDK_INT>=23) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                showToast("Write Permission Granted!", FancyToast.INFO);
                return true;
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
                return false;
            }
        }
        else{
            showToast("Write Permission Granted!",FancyToast.INFO);
            return true;
        }
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


        File nextDirectory = adapter.getFile(position);
        goToNewDirectory(nextDirectory);


    }

    private void goToNewDirectory(File newDirectory){
        if(newDirectory.isDirectory() && newDirectory.list()!=null) {
            updateList(newDirectory);
        }
        else if(newDirectory.isFile()){
            showToast(newDirectory.getName()+" is a File",FancyToast.INFO);
            if(adapter.isImage(newDirectory.getPath())){
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setDataAndType(Uri.parse(newDirectory.getAbsolutePath()), "image/*");
                startActivity(intent);
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.txtCurrentDIrectory:
                 goToPreviousDirectory();
                 break;
            case R.id.fabNewFolder:
                createNewFolder(currentPath.getAbsoluteFile());
                //showToast("New Folder Created",FancyToast.SUCCESS);
                break;
        }
    }

    @Override
    public void onBackPressed() {
        goToPreviousDirectory();
    }

    private void goToPreviousDirectory(){
        File prevDirectory = new File(currentPath.getParent());
        goToNewDirectory(prevDirectory);
    }
    private void createNewFolder(File currentPath){
        if(currentPath.isDirectory()){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            View dialogView = getLayoutInflater().inflate(R.layout.dialog_new_folder,null);
            DialogNewFolderBinding dialogBinding = DialogNewFolderBinding.bind(dialogView);
            builder.setView(dialogView).
                    setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            dialog.cancel();
                        }
                    }).setNegativeButton("Create a New Folder", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String folderName = dialogBinding.folderName.getText().toString();
                    File newFolder = new File(currentPath,folderName);
                    if(newFolder.mkdir()){
                        showToast(String.format("%s was created",folderName),FancyToast.SUCCESS);
                        updateList(currentPath);

                    }else{
                        showToast("New Folder was not created",FancyToast.ERROR);
                    }
                }
            });

            builder.create();
            builder.show();


        }
    }

    private void updateList(File path){
        currentPath = path;
        txtCurrentDirectory.setText(path.getAbsolutePath());
        adapter = new ListAdapter(this,path);
        listView.setAdapter(adapter);

    }
    boolean deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                 deleteRecursive(child);

        return fileOrDirectory.delete();
    }


    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        String selectedFile = adapter.files[position];
        File deleteFile = new File(currentPath,selectedFile);

        AlertDialog.Builder deleteDialog = new AlertDialog.Builder(this);

        deleteDialog.setIcon(R.drawable.delete);
        deleteDialog.setMessage(String .format("Are you Sure you want to Delete %s ?",selectedFile));
        deleteDialog.setPositiveButton("DELETE!", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(deleteRecursive(deleteFile)) {
                    showToast(selectedFile + " is deleted", FancyToast.WARNING);
                    updateList(currentPath);
                }
                else
                    showToast( selectedFile+" was not deleted",FancyToast.ERROR);

            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                dialog.cancel();
            }
        });

        deleteDialog.create();
        deleteDialog.show();

        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.info){
            showToast("#AK_11",FancyToast.INFO);
        }
        return true;
    }
}