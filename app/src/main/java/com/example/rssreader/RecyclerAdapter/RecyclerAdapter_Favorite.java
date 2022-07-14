package com.example.rssreader.RecyclerAdapter;

import static java.util.Collections.swap;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.example.rssreader.DbAdapter.DbAdapter_Favorite;
import com.example.rssreader.Fragment.Fragment_Favorite;
import com.example.rssreader.Function.ItemMoveCallback;
import com.example.rssreader.Function.StartDragListener;
import com.example.rssreader.Item.Item_Favorite;

import java.util.ArrayList;
import java.util.List;

import com.example.rssreader.R;

public class RecyclerAdapter_Favorite extends RecyclerView.Adapter<RecyclerAdapter_Favorite_ViewHolder> implements ItemMoveCallback.ItemTouchHelperContract {
    private Context mContext;
    private List<Item_Favorite> mItemList;
    private Fragment_Favorite mFragment;
    private DbAdapter_Favorite mDbAdapter;
    private final StartDragListener mStartDragListener;

    public  RecyclerAdapter_Favorite(Context context, List<Item_Favorite> ItemList, Fragment_Favorite fragment, StartDragListener startDragListener) {
        this.mContext = context;
        this.mItemList = ItemList;
        this.mFragment = fragment;
        this.mStartDragListener = startDragListener;
    }

    @Override
    public RecyclerAdapter_Favorite_ViewHolder onCreateViewHolder (ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_row_favorite, viewGroup, false);

        v.setBackgroundResource(R.drawable.style_listview_article);

        final RecyclerAdapter_Favorite_ViewHolder mh = new RecyclerAdapter_Favorite_ViewHolder(v);

        mh.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = mh.getAdapterPosition();
                //処理はonItemClick()に丸投げ
                onItemClick(v, position, mItemList);
            }
        });

        return mh;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(final RecyclerAdapter_Favorite_ViewHolder holder, final int i){
        holder.mTitle.setText(mItemList.get(i).getTitle().toString());
        holder.mDate.setText(mItemList.get(i).getDate().toString());
        holder.mSite.setText(mItemList.get(i).getSite().toString());

        final int pos = i;
        holder.mDelete.setOnClickListener(new View.OnClickListener() {
            public void onClick(final View v) {
                new AlertDialog.Builder(holder.mDelete.getContext())
                        .setTitle("確認")
                        .setMessage("お気に入りから削除します")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // OK button pressed
                                mDbAdapter = new DbAdapter_Favorite(mContext);
                                ejectDbFavorite(mItemList.get(pos).getUrl().toString());

                                mItemList.remove(pos);
                                mFragment.mAdapter.notifyItemRemoved(pos);

                                notifyItemRangeChanged(pos, mItemList.size());
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });
        holder.mMove.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() ==
                        MotionEvent.ACTION_DOWN) {
                    mStartDragListener.requestDrag(holder);
                }
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return (null != mItemList ? mItemList.size() : 0);
    }

    public void onItemClick(View view, int position, List<Item_Favorite> item) {
        //アダプタのインスタンスを作る際
        //このメソッドをオーバーライドして
        //クリックイベントの処理を設定する
    }

    @Override
    public void onRowMoved(int from, int to) {
        mDbAdapter = new DbAdapter_Favorite(mContext);
        exchangeDbFavorite(from, to);

        // ＊何故か mItemList のみだと落ちる
        swap(mFragment.mAdapter.mItemList, from, to);
        mFragment.mAdapter.notifyItemMoved(from, to);

        if (from < to) {
            notifyItemRangeChanged(from,  mItemList.size(), Boolean.FALSE);
        } else {
            notifyItemRangeChanged(to,  mItemList.size(), Boolean.FALSE);
        }

    }

    @SuppressLint("Range")
    private void exchangeDbFavorite (int from, int to) {
        List<Item_Favorite> itemList = new ArrayList<Item_Favorite>();

        mDbAdapter.open();

        try {
            mDbAdapter.begin();

            Cursor cursor = mDbAdapter.getTable();
            Item_Favorite currentItem = null;
            if (cursor.moveToFirst()) {
                do {
                    currentItem = new Item_Favorite();
                    currentItem.setTitle(cursor.getString(cursor.getColumnIndex(DbAdapter_Favorite.COL_TITLE)));
                    currentItem.setDate(cursor.getString(cursor.getColumnIndex(DbAdapter_Favorite.COL_DATE)));
                    currentItem.setUrl(cursor.getString(cursor.getColumnIndex(DbAdapter_Favorite.COL_URL)));
                    currentItem.setSite(cursor.getString(cursor.getColumnIndex(DbAdapter_Favorite.COL_SITE)));
                    itemList.add(currentItem);
                } while (cursor.moveToNext());
                cursor.close();

                mDbAdapter.deleteTable();

                swap(itemList, from, to);

                for (int n = 0; n < itemList.size(); n++) {
                    String tTitle = itemList.get(n).getTitle().toString();
                    String tDate = itemList.get(n).getDate().toString();
                    String tUrl = itemList.get(n).getUrl().toString();
                    String tSite = itemList.get(n).getSite().toString();
                    mDbAdapter.saveItem(tTitle, tDate, tUrl, tSite);
                }
            }

            mDbAdapter.success();
        } catch (Exception e) {
            Log.e("exchangeFavorite",  e.getMessage());
        } finally {
            mDbAdapter.end();
        }

        mDbAdapter.close();
    }

    @Override
    public void onRowSelected(RecyclerAdapter_Favorite_ViewHolder myViewHolder) {
    }

    @Override
    public void onRowClear(RecyclerAdapter_Favorite_ViewHolder myViewHolder) {
    }

    @SuppressLint("Range")
    private void ejectDbFavorite (String url) {
        List<Item_Favorite> itemList = new ArrayList<Item_Favorite>();

        mDbAdapter.open();

        try {
            mDbAdapter.begin();

            mDbAdapter.deleteRecordByUrl(url);

            Cursor cursor = mDbAdapter.getTable();
            Item_Favorite currentItem = null;
            if (cursor.moveToFirst()) {
                do {
                    currentItem = new Item_Favorite();
                    currentItem.setTitle(cursor.getString(cursor.getColumnIndex(DbAdapter_Favorite.COL_TITLE)));
                    currentItem.setDate(cursor.getString(cursor.getColumnIndex(DbAdapter_Favorite.COL_DATE)));
                    currentItem.setUrl(cursor.getString(cursor.getColumnIndex(DbAdapter_Favorite.COL_URL)));
                    currentItem.setSite(cursor.getString(cursor.getColumnIndex(DbAdapter_Favorite.COL_SITE)));
                    itemList.add(currentItem);
                } while (cursor.moveToNext());
                cursor.close();

                mDbAdapter.deleteTable();

                for (int n = 0; n < itemList.size(); n++) {
                    String tTitle = itemList.get(n).getTitle().toString();
                    String tDate = itemList.get(n).getDate().toString();
                    String tUrl = itemList.get(n).getUrl().toString();
                    String tSite = itemList.get(n).getSite().toString();
                    mDbAdapter.saveItem(tTitle, tDate, tUrl, tSite);
                }
            }

            mDbAdapter.success();
        } catch (Exception e) {
            Log.e("ejectDbFavorite",  e.getMessage());
        } finally {
            mDbAdapter.end();
        }

        mDbAdapter.close();
    }
}
