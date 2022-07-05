package com.example.rssreader.Fragment;

import static com.example.rssreader.Constants.NUM_LOAD_ARTICLE;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.ListFragment;

import com.example.rssreader.Activity.Activity_Webview;
import com.example.rssreader.DbAdapter_Article;
import com.example.rssreader.Item_Article;
import com.example.rssreader.R;
import com.example.rssreader.RssListAdapter;

import java.util.ArrayList;

public class Fragment_Article extends ListFragment {
    private ArrayList mItems;
    private RssListAdapter mAdapter;
    private DbAdapter_Article mDbAdapterArticle;
    private int listPos;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_main, container, false);

        return v;
    }

    @SuppressLint("Range")
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        listPos = 0;

        // Itemオブジェクトを保持するためのリストを生成し、アダプタに追加する
        mItems = new ArrayList();
        mAdapter = new RssListAdapter(this.getContext(), mItems);

        mDbAdapterArticle = new DbAdapter_Article(this.getContext());
        mDbAdapterArticle.open();
        Cursor cursor = mDbAdapterArticle.getTableArticle(DbAdapter_Article.COL_DATE + " DESC", listPos, NUM_LOAD_ARTICLE);

        Item_Article currentItem = null;
        if (cursor.moveToFirst()) {
            do {
                currentItem = new Item_Article();
                currentItem.setSite(mDbAdapterArticle.getSite(cursor.getInt(cursor.getColumnIndex(DbAdapter_Article.COL_ID_SITE))));
                currentItem.setTitle(cursor.getString(cursor.getColumnIndex(DbAdapter_Article.COL_TITLE)));
                currentItem.setDate(cursor.getString(cursor.getColumnIndex(DbAdapter_Article.COL_DATE)));
                currentItem.setUrl(cursor.getString(cursor.getColumnIndex(DbAdapter_Article.COL_URL)));
                //currentItem.setRead(cursor.getString(cursor.getColumnIndex(DbAdapter_Article.COL_READ)));
                mAdapter.add(currentItem);
            } while (cursor.moveToNext());
        }
        cursor.close();
        mDbAdapterArticle.close();

        this.setListAdapter(mAdapter);
    }

    // リストの項目を選択した時の処理
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Item_Article item = (Item_Article) mItems.get(position);

        Intent intent = new Intent(this.getContext(), Activity_Webview.class);
        intent.putExtra("TITLE", item.getTitle());
        intent.putExtra("DATE", item.getDate());
        intent.putExtra("URL", item.getUrl());
        startActivity(intent);
    }
}
