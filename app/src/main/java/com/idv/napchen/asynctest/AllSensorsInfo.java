package com.idv.napchen.asynctest;

import java.util.List;

/**
 * Created by napchen on 2017/3/13.
 */

public class AllSensorsInfo {

    private List<Sensor> sensorList;

    private static AllSensorsInfo singleton = new AllSensorsInfo();

    private  AllSensorsInfo() {

    }

    public static AllSensorsInfo getInstance() {

        return singleton;
    }

//    private volatile static AllSensorsInfo instance;
//
//    private AllSensorsInfo() {
//
//    }
//
//    public static AllSensorsInfo Instance() {
//
//        if(instance == null) {
//            synchronized (AllSensorsInfo.class) {
//                if(instance == null) {
//                    instance = new AllSensorsInfo();
//                }
//            }
//        }
//        return instance;
//    }

    public void setAllSensorsInfo(List<Sensor> list) {

        this.sensorList = list;
    }

    public List<Sensor> getAllSensorsInfo() {

        return this.sensorList;
    }

    public String getSensorName(int SensorId) {

        Sensor sensor = this.sensorList.get(SensorId-1);
        return sensor.getName();
    }

    public String getRealtimeChartTable(int SensorId) {

        Sensor sensor = this.sensorList.get(SensorId-1);
        return sensor.getDbRealValueTable();
    }

    public String getHistoryChartTable(int SensorId) {

        Sensor sensor = this.sensorList.get(SensorId-1);
        return sensor.getDbAverageValueTable();
    }

    public void setHiLoAlarm(int sensorIndex, Double hiAlarm, Double loAlarm) {

        Sensor sensor = this.sensorList.get(sensorIndex);
        sensor.hiAlarm = hiAlarm;
        sensor.loAlarm = loAlarm;
    }

}
