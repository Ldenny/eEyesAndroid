package com.idv.napchen.asynctest;

import java.util.List;

/**
 * Created by napchen on 2017/3/18.
 */

public class DisplaySensorValues {

    List<Double> value;
    List<String> date;

    public DisplaySensorValues() {
    }

    public DisplaySensorValues(List<Double> value, List<String> date) {
        this.value = value;
        this.date = date;
    }

    public List<Double> getValue() {
        return value;
    }

    public void setValue(List<Double> value) {
        this.value = value;
    }

    public List<String> getDate() {
        return date;
    }

    public void setDate(List<String> date) {
        this.date = date;
    }
}
