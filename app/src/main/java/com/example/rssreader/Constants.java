package com.example.rssreader;

public class Constants {
    private Constants () {}

    // URLとサイト名は同じ順番で記述する
    public static final String[] RSS_FEED_URL = {
            "http://alfalfalfa.com/index.rdf",
            "http://news4vip.livedoor.biz/index.rdf",
            "http://himasoku.com/index.rdf",
            "http://blog.livedoor.jp/news23vip/index.rdf",
            "http://kanasoku.info/index.rdf",
            "http://blog.livedoor.jp/nwknews/index.rdf",
            "http://workingnews.blog117.fc2.com/?xml",
            "http://michaelsan.livedoor.biz/index.rdf",
            "http://chaos2ch.com/index.rdf"
    };
    public static final String[] RSS_FEED_SITE = {
            "アルファルファモザイク",
            "ニュー速クオリティ",
            "暇人速報",
            "VIPPERな俺",
            "カナ速",
            "哲学ニュースnwk",
            "働くモノニュース",
            "もみあげチャ～シュ～",
            "カオスちゃんねる"
    };

    // 記事未読・既読
    public static final int FLAG_NO_READ = 0;
    public static final int FLAG_READ = 1;

    // ListViewの表示量
    public static int NUM_CURRENT_ARTICLE;
    public static final int NUM_LOAD_ARTICLE = 50;
    public static final int NUM_MAX_ARTICLE_NEW = 500;
    public static final int NUM_MAX_ARTICLE_POPULAR = 100;
}
