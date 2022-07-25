package com.example.rssreader.Fragment;

import static com.example.rssreader.Constants.FLAG_READ;
import static com.example.rssreader.Constants.NUM_MAX_FAVORITE;
import static com.example.rssreader.Debug.DEBUG_MODE;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.ListFragment;

import com.example.rssreader.Activity.Activity_WebView;
import com.example.rssreader.DbAdapter.DbAdapter_Article;
import com.example.rssreader.DbAdapter.DbAdapter_Favorite;
import com.example.rssreader.Item.Item_Article;
import com.example.rssreader.ListAdapter_Article;
import com.example.rssreader.R;

import java.util.ArrayList;

public class Fragment_Search extends ListFragment {
    private Boolean isStart;
    private Boolean isInit;
    private ArrayList mItems;
    private ListAdapter_Article mAdapter;
    private DbAdapter_Article mDbAdapterArticle;
    private DbAdapter_Favorite mDbAdapterFav;
    private ListView mListView;
    private TextView mTextView;
    private EditText mEditText;
    private String mTextSearch;
    private Button mButton;
    private ProgressBar mProgressBar;

    public Fragment_Search() {
        isStart = true;
        isInit = true;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (DEBUG_MODE) {
            Log.d("DEBUG_MODE", "Fragment_Search - onCreateView");
        }
        View v = inflater.inflate(R.layout.fragment_search, container, false);
        findView(v);

        setVisibility(View.VISIBLE);
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        if (DEBUG_MODE) {
            Log.d("DEBUG_MODE", "Fragment_Search - onViewCreated");
        }
        super.onViewCreated(view, savedInstanceState);

        mEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    setVisibility(View.INVISIBLE);
                    mTextSearch = mEditText.getText().toString();
                    searchArticle();
                }
                return false;
            }
        });
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setVisibility(View.INVISIBLE);
                mTextSearch = mEditText.getText().toString();
                searchArticle();
            }
        });

    }

    private void searchArticle() {
        isInit = false;

        initListAdapter();
        initListView();
        setVisibility(View.VISIBLE);
    }

    private void initListAdapter () {
        mItems = new ArrayList();
        mAdapter = new ListAdapter_Article(this.getContext(), mItems);

        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Item_Article item = (Item_Article) mItems.get(position);
                String tTitle = item.getTitle().toString();
                String tDate = item.getDate().toString();
                String tUrl = item.getUrl().toString();
                String tSite = item.getSite().toString();
                mDbAdapterFav = new DbAdapter_Favorite(getContext());
                insertFavorite(tTitle, tDate, tUrl, tSite);
                return true;
            }
        });
    }

    @SuppressLint("Range")
    private void initListView () {
        mDbAdapterArticle = new DbAdapter_Article(getContext());
        mDbAdapterArticle.open();
        Cursor cursor = mDbAdapterArticle.getTableArticle(mTextSearch,DbAdapter_Article.COL_DATE + " DESC");

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
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Item_Article item = (Item_Article) mItems.get(position);

        Intent intent = new Intent(this.getContext(), Activity_WebView.class);
        intent.putExtra("TITLE", item.getTitle());
        intent.putExtra("DATE", item.getDate());
        intent.putExtra("URL", item.getUrl());
        intent.putExtra("SITE", item.getSite());
        startActivity(intent);

        item.setRead(String.valueOf(FLAG_READ));
        updateRead(item.getUrl().toString());
    }

    private void updateRead (String url) {
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

    @SuppressLint("Range")
    private void refreshListView () {
        if ( DEBUG_MODE ) { Log.d("DEBUG_MODE", "Fragment_Search - refreshListView"); }

        mDbAdapterArticle = new DbAdapter_Article(getContext());
        mDbAdapterArticle.open();
        Cursor cursor = mDbAdapterArticle.getTableArticle(mTextSearch,DbAdapter_Article.COL_DATE + " DESC");

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

    @Override
    public void onStart () {
        super.onStart();

        if (!isStart && !isInit) {
            if ( DEBUG_MODE ) { Log.d("DEBUG_MODE", "Fragment_Search - onStart"); }
            refreshListView();
        }
    }

    @Override
    public void onResume () {
        super.onResume();
        if (isStart) {
            if ( DEBUG_MODE ) { Log.d("DEBUG_MODE", "Fragment_Search - onResume"); }
            isStart = false;
        }
    }

    private void insertFavorite (String title,String date, String url, String site) {
        mDbAdapterFav.open();
        Cursor cursor = mDbAdapterFav.getTable();

        try {
            mDbAdapterFav.begin();

            if (mDbAdapterFav.isUrl(url)) {
                new AlertDialog.Builder(getContext())
                        .setTitle("お気に入り")
                        .setMessage("既に追加されています\n追加できません")
                        .setPositiveButton("OK", null)
                        .show();
            }
            else if (cursor.getCount() == NUM_MAX_FAVORITE) {
                new AlertDialog.Builder(getContext())
                        .setTitle("お気に入り")
                        .setMessage("最大数に達しました\n追加できません")
                        .setPositiveButton("OK", null)
                        .show();
            }
            else if (cursor.getCount() < NUM_MAX_FAVORITE) {
                mDbAdapterFav.saveItem(title, date, url, site);
                mDbAdapterFav.success();

                new AlertDialog.Builder(getContext())
                        .setTitle("お気に入り")
                        .setMessage("追加しました")
                        .setPositiveButton("OK", null)
                        .show();
            } else {
                new AlertDialog.Builder(getContext())
                        .setTitle("お気に入り")
                        .setMessage("エラーが発生しました\n追加できません")
                        .setPositiveButton("OK", null)
                        .show();
            }
        } catch (Exception e) {
            Log.e("insertFavorite",  e.getMessage());
        } finally {
            mDbAdapterFav.end();
        }

        cursor.close();
        mDbAdapterFav.close();
    }

    private void findView (View v) {
        mListView = v.findViewById(android.R.id.list);
        mProgressBar = v.findViewById(R.id.progressBar);
        mTextView = v.findViewById(android.R.id.empty);
        mEditText = v.findViewById(R.id.text_search);
        mButton = v.findViewById(R.id.button_search);
    }

    private void setVisibility (int visibility) {
        if (visibility == View.VISIBLE) {
            mProgressBar.setVisibility(View.INVISIBLE);
        }
        else if (visibility == View.INVISIBLE) {
            mProgressBar.setVisibility(View.VISIBLE);
        }
    }
}