package com.jaymoh.restaurant;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class RegisterGcm extends Activity {
	
	//registration file
	static final String SERVER_URL="http://hackinrom.co.nf/gcm/register_users.php";
	
	//google project number
	static final String PROJECT_NUMBER="287222699434";
	
	static final String TAG_SUCCESS="success";
	static final String TAG_MESSAGE="message";
	String regId;
	GoogleCloudMessaging gcm;
	
	JSONParser jParser=new JSONParser();
	int success;
	//for registration attempts
	private static final int MAX_ATTEMPTS=5;
	private static final int BACKOFF_MILLI_SECONDS=2000;
	private static final Random random=new Random();
	
	static final String DISPLAY_MESSAGE_ACTION=
			 "com.jaymoh.restaurant.DISPLAY_MESSAGE";
	static final String EXTRA_MESSAGE="message";

	//log messages tag
	static final String TAG="Restaurant...";
	
	private ProgressDialog pDialog;
	EditText USERNAME;
	EditText EMAIL;
	EditText PASSWORD;
	
	Button btnRegister;
	
	String uname;
	String email;
	String pword;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.register_gcm);
		
		USERNAME=(EditText)findViewById(R.id.user_name);
		EMAIL=(EditText)findViewById(R.id.email);
		PASSWORD=(EditText)findViewById(R.id.pass_word);
		btnRegister=(Button)findViewById(R.id.btnReg);
		
		btnRegister.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				uname=USERNAME.getText().toString().trim();
				email=EMAIL.getText().toString();
				pword=PASSWORD.getText().toString();
				if(uname.isEmpty())
				{
					Toast.makeText(RegisterGcm.this, "enter username", Toast.LENGTH_SHORT).show();
				}
				else if(email.isEmpty())
				{
					Toast.makeText(RegisterGcm.this, "enter email", Toast.LENGTH_SHORT).show();
				}
				else if(pword.isEmpty())
				{
					Toast.makeText(RegisterGcm.this, "enter password", Toast.LENGTH_SHORT).show();
				}
				else
				{
					//getRegId();
					//perform network check here
					networkCheck();
				}
			}
		});
	}
	
	//method to bring load gcm reg id
	public void getRegId()
	{
		new AsyncTask<Void, Void, String>()
		{
			@Override protected String doInBackground(Void... params)
			{
				String msg="";
				try
				{
					if(gcm==null)
					{
						gcm=GoogleCloudMessaging.getInstance(getApplicationContext());
					}
					regId=gcm.register(PROJECT_NUMBER);
					msg="Device registered successful:: "+regId;
					Log.i("GCM", msg);
				}
				catch(IOException e)
				{
					msg="Error :"+e.getMessage();
				}
				return msg;
			}
			@Override
			protected void onPostExecute(String msg)
			{
				//call inner class to send details to server
				if(regId!=null)
					new SetGCMids().execute();
				else
					Toast.makeText(RegisterGcm.this,
							"an error occured while registering, please try again", Toast.LENGTH_LONG).show();
				
			}
		}.execute(null,null,null);
	}

	public class SetGCMids extends AsyncTask<String, String, String>
	{
		@Override
		protected void onPreExecute()
		{
			super.onPreExecute();
			pDialog=new ProgressDialog(RegisterGcm.this);
			pDialog.setMessage("Registering for notification service...");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(true);
			pDialog.show();
		}

		@Override
		protected String doInBackground(String... arg0) 
		{
			try
			{
				List<NameValuePair>params=new ArrayList<NameValuePair>();
				params.add(new BasicNameValuePair("user_id", uname));
				params.add(new BasicNameValuePair("gcm_regid", regId));
				params.add(new BasicNameValuePair("email", email));
				params.add(new BasicNameValuePair("pword", pword));
				
				Log.d("request!","starting");
				JSONObject json=jParser.makeHttpRequest(SERVER_URL, "POST", params);
				
				Log.d("Register attempt", json.toString());
				success=json.getInt(TAG_SUCCESS);
				if(success==1)
				{
					Log.d("Registered successfully", json.toString());
					//save username and password to preferences
					SharedPreferences sp=PreferenceManager.getDefaultSharedPreferences(RegisterGcm.this);
					Editor edit=sp.edit();
					edit.putString("gcm_uname", uname);
					edit.putString("gcm_pword", pword);
					edit.putString("gcm_regid", regId);
					edit.apply();
					
					return json.getString(TAG_MESSAGE);
				}
				else
				{
					Log.d("Failure registering", json.getString(TAG_MESSAGE));
					return json.getString(TAG_MESSAGE);
				}
			}
			catch(JSONException e)
			{
				e.printStackTrace();
			}
			return null;
		}
		@Override
		protected void onPostExecute(String result)
		{
			super.onPostExecute(result);
			pDialog.dismiss();
			Intent i=new Intent(RegisterGcm.this, Restaurant.class);
			finish();
			startActivity(i);
			//decide where to send the user
		}
		
	}
	
		//to perform network checks
		public void networkCheck()
		{
			if(!niggahHasActiveConnection())
			{
				new AlertDialog.Builder(RegisterGcm.this).setIcon(android.R.drawable.ic_dialog_alert)
				.setTitle("Closing the App")
				.setMessage("No Internet connection, check your settings")
				.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						RegisterGcm.this.startActivity(new Intent
								(android.provider.Settings.ACTION_WIRELESS_SETTINGS));
						
					}
				})
				.setNegativeButton("Close", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						finish();
						
					}
				})
				.show();
			}
			else
			{
					getRegId();
			}
		}
		
	//Internet connectivity status check 
	public boolean niggahHasActiveConnection()
	{
		ConnectivityManager conn=(ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo=conn.getActiveNetworkInfo();
		if(netInfo==null||!netInfo.isConnected()||!netInfo.isAvailable())
		{
			return false;
		}
		return true;
	}
	
}
