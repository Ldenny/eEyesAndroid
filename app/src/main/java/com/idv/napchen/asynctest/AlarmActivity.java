package com.idv.napchen.asynctest;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by napchen on 2017/3/15.
 */

public class AlarmActivity extends AppCompatActivity {

    private static final String GET_ALARM_INFO_ADDRESS = "/dbAlarmInfoGet.php?username=root&password=root&database=eEyes&appUserName=user&appPassword=password&type=getAlarmByUser";
    private static final String GET_ALARM_STATUS_ADDRESS = "/dbinfoGet.php?username=root&password=root&database=eEyes&appUserName=user&appPassword=password&type=getAlarmStatus";
    private static final String CLEAR_ALARM_STATUS_ADDRESS = "/dbinfoGet.php?username=root&password=root&database=eEyes&appUserName=user&appPassword=password&type=clearAlarmStatus";
    private static final String STOP_ALARM_CHECKING_ADDRESS = "/dbinfoGet.php?username=root&password=root&database=eEyes&appUserName=user&appPassword=password&type=stopAlarmChecking";
    private static final String START_ALARM_CHECKING_ADDRESS = "/SendAllAlarm/checkAlarmGet.php?username=root&password=root&database=eEyes&appUserName=user&type=checkAlarm&sec=1";
    private static final String GET_ALARM_CHECKING_ADDRESS = "/dbinfoGet.php?username=root&password=root&database=eEyes&appUserName=user&appPassword=password&type=getCheckingAlarmStatus";

    private ListView listView;

    private Button btnAlarmCheckingEnable, btnClearAlarmStatus, btnAlarmCheckingDisable;

    private ProgressDialog progressDialog;

    private AllSensorsInfo allSensorsInfo;

    private HttpGetSensorValue setHttpAlarmCheckinfEnable, setHttpAlarmCheckinfDisable, getHttpAlarmStatus, clearHttpAlarmStatus, getHttpCheckingAlarmStatus;

    private List<AlarmItem> alarmList;

    private SharedPreferences sharedPref;

    private String errMsg;

    private static String dbIP;

    private boolean isHttpResponse;

    boolean isAlarm;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);

        // Setup IP from settings file
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        dbIP = sharedPref.getString("mainIPAddress", null);

        findViews();
    }

    private void findViews() {

        listView = (ListView) findViewById(R.id.listView);

        btnAlarmCheckingEnable = (Button) findViewById(R.id.btnAlarmCheckingEnable);
        btnClearAlarmStatus = (Button) findViewById(R.id.btnClearAlarmStatus);
        btnAlarmCheckingDisable = (Button) findViewById(R.id.btnAlarmCheckingDisable);

        allSensorsInfo = AllSensorsInfo.getInstance();

        btnAlarmCheckingEnable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                
                setHttpAlarmCheckinfEnable = new HttpGetSensorValue(new HttpGetSensorValue.OnTaskCompleted() {
                    @Override
                    public void onTaskCompleted() {
                        String sensorValuesString = setHttpAlarmCheckinfEnable.getResultStringData();

                        Log.e("HTTP response",sensorValuesString);

                        if(sensorValuesString.substring(0,4).equals("HTTP")) {
                            errMsg = sensorValuesString;
                            return;
                        }

                        if(sensorValuesString.length() == 0) {
                            errMsg = "Http no response!";
                            return;
                        }

                        isHttpResponse = true;
                    }

                });

                btnAlarmCheckingEnable.setBackgroundColor(Color.WHITE);
                btnAlarmCheckingDisable.setBackgroundColor(Color.GRAY);

                isHttpResponse = false;
                String url = "http://" + dbIP + START_ALARM_CHECKING_ADDRESS;
                Log.e("setAlarmCheckinfEnable",url);
                setHttpAlarmCheckinfEnable.execute(url);
                Log.e("first","send...");
                try {
                    //set time in mili
                    Thread.sleep(300);
                    Log.e("first","delay...");
                }catch (Exception e){
                    e.printStackTrace();
                }

                if(isHttpResponse == false) {
                    displayWarningMessage(errMsg);
                    return;
                }
            }
        });

        btnClearAlarmStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                clearHttpAlarmStatus = new HttpGetSensorValue(new HttpGetSensorValue.OnTaskCompleted() {
                    @Override
                    public void onTaskCompleted() {

                        String sensorValuesString = clearHttpAlarmStatus.getResultStringData();

                        Log.e("HTTP response",sensorValuesString);

                        if(sensorValuesString.substring(0,4).equals("HTTP")) {
                            errMsg = sensorValuesString;
                            return;
                        }

                        if(sensorValuesString.length() == 0) {
                            errMsg = "Http no response!";
                            return;
                        }

                        isHttpResponse = true;
                    }
                });

                btnClearAlarmStatus.setText("No new alarms!");
                btnClearAlarmStatus.setBackgroundColor(Color.GREEN);

                isHttpResponse = false;
                String url = "http://" + dbIP + CLEAR_ALARM_STATUS_ADDRESS;
                Log.e("clearAlarmStatus",url);
                clearHttpAlarmStatus.execute(url);
                Log.e("first","send...");
                try {
                    //set time in mili
                    Thread.sleep(300);
                    Log.e("first","delay...");
                }catch (Exception e){
                    e.printStackTrace();
                }

                if(isHttpResponse == false) {
                    displayWarningMessage(errMsg);
                    return;
                }
            }
        });

        btnAlarmCheckingDisable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                setHttpAlarmCheckinfDisable = new HttpGetSensorValue(new HttpGetSensorValue.OnTaskCompleted() {
                    @Override
                    public void onTaskCompleted() {
                        String sensorValuesString = setHttpAlarmCheckinfDisable.getResultStringData();

                        Log.e("HTTP response",sensorValuesString);

                        if(sensorValuesString.substring(0,4).equals("HTTP")) {
                            errMsg = sensorValuesString;
                            return;
                        }

                        if(sensorValuesString.length() == 0) {
                            errMsg = "Http no response!";
                            return;
                        }

                        isHttpResponse = true;
                    }
                });

                btnAlarmCheckingEnable.setBackgroundColor(Color.GRAY);
                btnAlarmCheckingDisable.setBackgroundColor(Color.WHITE);

                isHttpResponse = false;
                String url = "http://" + dbIP + STOP_ALARM_CHECKING_ADDRESS;
                Log.e("setAlarmCheckinfDisable",url);
                setHttpAlarmCheckinfDisable.execute(url);
                Log.e("first","send...");
                try {
                    //set time in mili
                    Thread.sleep(300);
                    Log.e("first","delay...");
                }catch (Exception e){
                    e.printStackTrace();
                }

                if(isHttpResponse == false) {
                    displayWarningMessage(errMsg);
                    return;
                }
            }
        });

        getHttpAlarmStatus = new HttpGetSensorValue(new HttpGetSensorValue.OnTaskCompleted() {
            @Override
            public void onTaskCompleted() {
                String sensorValuesString = getHttpAlarmStatus.getResultStringData();

                Log.e("HTTP response",sensorValuesString);

                if(sensorValuesString.substring(0,4).equals("HTTP")) {
                    errMsg = sensorValuesString;
                    return;
                }

                if(sensorValuesString.length() == 0) {
                    errMsg = "Http no response!";
                    return;
                }

                isHttpResponse = true;

                try {
                    Log.e("after got HTTP","start JSON parsing...");

                    JSONObject jObj = new JSONObject(sensorValuesString);
                    String alarm = jObj.getString("alarm");
                    Log.e("JSON alarm",alarm);

                    if(alarm.equals("true")) {
                        isAlarm = true;
                    } else {
                        isAlarm = false;
                    }

                } catch (JSONException je) {
                    je.printStackTrace();
                }

                if(isHttpResponse == false) {
                    displayWarningMessage(errMsg);
                    return;
                }
            }
        });

        isHttpResponse = false;
        String url = "http://" + dbIP + GET_ALARM_STATUS_ADDRESS;
        Log.e("setAlarmCheckinfDisable",url);
        getHttpAlarmStatus.execute(url);

        int counter = 0;
        while (isHttpResponse == false) {
            try {
                //set time in mili
                Thread.sleep(300);
                Log.e("http", "delay...");
                counter++;
                if(counter >= 10) {
                    isHttpResponse = true;
                    Log.e("http", "timeout...");
                    break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

//        isHttpResponse = false;
//        String url = "http://" + dbIP + GET_ALARM_STATUS_ADDRESS;
//        Log.e("getAlarmStatus",url);
//        getHttpCheckingAlarmStatus.execute(url);
//        Log.e("first","send...");
//        try {
//            //set time in mili
//            Thread.sleep(500);
//            Log.e("first","delay...");
//        }catch (Exception e){
//            e.printStackTrace();
//        }
//
        if(isAlarm == true) {
            btnClearAlarmStatus.setText("!!! Alarm need to confirm !!!");
            btnClearAlarmStatus.setBackgroundColor(Color.RED);
        } else {
            btnClearAlarmStatus.setText("No New Alarms");
            btnClearAlarmStatus.setBackgroundColor(Color.GREEN);
        }


//        getHttpAlarmStatus = new HttpGetSensorValue(new HttpGetSensorValue.OnTaskCompleted() {
//            @Override
//            public void onTaskCompleted() {
//                String sensorValuesString = getHttpAlarmStatus.getResultStringData();
//
//                Log.e("HTTP response",sensorValuesString);
//
//                if(sensorValuesString.substring(0,4).equals("HTTP")) {
//                    displayWarningMessage(sensorValuesString);
//                    return;
//                }
//
//                if(sensorValuesString.length() == 0) {
//                    displayWarningMessage("Http no response!");
//                    return;
//                }
//
//                isHttpResponse = true;
//
//                try {
//                    Log.e("after got HTTP","start JSON parsing...");
//
//                    JSONObject jObj = new JSONObject(sensorValuesString);
//                    String alarm = jObj.getString("alarm");
//                    Log.e("JSON alarm",alarm);
//
//                    if(alarm.equals("true")) {
//                        isAlarm = true;
//                        btnAlarmCheckingEnable.setBackgroundColor(Color.WHITE);
//                        btnAlarmCheckingDisable.setBackgroundColor(Color.GRAY);
//                    } else {
//                        isAlarm = false;
//                        btnAlarmCheckingEnable.setBackgroundColor(Color.GRAY);
//                        btnAlarmCheckingDisable.setBackgroundColor(Color.WHITE);
//                    }
//
//                } catch (JSONException je) {
//                    je.printStackTrace();
//                }
//            }
//        });
//
//        isAlarm = true;
//        isHttpResponse = false;
//        String url = "http://" + dbIP + GET_ALARM_STATUS_ADDRESS;
//        Log.e("getAlarmStatus",url);
//        getHttpAlarmStatus.execute(url);
//        Log.e("first","send...");
//        try {
//            //set time in mili
//            Thread.sleep(500);
//            Log.e("first","delay...");
//        }catch (Exception e){
//            e.printStackTrace();
//        }
//
//        if(isAlarm == true) {
//            btnClearAlarmStatus.setText("!!! Alarm need to confirm !!!");
//            btnClearAlarmStatus.setBackgroundColor(Color.RED);
//        } else {
//            btnClearAlarmStatus.setText("No New Alarms");
//            btnClearAlarmStatus.setBackgroundColor(Color.GREEN);
//        }

        // get checking alarm status
        getHttpCheckingAlarmStatus = new HttpGetSensorValue(new HttpGetSensorValue.OnTaskCompleted() {
            @Override
            public void onTaskCompleted() {
                String sensorValuesString = getHttpCheckingAlarmStatus.getResultStringData();

                Log.e("HTTP response",sensorValuesString);

                if(sensorValuesString.substring(0,4).equals("HTTP")) {
                    errMsg = sensorValuesString;
                    return;
                }

                if(sensorValuesString.length() == 0) {
                    errMsg = "Http no response!";
                    return;
                }

                isHttpResponse = true;

                try {
                    Log.e("after got HTTP","start JSON parsing...");

                    JSONObject jObj = new JSONObject(sensorValuesString);
                    String alarm = jObj.getString("alarm");
                    Log.e("JSON alarm",alarm);

                    if(alarm.equals("true")) {
                        isAlarm = true;
                        btnAlarmCheckingEnable.setBackgroundColor(Color.WHITE);
                        btnAlarmCheckingDisable.setBackgroundColor(Color.GRAY);
                    } else {
                        isAlarm = false;
                        btnAlarmCheckingEnable.setBackgroundColor(Color.GRAY);
                        btnAlarmCheckingDisable.setBackgroundColor(Color.WHITE);
                    }

                } catch (JSONException je) {
                    je.printStackTrace();
                }
            }
        });

        isAlarm = false;
        isHttpResponse = false;
        url = "http://" + dbIP + GET_ALARM_CHECKING_ADDRESS;
        Log.e("getAlarmStatus",url);
        getHttpCheckingAlarmStatus.execute(url);
        Log.e("first","send...");
        try {
            //set time in mili
            Thread.sleep(500);
            Log.e("first","delay...");
        }catch (Exception e){
            e.printStackTrace();
        }

        if(isHttpResponse == false) {
            displayWarningMessage(errMsg);
            return;
        }

        if(isAlarm == true) {
            btnAlarmCheckingEnable.setBackgroundColor(Color.WHITE);
            btnAlarmCheckingDisable.setBackgroundColor(Color.GRAY);
        } else {
            btnAlarmCheckingEnable.setBackgroundColor(Color.GRAY);
            btnAlarmCheckingDisable.setBackgroundColor(Color.WHITE);
        }

        // get all alarm info.
        url = "http://" + dbIP + GET_ALARM_INFO_ADDRESS;
        Log.e("getAllAlarm",url);
        new GetAllAlarmInfo().execute(url);

    }

    private void displayWarningMessage(String msg) {

        Toast.makeText(AlarmActivity.this, msg, Toast.LENGTH_SHORT).show();

        AlertDialog alertDialog = new AlertDialog.Builder(AlarmActivity.this).create();
        alertDialog.setTitle("Alert");
        alertDialog.setMessage(msg);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // get inflater
        MenuInflater inflater = getMenuInflater();
        // load option menu by options_menu.xml
        inflater.inflate(R.menu.menu_alarm_set, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // compare id
        switch (item.getItemId()) {
            case R.id.action_edit:
                Intent intent = new Intent();
                intent.setClass(AlarmActivity.this, ActivityAlarmSetting.class);
                startActivity(intent);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private class GetAllAlarmInfo extends AsyncTask<String, Integer, List<AlarmItem>> {

        // pre execute before async start...
        @Override
        protected void onPreExecute() {
            Log.e("eEyes Alarm","onPreExecute...");
            progressDialog = new ProgressDialog(AlarmActivity.this);
            progressDialog.setMessage("Loading...");
            progressDialog.show();
        }

        @Override
        protected List<AlarmItem> doInBackground(String... params) {

            String url = params[0];
            Log.e("eEyes Alarm View",url);
            StringBuilder sb = new StringBuilder();

            try {

                URL myurl = new URL(url);
                HttpURLConnection con = (HttpURLConnection) myurl.openConnection();
                con.setRequestMethod("GET");
                con.setDoInput(true);           //
                con.setUseCaches(false);        // wothout catch usage
                con.connect();

                int status = con.getResponseCode();
                Log.e("HTTP","HTTP Connect...");
                if(status == HttpURLConnection.HTTP_OK) {
                    InputStream is = con.getInputStream();
                    InputStreamReader isr = new InputStreamReader(is);
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
                Log.e("MalformedURLException", me.toString());
            } catch (IOException ioe) {
                Log.e("IOException", ioe.toString());
            }

            if(sb.length() > 0) {
                // if have data, return data
                try {
                    alarmList = getJSONData(sb.toString());
                    return alarmList;
                } catch (JSONException je) {
                    je.printStackTrace();
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(List<AlarmItem> result) {

            progressDialog.dismiss();
            listView.setAdapter(new MyAdapter(result));
        }

        private class MyAdapter extends BaseAdapter {

            private List<AlarmItem> alarmList;

            public MyAdapter() {
            }

            public MyAdapter(List<AlarmItem> alarmList) {
                this.alarmList = alarmList;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {

                ViewHolder holder;

                if(convertView == null) {
                    holder = new ViewHolder();
                    LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
                    convertView = inflater.inflate(R.layout.listview_alarm, parent, false);

                    holder.tvName = (TextView) convertView.findViewById(R.id.tvName);
                    holder.tvValue = (TextView) convertView.findViewById(R.id.tvValue);
                    holder.tvType = (TextView) convertView.findViewById(R.id.tvType);
                    holder.tvDate = (TextView) convertView.findViewById(R.id.tvDate);

                    convertView.setTag(holder);
                } else {
                    holder = (ViewHolder) convertView.getTag();
                }

                AlarmItem alarm = alarmList.get(position);
                holder.tvName.setText(allSensorsInfo.getSensorName(alarm.getId()));
                holder.tvValue.setText(alarm.getValue().toString());
                holder.tvType.setText(alarm.getType());
                holder.tvDate.setText(alarm.getDate());

                return convertView;
            }

            @Override
            public Object getItem(int position) {
                return alarmList.get(position);
            }

            @Override
            public long getItemId(int position) {
                return 0;
            }

            @Override
            public int getCount() {
                return alarmList.size();
            }

            class ViewHolder {
                TextView tvName, tvValue, tvType, tvDate;
            }
        }

        private List<AlarmItem> getJSONData(String str) throws JSONException {

            List<AlarmItem> list = new ArrayList<>();

            Log.e("JSON",str);

            JSONObject jObj = new JSONObject(str);
            String result = jObj.getString("result");
            Log.e("JSON result",result);
            JSONArray jArray = jObj.getJSONArray("alarms");

            for (int i = 0; i < jArray.length(); i++) {
                JSONObject jAlarm = jArray.getJSONObject(i);

                Integer id = jAlarm.getInt("sensorID");
                Double value = jAlarm.getDouble("alarmValue");
                String date = jAlarm.getString("date");
                String type = jAlarm.getString("alarmType");

//                Sensor(Integer sensorID, String name, Double hiAlarm, Double loAlarm, Double latitude, Double longitude, String type, Double rangeHi, Double rangeLo, String unit, String desc,  boolean isSelected)
                AlarmItem alarm = new AlarmItem(id, value, type, date);
                list.add(alarm);
                Log.e("JSON Alarm",alarm.getId().toString());
                Log.e("JSON Alarm",alarm.getType());
                Log.e("JSON Alarm",alarm.getDate());
                Log.e("JSON Alarm",alarm.getValue().toString());
            }

            return list;
        }
    }
}
