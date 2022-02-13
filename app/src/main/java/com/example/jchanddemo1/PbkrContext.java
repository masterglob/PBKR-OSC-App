package com.example.jchanddemo1;

import android.util.Log;

public class PbkrContext {

    public static PbkrContext instance = new PbkrContext();

    private PbkrContext()
    {
        this.setServerPortIn(8000);
        this.setServerPortOut(9000);
        this.setServerIp("192.168.22.1");
    }


    private String mServerIp;
    private Integer mServerPortIn;
    private Integer mServerPortOut;

    public String getServerIp() {
        return mServerIp;
    }

    public void setServerIp(String mServerIp) {
        this.mServerIp = mServerIp;
        Log.i("SettingsFragment", "Server Ip changed to "+ mServerIp);
    }

    public Integer getServerPortIn() {
        return mServerPortIn;
    }

    public void setServerPortIn(Integer mServerPortIn) {
        this.mServerPortIn = mServerPortIn;
    }

    public Integer getServerPortOut() {
        return mServerPortOut;
    }

    public void setServerPortOut(Integer mServerPortOut) {
        this.mServerPortOut = mServerPortOut;
    }
}
