/*
* Copyright (C) 2015-2017 Zebra Technologies Corp
* All rights reserved.
*/
package com.symbol.securenfcsample1;

public abstract class TagOperation {

    abstract public String read();
    abstract public String write (String dataTobeWriiten);
    abstract public String increment(int value);
    abstract public String decrement(int value);

}
