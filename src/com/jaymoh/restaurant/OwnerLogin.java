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
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class OwnerLogin extends MainActivity {
	EditText username;
	EditText password;
	Button loginButton;
	
	String USERNAME;
	String PASSWORD;
	
	private ProgressDialog pDialog;
	
	//object for the JSONparser class
	JSONParser jsonParser=new JSONParser();
	
	//login location for php
	private static final String LOGIN_URL="http://hackinrom.co.nf/android/hotel_owner_login.php";
	
	//JSON element ids for response of the php script
			private static final String TAG_SUCCESS="success";
			private static final String TAG_MESSAGE="message";	
			
			@Override
			protected void onCreate(Bundle savedInstanceState)
			{
				super.onCreate(savedInstanceState);
				setContentView(R.layout.owner_login);
				super.onCreateDrawer();
				
				username=(EditText)findViewById(R.id.username);
				password=(EditText)findViewById(R.id.pword);
				loginButton=(Button)findViewById(R.id.btnlogin);
				
				
				loginButton.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						
						USERNAME=username.getText().toString();
						PASSWORD=password.getText().toString();
						if(USERNAME.isEmpty())
						{
							Toast.makeText(OwnerLogin.this, "Please enter your username", Toast.LENGTH_SHORT).show();
						}
						else if(PASSWORD.isEmpty())
						{
							Toast.makeText(OwnerLogin.this, "Please enter your password", Toast.LENGTH_SHORT).show();
						}
						else
						{
							//check connectivity
							//load inner class to perform authentication
							networkCheck();
							//new AttemptLogin().execute();
						}
					}
				});
			}
			public class AttemptLogin extends AsyncTask<String, String, String>
			{
				boolean failure=false;
				
				@Override
				protected void onPreExecute()
				{
					super.onPreExecute();
					pDialog=new ProgressDialog(OwnerLogin.this);
					pDialog.setMessage("Attempting to login "+USERNAME);
					pDialog.setIndeterminate(false);
					pDialog.setCancelable(true);
					pDialog.show();
				}
				@Override
				protected String doInBackground(String... args)
				{
					int success;
					
					try
					{
						List<NameValuePair>params=new ArrayList<NameValuePair>();
						params.add(new BasicNameValuePair("uname", USERNAME));
						params.add(new BasicNameValuePair("pword", PASSWORD));
						
						Log.d("request", "starting");
						
						//perform the login from the servers
						JSONObject json=jsonParser.makeHttpRequest(LOGIN_URL,
								"POST", params);
						
						//show progress on the log cat
						Log.d("Login attempt", json.toString());
						
						//check for login success
						success=json.getInt(TAG_SUCCESS);
						if(success==1)
						{
							Log.d("Login Successful!", json.toString());
							
							//save credentials for next activity
							SharedPreferences sp=PreferenceManager.getDefaultSharedPreferences(OwnerLogin.this);
							Editor edit=sp.edit();
							edit.putString("uname", USERNAME);
							edit.apply();
							
							Intent i=new Intent(OwnerLogin.this, MealSetter.class);
							i.putExtra("uname", USERNAME);
							finish();
							startActivity(i);
							return json.getString(TAG_MESSAGE);
						}
						else
						{
							Log.d("Login failure!", json.getString(TAG_MESSAGE));
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
				protected void onPostExecute(String file_url) {
					super.onPostExecute(file_url);
					//dismiss the progress dialog
					pDialog.dismiss();
					if(file_url!=null)
					{
						Toast.makeText(OwnerLogin.this, file_url,Toast.LENGTH_LONG).show();
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
							OwnerLogin.this.startActivity(new Intent
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
						new AttemptLogin().execute();

				}
			}
			@Override
			public boolean onCreateOptionsMenu(Menu menu)
			{
				MenuInflater inflater=getMenuInflater();
				inflater.inflate(R.menu.owner_menu, menu);
				return true;
			}
			@Override
			public boolean onOptionsItemSelected(MenuItem item)
			{
				switch(item.getItemId())
				{
				case R.id.request_username:
					//confirm if user had requested
					SharedPreferences sp=
					PreferenceManager.getDefaultSharedPreferences(OwnerLogin.this);
					String requested=sp.getString("requested", "");
					if(requested.equalsIgnoreCase(""))
					{
						Intent ii=new Intent(OwnerLogin.this, RequestUsername.class);
						finish();
						startActivity(ii);
					}
					else
					{
						Toast.makeText(OwnerLogin.this, "You already requested for username before", Toast.LENGTH_LONG).show();
					}
					return true;
					
					default:
						return super.onOptionsItemSelected(item);
				}
			}
}
