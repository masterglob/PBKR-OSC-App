package com.example.jchanddemo1;

import android.util.Log;

public class PbkrContext {

    public static PbkrContext instance = new PbkrContext();
    public int playStatus;
    public int pauseStatus;
    public int stopStatus;
    public String currentProject;
    public String currentTrack;
    public String currentTimeCode;
    private String mServerIp;
    private Integer mServerPortIn;
    private Integer mServerPortOut;
    private String[] m_projectName;
    private boolean[] m_projectVisible;
    private String[] m_projectColor;
    private String[] m_trackName;
    private int m_currentTrack;

    private PbkrContext()
    {
        this.setServerPortIn(8000);
        this.setServerPortOut(9000);
        this.setServerIp("192.168.22.1");
        this.currentProject = "No project loaded";
        this.currentTrack = "None";
        this.playStatus = -1;
        this.stopStatus = -1;
        this.pauseStatus = -1;
        this.currentTimeCode = "--:--";
        this.m_projectName = new String[nbProjects];
        this.m_projectColor = new String[nbProjects];
        this.m_trackName = new String[nbTracks];
        this.m_projectVisible = new boolean[nbProjects];
        this.m_currentTrack = -1;
    }


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

    public static final int nbProjects = 10;
    public String getProjectName(int pp){
        try{
            return m_projectName[pp];
        }
        catch (Exception e){
            return "";
        }
    }
    public void setProjectName(int pp, String paramStr) {
        try{
            m_projectName[pp] = paramStr;
        }
        catch (Exception e){
        }
    }
    public boolean getProjectVisibility(int pp){
        try{
            return m_projectVisible[pp];
        }
        catch (Exception e){
            return false;
        }
    }
    public void setProjectVisibility(int pp, boolean isVisible) {
        try{
            m_projectVisible[pp] = isVisible;
        }
        catch (Exception e){
        }
    }

    public String getProjectColor(int pp){
        try{
            return m_projectColor[pp];
        }
        catch (Exception e){
            return "gray";
        }
    }
    public void setProjectColor(int pp, String paramStr) {
        try{
            m_projectColor[pp] = paramStr;
        }
        catch (Exception e){
        }
    }
    public static final int nbTracks = 64;
    public void setTrackName(int tt, String title) {
        try{
            m_trackName[tt] = title;
        }
        catch (Exception e){
        }
    }
    public String getTrackName(int tt){
        try{
            return m_trackName[tt];
        }
        catch (Exception e){
            return "";
        }
    }

    public int getCurrentTrack() {
        return m_currentTrack;
    }
    public void setCurrentTrack(int tt) {
        m_currentTrack = tt;
    }
}
