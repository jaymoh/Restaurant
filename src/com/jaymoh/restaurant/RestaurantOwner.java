package com.jaymoh.restaurant;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class RestaurantOwner extends MainActivity{

	private static final String RESTAURANT_OWNER_URL=
			"http://hackinrom.co.nf/android/hotel_owner.php";
	//json tags
		private static final String TAG_SUCCESS="success";
		private static final String TAG_MESSAGE = "message";
		
		ProgressDialog pDialog=null;
		JSONParser jsonParser=new  JSONParser();
		
		private EditText Firstname;
		private EditText Lastname;
		private EditText Username;
		private EditText Password;
		private Button  Addowner;
		
		String first_name;
		String last_name;
		String user_name;
		String pass_word;
		
		@Override
		protected void onCreate(Bundle savedInstanceState)
		{
			super.onCreate(savedInstanceState);
			setContentView(R.layout.restaurant_owner);
			
			super.onCreateDrawer();
			Firstname=(EditText)findViewById(R.id.first_name);
			Lastname=(EditText)findViewById(R.id.last_name);
			Username=(EditText)findViewById(R.id.user_name);
			Password=(EditText)findViewById(R.id.pass_word);
			Addowner=(Button)findViewById(R.id.addOwner);
			
			Addowner.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					first_name=Firstname.getText().toString();
					last_name=Lastname.getText().toString();
					user_name=Username.getText().toString();
					pass_word=Password.getText().toString();
					
					//validate empty fields
					if(first_name.contains(" ")|| first_name.isEmpty())
					{
						Toast.makeText(RestaurantOwner.this, 
								"Please enter first name appropriately", Toast.LENGTH_SHORT).show();
					}
					else if(last_name.contains(" ")|| last_name.isEmpty())
					{
						Toast.makeText(RestaurantOwner.this, 
								"Please enter last name appropriately", Toast.LENGTH_SHORT).show();
					}
					else if(user_name.contains(" ")|| user_name.isEmpty())
					{
						Toast.makeText(RestaurantOwner.this, 
								"Please enter user name appropriately", Toast.LENGTH_SHORT).show();
					}
					else if(pass_word.isEmpty())
					{
						Toast.makeText(RestaurantOwner.this, 
								"Please set a password", Toast.LENGTH_SHORT).show();
					}
					else
					{
						AlertDialog.Builder builder=
								new AlertDialog.Builder(RestaurantOwner.this);
						builder.setTitle("Confirmation");
						builder.setMessage("Are you sure you want to add this restaurant owner?");
						//okay button
						builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								// load inner class
								//check for internet connectivity
								networkCheck();
								//new AddRestaurantOwner().execute();
							}
						});
						builder.setNegativeButton("Cancel", null);
						builder.show();
					}
				}
			});
		}
		
		public class AddRestaurantOwner extends AsyncTask<String, String, String>
		{
			@Override
			protected void onPreExecute()
			{
				super.onPreExecute();
				pDialog=new ProgressDialog(RestaurantOwner.this);
				pDialog.setMessage("Adding "+user_name);
				pDialog.setIndeterminate(false);
				pDialog.setCancelable(true);
				pDialog.show();
			}
			@Override
			protected String doInBackground(String... arg0)
			{
				int success;
				try
				{
					List<NameValuePair>params=new ArrayList<NameValuePair>();
					params.add(new BasicNameValuePair("fname", first_name));
					params.add(new BasicNameValuePair("lname", last_name));
					params.add(new BasicNameValuePair("uname",user_name));
					params.add(new BasicNameValuePair("pword", pass_word));
					
					Log.d("request!", "starting");
					JSONObject json=jsonParser.makeHttpRequest(RESTAURANT_OWNER_URL, "POST", params);
					
					Log.d("Update attempt", json.toString());
					
					//confirm success in addition
					success=json.getInt(TAG_SUCCESS);
					if(success==1)
					{
						Log.d("Owner added successfully!", json.toString());
						finish();
						return json.getString(TAG_MESSAGE);
					}
					else
					{
						Log.d("Failure adding owner details", json.getString(TAG_MESSAGE));
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
			protected void onPostExecute(String file_url)
			{
				pDialog.dismiss();
				if(file_url!=null)
				{
					Toast.makeText(RestaurantOwner.this, 
							file_url, Toast.LENGTH_SHORT).show();
				}
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
						RestaurantOwner.this.startActivity(new Intent
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
					new AddRestaurantOwner().execute();

			}
		}
}
 