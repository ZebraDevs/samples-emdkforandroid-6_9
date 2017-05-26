/*
* Copyright (C) 2015-2016 Symbol Technologies LLC
* All rights reserved.
*/
package com.symbol.securenfcsample1;

import com.symbol.emdk.securenfc.MifareDesfire;
import com.symbol.emdk.securenfc.MifareDesfireExpection;
import com.symbol.emdk.securenfc.MifareDesfireResults;
import com.symbol.emdk.securenfc.SamKey;
import com.symbol.emdk.securenfc.MifareDesfire.AuthenticateType;
import com.symbol.emdk.securenfc.MifareDesfire.CreditType;
import com.symbol.emdk.securenfc.MifareDesfire.FileCommMode;
import com.symbol.emdk.securenfc.MifareDesfire.FileSettings;

/**
 * Provides access to Mifare Desfire tag and I/O operations
 * using the Secure NFC EMDK APIs.
 */

public class MifareDesfireTagOperation extends TagOperation {

    MifareDesfire mifareDesfire;
    SamKey lSamKeyForReadWrite;
    public MifareDesfireTagOperation(MifareDesfire mifareDesfire) {
        this.mifareDesfire = mifareDesfire;
        lSamKeyForReadWrite = new SamKey();
        lSamKeyForReadWrite.keyNum = 0x05;
        lSamKeyForReadWrite.keyVer = 0x00;

    }

    @Override
    public String read() {

        String readData = null;

        try {

            mifareDesfire.selectApplication(MifareTagConstants.APP_ID);

            // Authenticating with Read&Write access key.If we authenticate
            // once with read&write access key this authentication
            // is valid for all the subsequent operations unless exception
            // has occured or disable() call.

            mifareDesfire.authenticate(AuthenticateType.NATIVE,
                    MifareTagConstants.CARD_KEY_FOR_READ_WRITE,
                    lSamKeyForReadWrite, null);

            // Note : ReadData API supports only payload of length less than or equal to 200 bytes

            byte[] rawData = mifareDesfire
                    .readData(
                            MifareTagConstants.STD_FILE_ID,
                            getCommunicationType(MifareTagConstants.STD_FILE_ID),
                            0, 100);

            if (rawData != null)
                readData = new String(rawData);

        } catch (final MifareDesfireExpection e) {

            e.printStackTrace();
            MainActivity.exception = (e.getResult().getDescription());
        }

        return readData;

    }

    @Override
    public String write(String dataToBeWritten) {

        String readData = null;

        try {

            mifareDesfire.selectApplication(MifareTagConstants.APP_ID);

            // Authenticating with Read&Write access key.If we authenticate
            // once with read&write access key this authentication
            // is valid for all the subsequent operations unless exception
            // has occured or disable() call.

            mifareDesfire.authenticate(AuthenticateType.NATIVE,
                    MifareTagConstants.CARD_KEY_FOR_READ_WRITE,
                    lSamKeyForReadWrite, null);

            // Here it is data is always written from the 0 offset, if user
            // wants to write in different offset then 3rd parameter has to
            // be changed in the writeData API.

            // Note : writeData API supports only payload of length less
            // than or equal to 200 bytes

            mifareDesfire.writeData(MifareTagConstants.STD_FILE_ID,
                    getCommunicationType(MifareTagConstants.STD_FILE_ID),
                    0, dataToBeWritten.getBytes());

            readData = "Write operation is executed Successfully";

        } catch (final MifareDesfireExpection e) {

            e.printStackTrace();

            MainActivity.exception = (e.getResult().getDescription());
        }

        return readData;

    }



    @Override
    public String increment(int value) {

        String exception = null;

        try {

            mifareDesfire.selectApplication(MifareTagConstants.APP_ID);

            // Authenticating with Read&Write access key.If we authenticate
            // once with read&write access key this authentication
            // is valid for all the subsequent operations unless exception
            // has occured or disable() call.

            mifareDesfire.authenticate(AuthenticateType.NATIVE,
                    MifareTagConstants.CARD_KEY_FOR_READ_WRITE,
                    lSamKeyForReadWrite, null);

            MainActivity.valueDataBefore = mifareDesfire
                    .getValue(MifareTagConstants.VAL_FILE_ID);

            mifareDesfire.credit(CreditType.STANDARD,
                    MifareTagConstants.VAL_FILE_ID,
                    getCommunicationType(MifareTagConstants.VAL_FILE_ID),
                    value);

            mifareDesfire.commitTransaction();

            MainActivity.valueDataAfter = mifareDesfire
                    .getValue(MifareTagConstants.VAL_FILE_ID);

        } catch (final MifareDesfireExpection e) {

            e.printStackTrace();
            exception = (e.getResult().getDescription());
        }
        return exception;


    }

    @Override
    public String decrement(int value) {

        String exception = null;

        try {

            mifareDesfire.selectApplication(MifareTagConstants.APP_ID);

            // Authenticating with Read&Write access key.If we authenticate
            // once with read&write access key this authentication
            // is valid for all the subsequent operations unless exception
            // has occured or disable() call.

            mifareDesfire.authenticate(AuthenticateType.NATIVE,
                    MifareTagConstants.CARD_KEY_FOR_READ_WRITE,
                    lSamKeyForReadWrite, null);

            MainActivity.valueDataBefore = mifareDesfire
                    .getValue(MifareTagConstants.VAL_FILE_ID);

            mifareDesfire.debit(MifareTagConstants.VAL_FILE_ID,
                    getCommunicationType(MifareTagConstants.VAL_FILE_ID),
                    Integer.valueOf(value));

            mifareDesfire.commitTransaction();

            MainActivity.valueDataAfter = mifareDesfire
                    .getValue(MifareTagConstants.VAL_FILE_ID);

        } catch (final MifareDesfireExpection e) {

            e.printStackTrace();
            exception = (e.getResult().getDescription());
        }
        return exception;

    }

    private FileCommMode getCommunicationType(int fileId)
            throws MifareDesfireExpection {

        FileSettings lFileSettings = null;
        if (mifareDesfire != null) {

            lFileSettings = mifareDesfire.getFileSettings((byte) fileId);
        }
        return lFileSettings.commMode;
    }
}
