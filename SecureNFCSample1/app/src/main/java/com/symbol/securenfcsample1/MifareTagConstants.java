/*
* Copyright (C) 2015-2016 Symbol Technologies LLC
* All rights reserved.
*/
package com.symbol.securenfcsample1;

public interface MifareTagConstants {

    // Tag keys for Desfire
    public static final byte CARD_KEY_FOR_READ = 0x01;
    public static final byte CARD_KEY_FOR_WRITE = 0x02;
    public static final byte CARD_KEY_FOR_READ_WRITE = 0x03;
    public static final byte CARD_KEY_FOR_DEBIT_AND_GETVALUE = 0x02;
    public static final byte CARD_KEY_FOR_CREDIT = 0x03;


    // File ID for Desfire
    public static final byte STD_FILE_ID = 1;
    public static final byte VAL_FILE_ID = 2;
    public static final byte RECORD_FILE_ID = 3;

    public static final byte RECORD_SIZE = 20;


    //Application Id for Desfire
    public static final byte APP_ID = 9;


    // Block numbers of keys in MifarePlusSL3 for authentication
    short KEY_A_VALUE_BLOCK = 0x4002;
    short KEY_B_VALUE_BLOCK = 0x4003;
    short KEY_A_DATA_BLOCK = 0x4004;
    short KEY_B_DATA_BLOCK = 0x4005;

    // Block numbers of MifarePlusSL3

    short VALUE_BLOCK_NO = 0x06;

    //Blocks are in same sector
    short DATA_BLOCK_1 = 0x08;
    short DATA_BLOCK_2 = 0x09;
    short DATA_BLOCK_3 = 0x0A;


}
