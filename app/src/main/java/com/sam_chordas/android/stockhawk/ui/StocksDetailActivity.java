package com.sam_chordas.android.stockhawk.ui;

import android.app.DownloadManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.app.LoaderManager;
import android.content.Loader;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.util.SortedList;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sa90.materialarcmenu.ArcMenu;
import com.sa90.materialarcmenu.StateChangeListener;
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

    private static SharedPreferences sharedPreferences;

    private static String companyBid;
    private static String companyName;
    private static String currency;
    private static String companyAmount;
    private static String companyPercent;
    private static String companyHighest;
    private static String companyLowest;

    private static String range;

    private boolean isLoaded = false;
    private static ActionBar actionBar;

    private static TextView nameView;
    private static TextView bidView;
    private static TextView currencyView;
    private static TextView symbolView;
    private static TextView amountView;
    private static TextView percentView;
    private static TextView highestView;
    private static TextView lowestView;
    private static TextView rangeView;

   /* @BindView(R.id.cName)
    static TextView nameView;
    @BindView(R.id.cBid)
    static TextView bidView;
    @BindView(R.id.cCurrency)
    static TextView currencyView; */

    @Override protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
       /* BottomSheetLayout bottomSheet = (BottomSheetLayout) findViewById(R.id.bottomsheet);
        bottomSheet.showWithSheetView(LayoutInflater.from(getApplicationContext()).inflate(R.layout.graph_detail, bottomSheet, false));
*/
        ButterKnife.bind(this);
        Intent intent = getIntent();
        Bundle args = new Bundle();
        range="1m";

        companySymbol=intent.getStringExtra(getResources().getString(R.string.string_symbol));
        companyBid=intent.getStringExtra(getResources().getString(R.string.price));
        companyPercent=intent.getStringExtra(getResources().getString(R.string.perc));
        companyAmount=intent.getStringExtra(getResources().getString(R.string.change));

        //update resources
        args.putString(getResources().getString(R.string.string_symbol), companySymbol);

        sharedPreferences=getSharedPreferences("Range", MODE_PRIVATE);

        setContentView(R.layout.activity_line_graph);

        //start AsyncTask to fetch details
        getLoaderManager().initLoader(CURSOR_LOADER_ID, args, this);
        new FetchStockDetails(getApplicationContext(), companySymbol).execute();

        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        //nameView=(TextView)findViewById(R.id.cName);
        bidView=(TextView)findViewById(R.id.cBid);
        //currencyView=(TextView)findViewById(R.id.cCurrency);
        symbolView=(TextView)findViewById(R.id.cSymbol);
        amountView=(TextView)findViewById(R.id.cAmount);
        percentView=(TextView)findViewById(R.id.cPercent);
        highestView=(TextView)findViewById(R.id.cHighest);
        lowestView=(TextView)findViewById(R.id.cLowest);
        rangeView=(TextView)findViewById(R.id.display_range);

        bidView.setText(companyBid);
        symbolView.setText(companySymbol);
        percentView.setText(companyPercent);
        amountView.setText(companyAmount);


        lineChartView= (LineChartView) findViewById(R.id.linechart);
       // final FrameLayout linearLayout=(FrameLayout)findViewById(R.id.landscape_details);

        //FAB Activity
        final ArcMenu arcMenu=(ArcMenu)findViewById(R.id.arcMenu);
      /*  arcMenu.setOnClickListener(new View.OnClickListener(){
                @Override
            public void onClick(View view){
                    if(arcMenu.isMenuOpened()){
                        Log.d(LOG_TAG, "Menu opened");
                        linearLayout.setVisibility(View.INVISIBLE);
                        Log.d(LOG_TAG, "Set invisible");
                    }
                }
        });*/

        FloatingActionButton fab1= (FloatingActionButton) arcMenu.findViewById(R.id.fab1);
        fab1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor=sharedPreferences.edit();
                editor.putString("Range", "1m");
                editor.commit();
                new FetchStockDetails(getApplicationContext(), companySymbol).execute();
                arcMenu.toggleMenu();
            }
        });
        FloatingActionButton fab2= (FloatingActionButton) arcMenu.findViewById(R.id.fab2);
        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor=sharedPreferences.edit();
                editor.putString("Range", "6m");
                editor.commit();
                new FetchStockDetails(getApplicationContext(), companySymbol).execute();
                arcMenu.toggleMenu();
            }
        });
        FloatingActionButton fab3= (FloatingActionButton) arcMenu.findViewById(R.id.fab3);
        fab3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor=sharedPreferences.edit();
                editor.putString("Range", "1y");
                editor.commit();
                new FetchStockDetails(getApplicationContext(), companySymbol).execute();
                arcMenu.toggleMenu();
            }
        });

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
        companyHighest=bundle.getString("Highest");
        companyLowest=bundle.getString("Lowest");

        //TODO Try Butterknife

        bidView.setText(companyBid);
        highestView.setText(companyHighest);
        lowestView.setText(companyLowest);

        //currencyView.setText(currency);
        actionBar.setTitle(companyName);

        Log.d("Butterknife", "Build started");
        if(values!=null)
        buildChart(values);

            //TODO ELSE
    }
    private static void buildChart(final ArrayList<Float> value) {
        Log.d("Chart", "Build started for " + companySymbol);
        List<PointValue> values = new ArrayList<>();
        range=sharedPreferences.getString("Range", "1m");
        rangeView.setText(range);
        for (int i = 0, j=0; i < value.size();j++) {
            values.add(new PointValue(j, value.get(i)));
            if(range.equalsIgnoreCase("1m"))
                i++;
            else if(range.equalsIgnoreCase("6m"))
                i+=6;
            else
                i+=12;
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
