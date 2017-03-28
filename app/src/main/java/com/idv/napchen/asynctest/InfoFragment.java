package com.idv.napchen.asynctest;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

/**
 * Created by denny on 2017/3/14.
 */
public class InfoFragment extends PreferenceFragment implements
        SharedPreferences.OnSharedPreferenceChangeListener {
    private SharedPreferences sharedPref;
    private SettingSingleton settingSingleton;




    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        settingSingleton = settingSingleton.getInstance();
        addPreferencesFromResource(R.xml.personal_info);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
    }
    @Override
    public void onResume(){
        super.onResume();
        sharedPref.registerOnSharedPreferenceChangeListener(this);
        findPreference("mainIPAddress").setSummary(sharedPref.getString("mainIPAddress", getString(R.string.db_main_placeholder)));
        findPreference("dbAccount").setSummary(sharedPref.getString("dbAccount",getString(R.string.db_userName_placeholder)));
        findPreference("dbPW").setSummary(sharedPref.getString("dbPW",getString(R.string.db_password_placeholder)));
        findPreference("dbName").setSummary(sharedPref.getString("dbName",getString(R.string.db_serverName_placeholder)));
        findPreference("dbUserName").setSummary(sharedPref.getString("dbUserName",getString(R.string.db_userName_placeholder)));
        findPreference("dbUserPw").setSummary(sharedPref.getString("dbUserPw",getString(R.string.db_user_password_placeholder)));
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        // Handle with IO Time
        if (!this.isAdded()) {
            return;
        }

        Preference pref = findPreference(key);

        if(pref instanceof EditTextPreference) {
            EditTextPreference etPref = (EditTextPreference) pref;
            if (pref.getKey().equals("mainIPAddress")) {

                etPref.setSummary(sharedPref.getString("mainIPAddress", getString(R.string.db_main_placeholder)));
                settingSingleton.setMainIPAddress(sharedPref.getString("mainIPAddress",""));

            } else if (pref.getKey().equals("dbAccount")) {

                etPref.setSummary(sharedPref.getString("dbAccount", getString(R.string.db_user_account_placeholder)));
                settingSingleton.setDbAccount(sharedPref.getString("dbAccount",""));

            } else if (pref.getKey().equals("dbPW")) {

                etPref.setSummary(sharedPref.getString("dbPW", getString(R.string.db_password_placeholder)));
                settingSingleton.setDbPW(sharedPref.getString("dbPW",""));

            } else if (pref.getKey().equals("dbName")) {

                etPref.setSummary(sharedPref.getString("dbName", getString(R.string.db_serverName_placeholder)));
                settingSingleton.setDbName(sharedPref.getString("dbName",""));

            } else if (pref.getKey().equals("dbUserName")) {

                etPref.setSummary(sharedPref.getString("dbUserName", getString(R.string.db_user_account_placeholder)));
                settingSingleton.setDbUserName(sharedPref.getString("dbUserName",""));

            } else if (pref.getKey().equals("dbUserPw")) {

                etPref.setSummary(sharedPref.getString("dbUserPw", getString(R.string.db_user_password_placeholder)));
                settingSingleton.setDbUserPw(sharedPref.getString("dbUserPw",""));
            }
        }
    }
}