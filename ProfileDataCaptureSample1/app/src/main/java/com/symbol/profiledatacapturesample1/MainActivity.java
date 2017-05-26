/*
* Copyright (C) 2015-2016 Symbol Technologies LLC
* All rights reserved.
*/
package com.symbol.profiledatacapturesample1;

import com.symbol.emdk.EMDKManager;
import com.symbol.emdk.EMDKManager.EMDKListener;
import com.symbol.emdk.EMDKResults;
import com.symbol.emdk.ProfileConfig;
import com.symbol.emdk.ProfileManager;
import com.symbol.emdk.ProfileConfig.ENABLED_STATE;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

public class MainActivity extends Activity implements EMDKListener {

    // Assign the profile name used in EMDKConfig.xml
    private String profileName = "DataCaptureProfile-1";

    // Declare a variable to store ProfileManager object
    private ProfileManager profileManager = null;

    // Declare a variable to store EMDKManager object
    private EMDKManager emdkManager = null;

    TextView statusTextView = null;
    CheckBox checkBoxCode128 = null;
    CheckBox checkBoxCode39 = null;
    CheckBox checkBoxEAN8 = null;
    CheckBox checkBoxEAN13 = null;
    CheckBox checkBoxUPCA = null;
    CheckBox checkBoxUPCE0 = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusTextView = (TextView) findViewById(R.id.textViewStatus);
        checkBoxCode128 = (CheckBox) findViewById(R.id.checkBoxCode128);
        checkBoxCode39 = (CheckBox) findViewById(R.id.checkBoxCode39);
        checkBoxEAN8 = (CheckBox) findViewById(R.id.checkBoxEAN8);
        checkBoxEAN13 = (CheckBox) findViewById(R.id.checkBoxEAN13);
        checkBoxUPCA = (CheckBox) findViewById(R.id.checkBoxUPCE);
        checkBoxUPCE0 = (CheckBox) findViewById(R.id.checkBoxUPCE0);

        // Set listener to the button
        addSetButtonListener();

        // The EMDKManager object will be created and returned in the callback.
        EMDKResults results = EMDKManager.getEMDKManager(getApplicationContext(), this);

        // Check the return status of processProfile
        if(results.statusCode == EMDKResults.STATUS_CODE.SUCCESS) {
            // EMDKManager object creation success
        }else {
            // EMDKManager object creation failed
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();

        // Clean up the objects created by EMDK manager
        if (profileManager != null)
            profileManager = null;

        if (emdkManager != null) {
            emdkManager.release();
            emdkManager = null;
        }
    }

    @Override
    public void onClosed() {

        // This callback will be issued when the EMDK closes unexpectedly.
        if (emdkManager != null) {
            emdkManager.release();
            emdkManager = null;
        }

        statusTextView.setText("Status: " + "EMDK closed unexpectedly! Please close and restart the application.");
    }

    @Override
    public void onOpened(EMDKManager emdkManager) {

        // This callback will be issued when the EMDK is ready to use.
        statusTextView.setText("EMDK open success. Setting the initial profile...");

        this.emdkManager = emdkManager;

        // Get the ProfileManager object to process the profiles
        profileManager = (ProfileManager) emdkManager.getInstance(EMDKManager.FEATURE_TYPE.PROFILE);

        // Initially set the original profile in Config xml. No extra data.
        new ProcessProfileAsyncTask().execute(new String[1]);
    }


    private void addSetButtonListener()
    {
        Button buttonSet = (Button)findViewById(R.id.buttonSet);
        buttonSet.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                // Call modifyProfile_XMLString() to modify existing profile using XML String.
                modifyProfile_XMLString();
            }
        });
    }

	private void modifyProfile_XMLString()
	{
		statusTextView.setText("");

		// Prepare XML to modify the existing profile
		String[] modifyData = new String[1];
		modifyData[0]=
				"<?xml version=\"1.0\" encoding=\"utf-8\"?>" +
				"<characteristic type=\"Profile\">" +
			        "<characteristic type=\"Barcode\" version=\"6.0\">" +
			            "<characteristic type=\"Decoders\">" +
			                "<parm name=\"decoder_code128\" value=\"" + String.valueOf(checkBoxCode128.isChecked()).toLowerCase() + "\"/>" +
			                "<parm name=\"decoder_code39\" value=\"" + String.valueOf(checkBoxCode39.isChecked()).toLowerCase() + "\"/>" +
			                "<parm name=\"decoder_ean8\" value=\"" + String.valueOf(checkBoxEAN8.isChecked()).toLowerCase() + "\"/>" +
			                "<parm name=\"decoder_ean13\" value=\"" + String.valueOf(checkBoxEAN13.isChecked()).toLowerCase() + "\"/>" +
			                "<parm name=\"decoder_upca\" value=\"" + String.valueOf(checkBoxUPCA.isChecked()).toLowerCase() + "\"/>" +
			                "<parm name=\"decoder_upce0\" value=\"" + String.valueOf(checkBoxUPCE0.isChecked()).toLowerCase() + "\"/>" +
			            "</characteristic>" +
			        "</characteristic>"+
				"</characteristic>";

        // Modify the original config xml profile with UI data
		new ProcessProfileAsyncTask().execute(modifyData[0]);
	}

  	private class ProcessProfileAsyncTask extends AsyncTask<String, Void, EMDKResults> {

		@Override
		protected EMDKResults doInBackground(String... params) {

			// Call processPrfoile with profile name, SET flag and config data to update the profile
			EMDKResults results = profileManager.processProfile(profileName, ProfileManager.PROFILE_FLAG.SET, params);

			return results;
		}

		@Override
		protected void onPostExecute(EMDKResults results) {

			super.onPostExecute(results);

			String resultString;

			//Check the return status of processProfile
			if(results.statusCode == EMDKResults.STATUS_CODE.SUCCESS) {

				resultString = "Profile update success.";

			}else {

				resultString = "Profile update failed.";
			}

			statusTextView.setText(resultString);
		}
	}
}

