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

public class RestaurantName extends MainActivity {
	
	private static final String RESTAURANT_NAME_URL=
			"http://hackinrom.co.nf/android/hotel_name.php";
	
	//json tags
	private static final String TAG_SUCCESS="success";
	private static final String TAG_MESSAGE = "message";
			
	ProgressDialog pDialog=null;
	JSONParser jsonParser=new  JSONParser();	
	
	private EditText HotelName;
	private EditText HotelLocation;
	private Button submitBtn;
	
	String HName;
	String HLocation;
	String USERNAME;
	
	int success;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.restaurant_name);
		
		super.onCreateDrawer();
		getActionBar().setDisplayHomeAsUpEnabled(true);
		Bundle extras=getIntent().getExtras();
		USERNAME=extras.getString("uname");
		
		HotelName=(EditText)findViewById(R.id.hotel_name);
		HotelLocation=(EditText)findViewById(R.id.hotel_location);
		submitBtn=(Button)findViewById(R.id.btnSubmitHotel);
		submitBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				HName=HotelName.getText().toString();
				HLocation=HotelLocation.getText().toString();
				//validation
				if(HName.isEmpty())
				{
					Toast.makeText(RestaurantName.this, 
							"Please enter hotel name", Toast.LENGTH_SHORT).show();
				}
				else if(HLocation.isEmpty())
				{
					Toast.makeText(RestaurantName.this, 
							"Please enter hotel location", Toast.LENGTH_SHORT).show();
				}
				else
				{
					AlertDialog.Builder builder=
							new AlertDialog.Builder(RestaurantName.this);
					builder.setTitle("Confirmation");
					builder.setMessage("Are you sure you want to add this restaurant details?");
					//okay button
					builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// load inner class
							//check for internet connectivity
							networkCheck();
							//new AddRestaurantName().execute();
						}
					});
					builder.setNegativeButton("Cancel", null);
					builder.show();
				}
			}
		});
				}
			
	public class AddRestaurantName extends AsyncTask<String, String, String>
	{
		@Override
		protected void onPreExecute()
		{
			super.onPreExecute();
			pDialog=new ProgressDialog(RestaurantName.this);
			pDialog.setMessage("Adding "+ HName+"...");
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
				params.add(new BasicNameValuePair("uname", USERNAME));
				params.add(new BasicNameValuePair("hname", HName));
				params.add(new BasicNameValuePair("location", HLocation));
				
				Log.d("request!", "starting");
				JSONObject json=jsonParser.makeHttpRequest(RESTAURANT_NAME_URL, "POST", params);
				
				Log.d("Update attempt", json.toString());
				
				//confirm success tag
				success=json.getInt(TAG_SUCCESS);
				if(success==1)
				{
					Log.d("Details added successfully", json.toString());
					Intent i=new Intent(RestaurantName.this, MealSetter.class);
					i.putExtra("uname", USERNAME);
					startActivity(i);
					return json.getString(TAG_MESSAGE);
				}
				else
				{
					Log.d("Failure!", json.getString(TAG_MESSAGE));
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
			if(result!=null)
			{
				Toast.makeText(RestaurantName.this, result, Toast.LENGTH_SHORT).show();
			}
		}
	}
	//internet connectivity checker
			public void networkCheck()
			{
				if(!niggahHasActiveConnection())
				{
					new AlertDialog.Builder(RestaurantName.this).setIcon(android.R.drawable.ic_dialog_alert)
					.setTitle("Closing the App")
					.setMessage("No Internet connection, check your settings")
					.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							RestaurantName.this.startActivity(new Intent
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
						new AddRestaurantName().execute();

				}
			}

}
