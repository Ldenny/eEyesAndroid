package com.idv.napchen.asynctest;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by napchen on 2017/3/18.
 */

public class HttpGetSensorValue extends AsyncTask<String, Integer, String> {

    public interface OnTaskCompleted {
        void onTaskCompleted();
    }

    private OnTaskCompleted listener;

    private String resultStringData;
    private String errorMsg;

    public HttpGetSensorValue(OnTaskCompleted listener) {
        super();
        this.listener = listener;
    }

    public HttpGetSensorValue() {
        super();
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected void onCancelled(String s) {
        super.onCancelled(s);
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
    }

    @Override
    protected String doInBackground(String... params) {

        String url = params[0];
        StringBuilder sb = new StringBuilder();
        int httpStatusCode = 0;

        try {
            URL myurl = new URL(url);           // set url

            // prepare connection
            HttpURLConnection con = (HttpURLConnection) myurl.openConnection();
            con.setRequestMethod("GET");        // HTTP method
            con.setDoInput(true);               // data will input
            con.setUseCaches(false);            // without caches
            con.setConnectTimeout(3000);
            con.connect();                      // start connection...

            httpStatusCode = con.getResponseCode(); // connection result

            if(httpStatusCode == HttpURLConnection.HTTP_OK) {
                InputStream is = con.getInputStream();              // get input stream
                InputStreamReader isr = new InputStreamReader(is);  // transfer input stream to input stream reader
                BufferedReader br = new BufferedReader(isr);

                String str;
                while ((str = br.readLine()) != null) {
                    // append buffer reader data to string builder
                    sb.append(str);
                }

                br.close();;
                isr.close();
                is.close();
            }

        } catch (MalformedURLException me) {
            errorMsg = me.toString();
        } catch (IOException ioe) {
            errorMsg = ioe.toString();
        }

        if(sb.length() > 0) {
            resultStringData = sb.toString();
        } else {
            if (httpStatusCode == 0) {
                resultStringData = "HTTP Error : " + errorMsg;
            } else {
                resultStringData = "HTTP Error, error code : " + Integer.toString(httpStatusCode);
            }
        }

        Log.e("Http return message : ",resultStringData);

        listener.onTaskCompleted();

        return null;
    }

    public String getResultStringData() {

        return resultStringData;
    }
}
