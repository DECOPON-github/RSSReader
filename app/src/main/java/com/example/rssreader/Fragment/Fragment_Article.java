package com.example.rssreader.Fragment;

import static com.example.rssreader.Constants.FLAG_READ;
import static com.example.rssreader.Constants.NUM_CURRENT_ARTICLE;
import static com.example.rssreader.Constants.NUM_LOAD_ARTICLE;
import static com.example.rssreader.Constants.NUM_MAX_ARTICLE;
import static com.example.rssreader.Constants.RSS_FEED_URL;
import static com.example.rssreader.Debug.DEBUG_MODE;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;
import androidx.fragment.app.ListFragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.rssreader.Activity.Activity_Webview;
import com.example.rssreader.DbAdapter_Article;
import com.example.rssreader.Item_Article;
import com.example.rssreader.R;
import com.example.rssreader.ListAdapter_Article;
import com.example.rssreader.Task_RssParser;

import java.util.ArrayList;

public class Fragment_Article extends ListFragment implements SwipeRefreshLayout.OnRefreshListener {
    private ArrayList mItems;
    private ListAdapter_Article mAdapter;
    private DbAdapter_Article mDbAdapterArticle;
    private ListView mListView;
    private ProgressBar mProgressBar;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private int listPos;
    private View mFooter;
    private Boolean isInit;
    private Boolean isLoad;
    private Boolean isLast;
    private Boolean isStart;

    public Fragment_Article() { isStart = true; }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (DEBUG_MODE) { Log.d("DEBUG_MODE", "Fragment_Article - onCreateView");}
        View v = inflater.inflate(R.layout.activity_article, container, false);

        findView(v);
        setVisibility(View.INVISIBLE);

        mSwipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.colorAccent));
        mSwipeRefreshLayout.setOnRefreshListener(this);

        return v;
    }

    @SuppressLint("Range")
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        if (DEBUG_MODE) { Log.d("DEBUG_MODE", "Fragment_Article - onViewCreated");}
        super.onViewCreated(view, savedInstanceState);
        initStart();
        setVisibility(View.VISIBLE);
    }

    private void initStart () {
        if (DEBUG_MODE) { Log.d("DEBUG_MODE", "Fragment_Article - startFragment");}
        listPos = 0;
        isInit = true;
        isLoad = true;
        isLast = false;
        initListAdapter();
        initListView();
    }

    private void initEnd () {
        if ( DEBUG_MODE ) { Log.d("DEBUG_MODE", "Fragment_Article - initEnd"); }
        if (listPos < NUM_CURRENT_ARTICLE && listPos < NUM_MAX_ARTICLE) {
            listPos += NUM_LOAD_ARTICLE;
        }
        isInit = false;
        isLoad = false;
    }

    // リストの項目を選択した時の処理
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        if (DEBUG_MODE) { Log.d("DEBUG_MODE", "Fragment_Article - onListItemClick");}
        Item_Article item = (Item_Article) mItems.get(position);

        Intent intent = new Intent(this.getContext(), Activity_Webview.class);
        intent.putExtra("TITLE", item.getTitle());
        intent.putExtra("DATE", item.getDate());
        intent.putExtra("URL", item.getUrl());
        startActivity(intent);

        item.setRead(String.valueOf(FLAG_READ));
        updateRead(item.getUrl().toString());
    }

    @SuppressLint("Range")
    private void refreshListView () {
        if ( DEBUG_MODE ) { Log.d("DEBUG_MODE", "Fragment_Article - refreshListView"); }

        mDbAdapterArticle = new DbAdapter_Article(getContext());
        mDbAdapterArticle.open();
        Cursor cursor = mDbAdapterArticle.getTableArticle(DbAdapter_Article.COL_DATE + " DESC", 0, listPos);

        if (cursor.moveToFirst()) {
            int i = 0;
            do {
                if (!mAdapter.getItem(i).getRead().equals(cursor.getString(cursor.getColumnIndex(DbAdapter_Article.COL_READ)))) {
                    mAdapter.getItem(i).setRead(String.valueOf(FLAG_READ));
                }
                i++;
            } while (cursor.moveToNext());
        }
        cursor.close();
        mDbAdapterArticle.close();

        mAdapter.notifyDataSetChanged();
    }

    private void updateRead (String url) {
        if (DEBUG_MODE) { Log.d("DEBUG_MODE", "Fragment_Article - updateRead");}
        mDbAdapterArticle.open();

        try {
            mDbAdapterArticle.begin();

            mDbAdapterArticle.updateReadArticle(url);

            mDbAdapterArticle.success();
        } catch (Exception e) {
            Log.e("updateRead", e.getMessage());
        } finally {
            mDbAdapterArticle.end();
        }

        mDbAdapterArticle.close();
    }

    private void initListAdapter () {
        if (DEBUG_MODE) { Log.d("DEBUG_MODE", "Fragment_Article - initListAdapter");}
        mItems = new ArrayList();
        mAdapter = new ListAdapter_Article(this.getContext(), mItems);
    }

    @SuppressLint("Range")
    private void initListView () {
        if (DEBUG_MODE) { Log.d("DEBUG_MODE", "Fragment_Article - initListView");}
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
                currentItem.setRead(cursor.getString(cursor.getColumnIndex(DbAdapter_Article.COL_READ)));
                mAdapter.add(currentItem);
            } while (cursor.moveToNext());
        }
        cursor.close();
        mDbAdapterArticle.close();

        this.setListAdapter(mAdapter);

        if (mFooter == null) {
            mFooter = getLayoutInflater().inflate(R.layout.item_footer,null);
        }
        //mListView.removeFooterView(mFooter);
        mListView.addFooterView(mFooter);
        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) { }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                boolean isLastItemVisible = totalItemCount == firstVisibleItem + visibleItemCount;
                if (isLastItemVisible && !isLoad && !isInit && !isLast) {
                    if ( DEBUG_MODE ) { Log.d("DEBUG_MODE", "Fragment_Article - initListView - onScroll"); }
                    isLoad = true;
                    if (listPos < NUM_CURRENT_ARTICLE - NUM_LOAD_ARTICLE && listPos < NUM_MAX_ARTICLE) {
                        addListView();
                        listPos += NUM_LOAD_ARTICLE;
                    }
                    else {
                        isLast = true;
                        mListView.removeFooterView(mFooter);
                    }
                    isLoad = false;
                }
            }
        });

        initEnd();
    }

    @SuppressLint("Range")
    private void addListView () {
        if ( DEBUG_MODE ) { Log.d("DEBUG_MODE", "Fragment_Article - addListView"); }
        mDbAdapterArticle = new DbAdapter_Article(getContext());
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
                currentItem.setRead(cursor.getString(cursor.getColumnIndex(DbAdapter_Article.COL_READ)));
                mAdapter.add(currentItem);
            } while (cursor.moveToNext());
        }
        cursor.close();
        mDbAdapterArticle.close();

        mAdapter.notifyDataSetChanged();
    }

    private void findView (View v) {
        mListView = v.findViewById(android.R.id.list);
        mProgressBar = v.findViewById(R.id.progressBar);
        mSwipeRefreshLayout = v.findViewById(R.id.swipelayout);
    }

    private void setVisibility (int visibility) {
        if (visibility == View.VISIBLE) {
            mProgressBar.setVisibility(View.INVISIBLE);
        }
        else if (visibility == View.INVISIBLE) {
            //mProgressBar.setVisibility(View.VISIBLE);
        }

        mListView.setVisibility(visibility);
    }

    @Override
    public void onRefresh() {
        if (DEBUG_MODE) { Log.d("DEBUG_MODE", "Fragment_Article - onRefresh");}

        setVisibility(View.INVISIBLE);
        Task_RssParser task = new Task_RssParser(this.getContext());
        task.execute(RSS_FEED_URL);
        task.setOnCallBack(new Task_RssParser.CallBackTask() {
            @Override
            public void CallBack(Boolean flag) {
                super.CallBack(flag);

                initStart();
                setVisibility(View.VISIBLE);
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    @Override
    public void onStart () {
        super.onStart();
        if (!isStart) {
            if ( DEBUG_MODE ) { Log.d("DEBUG_MODE", "Fragment_Article - onStart"); }
            refreshListView();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isStart) {
            if ( DEBUG_MODE ) { Log.d("DEBUG_MODE", "Fragment_Article - onResume"); }
            isStart = false;
        }
    }
}
