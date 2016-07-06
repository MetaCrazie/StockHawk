package com.sam_chordas.android.stockhawk.widget;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.provider.SyncStateContract;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;

/**
 * Created by praty on 01/07/2016.
 */
public class StockWidgetService extends RemoteViewsService {
    public static final String KEY_TAB_POSITION = "tab_position";
    private static final String LOG_TAG=StockWidgetService.class.getSimpleName();

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new WidgetItemRemoteView(this.getApplicationContext(),intent);
    }

    class WidgetItemRemoteView implements RemoteViewsService.RemoteViewsFactory{
        Context mContext;
        Cursor mCursor;
        Intent mIntent;

        public WidgetItemRemoteView(Context mContext, Intent mIntent) {
            this.mContext = mContext;
            this.mIntent = mIntent;
        }

        @Override
        public void onCreate() {
            mCursor = getContentResolver().query(
                    QuoteProvider.Quotes.CONTENT_URI,
                    new String[]{QuoteColumns._ID,
                            QuoteColumns.SYMBOL,
                            QuoteColumns.BIDPRICE,
                            QuoteColumns.CHANGE,
                            QuoteColumns.ISUP},
                    QuoteColumns.ISCURRENT + " = ?",
                    new String[]{"1"},
                    null
            );
        }

        @Override
        public int getCount() {
            return mCursor != null ? mCursor.getCount() : 0;
        }

        @Override
        public void onDataSetChanged() {
            // update Cursor when dataset is changed eg. stock added / removed

          /*  if (mCursor!=null)
                mCursor.close(); */

            final long pId = Binder.clearCallingIdentity();

            mCursor = getContentResolver().query(
                    QuoteProvider.Quotes.CONTENT_URI,
                    null,
                    QuoteColumns.ISCURRENT + " = ?",
                    new String[]{"1"},
                    null);

            Binder.restoreCallingIdentity(pId);
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public RemoteViews getViewAt(int position) {
            try{
                mCursor.moveToPosition(position);
                int changeColour;

                // get Stock Quote information
                String stockSymbol = mCursor.getString(mCursor.getColumnIndex(QuoteColumns.SYMBOL));
                String stockBidPrice = mCursor.getString(mCursor.getColumnIndex(QuoteColumns.BIDPRICE));
                String stockPriceChange = mCursor.getString(mCursor.getColumnIndex(QuoteColumns.CHANGE));
                int isUp = mCursor.getInt(mCursor.getColumnIndex(QuoteColumns.ISUP));

                // create List Item for Widget ListView
                RemoteViews listItemRemoteView = new RemoteViews(mContext.getPackageName(), R.layout.list_item_widget);
                listItemRemoteView.setTextViewText(R.id.stock_symbol,stockSymbol);
                listItemRemoteView.setTextViewText(R.id.bid_price,stockBidPrice);
                listItemRemoteView.setTextViewText(R.id.change,stockPriceChange);

                // if stock price is Up then background of price Change is Green else, Red
                if (isUp==1)
                    changeColour = R.drawable.percent_change_pill_green;
                else
                    changeColour = R.drawable.percent_change_pill_red;
                listItemRemoteView.setInt(R.id.change,"setBackgroundResource",changeColour);

                // set Onclick Item Intent
                Intent onClickItemIntent = new Intent();
                /*onClickItemIntent.putExtra(SyncStateContract.KEY_TAB_POSITION,position);*/
                listItemRemoteView.setOnClickFillInIntent(R.id.list_item_stock_quote,onClickItemIntent);
                return listItemRemoteView;
            }catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        public long getItemId(int position) {
            mCursor.moveToPosition(position);
            return mCursor.getLong(mCursor.getColumnIndex(QuoteColumns._ID));
        }

        @Override
        public void onDestroy() {
            if (mCursor!=null)
                mCursor.close();
        }

        @Override
        public RemoteViews getLoadingView() {
            return null;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }
    }
}
