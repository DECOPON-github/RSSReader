package com.example.rssreader.Activity;

import static com.example.rssreader.Constants.DOMAIN_REGISTER;
import static com.example.rssreader.Constants.FLAG_OFF_ACTIONVIEW;
import static com.example.rssreader.Constants.FLAG_ON_ACTIONVIEW;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.example.rssreader.DbAdapter.DbAdapter_Article;
import com.example.rssreader.Function.AdBlocker;
import com.example.rssreader.R;

import java.util.HashMap;
import java.util.Map;

public class Activity_WebView extends AppCompatActivity {
    private WebView mWebView;
    private String mUrl;
    private DbAdapter_Article mDbAdapterArticle;

    @SuppressLint("SetJavaScriptEnabled")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);

        mWebView = findViewById(R.id.webView);

        setWebView();

        mUrl = getIntent().getStringExtra("URL");
        mWebView.loadUrl(mUrl);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // 戻るページがある場合
        if (event.getAction() == KeyEvent.ACTION_DOWN
                && keyCode == KeyEvent.KEYCODE_BACK){
            if(mWebView.canGoBack()){
                mWebView.goBack();
            }
            else{
                finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode,  event);
    }

    @Override
    protected void onResume() {
        super.onResume();// バックグラウンドからフォアグランドに戻った時など
        if(mWebView != null){ // WebViewが空でなければ
            String url = mWebView.getUrl(); // 現在のウェブページを
            mWebView.loadUrl(url); // 再表示する
        }
    }

    @SuppressLint({"SetJavaScriptEnabled", "JavascriptInterface"})
    private void setWebView () {
        AdBlocker.init(this);
        mWebView.setWebViewClient(new WebViewClient() {
            private Map<String, Boolean> loadedUrls = new HashMap<String, Boolean>();

            @SuppressLint("ObsoleteSdkInt")
            @TargetApi(Build.VERSION_CODES.HONEYCOMB)
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
                boolean ad;
                if (!loadedUrls.containsKey(url)) {
                    ad = AdBlocker.isAd(url);
                    loadedUrls.put(url, ad);
                } else {
                    ad = loadedUrls.get(url);
                }
                return ad ? AdBlocker.createEmptyResource() :
                        super.shouldInterceptRequest(view, url);
            }

            //@Override
            //public boolean shouldOverrideUrlLoading(WebView view, String url) {
            //    Intent intent = null;
            //    mUrl = url;
//
            //    // 登録されているまとめブログ以外は外部アプリに飛ばす
            //    int isFlagActionView = FLAG_ON_ACTIONVIEW;
            //    for (String REGISTER : DOMAIN_REGISTER) {
            //        if (mUrl.contains(REGISTER)) {
            //            isFlagActionView = FLAG_OFF_ACTIONVIEW;
            //        }
            //    }
            //    if (isFlagActionView == FLAG_ON_ACTIONVIEW) {
            //        intent = new Intent(Intent.ACTION_VIEW, Uri.parse(mUrl));
            //        startActivity(intent);
            //        return true;
            //    }
//
            //    // 遷移先URLがDBに存在する場合は既読フラグを付ける
            //    mDbAdapterArticle = new DbAdapter_Article(getApplicationContext());
            //    mDbAdapterArticle.open();
            //    String[] tUrl = mUrl.split("\\?");
            //    if (mDbAdapterArticle.isArticleDb(tUrl[0])) {
            //        updateRead(tUrl[0]);
            //    }
            //    mDbAdapterArticle.close();
//
            //    return true;
            //}
        });
        mWebView.addJavascriptInterface(this, "Activity_WebView");
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setAppCacheEnabled(true);
        mWebView.getSettings().setAppCacheMaxSize(50 * 1024 * 1024);
        mWebView.getSettings().setAppCachePath("/cache");
    }

    private void updateRead (String url) {
        try {
            mDbAdapterArticle.begin();

            mDbAdapterArticle.updateReadArticle(url);

            mDbAdapterArticle.success();
        } catch (Exception e) {
            Log.e("updateRead", e.getMessage());
        } finally {
            mDbAdapterArticle.end();
        }
    }
}