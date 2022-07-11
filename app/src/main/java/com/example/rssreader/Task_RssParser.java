package com.example.rssreader;

import static android.content.ContentValues.TAG;

import static com.example.rssreader.Constants.NUM_CURRENT_ARTICLE;
import static com.example.rssreader.Debug.DEBUG_MODE;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.net.LocalSocketAddress;
import android.os.AsyncTask;
import android.util.Log;
import android.util.Xml;

import com.example.rssreader.Activity.Activity_Home;
import com.example.rssreader.Activity.Activity_Splash;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

// RssParserTask.java
public class Task_RssParser extends AsyncTask<String, Integer, ArrayList<Item_Article>> {
    private Context mContext;
    private DbAdapter_Article mDbAdapterArticle;
    private CallBackTask callbacktask;

    private static enum Namespace {
        DC      ("http://purl.org/dc/elements/1.1/"),
        RDF     ("http://www.w3.org/1999/02/22-rdf-syntax-ns#"),
        XHTML   ("http://www.w3.org/1999/xhtml");

        private final String uri;

        Namespace(String uri) {
            this.uri = uri;
        }
        public String uri() {
            return uri;
        }
    };

    // コンストラクタ
    public Task_RssParser(Context context) {
        mContext = context;
    }

    // タスクを実行した直後にコールされる
    @Override
    protected void onPreExecute() { if (DEBUG_MODE) { Log.d("DEBUG_MODE", "Task_RssParser - onPreExecute");} }

    // バックグラウンドにおける処理を担う。タスク実行時に渡された値を引数とする
    @Override
    protected ArrayList<Item_Article> doInBackground(String... params) {
        if (DEBUG_MODE) { Log.d("DEBUG_MODE", "Task_RssParser - doInBackground");}
        ArrayList<Item_Article> result = new ArrayList<Item_Article>();
        try {
            for (String url : params) {
                result.addAll(parseXml(url));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        mDbAdapterArticle = new DbAdapter_Article(mContext);
        mDbAdapterArticle.open();
        insertArticle(result);
        refreshId();
        mDbAdapterArticle.close();

        // ここで返した値は、onPostExecuteメソッドの引数として渡される
        return result;
    }

    // メインスレッド上で実行される
    @Override
    protected void onPostExecute(ArrayList<Item_Article> result) {
        if (DEBUG_MODE) { Log.d("DEBUG_MODE", "Task_RssParser - onPostExecute");}
        if (!result.isEmpty()) {
            callbacktask.CallBack(true);
        }
    }

    public void setOnCallBack(CallBackTask _cbj) {
        callbacktask = _cbj;
    }

    /**
     * コールバック用のstaticなclass
     */
    public static class CallBackTask {
        public void CallBack(Boolean flag) {
        }
    }

    private void insertArticle (ArrayList<Item_Article> articles) {
        if (DEBUG_MODE) { Log.d("DEBUG_MODE", "Task_RssParser - insertArticle");}
        try {
            mDbAdapterArticle.begin();

            for (Item_Article article : articles) {
                int id_site = mDbAdapterArticle.getIdSite(article.getSite().toString());
                String tTitle = article.getTitle().toString();
                String tDate = article.getDate().toString();
                String tUrl = article.getUrl().toString();
                int tRead = mDbAdapterArticle.isReadArticle(tUrl);

                mDbAdapterArticle.saveItemArticle(id_site, tTitle, tDate, tUrl, tRead);
            }

            mDbAdapterArticle.success();
        } catch (Exception e) {
            Log.e("insertArticle", e.getMessage());
        } finally {
            mDbAdapterArticle.end();
        }
    }

    @SuppressLint("Range")
    private void refreshId () {
        if (DEBUG_MODE) { Log.d("DEBUG_MODE", "Task_RssParser - refreshId");}
        ArrayList<Item_Article> articles = new ArrayList<Item_Article>();

        try {
            mDbAdapterArticle.begin();

            Item_Article currentItem = null;
            Cursor cursor = mDbAdapterArticle.getTableArticle();
            NUM_CURRENT_ARTICLE = cursor.getCount();

            if (cursor.moveToFirst()) {
                // articleのデータを一時保存
                do {
                    if (cursor.getInt(cursor.getColumnIndex(DbAdapter_Article.COL_ID_SITE)) != 0) {
                        currentItem = new Item_Article();
                        currentItem.setSite(mDbAdapterArticle.getSite(cursor.getInt(cursor.getColumnIndex(DbAdapter_Article.COL_ID_SITE))));
                        currentItem.setTitle(cursor.getString(cursor.getColumnIndex(DbAdapter_Article.COL_TITLE)));
                        currentItem.setDate(cursor.getString(cursor.getColumnIndex(DbAdapter_Article.COL_DATE)));
                        currentItem.setUrl(cursor.getString(cursor.getColumnIndex(DbAdapter_Article.COL_URL)));
                        //currentItem.setRead(cursor.getString(cursor.getColumnIndex(DbAdapter_Article.COL_READ)));
                        //currentItem.setPopular(cursor.getString(cursor.getColumnIndex(DbAdapter_Article.COL_POPULAR)));
                        articles.add(currentItem);
                    }
                } while (cursor.moveToNext());

                // articleテーブル・ID削除
                mDbAdapterArticle.deleteTableArticle();

                // 一時保存データをarticleテーブルに挿入
                for (Item_Article article : articles) {
                    int id_site = mDbAdapterArticle.getIdSite(article.getSite().toString());
                    String tTitle = article.getTitle().toString();
                    String tDate = article.getDate().toString();
                    String tUrl = article.getUrl().toString();
                    //int tRead = Integer.parseInt(article.getRead().toString());
                    int tRead = 0;
                    mDbAdapterArticle.saveItemArticle(id_site, tTitle, tDate, tUrl, tRead);
                }
            }

            cursor.close();
            mDbAdapterArticle.success();
        } catch (Exception e) {
            Log.e("refreshId",  e.getMessage());
        } finally {
            mDbAdapterArticle.end();
        }
    }

    public ArrayList<Item_Article> parseXml(String url) {
        if (DEBUG_MODE) { Log.d("DEBUG_MODE", "Task_RssParser - parseXml");}
        ArrayList<Item_Article> Items = null;
        try {
            Item_Article currentItem = null;
            Items = new ArrayList<Item_Article>();
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            URL mUrl = new URL(url);
            URLConnection con = mUrl.openConnection();
            con.setConnectTimeout(3000);
            Document document = builder.parse(con.getInputStream());
            Element root = document.getDocumentElement();

            String tSite;
            String tTitle;
            String tDate;
            String tUrl;
            /* Get and print Title of RSS Feed. */
            NodeList channel = root.getElementsByTagName("channel");
            tSite = ((Element) channel.item(0)).getElementsByTagName("title").item(0).getFirstChild().getNodeValue();

            /* Get Node list of RSS items */
            NodeList item_list = root.getElementsByTagName("item");
            for (int i = 0; i < item_list.getLength(); i++) {
                currentItem = new Item_Article();
                Element element = (Element) item_list.item(i);
                tTitle = element.getElementsByTagName("title").item(0).getFirstChild().getNodeValue();
                tUrl = element.getElementsByTagName("link").item(0).getFirstChild().getNodeValue();
                //NodeList item_date;
                if (tSite.equals("ワイらのまとめ")) {
                    tDate = element.getElementsByTagName("pubDate").item(0).getFirstChild().getNodeValue();
                } else {
                    tDate = element.getElementsByTagNameNS(Namespace.DC.uri(), "date").item(0).getFirstChild().getNodeValue();
                }
                currentItem.setSite(tSite);
                currentItem.setTitle(tTitle);
                currentItem.setDate(transDate(tDate, tSite));
                currentItem.setUrl(tUrl);
                Items.add(currentItem);
            }
        } catch (IOException e) {
            System.out.println("IO Exception");
        } catch (ParserConfigurationException | ParseException | SAXException e) {
            e.printStackTrace();
        }
        return Items;
    }

    private String transDate (String date, String site) throws ParseException {
        if (DEBUG_MODE) { Log.d("DEBUG_MODE", "Task_RssParser - transDate");}
        SimpleDateFormat sdf_in_1 = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z");
        SimpleDateFormat sdf_in_2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        SimpleDateFormat sdf_out = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        sdf_out.setTimeZone(TimeZone.getTimeZone("Asia/Tokyo"));
        Date tDate;
        if (site.equals("ワイらのまとめ")) {
            tDate = sdf_in_1.parse(date);
        } else {
            tDate = sdf_in_2.parse(date);
        }

        return sdf_out.format(tDate);
    }
}
