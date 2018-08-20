package com.example.muklahhn.inventoryapp.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;

import static com.example.muklahhn.inventoryapp.data.InventoryContract.InventoryEntry.COLUMN_ID;
import static com.example.muklahhn.inventoryapp.data.InventoryContract.InventoryEntry.TABLE_NAME;

/**
 * Created by Muklah H N on 05/08/2018.
 */

public class InventoryContentProvider extends ContentProvider {

    public static final int INVENTORY = 100;
    public static final int INVENTORY_WITH_ID = 101;

    private static final UriMatcher sUriMatcher = buildUriMatcher();

    public static UriMatcher buildUriMatcher() {

        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        uriMatcher.addURI(InventoryContract.AUTHORITY, InventoryContract.PATH_INVENTORIES, INVENTORY);
        uriMatcher.addURI(InventoryContract.AUTHORITY, InventoryContract.PATH_INVENTORIES + "/#", INVENTORY_WITH_ID);

        return uriMatcher;
    }

    private InventoryDbHelper inventoryDbHelper;

    @Override
    public boolean onCreate() {
        Context context = getContext();
        inventoryDbHelper = new InventoryDbHelper(context);
        return true;
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {

        final SQLiteDatabase db = inventoryDbHelper.getWritableDatabase();

        int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case INVENTORY:

                long id = db.insert(TABLE_NAME, null, values);
                if ( id > 0 ) {
                    returnUri = ContentUris.withAppendedId(InventoryContract.InventoryEntry.CONTENT_URI, id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return returnUri;
    }


    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        final SQLiteDatabase db = inventoryDbHelper.getReadableDatabase();

        int match = sUriMatcher.match(uri);
        Cursor retCursor;

        switch (match) {

            case INVENTORY_WITH_ID: {

                String inventoryId = uri.getLastPathSegment();
                String[] selectionArguments = new String[]{inventoryId};

                retCursor = db.query(TABLE_NAME,
                        projection,
                        COLUMN_ID + " = ? ",
                        selectionArguments,
                        null,
                        null,
                        sortOrder);
                break;
            }

            case INVENTORY:
                retCursor =  db.query(TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        retCursor.setNotificationUri(getContext().getContentResolver(), uri);

        return retCursor;
    }


    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {

        final SQLiteDatabase db = inventoryDbHelper.getWritableDatabase();
        int match = sUriMatcher.match(uri);
        int inventoryDeleted;

        switch (match) {

            case INVENTORY_WITH_ID:
                String id = uri.getPathSegments().get(1);
                inventoryDeleted = db.delete(TABLE_NAME, "id=?", new String[]{id});
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (inventoryDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return inventoryDeleted;
    }


    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORY_WITH_ID:
                selection = InventoryContract.InventoryEntry.COLUMN_ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                if (values.containsKey(InventoryContract.InventoryEntry.COLUMN_NAME)) {
                    String name = values.getAsString(InventoryContract.InventoryEntry.COLUMN_NAME);
                    if (name == null) {
                        throw new IllegalArgumentException("name required");
                    }
                }
                if (values.containsKey(InventoryContract.InventoryEntry.COLUMN_PRICE)) {
                    Integer price = values.getAsInteger(InventoryContract.InventoryEntry.COLUMN_PRICE);
                    if (price != null && price < 0) {
                        throw new
                                IllegalArgumentException("price required");
                    }
                }
                if (values.size() == 0) {
                    return 0;
                }

                SQLiteDatabase database = inventoryDbHelper.getWritableDatabase();

                int rowsUpdated = database.update(InventoryContract.InventoryEntry.TABLE_NAME, values, selection, selectionArgs);

                if (rowsUpdated != 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }

                return rowsUpdated;
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    @Override
    public String getType(@NonNull Uri uri) {

        throw new UnsupportedOperationException("Not yet implemented");
    }

}
