package com.example.jchanddemo1;

import android.util.Log;

import androidx.annotation.NonNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

public class PbkrUDP_Itf {
    @SuppressWarnings("FieldCanBeLocal")
    public static class OSC_Msg
    {
        public OSC_Msg(DatagramPacket packet){
            mStream = new  ByteArrayOutputStream();
            mStream.write(packet.getData(), 0, packet.getLength());
            mParam = new Parameter(mStream.toByteArray());
        }
        public OSC_Msg(String name){
            mStream = new  ByteArrayOutputStream();
            writeString(name);
            writeParamType(mParamVoid);
            mParam = new Parameter(mStream.toByteArray());
        }

        public OSC_Msg(String name, float fVal){
            mStream = new  ByteArrayOutputStream();
            writeString(name);
            writeParamType(mParamFloat);
            writeFloat(fVal);
            mParam = new Parameter(mStream.toByteArray());
        }
        public OSC_Msg(String name, String param){
            mStream = new  ByteArrayOutputStream();
            writeString(name);
            writeParamType(mParamString);
            writeString(param);
            mParam = new Parameter(mStream.toByteArray());
        }
        public OSC_Msg(String name, int param){
            mStream = new  ByteArrayOutputStream();
            writeString(name);
            writeParamType(mParamInt);
            writeInt(param);
        }

        @NonNull
        public String toString(){
            return mParam.getName() +
                    ",type=" + mParam.getType() +
                    ", value=" + mParam.getStringValue();
        }

        public Parameter getParam(){
            return mParam;
        }

        public DatagramPacket getPacket(PbkrUDP_Itf itf){
            return new DatagramPacket(mStream.toByteArray(), mStream.size(),
                    itf.m_sendAddress, itf.m_sendPort);
        }

        private void writeString(String s) {
            int addLen = 4 - (s.length() % 4);
            try {
                mStream.write(s.getBytes());
                mStream.write(mParamFill, 0, addLen);
            } catch (IOException e) {
                Log.e("PbkrUDP","writeString failed");
            }
        }

        private void writeFloat(float fVal){
            try {
                mStream.write(ByteBuffer.allocate(4).putFloat(fVal).array());
            } catch (IOException e) {
                mStream.write(0);
                mStream.write(0);
                mStream.write(0);
                mStream.write(0);
            }
        }

        private void writeInt(int param){
            mStream.write((byte) (param >> 24));
            mStream.write((byte) (param >> 16));
            mStream.write((byte) (param >> 8));
            mStream.write((byte) (param));
        }
        private void writeParamType(byte[] paramType){
            try {
                mStream.write(paramType);
            } catch (IOException e) {
                Log.e("PbkrUDP","writeInt failed");
            }
        }
        ByteArrayOutputStream mStream;
        private final byte[] mParamFill={0x00, 0x00, 0x00, 0x00};
        private final byte[] mParamInt={0x2c, 0x69, 0x00, 0x00};
        private final byte[] mParamVoid={0x2c, 0x00, 0x00, 0x00};
        private final byte[] mParamString={0x2c, 0x73, 0x00, 0x00};
        private final byte[] mParamFloat={0x2c, 102, 0x00, 0x00};
        private Parameter mParam;

        class Parameter{
            Parameter(byte[] parameter){
                m_IntValue = 0;
                m_FloatValue = 0.0f;
                m_StringValue = null;
                int i = 0;
                final int len = parameter.length;
                while (i < len && parameter[i] != '\0') {
                    i ++;
                }
                m_Name = new String(Arrays.copyOfRange(parameter, 0, i),
                        StandardCharsets.UTF_8);
                // skip all Zeros
                while (i < len && parameter[i] == '\0') {
                    i ++;
                }

                // Type
                final int iParamType = i;

                i = i + 4;
                if (i >= len) {
                    // Just consider void parameter
                    return;
                }

                byte[] paramType = Arrays.copyOfRange(parameter, iParamType, i);

                if (Arrays.equals(paramType, mParamString))
                {
                    m_type = "s";
                    int iParamEnd = i;
                    while (iParamEnd < len && parameter[iParamEnd] != '\0') {
                        iParamEnd ++;
                    }
                    m_StringValue =
                            new String(Arrays.copyOfRange(parameter, i, iParamEnd),
                                    StandardCharsets.UTF_8);
                } else if (Arrays.equals(paramType, mParamInt))
                {
                    m_type = "i";
                    if (i + 4 <= len) {
                        m_IntValue = parameter[i];
                        m_IntValue <<= 8;
                        i ++;
                        m_IntValue += parameter[i];
                        m_IntValue <<= 8;
                        i ++;
                        m_IntValue += parameter[i];
                        m_IntValue <<= 8;
                        i ++;
                        m_IntValue += parameter[i];
                    }
                    m_FloatValue = (float)m_IntValue;
                    m_StringValue = Integer.toString(m_IntValue);
                }else if (Arrays.equals(paramType, mParamFloat))
                {
                    m_type = "f";
                    m_FloatValue = ByteBuffer.wrap(Arrays.copyOfRange(parameter, i, i+4)).getFloat();
                    m_StringValue = Float.toString(m_FloatValue);
                    m_IntValue = (int) m_FloatValue;
                }
                else {
                    m_type = "v";
                }
            }
            String getName(){
                return m_Name;
            }
            String getType(){
                return m_type;
            }
            int getIntValue(){
                return m_IntValue;
            }
            float getFloatValue(){
                return m_FloatValue;
            }
            String getStringValue(){
                return m_StringValue;
            }
            private int m_IntValue;
            private float m_FloatValue;
            private String m_StringValue;
            private String m_type;
            private final String m_Name;
        }
    }

    PbkrUDP_Itf(){
        setStatus("Uninitialized");
        mRunning = true;
        mSendReady = false;
        mRecvReady = false;
        m_recvAddress = null;
        m_sendAddress = null;
        m_sendPort = 0;
        m_recvPort = 0;
        mLock = new Object();

        setStatus("Creating DatagramSocket");
        try {
            m_sendSock = new DatagramSocket();
            m_recvSock = new DatagramSocket();
        } catch (SocketException e) {
            fatalFailure("Failed to create DatagramSocket. Check application permissions");
            return;
        }

        mSendQueue = new LinkedList<>();
        mRcvQueue  = new LinkedList<>();
        mRcvThread = new Thread(this::mSendTaskImpl);
        mSendThread = new Thread(this::mRecvImpl);

        mRcvThread.start();
        mSendThread.start();
    }

    public void send(OSC_Msg message) {
        synchronized(mLock){
            mSendQueue.add(message);
        }
    }

    public OSC_Msg getNextMessage(int timeoutMs) {
        int remainingWait = timeoutMs;
        OSC_Msg result = null;
        while (remainingWait > 0){
            result = mRcvQueue.poll();
            if (result != null) break;
            sleep();
            remainingWait -= m_sleepMs;
        }
        return result;
    }

    public void reconfigure() {
        synchronized(mLock){
            // Empty sending stack
            mSendQueue.clear();
            mSendReady = false;
            mRecvReady = false;
            m_recvAddress = null;
            m_sendAddress = null;
        }
    }


    void fatalFailure(String msg)
    {
        synchronized(mLock){
            setStatus(msg);
            mRecvReady = false;
            mSendReady = false;
            mRunning = false;
        }
        Log.e("PbkrUDP_Itf", msg);
    }

    private void setupAddresses(){
        setStatus("Creating configuration addresses");
        final int recvPort =  PbkrContext.instance.getServerPortOut();
        final String serverIp = PbkrContext.instance.getServerIp();
        final String anyIp = "0.0.0.0";

        m_sendPort = PbkrContext.instance.getServerPortIn();
        m_recvPort = PbkrContext.instance.getServerPortOut();
        InetAddress anyAddress;
        try {
            anyAddress = InetAddress.getByName(anyIp);
            m_sendAddress = InetAddress.getByName(serverIp);
            m_recvAddress = new InetSocketAddress(anyAddress, recvPort);
        } catch (UnknownHostException e) {
            fatalFailure("Failed to create InetSocketAddress. Check application permissions");
            Log.e("PbkrOSC", "Failed to create InetSocketAddress ");
        }
    }

    private void sleep(){
        try {
            Thread.sleep(m_sleepMs);
        } catch (InterruptedException e) {
            setStatus ("InterruptedException in sleep");
            mRunning = false;
        }
    }

    private void mRecvImpl(){
        while (mRunning) {
            if (!mRecvReady){
                if (m_recvAddress == null){
                    Log.e("mRecvImpl","Not configured!");
                }
                else {
                    try {
                        m_recvSock.close();
                        m_recvSock = new DatagramSocket(m_recvAddress);
                        //Log.i("mRecvImpl","Bound to address " + m_recvAddress.toString());
                        mRecvReady = true;
                    } catch (SocketException e) {
                        Log.e("mRecvImpl","Cannot bind address " + m_recvAddress.toString());
                        Log.e("mRecvImpl",e.toString());
                    }
                }
                sleep();
                continue;
            }

            byte[] receiveData = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            try {
                m_recvSock.receive(receivePacket);
                if (receivePacket.getLength() > 0){
                    OSC_Msg message  = new OSC_Msg(receivePacket);
                    // Log.d("Received OSC msg:", message.toString());
                    if (mRcvQueue.size() > 200) {
                        mRcvQueue.poll(); // Just avoid infinite filling...
                    }
                    mRcvQueue.add(message);
                }
            } catch (IOException e) {
                Log.e("mRecvImpl","IOException while reading socket");
                mRunning = false;
            }
        }
    }

    private void mSendTaskImpl(){
        DatagramPacket packet = null;
        while (mRunning)  {
            synchronized(mLock){
                if(! mSendReady) {
                    mSendReady = true;
                    setupAddresses();
                }
                else {
                    OSC_Msg message =  mSendQueue.poll();
                    if (message != null)
                    {
                        packet = message.getPacket(this);
                    }
                }
            }

            if (packet != null) { // Send that packet
                setStatus("DatagramPacket to send");
                try {
                    m_sendSock.send(packet);
                    setStatus("DatagramPacket sent");
                } catch (IOException e) {
                    Log.e("PbkrOSC", "Failed to send message to " +
                            m_sendAddress.toString() + ":" + m_sendPort);
                }
                packet = null; //Discard packet in all cases
            }
            sleep();
        }

/*
        try {
            m_sendSock.bind(socketAddress);
            Log.i("PbkrOSC", "OSC output bound to " + sendIp + ":" + sendPort);
        } catch (SocketException e) {
            Log.e("PbkrOSC", "Failed to bind " + sendIp + ":" + sendPort);
            return false;
        }
*/
    }

    DatagramSocket m_sendSock;
    DatagramSocket m_recvSock;
    SocketAddress m_recvAddress;
    InetAddress m_sendAddress;
    int m_sendPort;
    int m_recvPort;
    private boolean mRunning;
    private boolean mSendReady;
    private boolean mRecvReady;
    private final Object mLock;
    private Queue<OSC_Msg> mSendQueue;
    private Queue<OSC_Msg> mRcvQueue;

    private Thread mSendThread;
    private Thread mRcvThread;
    private String m_Status;
    public String getStatus() {return m_Status;}
    private void setStatus(final String msg){
        m_Status = msg;
        // Log.d("UDP status", msg);
    }
    boolean isRunning(){return mRunning;}
    private final int m_sleepMs = 100;
}
