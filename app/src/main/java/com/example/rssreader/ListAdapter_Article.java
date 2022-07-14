package com.example.rssreader;

import static com.example.rssreader.Constants.FLAG_NO_READ;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.rssreader.Item.Item_Article;

import java.util.List;

public class ListAdapter_Article extends ArrayAdapter<Item_Article> {
    private LayoutInflater mInflater;
    private TextView mSite;
    private TextView mTitle;
    private TextView mDate;

    public ListAdapter_Article(Context context, List<Item_Article> objects) {
        super(context, 0,objects);
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    // 1行ごとのビューを生成する
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;

        if (convertView == null) {
            view = mInflater.inflate(R.layout.item_row_article, null);
        }

        // 現在参照しているリストの位置からItemを取得する
        Item_Article item = this.getItem(position);
        if (item != null) {
            // Itemから必要なデータを取り出し、それぞれTextViewにセットする
            String site = item.getSite().toString();
            mSite = (TextView) view.findViewById(R.id.site);
            mSite.setText(site);
            String title = item.getTitle().toString();
            mTitle = (TextView) view.findViewById(R.id.title);
            mTitle.setText(title);
            String date = item.getDate().toString();
            mDate = (TextView) view.findViewById(R.id.date);
            mDate.setText(date);

            View read = view.findViewById(R.id.read);
            if (Integer.parseInt(item.getRead().toString()) == FLAG_NO_READ) {
                read.setVisibility(View.INVISIBLE);
            } else {
                read.setVisibility(View.VISIBLE);
            }
        }
        return view;
    }
}
