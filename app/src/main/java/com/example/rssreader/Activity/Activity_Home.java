package com.example.rssreader.Activity;

import static com.example.rssreader.Constants.RSS_FEED_URL;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.example.rssreader.Item_Article;
import com.example.rssreader.R;
import com.example.rssreader.RssListAdapter;
import com.example.rssreader.Task_RssParser;

import java.util.ArrayList;

public class Activity_Home extends ListActivity {
    private ArrayList mItems;
    private RssListAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Itemオブジェクトを保持するためのリストを生成し、アダプタに追加する
        //mItems = new ArrayList();
        //mAdapter = new RssListAdapter(this, mItems);

        // タスクを起動する
        //Task_RssParser task = new Task_RssParser(this, mAdapter);
        //task.execute(RSS_FEED_URL);
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