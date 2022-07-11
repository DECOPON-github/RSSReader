package com.example.rssreader.Activity;

import static com.example.rssreader.Constants.NUM_LOAD_ARTICLE;

import android.annotation.SuppressLint;
import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.example.rssreader.DbAdapter_Article;
import com.example.rssreader.Item_Article;
import com.example.rssreader.R;
import com.example.rssreader.ListAdapter_Article;

import java.util.ArrayList;

public class Activity_Home extends ListActivity {
    private ArrayList mItems;
    private ListAdapter_Article mAdapter;
    private DbAdapter_Article mDbAdapterArticle;
    private int listPos;

    @SuppressLint("Range")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article);

        listPos = 0;

        // Itemオブジェクトを保持するためのリストを生成し、アダプタに追加する
        mItems = new ArrayList();
        mAdapter = new ListAdapter_Article(this, mItems);

        mDbAdapterArticle = new DbAdapter_Article(this);
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
    protected void onListItemClick(ListView l, View v, int position, long id) {
        Item_Article item = (Item_Article) mItems.get(position);

        Intent intent = new Intent(this, Activity_Webview.class);
        intent.putExtra("TITLE", item.getTitle());
        intent.putExtra("DATE", item.getDate());
        intent.putExtra("URL", item.getUrl());
        startActivity(intent);
    }
}