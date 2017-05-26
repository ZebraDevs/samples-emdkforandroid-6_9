/*
* Copyright (C) 2015-2016 Symbol Technologies LLC
* All rights reserved.
*/
package com.symbol.serialcommsample1;

import com.symbol.emdk.EMDKManager;
import com.symbol.emdk.EMDKManager.EMDKListener;
import com.symbol.emdk.EMDKManager.FEATURE_TYPE;
import com.symbol.emdk.EMDKResults;
import com.symbol.emdk.serialcomm.SerialComm;
import com.symbol.emdk.serialcomm.SerialCommException;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


public class MainActivity extends Activity implements EMDKListener {

    private String TAG = MainActivity.class.getSimpleName();
    private EMDKManager emdkManager = null;
    private SerialComm serialComm = null;

    private EditText editText = null;
    private TextView statusView = null;
    private Button readButton = null;
    private Button writeButton = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editText = (EditText) findViewById(R.id.editText1);
        editText.setText("Serial Communication Write Data Testing.");

        statusView = (TextView) findViewById(R.id.statusView);
        statusView.setText("");
        statusView.requestFocus();


        EMDKResults results = EMDKManager.getEMDKManager(getApplicationContext(), this);
        if (results.statusCode != EMDKResults.STATUS_CODE.SUCCESS) {
            statusView.setText("Failed to open EMDK");
        } else {
            statusView.setText("Opening EMDK...");
        }

        addReadButtonEvents();
        writeButtonEvents();
        setEnabled(false);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (emdkManager != null) {
            emdkManager.release();
            emdkManager = null;
        }
    }

    @Override
    public void onOpened(EMDKManager emdkManager) {
        this.emdkManager = emdkManager;

        Log.d(TAG, "EMDK opened");

        try{
            serialComm = (SerialComm) this.emdkManager.getInstance(FEATURE_TYPE.SERIALCOMM);

            Thread readThread = new Thread(new Runnable() {
                @Override
                public void run() {

                    String statusText = "";
                    if (serialComm != null) {
                        try{
                            serialComm.enable();
                            statusText = "Serial comm channel enabled";

                            setEnabled(true);

                        } catch(SerialCommException e){
                            Log.d(TAG, e.getMessage());
                            statusText = e.getMessage();
                            setEnabled(false);
                        }
                    } else {
                        statusText = FEATURE_TYPE.SERIALCOMM.toString() + " "+"Feature not supported or initilization error.";
                        setEnabled(false);
                    }
                    displayMessage(statusText);
                }

            });
            readThread.start();
        }
        catch(Exception e)
        {
            Log.d(TAG, e.getMessage());

            displayMessage(e.getMessage());
        }
    }

    @Override
    public void onClosed() {
        if(emdkManager != null) {
            emdkManager.release();
        }
        displayMessage("EMDK closed abruptly.");
    }

    private void addReadButtonEvents() {
        readButton = (Button) findViewById(R.id.ReadButton);
        readButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                Thread readThread = new Thread(new Runnable() {
                    @Override
                    public void run() {

                        setEnabled(false);
                        String statusText = "";
                        try {

                            byte[] readBuffer = serialComm.read(10000); //Timeout after 10 seconds

                            if(readBuffer != null) {
                                String tempString = new String(readBuffer);
                                statusText = "Data Read:\n" + tempString;
                            } else {
                                statusText = "No Data Available";
                            }

                        }catch (SerialCommException e) {
                            statusText ="read:"+ e.getResult().getDescription();
                        }
                        catch (Exception e) {
                            statusText = "read:"+ e.getMessage();
                        }
                        setEnabled(true);
                        displayMessage(statusText);
                    }
                });
                readThread.start();
            }
        });
    }

    private void writeButtonEvents() {
        writeButton = (Button) findViewById(R.id.WriteButton);

        writeButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setEnabled(false);
                try {
                    String writeData = editText.getText().toString();
                    int bytesWritten = serialComm.write(writeData.getBytes(), writeData.getBytes().length);
                    statusView.setText("Bytes written: "+ bytesWritten);
                } catch (SerialCommException e) {
                    statusView.setText("write: "+ e.getResult().getDescription());
                }
                catch (Exception e) {
                    statusView.setText("write: "+ e.getMessage() + "\n");
                }
                setEnabled(true);
            }
        });
    }

    void displayMessage(String message) {

        final String tempMessage = message;
        runOnUiThread(new Runnable() {
            public void run() {
                statusView.setText(tempMessage + "\n");
            }
        });
    }

    void setEnabled(boolean enableState) {

        final boolean tempState = enableState;
        runOnUiThread(new Runnable() {
            public void run() {
                readButton.setEnabled(tempState);
                writeButton.setEnabled(tempState);
                editText.setEnabled(tempState);
            }
        });
    }
}
