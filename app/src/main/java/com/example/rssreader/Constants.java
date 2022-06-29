package com.example.rssreader;

public class Constants {
    private Constants () {}

    // URLとサイト名は同じ順番で記述する
    public static final String[] RSS_FEED_URL = {
            "https://wairamatome.com/feed",
            "http://blog.livedoor.jp/nwknews/index.rdf",
            "http://majikichi.com/index.rdf"
    };
    public static final String[] RSS_FEED_SITE = {
            "ワイらのまとめ",
            "哲学ニュースnwk",
            "マジキチ速報"
    };

    // 記事未読・既読
    public static final int FLAG_NO_READ = 0;
    public static final int FLAG_READ = 1;
}
