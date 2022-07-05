package com.example.rssreader.Activity;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.rssreader.Fragment.Fragment_Article;
import com.example.rssreader.Fragment.Fragment_Favorite;
import com.example.rssreader.Fragment.Fragment_Search;
import com.example.rssreader.Fragment.Fragment_Site;
import com.example.rssreader.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.navigation.NavigationView;

public class Activity_Navigation extends AppCompatActivity {
    private final Fragment mFragArticle = new Fragment_Article();
    private final Fragment mFragSearch = new Fragment_Search();
    private final Fragment mFragSite = new Fragment_Site();
    private final Fragment mFragFavorite = new Fragment_Favorite();
    private final FragmentManager mFm = getSupportFragmentManager();
    private Fragment mActive = mFragArticle;
    private Boolean mFlagSearch = false;
    private Boolean mFlagSite = false;
    private Boolean mFlagFavorite = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        BottomNavigationView bottomNavigationView = findViewById(R.id.navigation_bottom);
        bottomNavigationView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        mFm.beginTransaction().add(R.id.frameLayout, mFragArticle, "1").commit();
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.nav_article:
                    mFm.beginTransaction().hide(mActive).show(mFragArticle).commit();
                    mActive = mFragArticle;
                    return true;
                case R.id.nav_search:
                    if (!mFlagSearch) {
                        mFlagSearch = true;
                        mFm.beginTransaction().add(R.id.frameLayout, mFragSearch, "2").commit();
                    }
                    mFm.beginTransaction().hide(mActive).show(mFragSearch).commit();
                    mActive = mFragSearch;
                    return true;
                case R.id.nav_site:
                    if (!mFlagSite) {
                        mFlagSite = true;
                        mFm.beginTransaction().add(R.id.frameLayout, mFragSite, "3").commit();
                    }
                    mFm.beginTransaction().hide(mActive).show(mFragSite).commit();
                    mActive = mFragSite;
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
