package com.example.jchanddemo1;

public class PbkrOSC {
    public static PbkrOSC instance = new PbkrOSC();

    public boolean reconfigure(){
        mUdp.reconfigure();
        return false;
    }

    private PbkrOSC()
    {
        mUdp = new PbkrUDP_Itf();

        Thread thread = new Thread(new Runnable() {
            public void run() {
                mTaskImpl();
            }
        });

        thread.start();
    }

    private void mTaskImpl(){
        while (mUdp.isRunning())
        {
            PbkrUDP_Itf.OSC_Msg msg = new PbkrUDP_Itf.OSC_Msg("/ping");
            mUdp.send(msg);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                mUdp.fatalFailure ("InterruptedException in sleep");
            }
        }
    }

    private PbkrUDP_Itf mUdp;

}
