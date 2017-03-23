
package com.idv.napchen.asynctest;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.SeekBar;
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
import java.util.List;

public class HistoryLineChartActivity extends AppCompatActivity implements
        OnChartValueSelectedListener {

    private LineChart mChart;
    private SeekBar mSeekBarX, mSeekBarY;
    private TextView tvX, tvY;

    private HttpGetSensorValue httpGetSensorValue;

    private List<DisplaySensorValues> displaySensorValuesList;

    private boolean isFirstGot, isSecondGot, isHttpResponse;

    private String url10 = "http://";
    private String url11 = "/dbSensorValueJSONGet.php?username=root&password=root&database=eEyes&table=SensorRawData&field=RawValue&sensorID=1&datefield=StartDate&startdate=";
    private String url12 = "2017-03-16%2013:00:00";
    private String url13 = "&enddate=";
    private String url14 = "2017-03-16%2013:03:00";
    private String url15 = "&type=getRange";
    private String url1;

    private String url21 = "/dbSensorValueJSONGet.php?username=root&password=root&database=eEyes&table=SensorRawData&field=RawValue&sensorID=2&datefield=StartDate&startdate=";
    private String url2;

    int totalCount;

    private SharedPreferences sharedPref;
    private static String dbIP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setup IP from settings file
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        dbIP = sharedPref.getString("mainIPAddress", null);

        displaySensorValuesList = new ArrayList<>();

        isFirstGot = false;
        isSecondGot = false;
        isHttpResponse = false;

        url1 = url10 + dbIP + url11 + url12 + url13 + url14 + url15;
        url2 = url10 + dbIP + url21 + url12 + url13 + url14 + url15;

        getInputDateRange();

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
                        Log.e("after got HTTP","start JSON parsing...");

                        DisplaySensorValues displaySensorValues = getJSONData(sensorValuesString);
                        displaySensorValuesList.add(displaySensorValues);

                    } catch (JSONException je) {
                        je.printStackTrace();
                    }
                }
            });

            if(j == 0) {
                httpGetSensorValue.execute(url1);
                Log.e("first","send...");
                try {
                    //set time in mili
                    Thread.sleep(300);
                    Log.e("first","delay...");
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            else {
                httpGetSensorValue.execute(url2);

                Log.e("second","send...");
                try {
                    //set time in mili
                    Thread.sleep(300);
                    Log.e("second","delay...");
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
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

            displayWarningMessage("no data");
            return;
        }

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_history_chart);

        mChart = (LineChart) findViewById(R.id.chart1);
        mChart.setOnChartValueSelectedListener(this);

        // no description text
        mChart.getDescription().setEnabled(false);

        // enable touch gestures
        mChart.setTouchEnabled(true);

        mChart.setDragDecelerationFrictionCoef(0.9f);

        // enable scaling and dragging
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);
        mChart.setDrawGridBackground(false);
        mChart.setHighlightPerDragEnabled(true);

        // if disabled, scaling can be done on x- and y-axis separately
        mChart.setPinchZoom(true);

        // set an alternative background color
        mChart.setBackgroundColor(Color.WHITE);

        // add data
        setData(totalCount, 30);

        mChart.animateX(2500);

        // get the legend (only possible after setting data)
        Legend l = mChart.getLegend();

        // modify the legend ...
        l.setForm(LegendForm.LINE);
//        l.setTypeface(mTfLight);
        l.setTextSize(11f);
        l.setTextColor(Color.BLACK);
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);
//        l.setYOffset(11f);

        XAxis xAxis = mChart.getXAxis();
//        xAxis.setTypeface(mTfLight);
        xAxis.setTextSize(11f);
        xAxis.setTextColor(Color.BLACK);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(false);

//        YAxis leftAxis = mChart.getAxisLeft();
//        leftAxis.setTypeface(mTfLight);
//        leftAxis.setTextColor(ColorTemplate.getHoloBlue());
//        leftAxis.setAxisMaximum(100f);
//        leftAxis.setAxisMinimum(0f);
//        leftAxis.setDrawGridLines(true);
//        leftAxis.setGranularityEnabled(true);

//        YAxis rightAxis = mChart.getAxisRight();
//        rightAxis.setTypeface(mTfLight);
//        rightAxis.setTextColor(Color.RED);
//        rightAxis.setAxisMaximum(100f);
//        rightAxis.setAxisMinimum(0f);
//        rightAxis.setDrawGridLines(false);
//        rightAxis.setDrawZeroLine(false);
//        rightAxis.setGranularityEnabled(false);
    }

    private void displayWarningMessage(String msg) {

        Toast.makeText(HistoryLineChartActivity.this, msg, Toast.LENGTH_SHORT).show();

        AlertDialog alertDialog = new AlertDialog.Builder(HistoryLineChartActivity.this).create();
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

    private void getInputDateRange() {

        Bundle bundle = this.getIntent().getExtras();
        String url = bundle.getString("startDate");
        url12 = url.replaceAll(" ", "%20");

        Log.e("url Start",url12);

        url = bundle.getString("endDate");
        url14 = url.replaceAll(" ", "%20");

        Log.e("url end",url14);

        url1 = url10 + dbIP + url11 + url12 + url13 + url14 + url15;
        url2 = url10 + dbIP + url21 + url12 + url13 + url14 + url15;

        Log.e("url Start",url1);
        Log.e("url end",url2);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.line, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.actionToggleValues: {
                List<ILineDataSet> sets = mChart.getData()
                        .getDataSets();

                for (ILineDataSet iSet : sets) {

                    LineDataSet set = (LineDataSet) iSet;
                    set.setDrawValues(!set.isDrawValuesEnabled());
                }

                mChart.invalidate();
                break;
            }
            case R.id.actionToggleHighlight: {
                if (mChart.getData() != null) {
                    mChart.getData().setHighlightEnabled(!mChart.getData().isHighlightEnabled());
                    mChart.invalidate();
                }
                break;
            }
            case R.id.actionToggleFilled: {

                List<ILineDataSet> sets = mChart.getData()
                        .getDataSets();

                for (ILineDataSet iSet : sets) {

                    LineDataSet set = (LineDataSet) iSet;
                    if (set.isDrawFilledEnabled())
                        set.setDrawFilled(false);
                    else
                        set.setDrawFilled(true);
                }
                mChart.invalidate();
                break;
            }
            case R.id.actionToggleCircles: {
                List<ILineDataSet> sets = mChart.getData()
                        .getDataSets();

                for (ILineDataSet iSet : sets) {

                    LineDataSet set = (LineDataSet) iSet;
                    if (set.isDrawCirclesEnabled())
                        set.setDrawCircles(false);
                    else
                        set.setDrawCircles(true);
                }
                mChart.invalidate();
                break;
            }
            case R.id.actionToggleCubic: {
                List<ILineDataSet> sets = mChart.getData()
                        .getDataSets();

                for (ILineDataSet iSet : sets) {

                    LineDataSet set = (LineDataSet) iSet;
                    set.setMode(set.getMode() == LineDataSet.Mode.CUBIC_BEZIER
                            ? LineDataSet.Mode.LINEAR
                            : LineDataSet.Mode.CUBIC_BEZIER);
                }
                mChart.invalidate();
                break;
            }
            case R.id.actionToggleStepped: {
                List<ILineDataSet> sets = mChart.getData()
                        .getDataSets();

                for (ILineDataSet iSet : sets) {

                    LineDataSet set = (LineDataSet) iSet;
                    set.setMode(set.getMode() == LineDataSet.Mode.STEPPED
                            ? LineDataSet.Mode.LINEAR
                            : LineDataSet.Mode.STEPPED);
                }
                mChart.invalidate();
                break;
            }
            case R.id.actionToggleHorizontalCubic: {
                List<ILineDataSet> sets = mChart.getData()
                        .getDataSets();

                for (ILineDataSet iSet : sets) {

                    LineDataSet set = (LineDataSet) iSet;
                    set.setMode(set.getMode() == LineDataSet.Mode.HORIZONTAL_BEZIER
                            ? LineDataSet.Mode.LINEAR
                            : LineDataSet.Mode.HORIZONTAL_BEZIER);
                }
                mChart.invalidate();
                break;
            }
            case R.id.actionTogglePinch: {
                if (mChart.isPinchZoomEnabled())
                    mChart.setPinchZoom(false);
                else
                    mChart.setPinchZoom(true);

                mChart.invalidate();
                break;
            }
            case R.id.actionToggleAutoScaleMinMax: {
                mChart.setAutoScaleMinMaxEnabled(!mChart.isAutoScaleMinMaxEnabled());
                mChart.notifyDataSetChanged();
                break;
            }
            case R.id.animateX: {
                mChart.animateX(3000);
                //mChart.highlightValue(9.7f, 1, false);
                break;
            }
            case R.id.animateY: {
                mChart.animateY(3000);
                break;
            }
            case R.id.animateXY: {
                mChart.animateXY(3000, 3000);
                break;
            }

            case R.id.actionSave: {
                if (mChart.saveToPath("title" + System.currentTimeMillis(), "")) {
                    Toast.makeText(getApplicationContext(), "Saving SUCCESSFUL!",
                            Toast.LENGTH_SHORT).show();
                } else
                    Toast.makeText(getApplicationContext(), "Saving FAILED!", Toast.LENGTH_SHORT)
                            .show();

                // mChart.saveToGallery("title"+System.currentTimeMillis())
                break;
            }
        }
        return true;
    }

    private void setData(int count, float range) {

        List<Double> values = new ArrayList<>();    // to store chart display values

        // setup first history chart data
        ArrayList<Entry> yVals1 = new ArrayList<Entry>();
        // get first history values object
        DisplaySensorValues dsv = displaySensorValuesList.get(0);

        values = dsv.getValue();
        // save values for LineDataSet
        for(int i = 0; i < totalCount; i++) {
            double value = values.get(i);
            yVals1.add(new Entry((float)i, (float)value));
        }

        // setup second history chart data
        ArrayList<Entry> yVals2 = new ArrayList<Entry>();
        // get second history values object
        dsv = displaySensorValuesList.get(1);
        values = dsv.getValue();
        // save values for LineDataSet
        for(int i = 0; i < totalCount; i++) {
            double value = values.get(i);
            yVals2.add(new Entry((float)i, (float)value));
        }

        LineDataSet set1, set2;

        if (mChart.getData() != null &&
                mChart.getData().getDataSetCount() > 0) {

            Log.e("HisChart","getData");

            set1 = (LineDataSet) mChart.getData().getDataSetByIndex(0);
            set2 = (LineDataSet) mChart.getData().getDataSetByIndex(1);
            set1.setValues(yVals1);
            set2.setValues(yVals2);
            mChart.getData().notifyDataChanged();
            mChart.notifyDataSetChanged();
        } else {

            Log.e("HisChart","setData");

            // create a dataset and give it a type
            set1 = new LineDataSet(yVals1, "DataSet 1");

            set1.setAxisDependency(AxisDependency.LEFT);
            set1.setColor(Color.RED);
            set1.setCircleColor(Color.RED);
            set1.setLineWidth(2f);
            set1.setCircleRadius(3f);
            set1.setFillAlpha(65);
            set1.setFillColor(ColorTemplate.getHoloBlue());
            set1.setHighLightColor(Color.rgb(244, 117, 117));
            set1.setDrawCircleHole(false);
            //set1.setFillFormatter(new MyFillFormatter(0f));
            //set1.setDrawHorizontalHighlightIndicator(false);
            //set1.setVisible(false);
            //set1.setCircleHoleColor(Color.WHITE);

            // create a dataset and give it a type
            set2 = new LineDataSet(yVals2, "DataSet 2");
            set2.setAxisDependency(AxisDependency.RIGHT);
            set2.setColor(Color.BLUE);
            set2.setCircleColor(Color.BLUE);
            set2.setLineWidth(2f);
            set2.setCircleRadius(3f);
            set2.setFillAlpha(65);
            set2.setFillColor(Color.RED);
            set2.setDrawCircleHole(false);
            set2.setHighLightColor(Color.rgb(244, 117, 117));
            //set2.setFillFormatter(new MyFillFormatter(900f));

            // create a data object with the datasets
            LineData data = new LineData(set1, set2);
            data.setValueTextColor(Color.BLACK);
            data.setValueTextSize(9f);

            // set data
            mChart.setData(data);
        }
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {
        Log.i("Entry selected", e.toString());

        mChart.centerViewToAnimated(e.getX(), e.getY(), mChart.getData().getDataSetByIndex(h.getDataSetIndex())
                .getAxisDependency(), 500);
    }

    @Override
    public void onNothingSelected() {
        Log.i("Nothing selected", "Nothing selected.");
    }

}
