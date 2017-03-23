package com.idv.napchen.asynctest;

/**
 * Created by napchen on 2017/3/15.
 */

public class AlarmItem {

    Integer id;
    Double value;
    String type;
    String date;

    public AlarmItem() {
    }

    public AlarmItem(Integer id, Double value, String type, String date) {
        this.id = id;
        this.value = value;
        this.type = type;
        this.date = date;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
