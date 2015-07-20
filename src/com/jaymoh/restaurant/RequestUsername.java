package com.jaymoh.restaurant;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class RequestUsername extends MainActivity{

	EditText FIRSTNAME;
	EditText LASTNAME;
	EditText RESTAURANTNAME;
	EditText LOCATION;
	EditText SUGGESTUNAME;
	Button requstBtn;
	
	String fname;
	String lname;
	String rname;
	String location;
	String sname;
	
	String details;
	boolean isSentPending;
	
	private static final String ACTION_SENT="com.jaymoh.restaurant.SENT";
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.request_username);
		super.onCreateDrawer();
		FIRSTNAME=(EditText)findViewById(R.id.fname);
		LASTNAME=(EditText)findViewById(R.id.lname);
		RESTAURANTNAME=(EditText)findViewById(R.id.rname);
		LOCATION=(EditText)findViewById(R.id.location);
		SUGGESTUNAME=(EditText)findViewById(R.id.suggestUname);
		requstBtn=(Button)findViewById(R.id.btnRequest);
		requstBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				fname=FIRSTNAME.getText().toString();
				lname=LASTNAME.getText().toString();
				rname=RESTAURANTNAME.getText().toString();
				location=LOCATION.getText().toString();
				sname=SUGGESTUNAME.getText().toString();
				
				if(fname.isEmpty()||fname.contains(" "))
				{
					Toast.makeText(RequestUsername.this, "Please enter firstname appropriately", Toast.LENGTH_SHORT).show();
				}
				else if(lname.isEmpty()||lname.contains(" "))
				{
					Toast.makeText(RequestUsername.this, "Please enter lastname appropriately", Toast.LENGTH_SHORT).show();
				}
				else if(rname.isEmpty())
				{
					Toast.makeText(RequestUsername.this, "Please enter restaurant name", Toast.LENGTH_SHORT).show();
				}
				else if(location.isEmpty())
				{
					Toast.makeText(RequestUsername.this, "Please enter location", Toast.LENGTH_SHORT).show();
				}
				else if(sname.isEmpty()||sname.contains(" "))
				{
					Toast.makeText(RequestUsername.this, "Please enter username appropriately", Toast.LENGTH_SHORT).show();
				}
				else
				{
					details="F:"+fname+" L:"+lname+" R:"+rname+" L:"+location+" SN:"+sname;
					//send sms
					sendSMS(details);
				}
			}
		});
	}
	private void sendSMS(String msg)
	{
		String toHackins="+254701437909";
		PendingIntent sentIntent=PendingIntent.getBroadcast(this, 0, new Intent(ACTION_SENT), 0);
		registerReceiver(sent, new IntentFilter(ACTION_SENT));
		isSentPending=true;
		
		SmsManager manager=SmsManager.getDefault();
		manager.sendTextMessage(toHackins, null, msg, sentIntent, null);
	}
	private BroadcastReceiver sent=new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			switch(getResultCode())
			{
			case Activity.RESULT_OK:
				Toast.makeText(RequestUsername.this, 
						"Request sent successfully, wait for confirmation", Toast.LENGTH_LONG).show();
				//update shared preferences so that the user never requests twice
				SharedPreferences sp=PreferenceManager.getDefaultSharedPreferences(RequestUsername.this);
				Editor edit=sp.edit();
				edit.putString("requested", "Yes");
				edit.apply();
				//sent user to home
				Intent ii=new Intent(RequestUsername.this, Restaurant.class);
				finish();
				startActivity(ii);
				break;
			case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
			case SmsManager.RESULT_ERROR_NO_SERVICE:
			case SmsManager.RESULT_ERROR_NULL_PDU:
			case SmsManager.RESULT_ERROR_RADIO_OFF:
				Toast.makeText(RequestUsername.this,
						"Error sending, please try again", Toast.LENGTH_SHORT).show();
				break;		
			}
			unregisterReceiver(this);
			isSentPending=false;
		}
	};
}
