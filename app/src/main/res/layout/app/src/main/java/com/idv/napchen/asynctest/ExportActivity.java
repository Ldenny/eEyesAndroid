package com.idv.napchen.asynctest;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by napchen on 2017/3/15.
 */

public class ExportActivity extends AppCompatActivity {

    private TextView tvTest;
    private Button btnSave,toFileList,getData;
    private EditText etFileName;
    private final static String TAG = "ExportActivity";
    private static final int WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 1;
    private SharedPreferences sharedPref;
    private static String dbIP;
    private List<Double> tempValue;
    private List<Double> humidValue;
    private List<String> dateTime;


    private HttpGetSensorValue httpGetSensorValue;

    private List<DisplaySensorValues> displaySensorValuesList;

    private boolean isFirstGot, isSecondGot;

    private String toCSV;
    int totalCount;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_savecsvfile);



        findViews();
    }

    private void findViews(){

        // Convert UI
        getData = (Button)findViewById(R.id.getData);
        etFileName = (EditText)findViewById(R.id.etFileName);
        toFileList = (Button)findViewById(R.id.gotoFileList);
        tvTest = (TextView)findViewById(R.id.prefTest);

        // Get settings from settings xml
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String dbIp = sharedPref.getString("mainIPAddress", null);

        tvTest.setText(dbIp);


        toFileList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ExportActivity.this, CSVFileListActivity.class);
                startActivity(intent);
            }
        });

        // Save button pressed
        btnSave = (Button)findViewById(R.id.btnSave);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDataToCSV();
            }
        });

        getData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {



            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    WRITE_EXTERNAL_STORAGE_REQUEST_CODE);
        }
    }
    private void saveFile(File dir, String fileName){
        if (!isStorageMounted()) {
            Toast.makeText(this, getString(R.string.msg_ExternalStorageNotFound), Toast.LENGTH_SHORT).show();
            return;
        }
        // Init BufferedWritter and FileOutputStream
        BufferedWriter bw = null;
        FileOutputStream fop = null;

        for(int i = 0;i < tempValue.size();i++){
            if(toCSV == null){
                toCSV = i + "," + tempValue.get(i).toString().toString() + "," + humidValue.get(i).toString() + "," + dateTime.get(i) + "\n";
            }else {
                toCSV = toCSV + i + "," + tempValue.get(i).toString() + "," + humidValue.get(i).toString() + "," + dateTime.get(i) + "\n";
            }
        }
        try {
            // 若此資料夾不存在
            if (!dir.exists()) {
                // 若此資料夾沒被建立
                if (!dir.mkdirs()) {
                    Toast.makeText(this, getString(R.string.msg_DirectoryNotCreated), Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            File file = new File(dir, fileName);
            fop = new FileOutputStream(file);
            byte[] contentInBytes = toCSV.getBytes();

            fop.write(contentInBytes);
            fop.flush();
            fop.close();

        }catch (IOException ie){
            Log.e(TAG,ie.toString());
        }finally {
            try {
                if (fop != null) {
                    fop.close();
                }
                if (bw != null){
                    bw.close();
                }
            }catch (IOException ie){
                Log.e(TAG,ie.toString());
            }
        }
        Toast.makeText(ExportActivity.this, fileName + " saved", Toast.LENGTH_SHORT).show();
    }


    private boolean isStorageMounted() {
        String result = Environment.getExternalStorageState();
        // MEDIA_MOUNTED代表可對外部媒體進行存取
        return result.equals(Environment.MEDIA_MOUNTED);
    }
    private void getDataToCSV(){

        // Init three values array and String to CSV
        tempValue = new ArrayList<>();
        humidValue = new ArrayList<>();
        dateTime = new ArrayList<>();
        toCSV = null;

        // Setup IP from settings file
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        dbIP = sharedPref.getString("mainIPAddress", null);

        displaySensorValuesList = new ArrayList<>();

        isFirstGot = false;
        isSecondGot = false;

        for(int j = 0; j < 2; j++) {
            httpGetSensorValue = new HttpGetSensorValue(new HttpGetSensorValue.OnTaskCompleted() {
                @Override
                public void onTaskCompleted() {
                    String sensorValuesString = httpGetSensorValue.getResultStringData();

                    try {
                        DisplaySensorValues displaySensorValues = getJSONData(sensorValuesString);
                        displaySensorValuesList.add(displaySensorValues);

                        int k;
                        if (isFirstGot == false) {
                            k = 0;
                        } else {
                            k = 1;
                        }
                        DisplaySensorValues dsv = displaySensorValuesList.get(k);

                        List<Double> values = new ArrayList<>();
                        List<String> dates = new ArrayList<>();
                        if(k == 0){
                            tempValue = dsv.getValue();
                        }else if (k == 1){
                            humidValue = dsv.getValue();
                            dateTime = dsv.getDate();
                        }
                        values = dsv.getValue();
                        dates = dsv.getDate();

                        Log.e("count", Integer.toString(values.size()));

                        for (int i = 0; i < values.size() / 30; i++) {
                            Log.e("value", values.get(i).toString());
                            Log.e("date", dates.get(i));
                        }

                        if (isFirstGot == false) {
                            isFirstGot = true;
                            Log.e("first", "got");
                        } else {
                            isSecondGot = true;
                            Log.e("second", "got");
                        }

                    } catch (JSONException je) {
                        je.printStackTrace();
                    }

                }
            });

            if (j == 0) {
                httpGetSensorValue.execute("http://" + dbIP + "/dbSensorValueJSONGet.php?username=root&password=root&database=eEyes&table=SensorRawData&field=RawValue&sensorID=1&datefield=StartDate&startdate=2017-03-12%2018:00:00&enddate=2017-03-12%2018:03:00&type=getRange");
                Log.e("first", "send...");
                try {
                    //set time in mili
                    Thread.sleep(100);
                    Log.e("first", "delay...");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {

                httpGetSensorValue.execute("http://" + dbIP + "/dbSensorValueJSONGet.php?username=root&password=root&database=eEyes&table=SensorRawData&field=RawValue&sensorID=2&datefield=StartDate&startdate=2017-03-12%2018:00:00&enddate=2017-03-12%2018:03:00&type=getRange");
                Log.e("second", "send...");
                try {
                    //set time in mili
                    Thread.sleep(100);
                    Log.e("first", "delay...");
                    String fileName = etFileName.getText().toString();
                    File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
                    saveFile(dir, fileName + ".csv");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }
    private DisplaySensorValues getJSONData(String str) throws JSONException {

        JSONObject jObj = new JSONObject(str);
        String result = jObj.getString("result");
        Log.e("JSON result",result);
        JSONArray jArray = jObj.getJSONArray("values");

        totalCount = jArray.length();

        List<Double> values = new ArrayList<>();
        List<String> dates = new ArrayList<>();

        Log.e("JSON length",Integer.toString(jArray.length()));

        for (int i = 0; i < jArray.length(); i++) {
            JSONObject jSensor = jArray.getJSONObject(i);

            Double value = jSensor.getDouble("value");
            String date = jSensor.getString("date");

            values.add(value);
            dates.add(date);

        }

        Log.e("Date","save to dsv OK!");

        DisplaySensorValues displaySensorValues = new DisplaySensorValues(values, dates);
        return displaySensorValues;
    }

}