package com.idv.napchen.asynctest;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
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

    private static final String JSON_ADDRESS = "/dbinfoGet.php?username=root&password=root&database=eEyes&appUserName=user&appPassword=password&type=getSensorByUser";

    private List<Sensor> sensorList;

    private ProgressDialog progressDialog;
    private AllSensorsInfo allSensorsInfo;

    private int httpStatusCode;
    private String errorMsg;

    private SharedPreferences sharedPref;
    private static String dbIP;

    private HttpGetSensorValue httpGetSensorValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // setup FrameLayout and NavigationView
        setContentView(R.layout.activity_navi_view_main);

        // setup action bar icon
        setUpActionBar();

        //
        initDrawer();
        initBody();

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
                        initBody();
                        break;
                }
                return true;
            }
        });
    }

    private void initBody() {
        // initial fragment body
        Fragment fragment = new com.idv.napchen.asynctest.HomeFragment();
        switchFragment(fragment);
//        setTitle(R.string.text_Open);
    }

    private void switchFragment(Fragment fragment) {

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.body, fragment);   // FrameLayout
        fragmentTransaction.commit();
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

    private void findViews() {

        httpStatusCode = 0;
        errorMsg = "";

        // get all sensor info.
        String httpHeader = getString(R.string.http_Header);
        dbIP = httpHeader + dbIP + JSON_ADDRESS;
        new GetAllSensorInfo().execute(dbIP);
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

            if(sensorList == null) {
                displayWarningMessage();
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
