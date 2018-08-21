package com.example.muklahhn.inventoryapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.muklahhn.inventoryapp.data.InventoryContract;

public class MainActivity extends AppCompatActivity implements
        CursorAdapter.RecyclerViewAdapterOnClickHandler,
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int INVENTORY_LOADER_ID = 0;

    private CursorAdapter mAdapter;
    RecyclerView mRecyclerView;

    TextView emptyList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRecyclerView = (RecyclerView) findViewById(R.id.inventories);
        emptyList = (TextView) findViewById(R.id.empty_list);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mAdapter = new CursorAdapter(this, this, this);
        mRecyclerView.setAdapter(mAdapter);

        FloatingActionButton fabButton = (FloatingActionButton) findViewById(R.id.fab);

        fabButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent addTaskIntent = new Intent(MainActivity.this, AddInventory.class);
                startActivity(addTaskIntent);
            }
        });

        getSupportLoaderManager().initLoader(INVENTORY_LOADER_ID, null, this);

    }

    public void IncrementDecrement(int id, int quantity) {
        if (quantity >= 0) {
            ContentValues values = new ContentValues();
            values.put(InventoryContract.InventoryEntry.COLUMN_QUANTITY, quantity);
            Uri updateUri = ContentUris.withAppendedId(InventoryContract.InventoryEntry.CONTENT_URI, id);
            getContentResolver().update(updateUri, values, null, null);
            Toast.makeText(this, "Quantity was change", Toast.LENGTH_SHORT).show();
            getSupportLoaderManager().restartLoader(INVENTORY_LOADER_ID, null, this);
        } else {
            Toast.makeText(this, "Product not available anymore", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClickItem(long id) {
        Intent inventoryDetailIntent = new Intent(MainActivity.this, DetailActivity.class);

        Uri uriInventoryClicked = InventoryContract.InventoryEntry.buildInventoriesUriWithId(id);
        inventoryDetailIntent.setData(uriInventoryClicked);
        startActivity(inventoryDetailIntent);
    }

    @Override
    protected void onResume() {
        super.onResume();

        getSupportLoaderManager().restartLoader(INVENTORY_LOADER_ID, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, final Bundle loaderArgs) {

        return new AsyncTaskLoader<Cursor>(this) {

            Cursor inventoryData = null;

            @Override
            protected void onStartLoading() {
                if (inventoryData != null) {

                    deliverResult(inventoryData);
                } else  {
                    forceLoad();
                }
        }

            @Override
            public Cursor loadInBackground() {

                try {
                    return getContentResolver().query(InventoryContract.InventoryEntry.CONTENT_URI,
                            null,
                            null,
                            null,
                            InventoryContract.InventoryEntry.COLUMN_ID);

                } catch (Exception e) {
                    Log.e(TAG, "Failed to asynchronously load data.");
                    e.printStackTrace();
                    return null;
                }
            }

            public void deliverResult(Cursor data) {
                inventoryData = data;
                if ((inventoryData != null) && (inventoryData.getCount() > 0)) {
                    emptyList.setVisibility(View.GONE);
                }
                else {
                    emptyList.setVisibility(View.VISIBLE);
                }

                super.deliverResult(data);
            }
        };

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

}
