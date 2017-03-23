package com.idv.napchen.asynctest;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;

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

import static android.content.ContentValues.TAG;

public class MainActivity extends AppCompatActivity {

    // 請求代碼，自行定義
    private final int CHART_SETTING_REQUEST = 0;
    private final int EXPORT_REQUEST = 1;
    private final int ALARM_REQUEST = 2;
    private final int SETTING_REQUEST = 3;

    private static final String JSON_ADDRESS = "/dbinfoGet.php?username=root&password=root&database=eEyes&appUserName=user&appPassword=password&type=getSensorByUser";

    private Button btnSubmit, btnExport, btnAlarm, btnSetting;

    private List<Sensor> sensorList;

    private ProgressDialog progressDialog;
    private ListView listView;
    private AllSensorsInfo allSensorsInfo;

    private int httpStatusCode;
    private String errorMsg;

    private SharedPreferences sharedPref;
    private static String dbIP;

    private HttpGetSensorValue httpGetSensorValue;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        allSensorsInfo = AllSensorsInfo.getInstance();

        // Setup IP from settings file
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        dbIP = sharedPref.getString("mainIPAddress", null);

        // Show token
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Main start token: " + refreshedToken);
        sendRegistrationToServer(refreshedToken);


        findViews();
    }

    private void sendRegistrationToServer(final String token){
        httpGetSensorValue = new HttpGetSensorValue(new HttpGetSensorValue.OnTaskCompleted() {

            @Override
            public void onTaskCompleted() {
                Log.d(TAG,"http://" + dbIP + "/dbSensorValue_GET.php?password=root&insertdata=" + token + "&database=eEyes&table=deviceToken&username=root&field=DeviceToken&insertdate=2017-03-22%2014:17:26&type=updateDeviceToken&datefield=LastUpdateDateTime");
                Log.d(TAG,"Updated DeviceToken");
            }
        });
        httpGetSensorValue.execute("http://" + dbIP + "/dbSensorValue_GET.php?password=root&insertdata=" + token + "&database=eEyes&table=deviceToken&username=root&field=DeviceToken&insertdate=2017-03-22%2014:17:26&type=updateDeviceToken&datefield=LastUpdateDateTime");


    }

    private void findViews() {

        btnSubmit = (Button) findViewById(R.id.btnChartSetting);
        btnExport = (Button) findViewById(R.id.btnExoprt);
        btnAlarm = (Button) findViewById(R.id.btnAlarm);
        btnSetting = (Button) findViewById(R.id.btnSetting);

        listView = (ListView) findViewById(R.id.listView);

        httpStatusCode = 0;
        errorMsg = "";

        // get all sensor info.
        String httpHeader = getString(R.string.http_Header);
        dbIP = httpHeader + dbIP + JSON_ADDRESS;
        new GetAllSensorInfo().execute(dbIP);

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if(sensorList == null) {
//                    displayWarningMessage();
//                } else {
                    Intent intent;
                    intent = new Intent(MainActivity.this, ChartSettingActivity.class);
                    startActivity(intent);
//                }
            }
        });

        btnExport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(sensorList == null) {
                    displayWarningMessage();
                } else {
                    Intent intent = new Intent();
                    intent.setClass(MainActivity.this, ExportActivity.class);
                    startActivity(intent);
                }
            }
        });

        btnAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(sensorList == null) {
                    displayWarningMessage();
                } else {
                    Intent intent = new Intent();
                    intent.setClass(MainActivity.this, AlarmActivity.class);
                    startActivity(intent);
                }
            }
        });

        btnSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();

                intent.setClass(MainActivity.this, SettingsActivity.class);
                //將自行定義的請求代碼一起送出，才能確認資料來源與出處是否為同一個
                startActivityForResult(intent, SETTING_REQUEST);
            }
        });
    }

    private void displayWarningMessage() {

        String errorExport;
        if (httpStatusCode == 0) {
            errorExport = "HTTP Error : " + errorMsg;
        } else {
            errorExport = "HTTP Error, error code : " + Integer.toString(httpStatusCode);
        }
        Toast.makeText(MainActivity.this, errorExport, Toast.LENGTH_SHORT).show();

        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
        alertDialog.setTitle("Alert");
        alertDialog.setMessage(errorExport);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    private class GetAllSensorInfo extends AsyncTask<String, Integer, List<Sensor>> {

        // pre execute before async start...
        @Override
        protected void onPreExecute() {
Log.e("AsyncTask","onPreExecute...");
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setMessage("Loading...");
            progressDialog.show();
        }

        @Override
        protected List<Sensor> doInBackground(String... params) {

            String url = params[0];
Log.e("AsyncTask",url);
            StringBuilder sb = new StringBuilder();

            try {

                URL myurl = new URL(url);
                HttpURLConnection con = (HttpURLConnection) myurl.openConnection();
                con.setRequestMethod("GET");
                con.setDoInput(true);           //
                con.setUseCaches(false);        // wothout catch usage
                con.setConnectTimeout(3000);
                con.connect();
                Log.e("AsyncTask","HTTP Connect...");
                httpStatusCode = con.getResponseCode();
Log.e("HTTP","HTTP Response...");
                if(httpStatusCode == HttpURLConnection.HTTP_OK) {
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
                } else {
                    Log.e("Status",Integer.toString(httpStatusCode));
                    return null;
                }
            } catch (MalformedURLException me) {
                Log.e("MalformedURLException", me.toString());
                errorMsg = me.toString();
            } catch (IOException ioe) {
                Log.e("IOException", ioe.toString());
                errorMsg = ioe.toString();
            }

            Log.e("Main", "after connection");

            if(sb.length() > 0) {
                // if have data, return data
                try {
                    Log.e("Main", "will parse JSON");
                    sensorList = getJSONData(sb.toString());
                    Log.e("Main", "parsed JSON");
                    return sensorList;
                } catch (JSONException je) {
                    je.printStackTrace();
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(List<Sensor> result) {

            progressDialog.dismiss();

            if(sensorList != null) {
                listView.setAdapter(new MyAdapter(result));
            } else {
//                String errorExport;
//                if(httpStatusCode == 0) {
//                    errorExport = "HTTP Error : " + errorMsg;
//                } else {
//                    errorExport = "HTTP Error, error code : " + Integer.toString(httpStatusCode);
//                }
                displayWarningMessage();
            }
        }

        private class MyAdapter extends BaseAdapter {

            private List<Sensor> sensorList;

            public MyAdapter() {
            }

            public MyAdapter(List<Sensor> sensorList) {
                this.sensorList = sensorList;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {

                ViewHolder holder;

                if(convertView == null) {
                    holder = new ViewHolder();
                    LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
                    convertView = inflater.inflate(R.layout.listview_park, parent, false);

                    holder.sName = (TextView) convertView.findViewById(R.id.tvName);
                    holder.tvType = (TextView) convertView.findViewById(R.id.tvType);
                    convertView.setTag(holder);
                } else {
                    holder = (ViewHolder) convertView.getTag();
                }

                Sensor sensor = sensorList.get(position);
                holder.sName.setText(sensor.getName());
                holder.tvType.setText(sensor.getType());

                return convertView;
            }

            @Override
            public Object getItem(int position) {
                return sensorList.get(position);
            }

            @Override
            public long getItemId(int position) {
                return 0;
            }

            @Override
            public int getCount() {
                return sensorList.size();
            }

            class ViewHolder {
                TextView sName, tvType;
            }
        }

        private List<Sensor> getJSONData(String str) throws JSONException {

            List<Sensor> list = new ArrayList<>();

            Log.e("JSON",str);

            JSONObject jObj = new JSONObject(str);
            String result = jObj.getString("result");
            Log.e("JSON result",result);
            JSONArray jArray = jObj.getJSONArray("sensors");

            for (int i = 0; i < jArray.length(); i++) {
                JSONObject jSensor = jArray.getJSONObject(i);

                Integer id = jSensor.getInt("sensorID");
                String name = jSensor.getString("sensorName");
                Double hiAlarm = jSensor.getDouble("hiAlarm");
                Double loAlarm = jSensor.getDouble("loAlarm");
                Double latitude = jSensor.getDouble("latitude");
                Double longitude = jSensor.getDouble("longitude");
                String type = jSensor.getString("sensorType");
                Double rangeHi = jSensor.getDouble("rangeHi");
                Double rangeLo = jSensor.getDouble("rangeLo");
                String unit = jSensor.getString("unit");
                String description = jSensor.getString("description");
                String dbRealValueTable = jSensor.getString("dbRealValueTable");
                String dbAverageValueTable = jSensor.getString("dbAverageValueTable");

//                Sensor(Integer sensorID, String name, Double hiAlarm, Double loAlarm, Double latitude, Double longitude, String type, Double rangeHi, Double rangeLo, String unit, String desc,  boolean isSelected)
                Sensor sensor = new Sensor(id, name, hiAlarm, loAlarm, latitude, longitude, type, rangeHi, rangeLo, unit, description, dbRealValueTable, dbAverageValueTable, true);
                list.add(sensor);
                Log.e("JSON Sensor",sensor.getName());
                Log.e("JSON Sensor",sensor.getType());
                Log.e("JSON Sensor",sensor.getDesc());
                Log.e("JSON Sensor",sensor.getSensorID().toString());
                Log.e("JSON Sensor",sensor.getHiAlarm().toString());
                Log.e("JSON Sensor",sensor.getLoAlarm().toString());
            }

            allSensorsInfo.setAllSensorsInfo(list);

            return list;
        }

    }
}
