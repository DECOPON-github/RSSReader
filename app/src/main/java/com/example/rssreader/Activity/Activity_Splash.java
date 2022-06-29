package com.example.rssreader.Activity;

import static com.example.rssreader.Constants.RSS_FEED_SITE;
import static com.example.rssreader.Constants.RSS_FEED_URL;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.example.rssreader.DbAdapter_Article;
import com.example.rssreader.Task_RssParser;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Activity_Splash extends AppCompatActivity {
    private DbAdapter_Article mDbAdapterArticle;
    private String mBeforeTime;
    private boolean mFlagInit;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //　Siteテーブル更新
        mDbAdapterArticle = new DbAdapter_Article(this);
        mDbAdapterArticle.open();
        Cursor cursor = mDbAdapterArticle.getTableSite();
        if (cursor.moveToFirst()) {
            // サイト追加・削除時の処理
            if (cursor.getCount() != RSS_FEED_SITE.length) {
                // 保留
            }
            cursor.close();
        } else {
            // 初回起動時
            insertSite();
            mFlagInit = true;
        }
        cursor.close();
        mDbAdapterArticle.close();

        if (mFlagInit) {
            initTask();
        } else {
            startupTask();
        }
    }

    private void insertSite () {
        try {
            mDbAdapterArticle.begin();

            for (String site : RSS_FEED_SITE) {
                mDbAdapterArticle.saveItemSite(site);
            }

            mDbAdapterArticle.success();
        } catch (Exception e) {
            Log.e("insertSite", e.getMessage());
        } finally {
            mDbAdapterArticle.end();
        }
    }

    private void initTask () {
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("初期処理中...");
        mProgressDialog.show();

        Task_RssParser task = new Task_RssParser(this);
        task.execute(RSS_FEED_URL);
        task.setOnCallBack(new Task_RssParser.CallBackTask() {
            @Override
            public void CallBack(Boolean flag) {
                super.CallBack(flag);

                mProgressDialog.dismiss();
                startupTask();
            }
        });
    }

    private void startupTask () {
        Task_RssParser task = new Task_RssParser(this);
        task.execute(RSS_FEED_URL);
        task.setOnCallBack(new Task_RssParser.CallBackTask() {
            @Override
            public void CallBack(Boolean flag) {
                super.CallBack(flag);

                // articleテーブル更新
                mDbAdapterArticle.open();
                deleteArticle();
                mDbAdapterArticle.close();

                Intent intent = new Intent(getApplicationContext(), Activity_Home.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void deleteArticle () {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -30);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        mBeforeTime = sdf.format(calendar.getTime());

        try {
            mDbAdapterArticle.begin();

            mDbAdapterArticle.deleteTableArticle(mBeforeTime);

            mDbAdapterArticle.success();
        } catch (Exception e) {
            Log.e("deleteArticle", e.getMessage());
        } finally {
            mDbAdapterArticle.end();
        }
    }
}
