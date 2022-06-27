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
            //for (String url : params) {
            //    result = parseXml(url);
            //}


            URL url = new URL(params[0]);
            InputStream is = url.openConnection().getInputStream();
            result = parseXml(is);
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

    // XMLをパースする
    //public RssListAdapter parseXml(InputStream is) throws IOException, XmlPullParserException {
    //    XmlPullParser parser = Xml.newPullParser();
    //    try {
    //        parser.setInput(is, null);
    //        int eventType = parser.getEventType();
    //        Item_Article currentItem = null;
    //        while (eventType != XmlPullParser.END_DOCUMENT) {
    //            String tag = null;
    //            switch (eventType) {
    //                case XmlPullParser.START_TAG:
    //                    tag = parser.getName();
    //                    if (tag.equals("item")) {
    //                        currentItem = new Item_Article();
    //                    } else if (currentItem != null) {
    //                        if (tag.equals("title")) {
    //                            currentItem.setTitle(parser.nextText());
    //                        } else if (tag.equals("link")) {
    //                            currentItem.setUrl(parser.nextText());
    //                        } else if (tag.equals("pubDate")) {
    //                            currentItem.setDate(transDate(parser.nextText()));
    //                        }
    //                    }
    //                    break;
    //                case XmlPullParser.END_TAG:
    //                    tag = parser.getName();
    //                    if (tag.equals("item")) {
    //                        mAdapter.add(currentItem);
    //                    }
    //                    break;
    //            }
    //            eventType = parser.next();
    //        }
    //    } catch (Exception e) {
    //        e.printStackTrace();
    //    }
    //    return mAdapter;
    //}

    public RssListAdapter parseXml(InputStream path) {
        try {
            Item_Article currentItem = null;
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(path);
            Element root = document.getDocumentElement();

            /* Get and print Title of RSS Feed. */
            NodeList channel = root.getElementsByTagName("channel");
            NodeList title = ((Element)channel.item(0)).getElementsByTagName("title");
            System.out.println("\nTitle: " + title.item(0).getFirstChild().getNodeValue() + "\n");

            /* Get Node list of RSS items */
            NodeList item_list = root.getElementsByTagName("item");
            for (int i = 0; i <item_list.getLength(); i++) {
                currentItem = new Item_Article();
                Element  element = (Element)item_list.item(i);
                NodeList item_title = element.getElementsByTagName("title");
                NodeList item_link  = element.getElementsByTagName("link");
                NodeList item_date  = element.getElementsByTagName("pubDate");
                //NodeList item_date = element.getElementsByTagNameNS(Namespace.DC.uri(), "date");
                System.out.println(" Title: " + item_title.item(0).getFirstChild().getNodeValue());
                System.out.println(" Date: " + item_date.item(0).getFirstChild().getNodeValue());
                System.out.println(" Link:  " + item_link.item(0).getFirstChild().getNodeValue() + "\n");
                currentItem.setSite(title.item(0).getFirstChild().getNodeValue());
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
        //SimpleDateFormat sdf_in_2 = new SimpleDateFormat("yyyy-MM-DD" + "T" + "HH:mm:ss Z");
        SimpleDateFormat sdf_out = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        Date tDate = sdf_in_1.parse(date);
        //sdf_in_1.setLenient(false);

        //try {
        //    sdf_in_2.format(sdf_in_2.parse(date));
        //}catch (ParseException p) {
        //    p.printStackTrace();
        //}

        return sdf_out.format(tDate);
    }
}
