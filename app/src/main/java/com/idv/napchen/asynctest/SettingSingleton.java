package com.idv.napchen.asynctest;

/**
 * Created by denny on 2017/3/28.
 */

public class SettingSingleton {


    private String mainIPAddress;
    private String dbAccount;
    private String dbPW;
    private String dbName;
    private String dbUserName;
    private String dbUserPw;

    private  static SettingSingleton settingSingleton = new SettingSingleton();

    private SettingSingleton(){

    }

    public static SettingSingleton getInstance(){
        return  settingSingleton;
    }

    public String getMainIPAddress() {
        return mainIPAddress;
    }

    public void setMainIPAddress(String mainIPAddress) {
        this.mainIPAddress = mainIPAddress;
    }

    public String getDbAccount() {
        return dbAccount;
    }

    public void setDbAccount(String dbAccount) {
        this.dbAccount = dbAccount;
    }

    public String getDbPW() {
        return dbPW;
    }

    public void setDbPW(String dbPW) {
        this.dbPW = dbPW;
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public String getDbUserName() {
        return dbUserName;
    }

    public void setDbUserName(String dbUserName) {
        this.dbUserName = dbUserName;
    }

    public String getDbUserPw() {
        return dbUserPw;
    }

    public void setDbUserPw(String dbUserPw) {
        this.dbUserPw = dbUserPw;
    }
}
