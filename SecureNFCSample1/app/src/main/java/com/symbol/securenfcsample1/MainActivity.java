/*
* Copyright (C) 2015-2016 Symbol Technologies LLC
* All rights reserved.
*/
package com.symbol.securenfcsample1;

import com.symbol.emdk.EMDKManager;
import com.symbol.emdk.EMDKResults;
import com.symbol.emdk.EMDKManager.EMDKListener;
import com.symbol.emdk.EMDKManager.FEATURE_TYPE;
import com.symbol.emdk.securenfc.MifareDesfire;
import com.symbol.emdk.securenfc.MifareDesfire.AuthenticateType;
import com.symbol.emdk.securenfc.MifareDesfire.CreditType;
import com.symbol.emdk.securenfc.MifareDesfire.FileCommMode;
import com.symbol.emdk.securenfc.MifareDesfire.FileSettings;
import com.symbol.emdk.securenfc.MifareDesfireExpection;
import com.symbol.emdk.securenfc.MifarePlusSL3;
import com.symbol.emdk.securenfc.MifarePlusSL3Exception;
import com.symbol.emdk.securenfc.MifareSam;
import com.symbol.emdk.securenfc.MifareSam.AdditionalAuthData;
import com.symbol.emdk.securenfc.MifareSam.ProtectionMode;
import com.symbol.emdk.securenfc.MifareDesfireResults;
import com.symbol.emdk.securenfc.MifareSamException;
import com.symbol.emdk.securenfc.SamKey;
import com.symbol.emdk.securenfc.SecureNfcException;
import com.symbol.emdk.securenfc.SecureNfcManager;
import com.symbol.emdk.securenfc.MifareSam.SamMode;
import com.symbol.emdk.securenfc.SecureNfcManager.SamType;
import com.symbol.emdk.securenfc.SecureNfcManager.TagTechType;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint.Join;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.symbol.emdk.securenfc.MifareSam.AdditionalAuthData;

public class MainActivity extends Activity implements EMDKListener {

    private EMDKManager emdkManager = null;
    private SamType samType;
    private MifareSam mifareSam;

    boolean isEnable;
    NfcAdapter mNfcAdapter;
    PendingIntent mfcPendingIntent;
    SecureNfcManager secureNfcMgr = null;
    MifareDesfire mMifareDesfire = null;
    MifarePlusSL3 mMifarePlusSl3 = null;

    TextView statusTV;
    TextView tagDataTV;

    StringBuilder status;
    String newline = "\n";
    StringBuilder tagData;

    Button read;
    Button write;

    EditText dataeditText;
    EditText valueeditText;

    // Data to be written to the tag
    String dataToBeWritten;

    Button inc;
    Button dec;
    static String exception = null;
    // Samkey for read from data file
    SamKey lSamKeyForReadWrite;

    // Samkey for write to data file
    // SamKey lSamKeyForWrite;

    Toast toast;

    Tag lTag;

    String value;
    TagOperation tagOperation = null;
    static int valueDataBefore;
    static int valueDataAfter;

    // AuthKey required for SAM to Host authentication
    byte[] authKey = new byte[0x10];
    static ProgressDialog mProgressDialog;

    enum Operations {
        READ, WRITE, INCREMENT, DECREMENT, UNDEFINED;
    }

    public static final String TAG = "com.symbol.securenfcsample1.MainActivity";
    Operations operation = Operations.UNDEFINED;

    String data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mNfcAdapter == null) {

            // Stop here, we definitely need NFC
            Log.e(TAG, "Device does not support NFC");
            Toast.makeText(getApplicationContext(), getText(R.string.no_nfc),
                    Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        status = new StringBuilder();
        tagData = new StringBuilder();

        statusTV = (TextView) findViewById(R.id.status);
        tagDataTV = (TextView) findViewById(R.id.tagData);

        read = (Button) findViewById(R.id.read);
        write = (Button) findViewById(R.id.write);

        dataeditText = (EditText) findViewById(R.id.editText1);
        valueeditText = (EditText) findViewById(R.id.editText2);

        inc = (Button) findViewById(R.id.inc);
        dec = (Button) findViewById(R.id.dec);

        // Read&Write Access key for read,write ,credit & debit operation.

        lSamKeyForReadWrite = new SamKey();
        lSamKeyForReadWrite.keyNum = 0x05;
        lSamKeyForReadWrite.keyVer = 0x00;

        readBtnClickListener(read);
        writeBtnClickListener(write);

        incBtnClickListener(inc);
        decBtnClickListener(dec);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle("Operation in progress");
        mProgressDialog.setMessage("Please do not remove the tag");
        mProgressDialog.setCancelable(false);
        mProgressDialog.setCanceledOnTouchOutside(false);

        if (!mNfcAdapter.isEnabled()) {
            Log.w(TAG, "Nfc is disabled in the device");

        } else {
            EMDKResults results = EMDKManager.getEMDKManager(
                    getApplicationContext(), this);

            // Check the return status of getEMDKManager ()
            if (results.statusCode == EMDKResults.STATUS_CODE.SUCCESS) {

                Log.i(TAG, "EMDK is opening...");

            } else {

                Log.i(TAG, "EMDK open request failed!");

            }
            Log.i(TAG, "Nfc is enabled in the device");

        }

        mfcPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
                getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, " onResume");
        mNfcAdapter
                .enableForegroundDispatch(this, mfcPendingIntent, null, null);

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (toast != null)
            toast.cancel();

    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(" ", " onStop called");

        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Log.i(" ", " onDestroy called");

        if (toast != null)
            toast.cancel();

        if (mMifareDesfire != null) {
            try {
                mMifareDesfire.disable();

            } catch (MifareDesfireExpection e) {

                e.printStackTrace();
            }

        }

        if (mMifarePlusSl3 != null) {
            try {
                mMifarePlusSl3.disable();

            } catch (MifarePlusSL3Exception e) {
                e.printStackTrace();
            }
        }

        if (emdkManager != null) {
            // Clean up the objects created by EMDK manager
            emdkManager.release();
        }

    }

    @Override
    public void onOpened(EMDKManager emdkManager) {

        this.emdkManager = emdkManager;
        Log.i("onOpened called", " ");
        status.append("\nApplication Initialized.");

        if (this.emdkManager != null) {

            secureNfcMgr = (SecureNfcManager) emdkManager
                    .getInstance(FEATURE_TYPE.SECURENFC);

            if (secureNfcMgr != null) {

                try {
                    samType = secureNfcMgr.getAvailableSam();

                    status.append(newline + newline + samType
                            + " SAM is available on the device.");

                    if (samType.equals(SamType.MIFARE)) {

                        mifareSam = (MifareSam) secureNfcMgr
                                .getSamInstance(samType);

                        mMifareDesfire = (MifareDesfire) secureNfcMgr
                                .getTagTechInstance(TagTechType.MIFARE_DESFIRE);

                        SamMode samMode = mifareSam.connect();

                        // SamKey required for the SAM to Host authentication.

                        SamKey samKey = new SamKey();
                        samKey.keyNum = 0x00;
                        samKey.keyVer = 0x00;

                        mifareSam.authenticateSam(authKey, samKey, null);

                        mifareSam.close();

                        setButtonEnabled(true);

                        status.append(newline + newline + samMode
                                + " SAM host authentication successful.");
                        statusTV.setText(status);

                    } else if (samType.equals(SamType.NONE)) {

                        status.append(newline + newline
                                + "SAM not available in the device");

                        AlertDialog.Builder dlgAlert = new AlertDialog.Builder(
                                this);

                        dlgAlert.setMessage("Please insert the SAM into the device and try again.");
                        dlgAlert.setTitle("Error Message...");
                        dlgAlert.setPositiveButton("OK", null);
                        dlgAlert.setCancelable(true);
                        dlgAlert.create().show();

                        dlgAlert.setPositiveButton("Ok",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int which) {

                                    }
                                });
                    }
                } catch (SecureNfcException e1) {

                    e1.printStackTrace();
                    status.append(newline + newline + newline
                            + "SecureNfcException Exception : "
                            + e1.getResult().getDescription());

                } catch (MifareSamException e) {

                    e.printStackTrace();

                    setButtonEnabled(false);

                    status.append(newline + newline + "MifareSam Exception : "
                            + e.getResult().getDescription());
                    statusTV.setText(status);
                }

            }

        }
    }

    @Override
    public void onClosed() {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                statusTV.setText("EMDK abruptly closed!");
            }
        });

    }

    public void onNewIntent(Intent intent) {
        Log.i(TAG, "Intent called");
        if (intent != null)
            tagDetection(intent);
    }

    private void tagDetection(Intent intent) {

        Log.d(TAG, "Tag Detected");

        if (toast != null)
            toast.cancel();

        tagDataTV.setText(" ");
        tagData = new StringBuilder(" ");

        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())
                || NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())
                || NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())) {

            lTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

            if (lTag != null) {
                TagTechType tagType;
                try {
                    tagType = secureNfcMgr.getTagTechType(lTag);

                    if (tagType.equals(TagTechType.MIFARE_DESFIRE)) {

                        mMifareDesfire = (MifareDesfire) secureNfcMgr
                                .getTagTechInstance(tagType);

                        tagOperation = new MifareDesfireTagOperation(
                                mMifareDesfire);

                        try {
                            if (!mMifareDesfire.isEnabled()) {

                                mMifareDesfire.enable(lTag);
                                tagData.append("\nMifare Desfire tag is enabled.");
                                tagDataTV.setText(tagData);

                            } else {
                                tagData.append("\nMifare Desfire tag is already enabled.");
                                tagDataTV.setText(tagData);

                            }
                        } catch (MifareDesfireExpection e) {
                            e.printStackTrace();
                        }

                    } else if (tagType.equals(TagTechType.MIFARE_PLUS_SL3)) {

                        mMifarePlusSl3 = (MifarePlusSL3) secureNfcMgr
                                .getTagTechInstance(tagType);

                        tagOperation = new MifarePlusSl3TagOperation(
                                mMifarePlusSl3);

                        try {

                            if (!mMifarePlusSl3.isEnabled()) {

                                mMifarePlusSl3.enable(lTag);
                                tagData.append("\nMifare PlusSl3 tag is enabled.");
                                tagDataTV.setText(tagData);

                            } else {
                                tagData.append("\nMifare PlusSl3 tag is already enabled.");
                                tagDataTV.setText(tagData);

                            }
                        } catch (MifarePlusSL3Exception e) {

                            e.printStackTrace();
                        }

                    }
                } catch (SecureNfcException e) {

                    e.printStackTrace();
                }

                operationToBeExecuted();

            }

        }

    }

    private void decBtnClickListener(View dec) {

        dec.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                mgr.hideSoftInputFromWindow(valueeditText.getWindowToken(), 0);
                toast = Toast.makeText(getApplicationContext(),
                        "Please bring the tag closer to the device",
                        Toast.LENGTH_SHORT);
                toast.show();

                operation = Operations.DECREMENT;
                tagDataTV.setText(" ");
                tagData = new StringBuilder(" ");
                tagData.append( "\nDecrement operation is selected");
                tagDataTV.setText(tagData);
            }
        });

    }

    private void incBtnClickListener(View inc) {

        inc.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                mgr.hideSoftInputFromWindow(valueeditText.getWindowToken(), 0);
                toast = Toast.makeText(getApplicationContext(),
                        "Please bring the tag closer to the device",
                        Toast.LENGTH_SHORT);
                toast.show();

                operation = Operations.INCREMENT;
                tagDataTV.setText(" ");
                tagData = new StringBuilder(" ");
                tagData.append( "\nIncrement operation is selected");
                tagDataTV.setText(tagData);

            }
        });

    }

    private void writeBtnClickListener(View write) {
        write.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                mgr.hideSoftInputFromWindow(dataeditText.getWindowToken(), 0);

                toast = Toast.makeText(getApplicationContext(),
                        "Please bring the tag closer to the device",
                        Toast.LENGTH_SHORT);
                toast.show();
                operation = Operations.WRITE;
                tagDataTV.setText(" ");
                tagData = new StringBuilder(" ");
                tagData.append(
                        "\nWrite operation is selected");
                tagDataTV.setText(tagData);
            }
        });

    }

    private void readBtnClickListener(View read) {

        // String readData;
        read.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                mgr.hideSoftInputFromWindow(dataeditText.getWindowToken(), 0);

                toast = Toast.makeText(getApplicationContext(),
                        "Please bring the tag closer to the device",
                        Toast.LENGTH_SHORT);
                toast.show();
                operation = Operations.READ;
                tagDataTV.setText(" ");
                tagData = new StringBuilder(" ");
                tagData.append( "\nRead operation is selected");
                tagDataTV.setText(tagData);
            }
        });
    }

    private void operationToBeExecuted() {

        int opCode = operation.ordinal();
        switch (opCode) {

            case 0:

                tagData.append( "\n\nRead operation is selected");
                mProgressDialog.show();
                new Thread() {

                    @Override
                    public void run() {

                        data = tagOperation.read();
                        handler.sendEmptyMessage(0);
                    }
                }.start();
                break;

            case 1:
                tagData.append(
                        "\n\nWrite operation is selected");
                dataToBeWritten = ((EditText) findViewById(R.id.editText1))
                        .getText().toString();

                if (!dataToBeWritten.equals("")) {

                    mProgressDialog.show();
                    new Thread() {

                        @Override
                        public void run() {

                            data = tagOperation.write(dataToBeWritten);
                            handler.sendEmptyMessage(0);
                        }
                    }.start();

                } else {

                    tagData.append(newline + newline + "Please enter the data");
                    tagDataTV.setText(tagData);

                }

                break;

            case 2:
                tagData.append( "\n\nIncrement operation is selected");
                value = ((EditText) findViewById(R.id.editText2)).getText()
                        .toString();

                if (!value.equals("")) {

                    mProgressDialog.show();

                    new Thread() {

                        @Override
                        public void run() {

                            data = tagOperation.increment(Integer.valueOf(value));
                            handler.sendEmptyMessage(1);

                        }
                    }.start();

                } else {

                    tagData.append(newline + newline + "Please enter the value");
                    tagDataTV.setText(tagData);
                }

                break;
            case 3:
                tagData.append( "\n\nDecrement operation is selected");
                value = ((EditText) findViewById(R.id.editText2)).getText()
                        .toString();

                if (!value.equals("")) {

                    mProgressDialog.show();

                    new Thread() {

                        @Override
                        public void run() {

                            data = tagOperation.decrement(Integer.valueOf(value));
                            handler.sendEmptyMessage(2);
                        }
                    }.start();

                } else {
                    tagData.append(newline + newline + "Please enter the value");
                    tagDataTV.setText(tagData);
                }

                break;
            default:
                Toast.makeText(getApplicationContext(),
                        "No operation is selected.", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private void setButtonEnabled(boolean enable) {

        read.setEnabled(enable);
        write.setEnabled(enable);
        inc.setEnabled(enable);
        dec.setEnabled(enable);

    }

    private void updateData(String result) {
        operation = Operations.UNDEFINED;

        mProgressDialog.dismiss();
        dataeditText.setText("");
        dataeditText.setHint("Enter Data");
        if (result != null) {
            tagData.append(newline + newline + result);
            tagDataTV.setText(tagData);

        } else if (exception != null) {
            tagData.append(newline + newline + exception);
            tagDataTV.setText(tagData);
        }
    }

    private void updateDecrement(String result) {
        operation = Operations.UNDEFINED;

        mProgressDialog.dismiss();
        valueeditText.setText("");
        valueeditText.setHint("Enter Value");

        if (result != null) {
            tagData.append(newline + newline + result);
            tagDataTV.setText(tagData);

        } else {

            tagData.append(newline + newline + "Data before decrement : $"
                    + valueDataBefore);

            tagData.append(newline + newline + "Data after decrement with $"
                    + value + " : $" + valueDataAfter);
            tagDataTV.setText(tagData);

        }
    }

    private void updateIncrement(String result) {
        operation = Operations.UNDEFINED;

        mProgressDialog.dismiss();

        valueeditText.setText("");
        valueeditText.setHint("Enter Value");

        if (result != null) {
            tagData.append(newline + newline + result);
            tagDataTV.setText(tagData);
        } else {
            tagData.append(newline + newline + "Data before increment : $"
                    + valueDataBefore);

            tagData.append(newline + newline + "Data after increment with $"
                    + value + " : $" + valueDataAfter);
            tagDataTV.setText(tagData);
        }
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    mProgressDialog.dismiss();
                    updateData(data);
                    break;
                case 1:
                    mProgressDialog.dismiss();
                    updateIncrement(data);
                    break;

                case 2:
                    mProgressDialog.dismiss();
                    updateDecrement(data);
                    break;
            }

        }
    };
}
