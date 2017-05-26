/*
* Copyright (C) 2015-2016 Symbol Technologies LLC
* All rights reserved.
*/
package com.symbol.securenfcsample1;

import com.symbol.emdk.securenfc.MifarePlusSL3;
import com.symbol.emdk.securenfc.MifarePlusSL3Exception;
import com.symbol.emdk.securenfc.SamKey;

/**
 * Provides access to Mifare Plus tag in security level 3 and I/O operations
 * using the Secure NFC EMDK APIs.
 */

public class MifarePlusSl3TagOperation extends TagOperation {

    MifarePlusSL3 mifarePlusSl3;

    SamKey lSamKeyForValueBlock;

    SamKey lSamKeyForDataBlock;

    MifarePlusSl3TagOperation(MifarePlusSL3 mifarePlusSl3) {

        this.mifarePlusSl3 = mifarePlusSl3;

        lSamKeyForValueBlock = new SamKey();
        lSamKeyForValueBlock.keyNum = 0x10;
        lSamKeyForValueBlock.keyVer = 0x00;

        lSamKeyForDataBlock = new SamKey();
        lSamKeyForDataBlock.keyNum = 0x12;
        lSamKeyForDataBlock.keyVer = 0x00;

    }

    @Override
    public String read() {

        String readData = null;

        try {

            // all the 3 blocks are on the same sector
            mifarePlusSl3.firstAuthentication(
                    MifareTagConstants.KEY_A_DATA_BLOCK, lSamKeyForDataBlock,
                    null, null);

            byte[] rawData = mifarePlusSl3.readBlock(true, true, true,
                    MifareTagConstants.DATA_BLOCK_1, (byte) 3);

            if (rawData != null)
                readData = new String(rawData);

        } catch (final MifarePlusSL3Exception e) {

            e.printStackTrace();
            MainActivity.exception = (e.getResult().getDescription());
        }

        return readData;
    }

    @Override
    public String write(String dataToBeWritten) {

        String readData = null;

        try {

            // all the 3 blocks are on the same sector

            mifarePlusSl3.firstAuthentication(
                    MifareTagConstants.KEY_A_DATA_BLOCK, lSamKeyForDataBlock,
                    null, null);

            mifarePlusSl3
                    .writeBlock(true, true, MifareTagConstants.DATA_BLOCK_1,
                            dataToBeWritten.getBytes());

            readData = "Write operation is executed Successfully";

        } catch (final MifarePlusSL3Exception e) {

            e.printStackTrace();
            MainActivity.exception = (e.getResult().getDescription());
        }

        return readData;

    }

    @Override
    public String increment(int value) {

        String exception = null;

        try {

            mifarePlusSl3.firstAuthentication(
                    MifareTagConstants.KEY_A_VALUE_BLOCK, lSamKeyForValueBlock,
                    null, null);

            MainActivity.valueDataBefore = mifarePlusSl3.readValue(false, true,
                    true, MifareTagConstants.VALUE_BLOCK_NO);

            mifarePlusSl3.increment(true, MifareTagConstants.VALUE_BLOCK_NO,
                    value);

            mifarePlusSl3.transfer(true, MifareTagConstants.VALUE_BLOCK_NO);

            MainActivity.valueDataAfter = mifarePlusSl3.readValue(false, true,
                    true, MifareTagConstants.VALUE_BLOCK_NO);

        } catch (MifarePlusSL3Exception e) {

            e.printStackTrace();
            exception = (e.getResult().getDescription());
        }
        return exception;
    }

    @Override
    public String decrement(int value) {

        String exception = null;

        try {

            mifarePlusSl3.firstAuthentication(
                    MifareTagConstants.KEY_A_VALUE_BLOCK, lSamKeyForValueBlock,
                    null, null);

            MainActivity.valueDataBefore = mifarePlusSl3.readValue(false, true,
                    true, MifareTagConstants.VALUE_BLOCK_NO);

            mifarePlusSl3.decrement(true, MifareTagConstants.VALUE_BLOCK_NO,
                    value);

            mifarePlusSl3.transfer(true, MifareTagConstants.VALUE_BLOCK_NO);

            MainActivity.valueDataAfter = mifarePlusSl3.readValue(false, true,
                    true, MifareTagConstants.VALUE_BLOCK_NO);

        } catch (MifarePlusSL3Exception e) {

            e.printStackTrace();
            exception = (e.getResult().getDescription());
        }
        return exception;
    }

}
