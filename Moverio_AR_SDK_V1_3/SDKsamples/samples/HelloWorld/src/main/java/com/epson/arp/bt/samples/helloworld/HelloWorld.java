/**
*
* Copyright (c) SEIKO EPSON CORPORATION 2016 - 2017. All rights reserved.
*
*/
package com.epson.arp.bt.samples.helloworld;

import android.os.Bundle;
import android.util.Log;

import com.epson.arp.bt.MoverioARActivity;
import com.epson.arp.bt.sdk.MoverioARSDK;

public class HelloWorld  extends MoverioARActivity
{
	public static final String TAG = HelloWorld.class.getName();
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		String version = MoverioARSDK.getSdkVersion();
		Log.i(TAG, version);
	}
}

