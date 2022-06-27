package com.example.rssreader.Activity;

import android.os.Bundle;
import android.view.KeyEvent;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;

import com.example.rssreader.R;

public class Activity_Webview extends AppCompatActivity {
    private WebView mWebview;
    private String mUrl;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);

        mUrl = getIntent().getStringExtra("URL");

        mWebview = findViewById(R.id.webView);
        mWebview.setWebViewClient(new WebViewClient());
        mWebview.getSettings().setJavaScriptEnabled(true);
        mWebview.loadUrl(mUrl);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // 戻るページがある場合
        if (event.getAction() == KeyEvent.ACTION_DOWN
                && keyCode == KeyEvent.KEYCODE_BACK){
            if(mWebview.canGoBack()){
                mWebview.goBack();
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
        if(mWebview != null){ // WebViewが空でなければ
            String url = mWebview.getUrl(); // 現在のウェブページを
            mWebview.loadUrl(url); // 再表示する
        }
    }
}