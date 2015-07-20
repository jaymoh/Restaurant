package com.jaymoh.restaurant;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;

@SuppressLint("NewApi")
public class GcmMessageHandler extends IntentService{
	String mes;
	String mealname;
	String userid;
	String pword;
	int nums=0;
	private Handler handler;
	DatabaseHelper db=new DatabaseHelper(GcmMessageHandler.this);
	NotificationManager notification;
	int notificationId=100;
	public GcmMessageHandler()
	{
		super("GcmMessageHandler");
	}
	
	@Override
	public void onCreate()
	{
		super.onCreate();
		handler=new Handler();
		
	}
	@Override
	protected void onHandleIntent(Intent intent)
	{
		Bundle extras=intent.getExtras();
		
		GoogleCloudMessaging gcm=GoogleCloudMessaging.getInstance(this);
		//get message received in the intent
		String messageType=gcm.getMessageType(intent);
		mes=extras.getString("message");
		mealname=extras.getString("meal_name");
		userid=extras.getString("customer");
		pword=extras.getString("pword");
		
		//call method to save to database
		//confirm if message came from restaurant owner or from customer
		if(pword=="hackinroms")
		{
			db.insertResponse(mealname, userid, mes);
		}
		else
		{
		db.insertMessage(mealname, userid, pword, mes);
		}
		//generate notification
		
		//close intent
		GcmBroadcastReceiver.completeWakefulIntent(intent);
	}
	
public void generateNotification(String Message, String meal)
{
	int icon=R.drawable.restaurant;
	long when=System.currentTimeMillis();
	NotificationCompat.Builder builder=new NotificationCompat.Builder(this);
	builder.setContentTitle(meal);
	builder.setContentText(Message);
	builder.setTicker("New message Alert!");
	builder.setSmallIcon(icon);
	builder.setNumber(++nums);
	builder.setDefaults(Notification.DEFAULT_SOUND);
	builder.setAutoCancel(true);
	
	/*
	//set big box style
	NotificationCompat.InboxStyle inboxStyle=
			new NotificationCompat.InboxStyle();
	String[] events=new String[nums];
	for(int i=0; i<events.length; i++)
	{
		events[i]=new String(meal +": : "+Message);
		inboxStyle.addLine(events[i]);
	}
	builder.setStyle(inboxStyle);
	
	*/
	if(pword=="hackinroms")
	{
	Intent resultIntent=new Intent(this, ReadMessageCustomer.class);
	TaskStackBuilder stack=TaskStackBuilder.create(this);
	stack.addParentStack(ReadMessageCustomer.class);
	stack.addNextIntent(resultIntent);
	PendingIntent resultPendingIntent=
			stack.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
	builder.setContentIntent(resultPendingIntent);
	notification=(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
	notification.notify(notificationId, builder.build());
	}
	else
	{
		Intent resultIntent=new Intent(this, ReadMessageOwner.class);
		TaskStackBuilder stack=TaskStackBuilder.create(this);
		stack.addParentStack(ReadMessageOwner.class);
		stack.addNextIntent(resultIntent);
		PendingIntent resultPendingIntent=
				stack.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
		builder.setContentIntent(resultPendingIntent);
		notification=(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
		notification.notify(notificationId, builder.build());
	}
	
	
}
}
