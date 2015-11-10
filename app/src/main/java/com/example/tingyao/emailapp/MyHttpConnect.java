package com.example.tingyao.emailapp;

import android.net.Uri;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by tingyao on 10/27/15.
 */
public class MyHttpConnect {
    private String link;
    private URL url;


    public MyHttpConnect(String addr){
        link=addr;
        try{
            url = new URL(link);
        } catch(MalformedURLException e){
            Log.e("connect",e.toString());
        }

    }
/*
    public URLConnection ConnectToBingService() throws IOException{
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("Content-Type","audio/wav; codec=\"audio/pcm\"; samplerate=16000");
        conn.setRequestProperty("Accept","application/json;text/xml");
        conn.setRequestProperty("ProtocolVersion","HTTP/1.1");

    }*/

    public URLConnection PostToServer(String params) throws IOException{
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoInput(true);
        conn.setDoOutput(true);

        OutputStream os = conn.getOutputStream();
        BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(os, "UTF-8"));
        writer.write(params);
        writer.flush();
        writer.close();
        os.close();

        conn.connect();
        return conn;
    }

    public String SetParams(HashMap<String, String> keyValuePairs){
        Uri.Builder builder = new Uri.Builder();
        for(Map.Entry<String, String> entry : keyValuePairs.entrySet())
            builder.appendQueryParameter(entry.getKey(),entry.getValue());
        return builder.build().getEncodedQuery();
    }

    public void CheckInBox(){
        //set params
        //post
        //analysis
    }
}
