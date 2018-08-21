package com.example.muklahhn.inventoryapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.muklahhn.inventoryapp.data.InventoryContract;
import com.squareup.picasso.Picasso;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DetailActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    public static final int IMAGE_CODE = 20;
    public static final int EXTERNAL_STORAGE__PERMISSION_CODE = 21;

    private String imageUri = "no image";

    public static final String[] INVENTORIES_DETAIL_PROJECTION = {
            InventoryContract.InventoryEntry.COLUMN_ID,
            InventoryContract.InventoryEntry.COLUMN_NAME,
            InventoryContract.InventoryEntry.COLUMN_PRICE,
            InventoryContract.InventoryEntry.COLUMN_QUANTITY,
            InventoryContract.InventoryEntry.COLUMN_IMAGE
    };

    public static final int INDEX_NAME = 1;
    public static final int INDEX_PRICE = 2;
    public static final int INDEX_QUANTITY = 3;
    public static final int INDEX_IMAGE = 4;

    private static final int ID_DETAIL_LOADER = 999;

    private Uri mUri;

    @BindView(R.id.name)
    EditText mName;
    @BindView(R.id.price)
    EditText mPrice;
    @BindView(R.id.quantity)
    EditText mQuantity;
    @BindView(R.id.image)
    ImageView mImage;

    @BindView(R.id.increase)
    Button mIncrease;
    @BindView(R.id.decrease)
    Button mDecrease;
    @BindView(R.id.delete)
    Button mDelete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        ButterKnife.bind(this);

        mUri = getIntent().getData();

        if (mUri == null) throw new NullPointerException("URI for DetailActivity cannot be null");

        getSupportLoaderManager().initLoader(ID_DETAIL_LOADER, null, this);

        mImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imagePermissions(v);
            }
        });

        mIncrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String favoriteMovieId = mUri.getLastPathSegment();

                Uri uri = InventoryContract.InventoryEntry.CONTENT_URI;
                uri = uri.buildUpon().appendPath(favoriteMovieId).build();

                int quantityString = Integer.parseInt(mQuantity.getText().toString().trim());
                if (quantityString >= 0) {
                    ContentValues values = new ContentValues();
                    values.put(InventoryContract.InventoryEntry.COLUMN_QUANTITY, ++quantityString);
                    getContentResolver().update(uri, values, null, null);
                }
            }
        });

        mDecrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String favoriteMovieId = mUri.getLastPathSegment();

                Uri uri = InventoryContract.InventoryEntry.CONTENT_URI;
                uri = uri.buildUpon().appendPath(favoriteMovieId).build();

                int quantityString = Integer.parseInt(mQuantity.getText().toString().trim());
                if (quantityString > 0) {
                    ContentValues values = new ContentValues();
                    values.put(InventoryContract.InventoryEntry.COLUMN_QUANTITY, --quantityString);
                    getContentResolver().update(uri, values, null, null);
                }
            }
        });


        mDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(DetailActivity.this);

                builder.setTitle("Confirmation");
                builder.setMessage("Are you sure you want to delete this item?");

                builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                getContentResolver().delete(mUri, null, null);
                                String text = "Deleted From Database";
                                Toast.makeText(getBaseContext(), text, Toast.LENGTH_LONG).show();
                                Intent back = new Intent(DetailActivity.this, MainActivity.class);
                                DetailActivity.this.startActivity(back);
                            }
                });

                builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Do nothing
                        dialog.dismiss();
                    }
                });

                AlertDialog alert = builder.create();
                alert.show();
            }
        });

    }

    public void onClickSave(View view) {
        saveEdit();
    }

    @SuppressLint("StringFormatInvalid")
    public void onClickOrder(View view) {
        String orderName = mName.getText().toString();
        String orderQuantity = mQuantity.getText().toString();

        String message = createMatchSummary(orderName, orderQuantity);

        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:"));
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.email_subject, orderName));
        intent.putExtra(Intent.EXTRA_TEXT, message);

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    private String createMatchSummary(String orderName, String orderQuantity) {
        String resultMessage = getString(R.string.orderMessage) + orderQuantity + " items of " + orderName;
        resultMessage += "\n" + getString(R.string.thank_you);
        return resultMessage;
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
                    .into(mImage);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle loaderArgs) {

        switch (loaderId) {

            case ID_DETAIL_LOADER:

                return new CursorLoader(this,
                        mUri,
                        INVENTORIES_DETAIL_PROJECTION,
                        null,
                        null,
                        null);

            default:
                throw new RuntimeException("Loader Not Implemented: " + loaderId);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        boolean cursorHasValidData = false;
        if (data != null && data.moveToFirst()) {

            cursorHasValidData = true;
        }

        if (!cursorHasValidData) {

            return;
        }

        String nameIndex = data.getString(INDEX_NAME);
        int priceIndex = data.getInt(INDEX_PRICE);
        int quantityIndex = data.getInt(INDEX_QUANTITY);
        imageUri = data.getString(INDEX_IMAGE);

        mName.setText(nameIndex);
        mPrice.setText(String.valueOf(priceIndex));
        mQuantity.setText(String.valueOf(quantityIndex));

        Picasso.with(this).load(imageUri)
                .placeholder(R.mipmap.ic_launcher)
                .fit()
                .into(mImage);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mName.setText("");
        mPrice.setText("");
        mQuantity.setText("");
    }

    private void saveEdit() {
        String name = mName.getText().toString().trim();
        String price = mPrice.getText().toString().trim();
        String quantity = mQuantity.getText().toString().trim();

        if (name.length() == 0 || price.length() == 0 || quantity.length() == 0) {
            String error = "Fill all information to edit product";
            Toast.makeText(getBaseContext(), error.toString(), Toast.LENGTH_LONG).show();
            return;
        }

            ContentValues values = new ContentValues();

            values.put(InventoryContract.InventoryEntry.COLUMN_NAME, name);
            values.put(InventoryContract.InventoryEntry.COLUMN_PRICE, price);
            values.put(InventoryContract.InventoryEntry.COLUMN_QUANTITY, quantity);
            values.put(InventoryContract.InventoryEntry.COLUMN_IMAGE, imageUri);

            int rowsAffected = getContentResolver().update(mUri, values, null, null);
            if (rowsAffected == 0) {
                Toast.makeText(this, "update_failed",
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "update successful",
                        Toast.LENGTH_SHORT).show();
                finish();
            }

        }

}
