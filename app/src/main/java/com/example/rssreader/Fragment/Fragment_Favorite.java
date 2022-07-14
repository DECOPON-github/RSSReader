package com.example.rssreader.Fragment;

import static com.example.rssreader.Debug.DEBUG_MODE;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rssreader.Activity.Activity_Webview;
import com.example.rssreader.DbAdapter.DbAdapter_Favorite;
import com.example.rssreader.Function.ItemMoveCallback;
import com.example.rssreader.Function.StartDragListener;
import com.example.rssreader.Item.Item_Favorite;
import com.example.rssreader.RecyclerAdapter.RecyclerAdapter_Favorite;

import java.util.ArrayList;
import java.util.List;

import com.example.rssreader.R;

public class Fragment_Favorite extends Fragment implements StartDragListener {
    private Boolean isStart;
    public RecyclerView mRecyclerView;
    private View mDivider;
    private List<Item_Favorite> mItems;
    public RecyclerAdapter_Favorite mAdapter;
    private ProgressBar mProgressBar;
    private DbAdapter_Favorite mDbAdapter;
    private ItemTouchHelper mItemTouchHelper;

    public Fragment_Favorite() { isStart = true; }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if ( DEBUG_MODE ) { Log.d("DEBUG_MODE", "Fragment_Favorite - onCreateView"); }
        View v = inflater.inflate(R.layout.fragment_favorite, container, false);
        findView(v);
        setVisibility(View.INVISIBLE);
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        if ( DEBUG_MODE ) { Log.d("DEBUG_MODE", "Fragment_Favorite - onViewCreated"); }
        super.onViewCreated(view, savedInstanceState);
        initRecyclerView();
        setRecyclerView();

        setVisibility(View.VISIBLE);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (isVisibleToUser && !isStart) {
            setRecyclerView ();
        }
    }

    private void initRecyclerView () {
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        RecyclerView.ItemDecoration itemDecoration =
                new DividerItemDecoration(mRecyclerView.getContext(), DividerItemDecoration.VERTICAL);
        ((DividerItemDecoration) itemDecoration).setDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.style_recyclerview_divider));
        mRecyclerView.addItemDecoration(itemDecoration);
        mRecyclerView.setHasFixedSize(true);
    }

    public void setRecyclerView () {
        if ( DEBUG_MODE ) { Log.d("DEBUG_MODE", "Fragment_Favorite - setRecyclerView"); }
        mAdapter = new RecyclerAdapter_Favorite(getContext(), setItemFavorite(), Fragment_Favorite.this, this) {
            @Override
            public void onItemClick(View view, int position, List<Item_Favorite> item) {
                Item_Favorite tItem = mItems.get(position);
                Intent intent = new Intent(getContext(), Activity_Webview.class);

                intent.putExtra("TITLE", tItem.getTitle());
                intent.putExtra("DATE", tItem.getDate());
                intent.putExtra("URL", tItem.getUrl());
                intent.putExtra("SITE", tItem.getSite());

                startActivity(intent);
            }
        };

        ItemTouchHelper.Callback callback =
                new ItemMoveCallback(mAdapter);
        mItemTouchHelper  = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(mRecyclerView);

        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void requestDrag(RecyclerView.ViewHolder viewHolder) {
        mItemTouchHelper.startDrag(viewHolder);
    }

    @SuppressLint("Range")
    private List<Item_Favorite> setItemFavorite() {
        mItems = new ArrayList<Item_Favorite>();

        mDbAdapter = new DbAdapter_Favorite(getContext());
        mDbAdapter.open();
        Cursor cursor = mDbAdapter.getTable();

        Item_Favorite currentItem = null;

        if (cursor.moveToFirst()) {
            do {
                currentItem = new Item_Favorite();
                currentItem.setTitle(cursor.getString(cursor.getColumnIndex(mDbAdapter.COL_TITLE)));
                currentItem.setDate(cursor.getString(cursor.getColumnIndex(mDbAdapter.COL_DATE)));
                currentItem.setUrl(cursor.getString(cursor.getColumnIndex(mDbAdapter.COL_URL)));
                currentItem.setSite(cursor.getString(cursor.getColumnIndex(mDbAdapter.COL_SITE)));
                mItems.add(currentItem);
            } while (cursor.moveToNext());
            cursor.close();
        } else {

        }

        return  mItems;
    }

    @Override
    public void onStart () {
        super.onStart();

        if (!isStart) {
            if ( DEBUG_MODE ) { Log.d("DEBUG_MODE", "Fragment_Favorite - onStart"); }
            setRecyclerView();
        }
    }

    @Override
    public void onResume () {
        super.onResume();
        if (isStart) {
            if ( DEBUG_MODE ) { Log.d("DEBUG_MODE", "Fragment_Favorite - onResume"); }
            isStart = false;
            setRecyclerView();
        }
    }

    private void findView (View v) {
        mProgressBar = v.findViewById(R.id.progressBar);
        mDivider = v.findViewById(R.id.divider);
        mRecyclerView = v.findViewById(R.id.recycler_view);
    }

    private void setVisibility (int visibility) {
        if (visibility == View.VISIBLE) {
            mProgressBar.setVisibility(View.INVISIBLE);
        }
        else if (visibility == View.INVISIBLE) {
            mProgressBar.setVisibility(View.VISIBLE);
        }

        mDivider.setVisibility(visibility);
        mRecyclerView.setVisibility(visibility);
    }
}
