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
import android.support.v7.app.AppCompatActivity;
import android.support.v7.util.SortedList;
import android.util.Log;
import android.view.View;
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
    private LineChartView lineChartView;

    private String companySymbol;
    private String companyName;
    private String exchangeName;
    private String currency;
    private String closePrice;
    private ArrayList<String> labels;
    private ArrayList<Float> values;
    private boolean isLoaded = false;

    @Override protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_line_graph);
        lineChartView= (LineChartView) findViewById(R.id.linechart);
        Intent intent = getIntent();
        Bundle args = new Bundle();
        args.putString(getResources().getString(R.string.string_symbol), intent.getStringExtra(getResources().getString(R.string.string_symbol)));
        getLoaderManager().initLoader(CURSOR_LOADER_ID, args, this);
        fetchStockDetails();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        companySymbol= args.getString(getResources().getString(R.string.string_symbol));
        return new CursorLoader(this, QuoteProvider.Quotes.CONTENT_URI,
                new String[]{ QuoteColumns.BIDPRICE},
                QuoteColumns.SYMBOL + " = ?",
                new String[]{args.getString(getResources().getString(R.string.string_symbol))},
                null);

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    public void buildData(ArrayList<Float> values) {
        Log.d("Data", "Build started for "+companySymbol);
        //layout manager
        final TextView nameView=(TextView)findViewById(R.id.cName);
        final TextView excView=(TextView)findViewById(R.id.cExchange);
        final TextView currencyView=(TextView)findViewById(R.id.cCurrency);
        final TextView bidView=(TextView)findViewById(R.id.cBid);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                nameView.setText(companyName);
                excView.setText(exchangeName);
                currencyView.setText(currency);
                bidView.setText(closePrice);
            }
        });

        buildChart(values);
    }
    private void buildChart(final ArrayList<Float> value){
        Log.d("Chart", "Build started for "+companySymbol);
        StocksDetailActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                List<PointValue> values=new ArrayList<>();
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
        });
    }

    private void fetchStockDetails() {

        OkHttpClient client=new OkHttpClient();
        Request request=new Request.Builder()
                .url("http://chartapi.finance.yahoo.com/instrument/1.0/" + companySymbol + "/chartdata;type=quote;range=5y/json")
                .build();
        Log.d("Url built","http://chartapi.finance.yahoo.com/instrument/1.0/" + companySymbol + "/chartdata;type=quote;range=5y/json" );
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                //failed DL
            }

            @Override
            public void onResponse(Response response) throws IOException {
                if (response.code() == 200){
                    try{
                        String result=response.body().string();
                        //trim string?
                        result=result.substring(29, result.length());
                        JSONObject object=new JSONObject(result);
                        //extract data
                        companyName=object.getJSONObject("meta").getString("Company-Name");
                        exchangeName=object.getJSONObject("meta").getString("Exchange-Name");
                        currency=object.getJSONObject("meta").getString("currency");
                        closePrice=object.getJSONObject("meta").getString("previous_close_price");

                        labels=new ArrayList<>();
                        values=new ArrayList<Float>();
                        JSONArray series=object.getJSONArray("series");
                        for(int i=0; i<=10; i++)
                        {
                            JSONObject seriesItem = series.getJSONObject(i);
                            SimpleDateFormat srcFormat = new SimpleDateFormat("yyyyMMdd");
                            String date = android.text.format.DateFormat.
                                getMediumDateFormat(getApplicationContext()).
                                format(srcFormat.parse(seriesItem.getString("Date")));
                            labels.add(date);
                            values.add(Float.parseFloat(seriesItem.getString("close")));
                        }
                        buildData(values);


                    }catch (Exception e){
                        //failed DL
                        e.printStackTrace();
                    }
                }else
                {
                    //failed DL
                }

            }
        });


    }

}
