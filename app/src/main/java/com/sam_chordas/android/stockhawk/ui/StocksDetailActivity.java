package com.sam_chordas.android.stockhawk.ui;

import android.app.DownloadManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.app.LoaderManager;
import android.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.util.SortedList;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.service.FetchStockDetails;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.Callback;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.view.LineChartView;

/**
 * Created by Pratyusha on 14/06/2016.
 */
public class StocksDetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    static private final int CURSOR_LOADER_ID=0;
    private Cursor mCursor;
    private static LineChartView lineChartView;
    private String LOG_TAG=StocksDetailActivity.class.getSimpleName();
    private static String companySymbol;

    private static String companyBid;
    private static String companyName;
    private static String currency;

    private boolean isLoaded = false;
    private static ActionBar actionBar;

    private static TextView nameView;
    private static TextView bidView;
    private static TextView currencyView;

   /* @BindView(R.id.cName)
    static TextView nameView;
    @BindView(R.id.cBid)
    static TextView bidView;
    @BindView(R.id.cCurrency)
    static TextView currencyView; */

    @Override protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_line_graph);
       /* BottomSheetLayout bottomSheet = (BottomSheetLayout) findViewById(R.id.bottomsheet);
        bottomSheet.showWithSheetView(LayoutInflater.from(getApplicationContext()).inflate(R.layout.graph_detail, bottomSheet, false));
*/
        ButterKnife.bind(this);
        Intent intent = getIntent();
        Bundle args = new Bundle();

        //update resources
        args.putString(getResources().getString(R.string.string_symbol), intent.getStringExtra(getResources().getString(R.string.string_symbol)));
        args.putString(getResources().getString(R.string.perc), intent.getStringExtra(getResources().getString(R.string.perc)));
        args.putString(getResources().getString(R.string.change), intent.getStringExtra(getResources().getString(R.string.change)));

        //start AsyncTask to fetch details
        getLoaderManager().initLoader(CURSOR_LOADER_ID, args, this);
        new FetchStockDetails(getApplicationContext(), companySymbol).execute();

        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        nameView=(TextView)findViewById(R.id.cName);
        bidView=(TextView)findViewById(R.id.cBid);
        currencyView=(TextView)findViewById(R.id.cCurrency);
        lineChartView= (LineChartView) findViewById(R.id.linechart);


    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        companySymbol= args.getString(getResources().getString(R.string.string_symbol));
        Log.d(LOG_TAG, companySymbol);
        return new CursorLoader(this, QuoteProvider.Quotes.CONTENT_URI,
                new String[]{ QuoteColumns.BIDPRICE},
                QuoteColumns.SYMBOL + " = ?",
                new String[]{args.getString(getResources().getString(R.string.string_symbol))},
                null);

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.d(LOG_TAG, "Loader Finished");
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.d(LOG_TAG, "Loader Reset");
    }

    public static void buildData(ArrayList<Float> values, Bundle bundle) {
        Log.d("Data", "Build started for "+companySymbol);
        companyName=bundle.getString("Company_Name");
        companyBid=bundle.getString("Bid_Price");
        currency=bundle.getString("Currency");

        //TODO Try Butterknife

        nameView.setText(companyName);
        bidView.setText(companyBid);
        currencyView.setText(currency);
        actionBar.setTitle(companyName);

        Log.d("Butterknife", "Build started");
        if(values!=null)
        buildChart(values);

            //TODO ELSE
    }
    private static void buildChart(final ArrayList<Float> value) {
        Log.d("Chart", "Build started for " + companySymbol);
        List<PointValue> values = new ArrayList<>();
        for (int i = 0; i < value.size(); i++) {
            values.add(new PointValue(i, value.get(i)));
        }
        Line line = new Line(values).setColor(Color.YELLOW).setHasLabelsOnlyForSelected(true);
        List<Line> lines = new ArrayList<>();
        lines.add(line);
        LineChartData data = new LineChartData();
        data.setLines(lines);

        lineChartView.setLineChartData(data);
        lineChartView.setVisibility(View.VISIBLE);

    }


}
