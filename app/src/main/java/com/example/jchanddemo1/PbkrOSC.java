package com.example.jchanddemo1;

import android.util.Log;

import com.example.jchanddemo1.ui.home.HomeFragment;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/*
    Received statuses:
    /ping => null
        CONTROL PAGE
    /pbkrctrl/projectName => string
    /pbkrctrl/lTrack => String (current track)
    /pbkrctrl/project[1..9] => project names
    /pbkrctrl/lTrack[1..64] => track name (active project)
    /pbkrctrl/mtTrackSel/2/1 => INT (current track = pos X/Y??? TODO)
        MENU PAGE
    /pbkrmenu/project[1..10] => project names
    /pbkrmenu/project[1..10]/color => project color (string)
    /pbkrmenu/project[1..10]/visible => project visible (int = 0|1)
 */

public class PbkrOSC {
    public static PbkrOSC instance = new PbkrOSC();
    private HomeFragment m_homeFragment;
    private MainActivity mActivity;

    public boolean reconfigure(){
        mUdp.reconfigure();
        return false;
    }

    public void send(String Command){
        mUdp.send(new PbkrUDP_Itf.OSC_Msg(Command));
    }

    public void send(String Command, float fVal){
        mUdp.send(new PbkrUDP_Itf.OSC_Msg(Command, fVal));
    }

    private PbkrOSC()
    {
        mUdp = new PbkrUDP_Itf();
        m_homeFragment = null;
        mActivity = null;

        Thread thread = new Thread(new Runnable() {
            public void run() {
                mTaskImpl();
            }
        });

        thread.start();
    }

    private void mTaskImpl(){
        boolean mPingSent = false;
        final Pattern rePing = Pattern.compile("^/ping$");
        final Pattern reCurrProjectName = Pattern.compile("^/pbkrctrl/projectName$");
        final Pattern reCurrTrackName = Pattern.compile("^/pbkrctrl/lTrack$");
        final Pattern reCtrlProject = Pattern.compile("^/pbkrctrl/project([0-9])$");
        final Pattern reCtrlTrackName = Pattern.compile("^/pbkrctrl/lTrack([1-9][0-9]*)$");
        final Pattern reCtrlTrackId   = Pattern.compile("^/pbkrctrl/mtTrackSel/([0-9]+)/([0-9]+)$");
        final Pattern reMenuProjName   = Pattern.compile("^/pbkrmenu/project([1-9][0-9]*)$");
        final Pattern reMenuProjColor   = Pattern.compile("^/pbkrmenu/project([1-9][0-9]*)/color$");
        final Pattern reMenuProjVisible   = Pattern.compile("^/pbkrmenu/project([1-9][0-9]*)/visible$");
        final Pattern reMenuLine1   = Pattern.compile("^/pbkrmenu/menuL1$");
        final Pattern reMenuLine2   = Pattern.compile("^/pbkrmenu/menuL2$");
        final Pattern reMenu2Line1   = Pattern.compile("^/pbkrctrl/lMenuL1$");
        final Pattern reMenu2Line2   = Pattern.compile("^/pbkrctrl/lMenuL2$");
        final Pattern reMenuLine1Color   = Pattern.compile("^/pbkrmenu/menuL1/color$");
        final Pattern reMenuLine2Color   = Pattern.compile("^/pbkrmenu/menuL2/color$");
        final Pattern reCtrlPlay = Pattern.compile("^/pbkrctrl/pPlay$");
        final Pattern reCtrlPause = Pattern.compile("^/pbkrctrl/pPause$");
        final Pattern reCtrlStop = Pattern.compile("^/pbkrctrl/pStop$");
        final Pattern reCtrlTimeCode = Pattern.compile("^/pbkrctrl/timecode$");


        while (mUdp.isRunning()) {
            if (mPingSent == false) {
                mUdp.send(new PbkrUDP_Itf.OSC_Msg("/ping"));
                Log.i("OSC", "send PING");
                mPingSent = true;
                mUdp.send(new PbkrUDP_Itf.OSC_Msg("/refresh"));
            }
            else {
                PbkrUDP_Itf.OSC_Msg msg = mUdp.getNextMessage(1000);

                if (msg == null) {
                    mPingSent = false;
                } else {
                    // Process message
                    PbkrUDP_Itf.OSC_Msg.Parameter param = msg.getParam();
                    String name = param.getName();
                    String paramStr;
                    String paramType = param.getType();
                    if (paramType == null){
                        paramStr="<NULL>";
                    } else if (paramType.equals("f")){
                        paramStr = "f:" + String.valueOf(param.getFloatValue());
                    } else if (paramType.equals("i")){
                        paramStr = "i:" + String.valueOf(param.getIntValue());
                    }
                    else{
                        paramStr = param.getStringValue();
                    }
                    Matcher m;

                    m = rePing.matcher(name);
                    if (m.matches()){
                        // TODO : implement some "connected" status?
                        continue;
                    }

                    m = reCtrlPlay.matcher(name);
                    if (m.matches()){
                        PbkrContext.instance.playStatus = (param.getFloatValue() > 0.01 ? 1 : 0);
                        Log.e("OSC recevied reCtrlPlay", "PbkrContext.instance.playStatus=" +String.valueOf(PbkrContext.instance.playStatus));
                        m_homeFragment.refreshPlayStatus();
                        continue;
                    }

                    m = reCtrlPause.matcher(name);
                    if (m.matches()){
                        PbkrContext.instance.pauseStatus = (param.getFloatValue() > 0.01 ? 1 : 0);
                        Log.e("OSC recevied pauseStatus", "PbkrContext.instance.pauseStatus=" +String.valueOf(PbkrContext.instance.pauseStatus));
                        m_homeFragment.refreshPlayStatus();
                        continue;
                    }

                    m = reCtrlStop.matcher(name);
                    if (m.matches()){
                        PbkrContext.instance.stopStatus = (param.getFloatValue() > 0.01 ? 1 : 0);
                        Log.e("OSC recevied reCtrlStop", "PbkrContext.instance.stopStatus=" +String.valueOf(PbkrContext.instance.stopStatus));
                        m_homeFragment.refreshPlayStatus();
                        continue;
                    }

                    m = reCtrlTimeCode.matcher(name);
                    if (m.matches()){
                        PbkrContext.instance.currentTimeCode = paramStr;
                        m_homeFragment.refreshCurrentTimeCode();
                        continue;
                    }

                    m = reCurrProjectName.matcher(name);
                    if (m.matches()){
                        PbkrContext.instance.currentProject = paramStr;
                        m_homeFragment.refreshCurrentProjectName();
                        continue;
                    }

                    m = reCurrTrackName.matcher(name);
                    if (m.matches()){
                        PbkrContext.instance.currentTrack = paramStr;
                        m_homeFragment.refreshCurrentTrackName();
                        continue;
                    }

                    m = reCtrlProject.matcher(name);
                    if (m.matches()){
                        // TODO
                        continue;
                    }

                    m = reCtrlTrackName.matcher(name);
                    if (m.matches()){
                        // TODO
                        continue;
                    }

                    m = reCtrlTrackId.matcher(name);
                    if (m.matches()){
                        // TODO
                        continue;
                    }

                    m = reMenuProjName.matcher(name);
                    if (m.matches()){
                        // TODO
                        continue;
                    }

                    m = reMenuProjColor.matcher(name);
                    if (m.matches()){
                        // TODO
                        continue;
                    }

                    m = reMenuProjVisible.matcher(name);
                    if (m.matches()){
                        // TODO
                        continue;
                    }

                    m = reMenu2Line1.matcher(name);
                    if (m.matches()){
                        // TODO
                        continue;
                    }

                    m = reMenu2Line2.matcher(name);
                    if (m.matches()){
                        // TODO
                        continue;
                    }
                    m = reMenuLine1.matcher(name);
                    if (m.matches()){
                        // TODO
                        continue;
                    }

                    m = reMenuLine2.matcher(name);
                    if (m.matches()){
                        // TODO
                        continue;
                    }

                    m = reMenuLine1Color.matcher(name);
                    if (m.matches()){
                        // TODO
                        continue;
                    }

                    m = reMenuLine2Color.matcher(name);
                    if (m.matches()){
                        // TODO
                        continue;
                    }


                    Log.e("OSC command", "Unknown command:" + name);
                }
            }
        }
    }

    private PbkrUDP_Itf mUdp;

    public void setMainActivity(MainActivity mainActivity) {
        mActivity = mainActivity;
    }

    public void setMainHomePage(HomeFragment homeFragment) {
        m_homeFragment = homeFragment;
    }
}
