package com.idv.napchen.asynctest;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

/**
 * Created by napchen on 2017/3/25.
 */

public class ActivityAlarmSetting extends AppCompatActivity {

    private ListView listView;

    private Dialog myDialog;

    private Button btnOK, btnCancel;

    private EditText etHiAlarm, etLoAlarm;

    private AllSensorsInfo allSensorsInfo;

    private HttpGetSensorValue httpGetSensorValue;

    private MyAdapter adapter;

    private List<Sensor> sensorList;

    private SharedPreferences sharedPref;

    private static String dbIP;

    private final String url0 = "http://";
    private final String url11 = "/dbInfoGet.php?username=root&password=root&database=eEyes&type=setHiLoAlarm&data={%22sensorID%22:1,%22hiAlarm%22:";
    private final String url12 = "/dbInfoGet.php?username=root&password=root&database=eEyes&type=setHiLoAlarm&data={%22sensorID%22:2,%22hiAlarm%22:";
    private String url2 = "78.9";
    private final String url3 = ",%22loAlarm%22:";
    private String url4 = "23.4";
    private final String url5 = "}";

    boolean isUpdated, isFirst;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_alarm_setting);

        allSensorsInfo = AllSensorsInfo.getInstance();
        sensorList = allSensorsInfo.getAllSensorsInfo();

        // Setup IP from settings file
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        dbIP = sharedPref.getString("mainIPAddress", null);

        Log.e("IP", dbIP);

        findViews();
    }

    private void findViews() {

        listView = (ListView) findViewById(R.id.lvAlarmSetting);

        Log.e("Sensor Count",Integer.toString(sensorList.size()));

        adapter = new MyAdapter(sensorList);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if(position == 0) {
                    isFirst = true;
                } else {
                    isFirst = false;
                }

                final Sensor sensor = sensorList.get(position);
                Toast.makeText(ActivityAlarmSetting.this, sensor.getName(), Toast.LENGTH_SHORT).show();

                myDialog = new Dialog(ActivityAlarmSetting.this);
                myDialog.setTitle("更改上下限警報值");
                // 使用者無法自行取消對話視窗，需要進行操作才行
                myDialog.setCancelable(true);
                myDialog.setContentView(R.layout.dialog_alarm_setting);

                Window dialogWindow = myDialog.getWindow();

                dialogWindow.setGravity(Gravity.CENTER);

                WindowManager.LayoutParams lp = dialogWindow.getAttributes();
                lp.width = 1000;
                lp.alpha = 0.8f;
                dialogWindow.setAttributes(lp);

                btnOK = (Button) myDialog.findViewById(R.id.btnOK);
                btnOK.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        etHiAlarm = (EditText) myDialog.findViewById(R.id.etHiAlarm);
                        etLoAlarm = (EditText) myDialog.findViewById(R.id.etLoAlarm);
                        String hiAlarmStr = etHiAlarm.getText().toString().trim();
                        String loAlarmStr = etLoAlarm.getText().toString().trim();

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
                                isUpdated = true;

//                                adapter.notifyDataSetChanged();

                            }
                        });

                        url2 = hiAlarmStr;
                        url4 = loAlarmStr;
                        String url;
                        int sensorIndex;

                        double hiAlarm = Double.parseDouble(hiAlarmStr);
                        double loAlarm = Double.parseDouble(loAlarmStr);

                        if(isFirst) {
                            url = url0 + dbIP + url11 + url2 + url3 + url4 + url5;
                            sensorIndex = 0;
                        } else {
                            url = url0 + dbIP + url12 + url2 + url3 + url4 + url5;
                            sensorIndex = 1;
                        }

                        allSensorsInfo.setHiLoAlarm(sensorIndex, hiAlarm, loAlarm);
                        sensorList = allSensorsInfo.getAllSensorsInfo();

                        Log.e("update url",url);
                        httpGetSensorValue.execute(url);

                        int counter = 0;
                        isUpdated = false;

                        while (isUpdated == false) {
                            try {
                                //set time in mili
                                Thread.sleep(300);
                                Log.e("http", "delay...");
                                counter++;
                                if(counter >= 10) {
                                    isUpdated = true;
                                    Log.e("http", "timeout...");
                                    break;
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        adapter.notifyDataSetChanged();

                        myDialog.cancel();
                    }
                });

                btnCancel = (Button) myDialog.findViewById(R.id.btnCancel);
                btnCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        myDialog.cancel();
                        Toast.makeText(ActivityAlarmSetting.this, "cancel", Toast.LENGTH_SHORT).show();
                    }
                });

                myDialog.show();
            }
        });
    }

    private void displayWarningMessage(String msg) {

        Toast.makeText(ActivityAlarmSetting.this, msg, Toast.LENGTH_SHORT).show();

        AlertDialog alertDialog = new AlertDialog.Builder(ActivityAlarmSetting.this).create();
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
                convertView = inflater.inflate(R.layout.listview_alarm_setting, parent, false);

                holder.tvName = (TextView) convertView.findViewById(R.id.tvSensorName);
                holder.tvHiAlarm = (TextView) convertView.findViewById(R.id.tvHiAlarm);
                holder.tvLoAlarm = (TextView) convertView.findViewById(R.id.tvLoAlarm);
/*
                // Textview click
                final TextView tvName = (TextView) convertView.findViewById(R.id.tvSensorName);
                tvName.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        switch (v.getId()) {
                            case R.id.tvSensorName:
                                Toast.makeText(ActivityAlarmSetting.this, tvName.getText(), Toast.LENGTH_SHORT).show();
                                break;
                        }
                    }
                });

                // Textview click
                final TextView tvHiAlarm = (TextView) convertView.findViewById(R.id.tvHiAlarm);
                tvHiAlarm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        switch (v.getId()) {
                            case R.id.tvHiAlarm:
                                Toast.makeText(ActivityAlarmSetting.this, tvHiAlarm.getText(), Toast.LENGTH_SHORT).show();
                                break;
                        }
                    }
                });

                // Textview click
                final TextView tvLoAlarm = (TextView) convertView.findViewById(R.id.tvLoAlarm);
                tvLoAlarm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        switch (v.getId()) {
                            case R.id.tvLoAlarm:
                                Toast.makeText(ActivityAlarmSetting.this, tvLoAlarm.getText(), Toast.LENGTH_SHORT).show();
                                break;
                        }
                    }
                });
*/

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            Sensor sensor = sensorList.get(position);
            holder.tvName.setText(sensor.getName());
            holder.tvHiAlarm.setText(sensor.getHiAlarm().toString());
            holder.tvLoAlarm.setText(sensor.getLoAlarm().toString());

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
            TextView tvName, tvHiAlarm, tvLoAlarm;
        }
    }
}
