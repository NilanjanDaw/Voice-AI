package nilanjan.voice;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class BluetoothComm {

    //Required bluetooth objects
    public BluetoothDevice device = null;
    public BluetoothSocket socket = null;
    public BluetoothAdapter mBluetoothAdapter = null;
    public InputStream receiveStream = null;
    public BufferedReader receiveReader = null;
    public OutputStream sendStream = null;

    //this thread will listen to incoming messages. It will be killed when connection is closed
    // private ReceiverThread receiverThread;

    //these handlers corresponds to those in the main activity
    Handler handlerStatus, handlerMessage;

    public static int CONNECTED = 1;
    public static int DISCONNECTED = 2;
    static final String TAG = "Chihuahua";

    public BluetoothComm(Handler hstatus, Handler h) {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        handlerStatus = hstatus;
        handlerMessage = h;
    }

    //when called from the main activity, it sets the connection with the remote device
    public OutputStream connect(String txt) {
        OutputStream x;

        Set<BluetoothDevice> setpairedDevices = mBluetoothAdapter.getBondedDevices();
        BluetoothDevice[] pairedDevices = (BluetoothDevice[]) setpairedDevices.toArray(new BluetoothDevice[setpairedDevices.size()]);

        boolean foundChihuahua = false;
        for(int i=0;i<pairedDevices.length;i++) {
            if(pairedDevices[i].getName().contains(txt.trim())) {
                device = pairedDevices[i];
                try {
                    //the String "00001101-0000-1000-8000-00805F9B34FB" is standard for Serial connections
                    socket = device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
                foundChihuahua = true;
                break;
            }
        }
        if(foundChihuahua == false){
            Log.v(TAG, "You have not turned on your Chihuahua");
        }

        //receiverThread = new ReceiverThread(handlerMessage);
        // new Thread() {
        //    @Override public void run() {
        try {
            mBluetoothAdapter.cancelDiscovery();
            socket.connect();
            if(socket==null)
                Log.d("BTStream","null socket");
            else
                Log.d(TAG,socket.toString());
            //x=socket.getOutputStream();
            receiveStream = socket.getInputStream();
            receiveReader = new BufferedReader(new InputStreamReader(receiveStream));
            sendStream = socket.getOutputStream();
            Log.d("Thread",sendStream.toString());
            Message msg = handlerStatus.obtainMessage();
            msg.arg1 = CONNECTED;
            handlerStatus.sendMessage(msg);

            //receiverThread.start();
            return socket.getOutputStream();
        }
        catch (IOException e) {
            Log.d("N", "Connection Failed : "+e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    // }.start();
    //}

    /* public void stream(BluetoothSocket socket){
         try {
             if(sendStream==null) {
                 //sendStream = socket.getOutputStream();
                 receiveStream = socket.getInputStream();
                 receiveReader = new BufferedReader(new InputStreamReader(receiveStream));
                 sendStream = socket.getOutputStream();
                 for(int i=0;i<100;i++)

                 if(socket!=null)
                 Log.d(TAG,sendStream.toString());
                 else
                     Log.d(TAG,"stream socket null");
             }
         }catch (IOException e)
         {
             Log.d("stream","Exception");
         }
     }*/
    //properly closing the socket and updating the status
    public void close() {
        try {
            if(socket!=null) {
                socket.close();
                Log.d("BTStream","close BT");
                //receiverThread.interrupt();
                Message msg = handlerStatus.obtainMessage();
                msg.arg1 = DISCONNECTED;
                handlerStatus.sendMessage(msg);
            }

        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendData(byte[] msg,OutputStream strm){
        try {
            if(strm!=null) {
                strm.write(msg);
                Log.d("BluetoothSend","true");
                // sendStream.flush();
            }
            else
                Log.d("BTStream","sendStream NULL");
        } catch (Exception e) {
            if(sendStream==null)
                Log.d("BTStream","sendStream NULL");
            e.printStackTrace();
        }
    }
    //this thread listens to replies from Arduino as it performs actions, then update the log through the Handler
  /*  private class ReceiverThread extends Thread {
        Handler handler;

        ReceiverThread(Handler h) {
            handler = h;
        }

        @Override public void run() {
            while(socket != null) {
                if (isInterrupted()){
                    try {
                        join();
                    }
                    catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    if(receiveStream.available() > 0) {
                        String dataToSend = ""; //when we hit a line break, we send the data

                        dataToSend = receiveReader.readLine();
                        if (dataToSend != null){
                            Log.v(TAG, dataToSend);
                            Message msg = handler.obtainMessage();
                            Bundle b = new Bundle();
                            b.putString("receivedData", dataToSend);
                            msg.setData(b);
                            handler.sendMessage(msg);
                            dataToSend = "";
                        }

                    }
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }*/

}
