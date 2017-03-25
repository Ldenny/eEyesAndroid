
package com.idv.napchen.asynctest;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.Legend.LegendForm;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis.AxisDependency;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class RealtimeLineChartActivity extends AppCompatActivity implements
        OnChartValueSelectedListener {

    private LineChart mChart, mChart2;
    private Handler handler;

    private HttpGetSensorValue httpGetSensorValue;

    private TextView tvTemp, tvHumi;

    private List<DisplaySensorValues> displaySensorValuesList;

    List<Double> values1;
    List<Double> values2;

    private boolean isFirst, isSecond;

    private String url11 = "/dbSensorValueJSONGet.php?username=root&password=root&database=eEyes&table=RealID10001&field=RealValue&sensorID=1&datefield=Date&startdate=";
    private String url12 = "2017-03-20%2009:37:01";
    private String url13 = "&enddate=2017-02-28%2015:30:00&type=getNew";
    private String url1 = url11 + url12 + url13;

    private String url21 = "/dbSensorValueJSONGet.php?username=root&password=root&database=eEyes&table=RealID10002&field=RealValue&sensorID=2&datefield=Date&startdate=";
    private String url22 = "2017-03-20%2009:37:01";
    private String url23 = "&enddate=2017-02-28%2015:30:00&type=getNew";
    private String url2 = url21 + url22 + url23;

    private int onChartCount, onChartCount2, totalCount, timeoutCount;

    private SharedPreferences sharedPref;
    private static String dbIP;

    boolean isHttpResponse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_realtime_chart);

        tvTemp = (TextView) findViewById(R.id.tvTemp);
        tvHumi = (TextView) findViewById(R.id.tvHumi);

        // Setup IP from settings file
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        dbIP = sharedPref.getString("mainIPAddress", null);

        String httpHeader = getString(R.string.http_Header);
        url11 = httpHeader + dbIP + url11;
        url21 = httpHeader + dbIP + url21;

        values1 = new ArrayList<>();
        values2 = new ArrayList<>();

        onChartCount = 0;
        totalCount = 0;
        timeoutCount = 0;

        isFirst = false;
        isSecond = false;

        isHttpResponse = false;

        int year, month, day, hour, minute, second;

        Calendar c = Calendar.getInstance();
        year = c.get(Calendar.YEAR);
        month = c.get(Calendar.MONTH);
        day = c.get(Calendar.DAY_OF_MONTH);
        hour = c.get(Calendar.HOUR_OF_DAY);
        minute = c.get(Calendar.MINUTE);
        second = c.get(Calendar.SECOND);

        StringBuilder sb = new StringBuilder().append(year).append("-")
                //「month + 1」是因為一月的值是0而非1
                .append(parseNum(month + 1)).append("-").append(parseNum(day)).append("%20")
                .append(hour).append(":").append(parseNum(minute)).append(":").append(parseNum(second));

        url12 = sb.toString();

        Log.e("now time",url12);

        handler = new Handler();
        handler.postDelayed(timerRun, 1000);

        mChart = (LineChart) findViewById(R.id.chart1);
        mChart.setOnChartValueSelectedListener(this);

        // enable description text
        mChart.getDescription().setEnabled(true);

        // enable touch gestures
        mChart.setTouchEnabled(true);

        // enable scaling and dragging
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);
        mChart.setDrawGridBackground(false);

        // if disabled, scaling can be done on x- and y-axis separately
        mChart.setPinchZoom(true);

        // set an alternative background color
        mChart.setBackgroundColor(Color.WHITE);


        mChart2 = (LineChart) findViewById(R.id.chart2);
        mChart2.setOnChartValueSelectedListener(this);

        // enable description text
        mChart2.getDescription().setEnabled(true);

        // enable touch gestures
        mChart2.setTouchEnabled(true);

        // enable scaling and dragging
        mChart2.setDragEnabled(true);
        mChart2.setScaleEnabled(true);
        mChart2.setDrawGridBackground(false);

        // if disabled, scaling can be done on x- and y-axis separately
        mChart2.setPinchZoom(true);

        // set an alternative background color
        mChart2.setBackgroundColor(Color.WHITE);


        LineData data = new LineData();
        data.setValueTextColor(Color.BLACK);

        LineData data2 = new LineData();
        data2.setValueTextColor(Color.BLACK);

        // add empty data
        mChart.setData(data);
        mChart2.setData(data2);

        // get the legend (only possible after setting data)
        Legend l = mChart.getLegend();

        // modify the legend ...
        l.setForm(LegendForm.LINE);
//        l.setTypeface(mTfLight);
        l.setTextColor(Color.BLACK);

        XAxis xl = mChart.getXAxis();
//        xl.setTypeface(mTfLight);
        xl.setTextColor(Color.BLACK);
        xl.setDrawGridLines(false);
        xl.setAvoidFirstLastClipping(true);
        xl.setEnabled(true);


        Legend l2 = mChart2.getLegend();

        // modify the legend ...
        l2.setForm(LegendForm.LINE);
//        l.setTypeface(mTfLight);
        l2.setTextColor(Color.BLACK);

        XAxis xl2 = mChart2.getXAxis();
//        xl.setTypeface(mTfLight);
        xl2.setTextColor(Color.BLACK);
        xl2.setDrawGridLines(false);
        xl2.setAvoidFirstLastClipping(true);
        xl2.setEnabled(true);

//        YAxis leftAxis = mChart.getAxisLeft();
////        leftAxis.setTypeface(mTfLight);
//        leftAxis.setTextColor(Color.WHITE);
//        leftAxis.setAxisMaximum(100f);
//        leftAxis.setAxisMinimum(0f);
//        leftAxis.setDrawGridLines(true);
//
//        YAxis rightAxis = mChart.getAxisRight();
//        rightAxis.setEnabled(false);

//        feedMultiple();
    }

    // 若數字有十位數，直接顯示；若只有個位數則補0後再顯示。例如7會改成07後再顯示
    private static String parseNum(int day) {
        if (day >= 10)
            return String.valueOf(day);
        else
            return "0" + String.valueOf(day);
    }

    private final Runnable timerRun = new Runnable()
    {
        public void run()
        {
//            ++m_nTime; // 經過的秒數 + 1
            handler.postDelayed(this, 1000);

            Log.e("timer event","1 sec entry");

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
                            displayWarningMessage(sensorValuesString);
                            return;
                        }

                        if(sensorValuesString.length() == 0) {
                            displayWarningMessage("Http no response!");
                            return;
                        }

                        isHttpResponse = true;

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
                    url1 = url11 + url12 + url13;
                    Log.e("first",url1);
                    httpGetSensorValue.execute(url1);
                    isFirst = true;

                    while (isFirst == true) {
                        try {
                            //set time in mili
                            Thread.sleep(300);
                            Log.e("first", "delay...");
                            counter++;
                            if(counter >= 10) {
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
                    url2 = url21 + url12 + url23;
                    Log.e("second",url2);
                    httpGetSensorValue.execute(url2);
                    isSecond = true;

                    while (isSecond == true) {
                        try {
                            //set time in mili
                            Thread.sleep(300);
                            Log.e("second", "delay...");
                            counter++;
                            if(counter >= 10) {
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
                if (totalCount > onChartCount) {
                    addEntry();
                    addEntry2();
                    Log.e("realchart", "added...");
                }
                timeoutCount = 0;
            } else {
                Log.e("realchart", "timeout...");
                timeoutCount++;
                if(timeoutCount >= 5) {
                    displayWarningMessage("No sensor data received!");
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
                values1.add(value);
            } else {
                if(values1.size() > values2.size()) {
                    values2.add(value);

                    String date = jSensor.getString("date");

                    url12 = date.replaceAll(" ", "%20");
                    totalCount++;
                }
            }
        }

        if(isFirst) {
            Log.e("First value count", Integer.toString(values1.size()));
        } else {
            Log.e("Second value count", Integer.toString(values2.size()));
        }
    }

    private void displayWarningMessage(String msg) {

        Toast.makeText(RealtimeLineChartActivity.this, msg, Toast.LENGTH_SHORT).show();

        AlertDialog alertDialog = new AlertDialog.Builder(RealtimeLineChartActivity.this).create();
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
    protected void onDestroy() {

        super.onDestroy();
        handler.removeCallbacks(timerRun);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.realtime, menu);
        return true;
    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//
//        switch (item.getItemId()) {
//            case R.id.actionAdd: {
//                addEntry();
//                break;
//            }
//            case R.id.actionClear: {
//                mChart.clearValues();
//                Toast.makeText(this, "Chart cleared!", Toast.LENGTH_SHORT).show();
//                break;
//            }
//            case R.id.actionFeedMultiple: {
////                feedMultiple();
//                break;
//            }
//        }
//        return true;
//    }

    private void addEntry() {

        LineData data = mChart.getData();

        if(data != null) {

            ILineDataSet set = data.getDataSetByIndex(0);
            if (set == null) {
                set = createSet();
                data.addDataSet(set);
            }

            onChartCount2 = onChartCount;
            if(totalCount > onChartCount) {
                int count = totalCount - onChartCount;
                for(int i = 0; i < count; i++) {
                    double value = values1.get(onChartCount);
                    data.addEntry(new Entry(set.getEntryCount(), (float) value), 0);
                    onChartCount++;

                    tvTemp.setText(Double.toString(value));
                }
            }
//            data.addEntry(new Entry(set.getEntryCount(), (float) (Math.random() * 40) + 30f), 0);
//            data.addEntry(new Entry(set2.getEntryCount(), (float) (Math.random() * 20) + 20f), 0);
            data.notifyDataChanged();
            // let the chart know it's data has changed
            mChart.notifyDataSetChanged();
            // limit the number of visible entries
            mChart.setVisibleXRangeMaximum(120);
            // mChart.setVisibleYRange(30, AxisDependency.LEFT);

            // move to the latest entry
            mChart.moveViewToX(data.getEntryCount());

            Log.e("EntryCount",Integer.toString(data.getEntryCount()));
            Log.e("DataSetCount",Integer.toString(mChart.getData().getDataSetCount()));
        }

    }

    private void addEntry2() {

        LineData data = mChart2.getData();

        if(data != null) {

            ILineDataSet set = data.getDataSetByIndex(0);
            if (set == null) {
                set = createSet2();
                data.addDataSet(set);
            }

            if(totalCount > onChartCount2) {
                int count = totalCount - onChartCount2;
                for(int i = 0; i < count; i++) {
                    double value = values2.get(onChartCount2);
                    data.addEntry(new Entry(set.getEntryCount(), (float) value), 0);
                    onChartCount2++;

                    tvHumi.setText(Double.toString(value));
                }
            }
//            data.addEntry(new Entry(set.getEntryCount(), (float) (Math.random() * 40) + 30f), 0);
//            data.addEntry(new Entry(set2.getEntryCount(), (float) (Math.random() * 20) + 20f), 0);
            data.notifyDataChanged();
            // let the chart know it's data has changed
            mChart2.notifyDataSetChanged();
            // limit the number of visible entries
            mChart2.setVisibleXRangeMaximum(120);
            // mChart.setVisibleYRange(30, AxisDependency.LEFT);

            // move to the latest entry
            mChart2.moveViewToX(data.getEntryCount());

            Log.e("EntryCount",Integer.toString(data.getEntryCount()));
            Log.e("DataSetCount",Integer.toString(mChart2.getData().getDataSetCount()));
        }

    }

    private LineDataSet createSet() {

        LineDataSet set1 = new LineDataSet(null, "房間溫度");
        set1.setAxisDependency(AxisDependency.LEFT);
        set1.setColor(ColorTemplate.getHoloBlue());
        set1.setCircleColor(Color.RED);
        set1.setLineWidth(2f);
        set1.setCircleRadius(4f);
        set1.setFillAlpha(65);
        set1.setFillColor(ColorTemplate.getHoloBlue());
        set1.setHighLightColor(Color.rgb(244, 117, 117));
        set1.setValueTextColor(Color.GREEN);
        set1.setValueTextSize(9f);
        set1.setDrawValues(false);
        return set1;
    }

    private LineDataSet createSet2() {

        LineDataSet set1 = new LineDataSet(null, "房間濕度");
        set1.setAxisDependency(AxisDependency.LEFT);
        set1.setColor(ColorTemplate.getHoloBlue());
        set1.setCircleColor(Color.BLUE);
        set1.setLineWidth(2f);
        set1.setCircleRadius(4f);
        set1.setFillAlpha(65);
        set1.setFillColor(ColorTemplate.getHoloBlue());
        set1.setHighLightColor(Color.rgb(244, 117, 117));
        set1.setValueTextColor(Color.RED);
        set1.setValueTextSize(9f);
        set1.setDrawValues(false);
        return set1;
    }

//    private Thread thread;
//
//    private void feedMultiple() {
//
//        if (thread != null)
//            thread.interrupt();
//
//        final Runnable runnable = new Runnable() {
//
//            @Override
//            public void run() {
//                addEntry();
//            }
//        };
//
//        thread = new Thread(new Runnable() {
//
//            @Override
//            public void run() {
//                for (int i = 0; i < 1000; i++) {
//
//                    // Don't generate garbage runnables inside the loop.
//                    runOnUiThread(runnable);
//
//                    try {
//                        Thread.sleep(1000);
//                    } catch (InterruptedException e) {
//                        // TODO Auto-generated catch block
//                        e.printStackTrace();
//                    }
//                }
//            }
//        });
//
//        thread.start();
//    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {
        Log.i("Entry selected", e.toString());
    }

    @Override
    public void onNothingSelected() {
        Log.i("Nothing selected", "Nothing selected.");
    }

//    @Override
//    protected void onPause() {
//        super.onPause();
//
//        if (thread != null) {
//            thread.interrupt();
//        }
//    }
}
