package com.idv.napchen.asynctest;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
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

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle actionBarDrawerToggle;

    private TextView tvTempValue, tvHumiValue;

    private Handler handler;

    HttpGetSensorValue setHttpAlarmCheckinfEnable;

    private static final String JSON_ADDRESS = "/dbinfoGet.php?username=root&password=root&database=eEyes&appUserName=user&appPassword=password&type=getSensorByUser";
    private static final String START_ALARM_CHECKING_ADDRESS = "/SendAllAlarm/checkAlarmGet.php?username=root&password=root&database=eEyes&appUserName=user&type=checkAlarm&sec=1";

    private List<Sensor> sensorList;

    private ProgressDialog progressDialog;
    private AllSensorsInfo allSensorsInfo;

    private int httpStatusCode;
    private String errorMsg;

    private SharedPreferences sharedPref;
    private static String dbIP;

    private HttpGetSensorValue httpGetSensorValue;

    private boolean isFirst, isSecond, isHandlerEnable, isAllSensorGot, isHttpResponse, isTokenSent;

    private String url1 = "/dbSensorValueJSONGet.php?username=root&password=root&database=eEyes&table=RealID10001&field=RealValue&sensorID=1&datefield=Date&startdate=2017-03-20%2009:37:01&enddate=2017-02-28%2015:30:00&type=getNewest";

    private String url2 = "/dbSensorValueJSONGet.php?username=root&password=root&database=eEyes&table=RealID10002&field=RealValue&sensorID=2&datefield=Date&startdate=2017-03-20%2009:37:01&enddate=2017-02-28%2015:30:00&type=getNewest";

    private double tempValue, humiValue;

    private int timeoutCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // setup FrameLayout and NavigationView
        setContentView(R.layout.activity_navi_view_main);

        // setup action bar icon
        setUpActionBar();

        //
        initDrawer();

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

    @Override
    protected void onStop() {
        super.onStop();

        Log.e("onStop", "entry...");

        if(isHandlerEnable == true) {
            Log.e("onStop", "stop...");
            isHandlerEnable = false;
            handler.removeCallbacks(timerRun);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.e("onPause", "entry...");
        if(isHandlerEnable == true) {
            Log.e("onPause", "stop...");
            isHandlerEnable = false;
            handler.removeCallbacks(timerRun);
        }
    }

    @Override
    public void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // 讓Drawer關閉時，出現三條線 |||
        actionBarDrawerToggle.syncState();
    }

    private void setUpActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // 讓Drawer開啟時，在左上角出現一個"←"圖示
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void initDrawer() {

        // setup DrawerLayout
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        // text_Open and text_Close not use
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.text_Open, R.string.text_Close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);

        navigationView = (NavigationView) findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                // make selected item dark
                menuItem.setChecked(true);
                // return drawer
                drawerLayout.closeDrawers();

                Intent intent;
                intent = new Intent(MainActivity.this, ChartSettingActivity.class);

                switch (menuItem.getItemId()) {
                    case R.id.item_charting:
                        intent = new Intent(MainActivity.this, ChartSettingActivity.class);
                        startActivity(intent);
                        break;
                    case R.id.item_export:
//                        Intent intent = new Intent();
                        intent.setClass(MainActivity.this, ExportActivity.class);
                        startActivity(intent);
                        break;
                    case R.id.item_alarm:
//                        Intent intent = new Intent();
                        intent.setClass(MainActivity.this, AlarmActivity.class);
                        startActivity(intent);
                        break;
                    case R.id.item_settings:
//                        Intent intent = new Intent();
                        intent.setClass(MainActivity.this, SettingsActivity.class);
                        startActivity(intent);
                        break;
                    default:
                        break;
                }
                return true;
            }
        });
    }

    private void switchFragment(Fragment fragment) {

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.body, fragment);   // FrameLayout
        fragmentTransaction.commit();
    }

    private void sendRegistrationToServer(final String token){

        httpGetSensorValue = new HttpGetSensorValue(new HttpGetSensorValue.OnTaskCompleted() {

            @Override
            public void onTaskCompleted() {
                Log.d(TAG,"http://" + dbIP + "/dbSensorValue_GET.php?password=root&insertdata=" + token + "&database=eEyes&table=deviceToken&username=root&field=DeviceToken&insertdate=2017-03-22%2014:17:26&type=updateDeviceToken&datefield=LastUpdateDateTime");
                Log.d(TAG,"Updated DeviceToken");

                isHttpResponse = true;
            }
        });

        isHttpResponse = false;
        httpGetSensorValue.execute("http://" + dbIP + "/dbSensorValue_GET.php?password=root&insertdata=" + token + "&database=eEyes&table=deviceToken&username=root&field=DeviceToken&insertdate=2017-03-22%2014:17:26&type=updateDeviceToken&datefield=LastUpdateDateTime");

        try {
            //set time in mili
            Thread.sleep(300);
            Log.e("HttpToken", "delay...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void findViews() {

        httpStatusCode = 0;
        errorMsg = "";

        // set check alarm
        setHttpAlarmCheckinfEnable = new HttpGetSensorValue(new HttpGetSensorValue.OnTaskCompleted() {
            @Override
            public void onTaskCompleted() {
                String sensorValuesString = setHttpAlarmCheckinfEnable.getResultStringData();

                Log.e("HTTP response",sensorValuesString);

                if(sensorValuesString.substring(0,4).equals("HTTP")) {
                    errorMsg = sensorValuesString;
                    return;
                }

                if(sensorValuesString.length() == 0) {
                    errorMsg = "Http no response!";
                    return;
                }

                isHttpResponse = true;
            }

        });

        isHttpResponse = false;
        String url = "http://" + dbIP + START_ALARM_CHECKING_ADDRESS;
        Log.e("setAlarmCheckinfEnable",url);
        setHttpAlarmCheckinfEnable.execute(url);

        int counter = 0;
        while (isHttpResponse == false) {
            try {
                //set time in mili
                Thread.sleep(300);
                Log.e("HttpToken", "delay...");
                counter++;
                if(counter >= 10) {
                    Log.e("HttpToken", "timeout...");
                    break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if(isHttpResponse == false) {
            displayWarningMessage(errorMsg);
            return;
        }

        isAllSensorGot = false;

        // get all sensor info.
        String httpHeader = getString(R.string.http_Header);
        url = httpHeader + dbIP + JSON_ADDRESS;
//        dbIP = httpHeader + dbIP + JSON_ADDRESS;
        new GetAllSensorInfo().execute(url);

        counter = 0;
        while (isAllSensorGot == false) {
            try {
                Thread.sleep(300);
                Log.e("GetAllSensorInfo", "delay...");
                counter++;
                if(counter >= 10) {
                    Log.e("isAllSensorGot", "timeout...");
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        tvTempValue = (TextView) findViewById(R.id.tvTempValue);
        tvHumiValue = (TextView) findViewById(R.id.tvHumiValue);

        url1 = "http://" + dbIP + url1;
        url2 = "http://" + dbIP + url2;

        isHandlerEnable = true;
        startGetSensorValue();
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.e("onResume", "entry");

        if(isHandlerEnable == false && isAllSensorGot == true) {
            Log.e("onResume", "handler...");
            isHandlerEnable = true;
            startGetSensorValue();
        }
    }

    private void startGetSensorValue() {

        tempValue = 0;
        humiValue = 0;
        timeoutCount = 0;

        isFirst = false;
        isSecond = false;

        Log.e("GetAllSensorInfo", url1);
        Log.e("GetAllSensorInfo", url2);

        handler = new Handler();
        handler.postDelayed(timerRun, 1000);
    }

    private final Runnable timerRun = new Runnable() {

        public void run()
        {
//            ++m_nTime; // 經過的秒數 + 1
            handler.postDelayed(this, 1000);

            Log.e("main timer event","1 sec entry");

            if(isFirst == true || isSecond == true) {
                Log.e("timer event","still wait data leave...");
                return;
            }

            boolean isTimeOut = false;

            for(int j = 0; j < 2; j++) {
                httpGetSensorValue = new HttpGetSensorValue(new HttpGetSensorValue.OnTaskCompleted() {
                    @Override
                    public void onTaskCompleted() {
                        String sensorValuesString = httpGetSensorValue.getResultStringData();

                        Log.e("HTTP response",sensorValuesString);

                        if(sensorValuesString.substring(0,4).equals("HTTP")) {
                            errorMsg = sensorValuesString;
                            return;
                        }

                        if(sensorValuesString.length() == 0) {
                            errorMsg = "Http no response!";
                            return;
                        }

                        try {
                            getJSONData(sensorValuesString);
                            if(isFirst) {
                                isFirst = false;
                            } else {
                                isSecond = false;
                            }
                        } catch (JSONException je) {
                            je.printStackTrace();
                        }
                    }
                });

                int counter = 0;

                if(j == 0) {
                    Log.e("first",url1);
                    httpGetSensorValue.execute(url1);
                    isFirst = true;

                    while (isFirst == true) {
                        try {
                            //set time in mili
                            Thread.sleep(100);
                            Log.e("first", "delay...");
                            counter++;
                            if(counter >= 30) {
                                isFirst = false;
                                isTimeOut = true;
                                Log.e("first", "timeout...");
                                break;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } else if(isTimeOut == false) {
                    Log.e("second",url2);
                    httpGetSensorValue.execute(url2);
                    isSecond = true;

                    while (isSecond == true) {
                        try {
                            //set time in mili
                            Thread.sleep(100);
                            Log.e("second", "delay...");
                            counter++;
                            if(counter >= 30) {
                                isSecond = false;
                                isTimeOut = true;
                                Log.e("second", "timeout...");
                                break;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            if(isTimeOut == false) {

                tvTempValue.setText(Double.toString(tempValue));
                tvHumiValue.setText(Double.toString(humiValue));
                Log.e("sensor value", "updated...");

                timeoutCount = 0;
            } else {
                Log.e("sensor value", "timeout...");
                timeoutCount++;
                if(timeoutCount >= 5) {
                    displayWarningMessage(errorMsg);
                    handler.removeCallbacks(timerRun);
                }
            }
        }
    };

    private void getJSONData(String str) throws JSONException {

        JSONObject jObj = new JSONObject(str);
        String result = jObj.getString("result");
        Log.e("JSON result",result);
        JSONArray jArray = jObj.getJSONArray("values");

        Log.e("JSON length",Integer.toString(jArray.length()));

        for (int i = 0; i < jArray.length(); i++) {
            JSONObject jSensor = jArray.getJSONObject(i);

            Integer id = jSensor.getInt("id");
            Double value = jSensor.getDouble("value");

            if(id == 1) {
                tempValue = value;
            } else {
                humiValue = value;
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    drawerLayout.openDrawer(GravityCompat.START);
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void displayWarningMessage(String msg) {

        Log.e("displayWarningMessage","msg");

        Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();

        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
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

                    isAllSensorGot = true;
                    Log.e("isAllSensorGot", "GOT~");

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

            if(sensorList == null) {
                displayWarningMessage("no sensor info!");
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
