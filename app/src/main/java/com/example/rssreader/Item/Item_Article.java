package com.example.rssreader.Item;

public class Item_Article {
    // 記事の元サイト
    private CharSequence mSite;
    // 記事のタイトル
    private CharSequence mTitle;
    // 記事の日付
    private CharSequence mDate;
    // 記事のURL
    private CharSequence mUrl;
    // 記事の本文
    private CharSequence mRead;

    public Item_Article() {
        mSite = "";
        mTitle = "";
        mDate = "";
        mUrl = "";
        mRead = "";
    }

    public CharSequence getSite() {
        return mSite;
    }

    public void setSite(CharSequence site) {
        mSite = site;
    }

    public CharSequence getTitle() {
        return mTitle;
    }

    public void setTitle(CharSequence title) {
        mTitle = title;
    }

    public CharSequence getDate() {
        return mDate;
    }

    public void setDate(CharSequence date) {
        mDate = date;
    }

    public CharSequence getUrl() {
        return mUrl;
    }

    public void setUrl(CharSequence url) {
        mUrl = url;
    }

    public CharSequence getRead() { return mRead; }

    public void setRead(CharSequence read) { mRead = read; }
}
