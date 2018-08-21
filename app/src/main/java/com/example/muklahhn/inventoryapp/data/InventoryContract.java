package com.example.muklahhn.inventoryapp.data;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Muklah H N on 14/07/2018.
 */

public final class InventoryContract {

    public static final String AUTHORITY = "com.example.muklahhn.inventoryapp";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    public static final String PATH_INVENTORIES = "inventories";

    public static final class InventoryEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_INVENTORIES).build();

        public static final String TABLE_NAME = "inventories";

        public static final String COLUMN_ID  = "id";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_PRICE = "price";
        public static final String COLUMN_QUANTITY = "quantity";
        public static final String COLUMN_IMAGE = "image";

        public static Uri buildInventoriesUriWithId(long id) {
            return CONTENT_URI.buildUpon()
                    .appendPath(Long.toString(id))
                    .build();
        }

    }
}