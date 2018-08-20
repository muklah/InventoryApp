package com.example.muklahhn.inventoryapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Muklah H N on 14/07/2018.
 */

public class InventoryDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "inventoriesDb.db";

    private static final int VERSION = 7;

    InventoryDbHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        final String CREATE_TABLE = "CREATE TABLE "  + InventoryContract.InventoryEntry.TABLE_NAME + " (" +
                InventoryContract.InventoryEntry.COLUMN_ID               + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                InventoryContract.InventoryEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                InventoryContract.InventoryEntry.COLUMN_PRICE + " INTEGER NOT NULL, " +
                InventoryContract.InventoryEntry.COLUMN_QUANTITY    + " INTEGER NOT NULL, " +
                InventoryContract.InventoryEntry.COLUMN_IMAGE    + " TEXT NOT NULL);";

        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + InventoryContract.InventoryEntry.TABLE_NAME);
        onCreate(db);
    }
}
