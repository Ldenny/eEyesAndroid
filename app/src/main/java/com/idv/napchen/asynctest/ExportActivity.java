package com.idv.napchen.asynctest;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static android.R.attr.end;
import static android.R.attr.password;
import static android.R.attr.type;

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

    private boolean isFirstGot, isSecondGot,isHttpResponse;

    private String errMsg;

    private static EditText etStartDate, etEndDate;

    private static int year, month, day, hour, minute;

    private int runTimes;

    private SettingSingleton settingSingleton;

    private String toCSV,startDate,endDate;
    int totalCount;

    private static String dateStr;

    private String mainIP,dbAccount,dbPW,dbName,dbUserName,dbUserPw;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_savecsvfile);

        // get settings from singleton
        settingSingleton = settingSingleton.getInstance();
        getAllSettings();



        isFirstGot = false;
        isSecondGot = false;
        isHttpResponse = false;


        findViews();
    }

    private void findViews(){

        // Convert UI
        etFileName = (EditText)findViewById(R.id.etFileName);
        toFileList = (Button)findViewById(R.id.gotoFileList);
        etStartDate = (EditText) findViewById(R.id.etStartDate);
        etEndDate = (EditText) findViewById(R.id.etEndDate);

        showRightNow();

        etStartDate.setText("2017-03-12 18:00:00");
        etEndDate.setText("2017-03-12 18:05:00");

        // Get settings from settings xml
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        dbIP = sharedPref.getString("mainIPAddress", null);



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


                Log.e("button","1st check...");
                startDate = etStartDate.getText().toString();
                startDate = startDate.replace(" ","%20");
                Log.e("button",startDate);
                endDate = etEndDate.getText().toString();
                endDate = endDate.replace(" ","%20");
                Log.e("button",endDate);

                getDataToCSV();
            }
        });

        etStartDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerFragment timePickerFragment = new TimePickerFragment(v);
                FragmentManager fm = getSupportFragmentManager();
                timePickerFragment.show(fm, "timePicker");

                DatePickerFragment datePickerFragment = new DatePickerFragment();
                fm = getSupportFragmentManager();
                datePickerFragment.show(fm, "datePicker");

            }
        });
        etEndDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerFragment timePickerFragment = new TimePickerFragment(v);
                FragmentManager fm = getSupportFragmentManager();
                timePickerFragment.show(fm, "timePicker");

                DatePickerFragment datePickerFragment = new DatePickerFragment();
                fm = getSupportFragmentManager();
                datePickerFragment.show(fm, "datePicker");
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
            int j = i+1;
            if(toCSV == null){
                toCSV = "No,Temp,Humid,Time\n" + j + "," + tempValue.get(i).toString().toString() + "," + humidValue.get(i).toString() + "," + dateTime.get(i) + "\n";
            }else {
                toCSV = toCSV + j + "," + tempValue.get(i).toString() + "," + humidValue.get(i).toString() + "," + dateTime.get(i) + "\n";
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
                Toast.makeText(ExportActivity.this, fileName + " saved", Toast.LENGTH_SHORT).show();
            }catch (IOException ie){
                Log.e(TAG,ie.toString());
            }catch (Exception e){
                Log.e(TAG,e.toString());
            }
        }

    }


    //"http://" + dbIP + "/dbSensorValueJSONGet.php?username=root&password=root&database=eEyes&table=SensorRawData&field=RawValue&sensorID=1&datefield=StartDate&startdate=2017-03-12%2018:00:00&enddate=2017-03-12%2018:03:00&type=getRange"
    //"http://" + dbIP + "/dbSensorValueJSONGet.php?username=root&password=root&database=eEyes&table=SensorRawData&field=RawValue&sensorID=2&datefield=StartDate&startdate=2017-03-12%2018:00:00&enddate=2017-03-12%2018:03:00&type=getRange"
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
        //dbIP = sharedPref.getString("mainIPAddress", null);

        displaySensorValuesList = new ArrayList<>();

        isFirstGot = false;
        isSecondGot = false;
        isHttpResponse = false;

        boolean isTimeout = false;


        for(int j = 0; j < 2; j++) {
            runTimes = j;
            httpGetSensorValue = new HttpGetSensorValue(new HttpGetSensorValue.OnTaskCompleted() {
                @Override
                public void onTaskCompleted() {
                    String sensorValuesString = httpGetSensorValue.getResultStringData();

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

                        DisplaySensorValues displaySensorValues = getJSONData(sensorValuesString);
                        displaySensorValuesList.add(displaySensorValues);
                        if(runTimes == 0){
                            tempValue = displaySensorValues.getValue();
                        }else if (runTimes == 1){
                            humidValue = displaySensorValues.getValue();
                            dateTime = displaySensorValues.getDate();
                        }

                    } catch (JSONException je) {
                        je.printStackTrace();
                    }

                    isHttpResponse = true;
                }
            });

            isHttpResponse = false;
            int counter = 0;


            if(j == 0) {
                String url1 ="http://" + mainIP + "/dbSensorValueJSONGet.php?username=" + dbAccount + "&password=" + dbPW + "&database=" + dbName + "&table=SensorRawData&field=RawValue&sensorID=1&datefield=StartDate&startdate="+startDate+"&enddate="+endDate+"&type=getRange";

                Log.e("url Start",url1);

                httpGetSensorValue.execute(url1);

                while (isHttpResponse == false) {
                    try {
                        Thread.sleep(300);
                        Log.e("first", "delay...");
                        counter++;
                        if(counter >= 10) {
                            Log.e("first", "timeout...");
                            isTimeout = true;
                            break;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                    if (isTimeout == true) {
                        displayWarningMessage(errMsg);
                        return;
                    }

            }
            else {
                String url2 = "http://" + mainIP + "/dbSensorValueJSONGet.php?username=" + dbAccount + "&password=" + dbPW + "&database=" + dbName + "&table=SensorRawData&field=RawValue&sensorID=2&datefield=StartDate&startdate="+startDate+"&enddate="+endDate+"&type=getRange";
                Log.e("url end",url2);
                httpGetSensorValue.execute(url2);

                while (isHttpResponse == false) {
                    try {
                        Thread.sleep(300);
                        Log.e("second", "delay...");
                        counter++;
                        if(counter >= 10) {
                            Log.e("second", "timeout...");
                            isTimeout = true;
                            break;
                        }else{
                            String fileName = etFileName.getText().toString();
                            File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
                            saveFile(dir, fileName + ".csv");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                if(isTimeout == true) {
                    displayWarningMessage(errMsg);
                    return;
                }
            }





            ////


        }

        while(isHttpResponse == false) {
            try {
                //set time in mili
                Thread.sleep(1000);
            }catch (Exception e){
                e.printStackTrace();
            }
            Log.e("wait","1 sec...");
        }

        if(displaySensorValuesList.size() < 2) {

            displayWarningMessage("no data or data error");
            return;
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

    private boolean checkDateFormat(String dateStr) {

        // 0123456789012345678
        // 2017-03-16 13:00:00
        Integer dateNO;

        try{
            dateNO = Integer.valueOf(dateStr.substring(0,4));

            if(dateNO > 10000) {
                return false;
            }

            dateNO = Integer.valueOf(dateStr.substring(5,7));

            Log.e("year",dateNO.toString());

            if(dateNO > 12 && dateNO < 1) {
                return false;
            }

            dateNO = Integer.valueOf(dateStr.substring(8,10));

            Log.e("year",dateNO.toString());
            if(dateNO > 31 && dateNO < 1) {
                return false;
            }

            dateNO = Integer.valueOf(dateStr.substring(11,13));

            Log.e("year",dateNO.toString());
            if(dateNO > 23 && dateNO < 0) {
                return false;
            }

            dateNO = Integer.valueOf(dateStr.substring(14,16));

            Log.e("year",dateNO.toString());
            if(dateNO > 59 && dateNO < 0) {
                return false;
            }

            dateNO = Integer.valueOf(dateStr.substring(17,19));

            Log.e("year",dateNO.toString());
            if(dateNO > 59 && dateNO < 0) {
                return false;
            }
        }catch(NumberFormatException e){
            Log.e("NumberFormatException",e.getMessage());
            return false;
        }
        return true;
    }

    private void displayWarningMessage(String msg) {

        Toast.makeText(ExportActivity.this, msg, Toast.LENGTH_SHORT).show();

        AlertDialog alertDialog = new AlertDialog.Builder(ExportActivity.this).create();
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



    private static void showRightNow() {

        Calendar c = Calendar.getInstance();
        year = c.get(Calendar.YEAR);
        month = c.get(Calendar.MONTH);
        day = c.get(Calendar.DAY_OF_MONTH);
        hour = c.get(Calendar.HOUR_OF_DAY);
        minute = c.get(Calendar.MINUTE);
        updateInfo();
    }

    // 將指定的日期顯示在TextView上
    private static void updateInfo() {
        dateStr = (new StringBuilder().append(year).append("-")
                //「month + 1」是因為一月的值是0而非1
                .append(parseNum(month + 1)).append("-").append(parseNum(day)).append(" ")
                .append(hour).append(":").append(parseNum(minute)).append(":00")).toString();
    }

    // 若數字有十位數，直接顯示；若只有個位數則補0後再顯示。例如7會改成07後再顯示
    private static String parseNum(int day) {
        if (day >= 10)
            return String.valueOf(day);
        else
            return "0" + String.valueOf(day);
    }

    public static class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

        @Override
        // 改寫此方法以提供Dialog內容
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // 建立DatePickerDialog物件
            // this為OnDateSetListener物件
            // year、month、day會成為日期挑選器預選的年月日
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    getActivity(), this, year, month, day);
            return datePickerDialog;
        }

        @Override
        // 日期挑選完成會呼叫此方法，並傳入選取的年月日
        public void onDateSet(DatePicker datePicker, int y, int m, int d) {
            year = y;
            month = m;
            day = d;
        }
    }

    public static class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {

        View v;

        public TimePickerFragment() {
        }

        public TimePickerFragment(View v) {
            this.v = v;
        }

        @Override
        // 改寫此方法以提供Dialog內容
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // 建立TimePickerDialog物件
            // this為OnTimeSetListener物件
            // hour、minute會成為時間挑選器預選的時與分
            // false 設定是否為24小時制顯示
            TimePickerDialog timePickerDialog = new TimePickerDialog(
                    getActivity(), this, hour, minute, true);
            return timePickerDialog;
        }

        @Override
        // 時間挑選完成會呼叫此方法，並傳入選取的時與分
        public void onTimeSet(TimePicker timePicker, int h, int m) {
            hour = h;
            minute = m;
            updateInfo();

            Log.e("View",Integer.toString(h));
            Log.e("View",Integer.toString(m));
            Log.e("View",dateStr);

            switch(v.getId()) {
                case R.id.etStartDate:
                    etStartDate.setText(dateStr);
                    Log.e("View","etStartDate");
                    // it was the first button
                    break;
                case R.id.etEndDate:
                    // it was the second button
                    etEndDate.setText(dateStr);
                    Log.e("View","etEndDate");
                    break;
            }
        }
    }


    private void getAllSettings(){

        // Get settings
        mainIP = settingSingleton.getMainIPAddress();
        dbAccount = settingSingleton.getDbAccount();
        dbPW = settingSingleton.getDbPW();
        dbName = settingSingleton.getDbName();
        dbUserName = settingSingleton.getDbUserName();
        dbUserPw = settingSingleton.getDbUserPw();

    }

}