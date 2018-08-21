package com.example.muklahhn.inventoryapp;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.muklahhn.inventoryapp.data.InventoryContract;
import com.squareup.picasso.Picasso;

/**
 * Created by Muklah H N on 05/08/2018.
 */

public class CursorAdapter extends RecyclerView.Adapter<CursorAdapter.InventoryViewHolder> {

    private Cursor mCursor;
    private Context mContext;
    private final MainActivity mainActivity;
    private final CursorAdapter.RecyclerViewAdapterOnClickHandler mClickHandler;

    public interface RecyclerViewAdapterOnClickHandler {
        void onClickItem(long id);
    }

    public CursorAdapter(@NonNull Context context, RecyclerViewAdapterOnClickHandler clickHandler, MainActivity mainContext) {
        mContext = context;
        mClickHandler = clickHandler;
        this.mainActivity = mainContext;
    }

    class InventoryViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView empty;
        TextView name;
        TextView price;
        TextView quantity;
        ImageView image;
        Button sale;

        public InventoryViewHolder(View itemView) {
            super(itemView);

            name = (TextView) itemView.findViewById(R.id.name);
            price = (TextView) itemView.findViewById(R.id.price);
            quantity = (TextView) itemView.findViewById(R.id.quantity);
            image = (ImageView) itemView.findViewById(R.id.image);
            sale = (Button) itemView.findViewById(R.id.sale);
            itemView.setOnClickListener(this);
        }


        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            mCursor.moveToPosition(adapterPosition);

            int inventoryIdIndex = mCursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_ID);
            long id = mCursor.getLong(inventoryIdIndex);
            mClickHandler.onClickItem(id);
        }

    }

    @Override
    public InventoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.inventory_item, parent, false);

        return new InventoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(InventoryViewHolder holder, int position) {

        int idIndex = mCursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_ID);
        int nameIndex = mCursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_NAME);
        int priceIndex = mCursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PRICE);
        int quantityIndex = mCursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_QUANTITY);
        int pictureColumnIndex = mCursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_IMAGE);

        mCursor.moveToPosition(position);

        final int id = mCursor.getInt(idIndex);
        String name = mCursor.getString(nameIndex);
        int price = mCursor.getInt(priceIndex);
        final int quantity = mCursor.getInt(quantityIndex);
        Uri productPicture = Uri.parse(mCursor.getString(pictureColumnIndex));

        holder.itemView.setTag(id);
        holder.name.setText(name);

        String priceString = "Price: " + price;
        holder.price.setText(priceString);

        String quantityString = "Quantity: " + quantity;
        holder.quantity.setText(quantityString);

        Picasso.with(mContext).load(productPicture)
                .placeholder(R.mipmap.ic_launcher)
                .fit()
                .into(holder.image);

        holder.sale.setOnClickListener(new View.OnClickListener() {
            int updatedQuantity = quantity - 1;

            @Override
            public void onClick(View view) {
                mainActivity.IncrementDecrement(id, updatedQuantity);
            }
        });

    }

    @Override
    public int getItemCount() {
        if (mCursor == null) {
            return 0;
        }
        return mCursor.getCount();
    }

    public Cursor swapCursor(Cursor c) {
        if (mCursor == c) {
            return null;
        }
        Cursor temp = mCursor;
        this.mCursor = c;

        if (c != null) {
            this.notifyDataSetChanged();
        }
        return temp;
    }


}
