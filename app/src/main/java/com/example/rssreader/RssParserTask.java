package com.example.rssreader;

import android.app.ProgressDialog;
import android.net.LocalSocketAddress;
import android.os.AsyncTask;
import android.util.Xml;

import com.example.rssreader.Activity.Activity_Home;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

// RssParserTask.java
public class RssParserTask extends AsyncTask<String, Integer, RssListAdapter> {
    private Activity_Home mActivity;
    private RssListAdapter mAdapter;
    private ProgressDialog mProgressDialog;
    private NodeList mSite;

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
    public RssParserTask(Activity_Home activity, RssListAdapter adapter) {
        mActivity = activity;
        mAdapter = adapter;
    }

    // タスクを実行した直後にコールされる
    @Override
    protected void onPreExecute() {
        // プログレスバーを表示する
        mProgressDialog = new ProgressDialog(mActivity);
        mProgressDialog.setMessage("Now Loading...");
        mProgressDialog.show();
    }

    // バックグラウンドにおける処理を担う。タスク実行時に渡された値を引数とする
    @Override
    protected RssListAdapter doInBackground(String... params) {
        RssListAdapter result = null;
        try {
            // HTTP経由でアクセスし、InputStreamを取得する
            for (String url : params) {
                result = parseXml(url);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // ここで返した値は、onPostExecuteメソッドの引数として渡される
        return result;
    }

    // メインスレッド上で実行される
    @Override
    protected void onPostExecute(RssListAdapter result) {
        mProgressDialog.dismiss();
        mActivity.setListAdapter(result);
    }

    public RssListAdapter parseXml(String url) {
        try {
            Item_Article currentItem = null;
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(url);
            Element root = document.getDocumentElement();

            /* Get and print Title of RSS Feed. */
            NodeList channel = root.getElementsByTagName("channel");
            mSite = ((Element)channel.item(0)).getElementsByTagName("title");

            /* Get Node list of RSS items */
            NodeList item_list = root.getElementsByTagName("item");
            for (int i = 0; i <item_list.getLength(); i++) {
                currentItem = new Item_Article();
                Element  element = (Element)item_list.item(i);
                NodeList item_title = element.getElementsByTagName("title");
                NodeList item_link  = element.getElementsByTagName("link");
                NodeList item_date;
                if (mSite.item(0).getFirstChild().getNodeValue().equals("ワイらのまとめ")) {
                    item_date  = element.getElementsByTagName("pubDate");
                } else {
                    item_date = element.getElementsByTagNameNS(Namespace.DC.uri(), "date");
                }
                currentItem.setSite(mSite.item(0).getFirstChild().getNodeValue());
                currentItem.setTitle(item_title.item(0).getFirstChild().getNodeValue());
                currentItem.setDate(transDate(item_date.item(0).getFirstChild().getNodeValue()));
                currentItem.setUrl(item_link.item(0).getFirstChild().getNodeValue());
                mAdapter.add(currentItem);
            }
        } catch (IOException e) {
            System.out.println("IO Exception");
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return mAdapter;
    }

    private String transDate (String date) throws ParseException {
        SimpleDateFormat sdf_in_1 = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z");
        SimpleDateFormat sdf_in_2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        SimpleDateFormat sdf_out = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        Date tDate;
        if (mSite.item(0).getFirstChild().getNodeValue().equals("ワイらのまとめ")) {
            tDate = sdf_in_1.parse(date);
        } else {
            tDate = sdf_in_2.parse(date);
        }

        return sdf_out.format(tDate);
    }
}
