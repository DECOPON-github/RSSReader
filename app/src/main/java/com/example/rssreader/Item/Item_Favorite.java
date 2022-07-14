package com.example.rssreader.Item;

public class Item_Favorite {
    // タイトル
    private CharSequence mTitle;
    // 日時
    private CharSequence mDate;
    // リンク
    private CharSequence mUrl;
    // サムネイル
    private CharSequence mSite;

    public Item_Favorite() {}

    public Item_Favorite(String tTitle, String tDate, String tUrl, String tSite) {
        mTitle = tTitle;
        mDate = tDate;
        mUrl = tUrl;
        mSite = tSite;
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

    public CharSequence getSite() {
        return mSite;
    }

    public void setSite(CharSequence site) {
        mSite = site;
    }

}
