package com.sam_chordas.android.stockhawk.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.SyncStateContract;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.TextView;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.ui.MyStocksActivity;
import com.sam_chordas.android.stockhawk.ui.StocksDetailActivity;

/**
 * Created by praty on 01/07/2016.
 */
public class StockWidgetProvider extends AppWidgetProvider {

    private static final String TAP_ACTION = "com.example.android.stackwidget.TAP_ACTION";
    public static final String EXTRA = "com.example.android.stackwidget.EXTRA";
    private static String LOG_TAG = StockWidgetProvider.class.getSimpleName();
    int[] mAppWidgetIds;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int widgetId : appWidgetIds) {
            Intent intent = new Intent(context, StockWidgetService.class);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
            mAppWidgetIds=appWidgetIds;

            // create Widget
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
            views.setRemoteAdapter(R.id.widget_listview, intent);

            //find placeholder text
            views.setEmptyView(R.id.widget_listview, R.id.widget_textview);

            // Open main screen on List Item click
            /*Intent intentStockGraph = new Intent(context, MyStocksActivity.class);
            intentStockGraph.setAction("SHOW_ACTIVITY");
            PendingIntent pendingIntent = TaskStackBuilder.create(context)
                    .addNextIntentWithParentStack(intentStockGraph)
                    .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
            views.setPendingIntentTemplate(R.id.widget_head, pendingIntent);
            appWidgetManager.updateAppWidget(widgetId, views);*/

            Log.d(LOG_TAG, "Widget on click action");

            Intent configIntent = new Intent(context, MyStocksActivity.class);
            PendingIntent configPendingIntent = PendingIntent.getActivity(context, 0, configIntent, 0);
            views.setOnClickPendingIntent(R.id.widget_head, configPendingIntent);
            appWidgetManager.updateAppWidget(widgetId, views);

            //refresh widget on manual button click
            Intent refreshIntent = new Intent(context,StockWidgetProvider.class);
            refreshIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            refreshIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
            PendingIntent pendingIntent1 = PendingIntent.getBroadcast(context,
                    0, refreshIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            views.setOnClickPendingIntent(R.id.refresh, pendingIntent1);

            Intent updateIntent= new Intent(context, StockWidgetProvider.class);
            updateIntent.setAction(StockWidgetProvider.TAP_ACTION);
            updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetIds);
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
            PendingIntent updatePendingIntent = PendingIntent.getBroadcast(context, 0, updateIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            views.setPendingIntentTemplate(R.id.widget_listview, updatePendingIntent);


            // Update Widget on HomeScreen
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds,R.id.widget_listview);
            appWidgetManager.updateAppWidget(widgetId, views);

        }

        super.onUpdate(context, appWidgetManager, appWidgetIds);

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // Receive Broadcast About Stock Data Update
        super.onReceive(context, intent);
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context,getClass()));
            // update All Widgets
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds,R.id.widget_listview);

    }
}