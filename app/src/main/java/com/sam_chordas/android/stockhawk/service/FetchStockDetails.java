package com.sam_chordas.android.stockhawk.service;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.sam_chordas.android.stockhawk.ui.StocksDetailActivity;
import com.squareup.okhttp.Callback;
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
    private ArrayList<String> labels;
    private ArrayList<Float> values;

    public FetchStockDetails(Context context, String symbol)
    {
        mContext=context;
        client=new OkHttpClient();
        urlString="http://chartapi.finance.yahoo.com/instrument/1.0/" + symbol + "/chartdata;type=quote;range=5y/json";
        Request request=new Request.Builder()
                .url(urlString)
                .build();
        Log.d("AsyncTask","Started" );


    }

    @Override
    protected ArrayList doInBackground(Void... params) {
        Request request=new Request.Builder()
                .url(urlString)
                .build();
        Log.d("Url built",urlString );
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
                                    getMediumDateFormat(mContext).
                                    format(srcFormat.parse(seriesItem.getString("Date")));
                            labels.add(date);
                            values.add(Float.parseFloat(seriesItem.getString("close")));
                        }
                       //CALL: StocksDetailActivity.buildData(values);


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
        return null;
    }
}
