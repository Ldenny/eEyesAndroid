package com.idv.napchen.asynctest;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;
import java.util.List;

import static android.R.attr.end;
import static android.R.attr.y;

/**
 * Created by napchen on 2017/3/15.
 */

public class ChartSettingActivity extends AppCompatActivity {

    // 請求代碼，自行定義

    private Button btnRealtimeChart, btnHistoryChart;

    private static EditText etStartDate, etEndDate;

    private List<Sensor> sensorList;

    private static int year, month, day, hour, minute;

    private static String dateStr;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart_setting);

        findViews();
    }

    private void findViews() {

        btnRealtimeChart = (Button) findViewById(R.id.btnRealtimeChart);
        btnHistoryChart = (Button) findViewById(R.id.btnHistoryChart);
        etStartDate = (EditText) findViewById(R.id.etStartDate);
        etEndDate = (EditText) findViewById(R.id.etEndDate);

        showRightNow();

        etStartDate.setText("2017-03-16 13:00:00");
        etEndDate.setText("2017-03-16 13:05:00");

        btnHistoryChart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.e("button","pressed...");
                if(etStartDate.getText().toString().trim().length() == 0 || etEndDate.getText().toString().trim().length() == 0) {
                    Log.e("input date","null");

                    displayWarningMessage();
                    return;
                }

                Log.e("button","1st check...");
                String startDate = etStartDate.getText().toString();
                Log.e("button",startDate);
                String endDate = etEndDate.getText().toString();
                Log.e("button",endDate);

                if(checkDateFormat(startDate) == false || checkDateFormat(endDate) == false) {

                    Log.e("check date","error");

                    displayWarningMessage();
                    return;
                }

                Intent intent = new Intent(ChartSettingActivity.this, HistoryLineChartActivity.class);
                Bundle bundle = new Bundle();

                bundle.putString("startDate", startDate);
                bundle.putString("endDate", endDate);
                intent.putExtras(bundle);

                startActivity(intent);
            }
        });

        btnRealtimeChart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(ChartSettingActivity.this, RealtimeLineChartActivity.class);
                startActivity(intent);
            }
        });

        etStartDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
//                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

                TimePickerFragment timePickerFragment = new TimePickerFragment(v);
                FragmentManager fm = getSupportFragmentManager();
                timePickerFragment.show(fm, "timePicker");

                DatePickerFragment datePickerFragment = new DatePickerFragment();
                fm = getSupportFragmentManager();
                datePickerFragment.show(fm, "datePicker");

//                updateInfo();
//                etStartDate.setText(dateStr);
            }
        });

        etEndDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
//                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

                TimePickerFragment timePickerFragment = new TimePickerFragment(v);
                FragmentManager fm = getSupportFragmentManager();
                timePickerFragment.show(fm, "timePicker");

                DatePickerFragment datePickerFragment = new DatePickerFragment();
                fm = getSupportFragmentManager();
                datePickerFragment.show(fm, "datePicker");

//                updateInfo();
//                etEndDate.setText(dateStr);
            }
        });

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

    private void displayWarningMessage() {

        Toast.makeText(ChartSettingActivity.this, "Date/Time setting error", Toast.LENGTH_SHORT).show();

        AlertDialog alertDialog = new AlertDialog.Builder(ChartSettingActivity.this).create();
        alertDialog.setTitle("Alert");
        alertDialog.setMessage("Date/Time setting error");
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
                    getActivity(), this, hour, minute, false);
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
}
