package com.example.muklahhn.inventoryapp;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.muklahhn.inventoryapp.data.InventoryContract;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;

import static java.security.AccessController.getContext;

public class AddInventory extends AppCompatActivity {

    public static final int IMAGE_CODE = 20;
    public static final int EXTERNAL_STORAGE__PERMISSION_CODE = 21;

    private String imageUri = "no image";

    private ImageView image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_inventory);

        image = (ImageView) findViewById(R.id.imagen_producto);

        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imagePermissions(v);
            }
        });

    }

    public void imagePermissions(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_GRANTED) {
                selectImage();
            } else {
                String[] permissionRequest = {Manifest.permission.READ_EXTERNAL_STORAGE};
                requestPermissions(permissionRequest, EXTERNAL_STORAGE__PERMISSION_CODE);
            }
        } else {
            selectImage();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == EXTERNAL_STORAGE__PERMISSION_CODE && grantResults[0] ==
                PackageManager.PERMISSION_GRANTED) {
            selectImage();
        } else {
            Toast.makeText(this, R.string.app_name, Toast.LENGTH_LONG).show();
        }
    }

    private void selectImage() {
        Intent selectImage = new Intent(Intent.ACTION_PICK);

        File imageFile = Environment.getExternalStoragePublicDirectory
                (Environment.DIRECTORY_PICTURES);
        String imagePath = imageFile.getPath();
        Uri data = Uri.parse(imagePath);

        selectImage.setDataAndType(data, "image/*");

        startActivityForResult(selectImage, IMAGE_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == IMAGE_CODE && resultCode == RESULT_OK) {
            if (data != null) {
            }
            Uri mImageUri = data.getData();
            imageUri = mImageUri.toString();

            Picasso.with(this).load(mImageUri)
                    .placeholder(R.mipmap.ic_launcher)
                    .fit()
                    .into(image);
        }
    }


    public void onClickAddInventory(View view) {

        String name = ((EditText) findViewById(R.id.name)).getText().toString();
        String price = ((EditText) findViewById(R.id.price)).getText().toString();
        String quantity = ((EditText) findViewById(R.id.quantity)).getText().toString();

        if (name.length() == 0 || price.length() == 0 || quantity.length() == 0) {
            String error = "Fill all information to add product";
            Toast.makeText(getBaseContext(), error.toString(), Toast.LENGTH_LONG).show();
            return;
        }

        ContentValues contentValues = new ContentValues();
        contentValues.put(InventoryContract.InventoryEntry.COLUMN_NAME, name);
        contentValues.put(InventoryContract.InventoryEntry.COLUMN_PRICE, price);
        contentValues.put(InventoryContract.InventoryEntry.COLUMN_QUANTITY, quantity);
        contentValues.put(InventoryContract.InventoryEntry.COLUMN_IMAGE, imageUri);

        Uri uri = getContentResolver().insert(InventoryContract.InventoryEntry.CONTENT_URI, contentValues);

        if(uri != null) {
            String text = "Added Successfully";
            Toast.makeText(getBaseContext(), text.toString(), Toast.LENGTH_LONG).show();
        }

        finish();

    }
}
