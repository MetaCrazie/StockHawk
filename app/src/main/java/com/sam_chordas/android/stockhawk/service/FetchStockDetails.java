package com.sam_chordas.android.stockhawk.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.sam_chordas.android.stockhawk.ui.StocksDetailActivity;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Created by praty on 28/06/2016.
 */
public class FetchStockDetails extends AsyncTask<Void, Void, ArrayList> {

    private String LOG_TAG=FetchStockDetails.class.getSimpleName();
    private String urlString="";
    private Context mContext;
    private OkHttpClient client;
    private String companyName;
    private String exchangeName;
    private String currency;
    private String closePrice;
    private String range;
    private String highest;
    private String lowest;
    private ArrayList<String> labels;
    private ArrayList<Float> values;
    private SharedPreferences sharedPreferences;

    public FetchStockDetails(Context context, String symbol)
    {
        mContext=context;
        client=new OkHttpClient();
        sharedPreferences = mContext.getSharedPreferences("Range", Context.MODE_PRIVATE);
        range=sharedPreferences.getString("Range", "1m");
        urlString="http://chartapi.finance.yahoo.com/instrument/1.0/" + symbol + "/chartdata;type=quote;range="+ range +"/json";

        Log.d("AsyncTask","Started" );


    }

    @Override
    protected ArrayList doInBackground(Void... params) {
        Request request=new Request.Builder()
                .url(urlString)
                .build();
        Log.d("Url built",urlString );
        Response response = null;
        try {
            response = client.newCall(request).execute();
            try{
                String result=response.body().string();
                //trim string?
                result=result.substring(29, result.length());
                JSONObject object=new JSONObject(result);
                //extract data
                try{
                companyName=object.getJSONObject("meta").getString("Company-Name");
                }catch (Exception e)
                {
                    e.printStackTrace();
                }
                try{
                    exchangeName=object.getJSONObject("meta").getString("Exchange-Name");
                }catch (Exception e){
                    e.printStackTrace();
                }
                try{
                    currency=object.getJSONObject("meta").getString("currency");
                }catch (Exception e)
                {
                    e.printStackTrace();
                }
                try{
                    closePrice=object.getJSONObject("meta").getString("previous_close_price");
                }catch (Exception e)
                {
                    e.printStackTrace();
                }
                try{
                    JSONObject close=object.getJSONObject("ranges").getJSONObject("close");
                    highest=close.getString("max");
                    lowest=close.getString("min");
                    Log.d(LOG_TAG, highest);
                    Log.d(LOG_TAG, lowest);
                }catch (Exception e){
                    e.printStackTrace();
                }


                labels=new ArrayList<>();
                values=new ArrayList<Float>();
                JSONArray series=object.getJSONArray("series");
                for(int i=0; i<series.length();i++)
                {
                    JSONObject seriesItem = series.getJSONObject(i);
                    SimpleDateFormat srcFormat = new SimpleDateFormat("yyyyMMdd");
                    String date = android.text.format.DateFormat.
                            getMediumDateFormat(mContext).
                            format(srcFormat.parse(seriesItem.getString("Date")));
                    labels.add(date);
                    values.add(Float.parseFloat(seriesItem.getString("close")));

                }



            }catch (Exception e){
                //failed DL
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    return values;
    }

    @Override
    protected void onPostExecute(ArrayList values) {
        Bundle bundle=new Bundle();
        bundle.putString("Company_Name", companyName);
        bundle.putString("Bid_Price", closePrice);
        bundle.putString("Currency", currency);
        bundle.putString("Highest", highest);
        bundle.putString("Lowest", lowest);
        StocksDetailActivity.buildData(values, bundle);
    }
}
