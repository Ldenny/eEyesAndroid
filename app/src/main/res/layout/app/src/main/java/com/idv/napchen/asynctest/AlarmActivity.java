package com.idv.napchen.asynctest;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

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

    private ListView listView;

    private ProgressDialog progressDialog;

    private AllSensorsInfo allSensorsInfo;

    private List<AlarmItem> alarmList;

    private SharedPreferences sharedPref;

    private static String dbIP;




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

        allSensorsInfo = AllSensorsInfo.getInstance();

        dbIP = "http://" + dbIP +GET_ALARM_INFO_ADDRESS;

        new GetAllAlarmInfo().execute(dbIP);
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
