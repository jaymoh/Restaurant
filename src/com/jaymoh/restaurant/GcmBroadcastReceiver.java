package com.jaymoh.restaurant;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

public class GcmBroadcastReceiver extends WakefulBroadcastReceiver {
	
	@Override
	public void onReceive(Context context, Intent intent)
	{
		//specifies that the class with the below name will handle the message
		ComponentName comp=new ComponentName(context.getPackageName(),
				GcmMessageHandler.class.getName());
		
		//starts the service to keep device awake
		startWakefulService(context, (intent.setComponent(comp)));
		setResultCode(Activity.RESULT_OK);
	}

}
