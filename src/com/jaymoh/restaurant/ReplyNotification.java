package com.jaymoh.restaurant;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
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
import android.widget.TextView;
import android.widget.Toast;

public class ReplyNotification extends Activity{
	
	static final String REPLY_URL="http://hackinrom.co.nf/gcm/reply_notification.php";
	static final String TAG_SUCCESS="success";
	static final String TAG_MESSAGE="message";
	int success;
	
	ProgressDialog pDialog;
	JSONParser jParser=new JSONParser();
	
	Long id;
	EditText confirmationTxt;
	Button confirmationBtn;
	
	String PASSWORD;
	String CUSTOMER;
	String MESSAGE;
	String MEALNAME;
	String ReplyMessage;
	
	TextView cusName;
	TextView mealName;
	TextView cusMessage;
	
	String HOTELID;
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.reply_notification);
		
		getActionBar().setDisplayHomeAsUpEnabled(true);
		Bundle extras=getIntent().getExtras();
		
		SharedPreferences sp=PreferenceManager.getDefaultSharedPreferences(ReplyNotification.this);
		HOTELID=sp.getString("gcm_hotelid", "");
		id=extras.getLong("ids");
		cusName=(TextView)findViewById(R.id.replycustomerName);
		mealName=(TextView)findViewById(R.id.replymealName);
		cusMessage=(TextView)findViewById(R.id.replyMessage);
		
		confirmationTxt=(EditText)findViewById(R.id.replyTxt);
		confirmationBtn=(Button)findViewById(R.id.replySubmit);
		confirmationBtn.setEnabled(false);
		confirmationBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				ReplyMessage=confirmationTxt.getText().toString();
				if(ReplyMessage.isEmpty())
				{
					Toast.makeText(ReplyNotification.this, "Please type your reply", Toast.LENGTH_SHORT).show();
					
				}
				else
				{
					//check for connectivity
					networkCheck();
				}
				
			}
		});
	}
	@Override
	protected void onResume()
	{
		super.onResume();
		new LoadDetails().execute(id);
	}
//inner class to get the details
	private class LoadDetails extends AsyncTask<Long, Object, Cursor>
	{
		DatabaseHelper db=new DatabaseHelper(ReplyNotification.this);
		@Override
		protected Cursor doInBackground(Long... params)
		{
			return db.getSingleRecord(params[0]);
		}
		@Override 
		protected void onPostExecute(Cursor result)
		{
			super.onPostExecute(result);
			result.moveToFirst(); //move to the first item
			int mealIndex=result.getColumnIndex("meal_name");
			int customerIndex=result.getColumnIndex("customer");
			int messageIndex=result.getColumnIndex("message");
			int pwordIndex=result.getColumnIndex("password");
			
			PASSWORD=result.getString(pwordIndex);
			CUSTOMER=result.getString(customerIndex);
			MESSAGE=result.getString(messageIndex);
			MEALNAME=result.getString(mealIndex);
			
			//update textviews and enable submit button
			cusName.setText(CUSTOMER);
			mealName.setText(MEALNAME);
			cusMessage.setText(MESSAGE);
			confirmationBtn.setEnabled(true);
			
			//close db connection
			result.close();
			db.close();
		}
	}
	//internet connectivity checker
	public void networkCheck()
	{
		if(!niggahHasActiveConnection())
		{
			new AlertDialog.Builder(getApplicationContext()).setIcon(android.R.drawable.ic_dialog_alert)
			.setTitle("Closing the App")
			.setMessage("No Internet connection, check your settings")
			.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					ReplyNotification.this.startActivity(new Intent
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
			new SendNotification().execute();

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
	//inner class to send notification
	public class SendNotification extends AsyncTask<String, String, String>
	{
		@Override
		protected void onPreExecute()
		{
			super.onPreExecute();
			pDialog=new ProgressDialog(ReplyNotification.this);
			pDialog.setMessage("Sending reply...");
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
				params.add(new BasicNameValuePair("hotel_id", HOTELID));
				params.add(new BasicNameValuePair("meal_name", MEALNAME));
				params.add(new BasicNameValuePair("message", ReplyMessage));
				params.add(new BasicNameValuePair("pword", PASSWORD));
				params.add(new BasicNameValuePair("customer", CUSTOMER));
				
				Log.d("request!", "starting");
				JSONObject json=jParser.makeHttpRequest(REPLY_URL, "POST", params);
				
				Log.d("Reply attempt", json.toString());
				
				success=json.getInt(TAG_SUCCESS);
				if(success==1)
				{
					Log.d("Reply sent", json.toString());
					finish();
					return json.getString(TAG_MESSAGE);
				}
				else
				{
					Log.d("Failure sending reply", json.getString(TAG_MESSAGE));
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
			if(result!=null)
			{
				Toast.makeText(ReplyNotification.this, result, Toast.LENGTH_LONG).show();
			}
		}
	}
}
