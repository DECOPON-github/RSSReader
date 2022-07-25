package com.example.rssreader.Activity;

import static com.example.rssreader.Debug.DEBUG_MODE;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.rssreader.Fragment.Fragment_Article;
import com.example.rssreader.Fragment.Fragment_Favorite;
import com.example.rssreader.Fragment.Fragment_Search;
import com.example.rssreader.Fragment.Fragment_Popular;
import com.example.rssreader.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class Activity_Navigation extends AppCompatActivity {
    private final Fragment mFragArticle = new Fragment_Article();
    private final Fragment mFragSearch = new Fragment_Search();
    private final Fragment mFragPopular = new Fragment_Popular();
    private final Fragment mFragFavorite = new Fragment_Favorite();
    private final FragmentManager mFm = getSupportFragmentManager();
    private Fragment mActive = mFragArticle;
    private Boolean mFlagArticle = false;
    private Boolean mFlagSearch = false;
    private Boolean mFlagFavorite = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (DEBUG_MODE) { Log.d("DEBUG_MODE", "Activity_Navigation - onCreate");}
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        BottomNavigationView bottomNavigationView = findViewById(R.id.navigation_bottom);
        bottomNavigationView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        mFm.beginTransaction().add(R.id.frameLayout, mFragPopular, "1").commit();
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.nav_popular:
                    mFm.beginTransaction().hide(mActive).show(mFragPopular).commit();
                    mActive = mFragPopular;
                    return true;
                case R.id.nav_article:
                    if (!mFlagArticle) {
                        mFlagArticle = true;
                        mFm.beginTransaction().add(R.id.frameLayout, mFragArticle, "1").commit();
                    }
                    mFm.beginTransaction().hide(mActive).show(mFragArticle).commit();
                    mActive = mFragArticle;
                    return true;
                case R.id.nav_search:
                    if (!mFlagSearch) {
                        mFlagSearch = true;
                        mFm.beginTransaction().add(R.id.frameLayout, mFragSearch, "3").commit();
                    }
                    mFm.beginTransaction().hide(mActive).show(mFragSearch).commit();
                    mActive = mFragSearch;
                    return true;
                case R.id.nav_favorite:
                    if (!mFlagFavorite) {
                        mFlagFavorite = true;
                        mFm.beginTransaction().add(R.id.frameLayout, mFragFavorite, "4").commit();
                    }
                    mFm.beginTransaction().hide(mActive).show(mFragFavorite).commit();
                    mFragFavorite.setUserVisibleHint(true);
                    mActive = mFragFavorite;
                    return true;
            }
            return false;
        }
    };
}
