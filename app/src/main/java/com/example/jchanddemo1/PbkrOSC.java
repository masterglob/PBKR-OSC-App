package com.example.jchanddemo1;

import android.util.Log;

import com.example.jchanddemo1.ui.home.HomeFragment;
import com.example.jchanddemo1.ui.projects.ProjectsFragment;

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
    /pbkrctrl/mtTrackSel/2/1 => INT (current track = pos Y/X)
        MENU PAGE
    /pbkrmenu/project[1..10] => project names
    /pbkrmenu/project[1..10]/color => project color (string)
    /pbkrmenu/project[1..10]/visible => project visible (int = 0|1)
 */

public class PbkrOSC {
    public static PbkrOSC instance = new PbkrOSC();
    private HomeFragment m_homeFragment;
    private ProjectsFragment m_projectsFragment;
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
        m_projectsFragment = null;
        mActivity = null;

        Thread thread = new Thread(this::mTaskImpl);

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

        PbkrContext context = PbkrContext.instance;

        while (mUdp.isRunning()) {
            if (!mPingSent) {
                mUdp.send(new PbkrUDP_Itf.OSC_Msg("/ping"));
                Log.i("OSC", "send PING");
                mPingSent = true;
                // mUdp.send(new PbkrUDP_Itf.OSC_Msg("/refresh")); // not required because already send by ping answer
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
                        context.playStatus = (param.getFloatValue() > 0.01 ? 1 : 0);
                        if (m_homeFragment != null) m_homeFragment.refreshPlayStatus();
                        continue;
                    }

                    m = reCtrlPause.matcher(name);
                    if (m.matches()){
                        context.pauseStatus = (param.getFloatValue() > 0.01 ? 1 : 0);
                        if (m_homeFragment != null) m_homeFragment.refreshPlayStatus();
                        continue;
                    }

                    m = reCtrlStop.matcher(name);
                    if (m.matches()){
                        context.stopStatus = (param.getFloatValue() > 0.01 ? 1 : 0);
                        if (m_homeFragment != null)  m_homeFragment.refreshPlayStatus();
                        continue;
                    }

                    m = reCtrlTimeCode.matcher(name);
                    if (m.matches()){
                        context.currentTimeCode = paramStr;
                        if (m_homeFragment != null) m_homeFragment.refreshCurrentTimeCode();
                        continue;
                    }

                    m = reCurrProjectName.matcher(name);
                    if (m.matches()){
                        context.currentProject = paramStr;
                        if (m_homeFragment != null) m_homeFragment.refreshCurrentProjectName();
                        continue;
                    }

                    m = reCurrTrackName.matcher(name);
                    if (m.matches()){
                        context.currentTrack = paramStr;
                        if (m_homeFragment != null) m_homeFragment.refreshCurrentTrackName();
                        continue;
                    }

                    m = reCtrlTrackName.matcher(name);
                    if (m.matches() && m.group(1) != null){
                        try {
                            int trackId = Integer.parseInt(m.group(1));
                            context.setTrackName(trackId, paramStr);
                            if (m_homeFragment != null) m_homeFragment.setTrackName(trackId, paramStr);
                        }
                        catch (NumberFormatException n){
                            Log.e("reCtrlTrackName","Param=" + paramStr + "TT=[" +m.group(1) + "]");
                        }
                        continue;
                    }

                    m = reCtrlTrackId.matcher(name);
                    if (m.matches() && m.group(1) != null && m.group(2) != null){
                        try {
                            int y = Integer.valueOf(m.group(1));
                            int x = Integer.valueOf(m.group(2));
                            final int tt = trackId(x, y);
                            context.setCurrentTrack(tt);
                            if (m_homeFragment != null) m_homeFragment.setCurrentTrackNum(tt);
                        }
                        catch (NumberFormatException n){
                            Log.e("reCtrlTrackId","Param=" + paramStr + "X,Y=[" +m.group(2)+","+ m.group(1) + "]");
                        }
                        continue;
                    }

                    m = reMenuProjName.matcher(name);
                    if (m.matches() && m.group(1) != null){
                        try {
                            int pp = Integer.valueOf(m.group(1)) - 1;
                            context.setProjectName(pp, paramStr);
                            if (m_projectsFragment != null) m_projectsFragment.setProjectName(pp, paramStr);
                        }
                        catch (NumberFormatException n){
                            Log.e("reMenuProjName","Param=" + paramStr + "PP=[" + m.group(1) + "]");
                        }
                        continue;
                    }

                    m = reMenuProjColor.matcher(name);
                    if (m.matches() && m.group(1) != null){
                        try {
                            int pp = Integer.valueOf(m.group(1)) - 1;
                            context.setProjectColor(pp, paramStr);
                            if (m_projectsFragment != null) m_projectsFragment.setProjectColor(pp, paramStr);
                        }
                        catch (NumberFormatException n){
                            Log.e("reMenuProjColor","Param=" + paramStr + "PP=[" + m.group(1) + "]");
                        }
                        continue;
                    }

                    m = reMenuProjVisible.matcher(name);
                    if (m.matches() && m.group(1) != null){
                        try {
                            int pp = Integer.valueOf(m.group(1)) - 1;
                            context.setProjectVisibility(pp, param.getIntValue() > 0);
                            if (m_projectsFragment != null) m_projectsFragment.setProjectVisible(pp, param.getIntValue() > 0);
                        }
                        catch (NumberFormatException n){
                            Log.e("reMenuProjVisible","Param=" + paramStr + "PP=[" + m.group(1) + "]");
                        }
                        continue;
                    }

                    m = reCtrlProject.matcher(name);
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
                        if (m_projectsFragment != null) m_projectsFragment.setMenuL1(paramStr);
                        continue;
                    }

                    m = reMenuLine2.matcher(name);
                    if (m.matches()){
                        if (m_projectsFragment != null) m_projectsFragment.setMenuL2(paramStr);
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

    static private final int PAD_WIDTH = 7;
    static private final int PAD_HEIGHT = 2;
    /**
     * @param x Xpos in pad (left=1, right = PAD_NBX)
     * @param y YPos in pad (top = PAD_NBY, bott = 1)
     * @return Track Id (first = 0)
     */
    static public int trackId(int x, int y){
        return (x - 1) + (PAD_HEIGHT - y) * PAD_WIDTH;
    }

    /**
     * @param trackId Track Id (First = 0)
     * @return PAD X position in [1..PAD_WIDTH]
     */
    static public int trackPadX(int trackId){
        return 1+ (trackId % PAD_WIDTH);
    }
    /**
     * @param trackId Track Id (First = 0)
     * @return PAD Y position in [1..PAD_HEIGHT]
     */
    static public int trackPadY(int trackId){
        return PAD_HEIGHT - (trackId / PAD_WIDTH);
    }

    private PbkrUDP_Itf mUdp;

    public void setMainActivity(MainActivity mainActivity) {
        mActivity = mainActivity;
    }

    public void setMainHomePage(HomeFragment homeFragment) {m_homeFragment = homeFragment;}
    public void setProjectPage(ProjectsFragment fragment) {
        m_projectsFragment = fragment;
    }
}
