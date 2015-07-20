package com.jaymoh.restaurant;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class RegisterGcmHotel extends Activity{
	
	String gcm_hotelid;
	String gcm_pword;
	String gcm_regid;
	String gcm_uname;
	
	int success;
	static final String TAG_SUCCESS="success";
	static final String TAG_MESSAGE="message";
	
	static final String SERVER_URL="http://hackinrom.co.nf/gcm/register_users.php";
	JSONParser jParser=new JSONParser();
	private ProgressDialog pDialog;
	
	GoogleCloudMessaging gcm;
	//google project number
	static final String PROJECT_NUMBER="287222699434";
	
	protected void onCreate(Bundle savedInstanceState)
	{
	super.onCreate(savedInstanceState);
	
	SharedPreferences prefs=PreferenceManager.getDefaultSharedPreferences(RegisterGcmHotel.this);
	gcm_hotelid=prefs.getString("gcm_hotelid", "");
	gcm_regid=prefs.getString("gcm_regid", "");
	gcm_uname=prefs.getString("uname", "");
	
	if(gcm_regid.equalsIgnoreCase(""))
	{
		//try to register
		getRegId();
	}
	else
	{
		//send hotel id to server
		new SetGCMids().execute();
	}
	}
	
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
					gcm_regid=gcm.register(PROJECT_NUMBER);
					msg="Device registered successful:: "+gcm_regid;
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
				if(gcm_regid!=null)
				{
					SharedPreferences prefs=PreferenceManager.getDefaultSharedPreferences(RegisterGcmHotel.this);
					Editor edit=prefs.edit();
					edit.putString("gcm_regid", gcm_regid);
					edit.apply();
					new SetGCMids().execute();
					
				}
				else
					Toast.makeText(RegisterGcmHotel.this,
							"an error occured while registering, please try again", Toast.LENGTH_LONG).show();
				
			}
		}.execute(null,null,null);
	}
	//inner class to set gcm ids
	public class SetGCMids extends AsyncTask<String, String, String>
	{
		@Override
		protected void onPreExecute()
		{
			super.onPreExecute();
			pDialog=new ProgressDialog(RegisterGcmHotel.this);
			pDialog.setMessage("Registering for notification service...");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(true);
			pDialog.show();
		}

		@Override
		protected String doInBackground(String... arg0) {
			try
			{
			List<NameValuePair>params=new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("user_id", gcm_hotelid));
			params.add(new BasicNameValuePair("gcm_regid", gcm_regid));
			params.add(new BasicNameValuePair("email", gcm_uname));
			params.add(new BasicNameValuePair("pword", "hackinroms"));
			
			Log.d("Request", "starting");
			JSONObject json=jParser.makeHttpRequest(SERVER_URL, "POST", params);
			Log.d("Register attempt", json.toString());
			success=json.getInt(TAG_SUCCESS);
			if(success==1)
			{
				Log.d("Registered successfully", json.toString());
				//
				SharedPreferences sp=PreferenceManager.getDefaultSharedPreferences(RegisterGcmHotel.this);
				Editor edit=sp.edit();
				edit.putString("hotel_registered", "hackinroms");
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
			Intent i=new Intent(RegisterGcmHotel.this,MealSetter.class);
			finish();
			startActivity(i);
		}
		
	}

}
