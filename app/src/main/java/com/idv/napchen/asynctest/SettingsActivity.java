package com.idv.napchen.asynctest;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by denny on 2017/3/14.
 */

public class SettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle saveInstanceState){
       super.onCreate(saveInstanceState);
        setContentView(R.layout.activity_settings);
        getFragmentManager().beginTransaction()
                .replace(R.id.frameLayout,new InfoFragment(), "InfoFragment")
                .commit();
    }
}
