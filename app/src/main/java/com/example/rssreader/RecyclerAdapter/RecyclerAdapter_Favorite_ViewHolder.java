package com.example.rssreader.RecyclerAdapter;

import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.rssreader.R;

public class RecyclerAdapter_Favorite_ViewHolder extends RecyclerView.ViewHolder   {
    public TextView mTitle;
    public TextView mDate;
    public TextView mSite;
    public ImageButton mDelete;
    public ImageButton mMove;

    public RecyclerAdapter_Favorite_ViewHolder(View view) {
        super(view);
        this.mTitle = view.findViewById(R.id.title);
        this.mDate = view.findViewById(R.id.date);
        this.mSite = view.findViewById(R.id.site);
        this.mDelete = view.findViewById(R.id.button_delete);
        this.mMove = view.findViewById(R.id.button_move);
    }
}
