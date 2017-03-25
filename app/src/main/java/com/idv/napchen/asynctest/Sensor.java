package com.idv.napchen.asynctest;

/**
 * Created by napchen on 2017/3/13.
 */

public class Sensor {

    Integer sensorID;
    String name;
    Double value;
    String date;

    Double hiAlarm;
    Double loAlarm;

    Double latitude;
    Double longitude;

    String type;

    Double rangeHi;
    Double rangeLo;

    String unit;
    String desc;;

    String dbRealValueTable;
    String dbAverageValueTable;

    Double alarmValue;
    String alarmType;

    boolean isSelected;

    public Sensor() {
    }

    public Sensor(Integer sensorID, String name, Double hiAlarm, Double loAlarm, Double latitude, Double longitude, String type, Double rangeHi, Double rangeLo, String unit, String desc, String dbRealValueTable, String dbAverageValueTable, boolean isSelected) {
        this.sensorID = sensorID;
        this.name = name;
        this.hiAlarm = hiAlarm;
        this.loAlarm = loAlarm;
        this.latitude = latitude;
        this.longitude = longitude;
        this.type = type;
        this.rangeHi = rangeHi;
        this.rangeLo = rangeLo;
        this.unit = unit;
        this.desc = desc;
        this.dbRealValueTable = dbRealValueTable;
        this.dbAverageValueTable = dbAverageValueTable;
        this.isSelected = isSelected;
    }

    public Integer getSensorID() {
        return sensorID;
    }

    public void setSensorID(Integer sensorID) {
        this.sensorID = sensorID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Double getHiAlarm() {
        return hiAlarm;
    }

    public void setHiAlarm(Double hiAlarm) {
        this.hiAlarm = hiAlarm;
    }

    public Double getLoAlarm() {
        return loAlarm;
    }

    public void setLoAlarm(Double loAlarm) {
        this.loAlarm = loAlarm;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Double getRangeHi() {
        return rangeHi;
    }

    public void setRangeHi(Double rangeHi) {
        this.rangeHi = rangeHi;
    }

    public Double getRangeLo() {
        return rangeLo;
    }

    public void setRangeLo(Double rangeLo) {
        this.rangeLo = rangeLo;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getDbRealValueTable() {
        return dbRealValueTable;
    }

    public void setDbRealValueTable(String dbRealValueTable) {
        this.dbRealValueTable = dbRealValueTable;
    }

    public String getDbAverageValueTable() {
        return dbAverageValueTable;
    }

    public void setDbAverageValueTable(String dbAverageValueTable) {
        this.dbAverageValueTable = dbAverageValueTable;
    }

    public Double getAlarmValue() {
        return alarmValue;
    }

    public void setAlarmValue(Double alarmValue) {
        this.alarmValue = alarmValue;
    }

    public String getAlarmType() {
        return alarmType;
    }

    public void setAlarmType(String alarmType) {
        this.alarmType = alarmType;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
