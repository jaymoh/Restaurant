package com.jaymoh.restaurant;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class AddReview extends Activity{

	private static final String TAG_SUCCESS="success";
	private static final String TAG_MESSAGE="message";
	private static final String TAG_HNAME="hname";
	private static final String TAG_HID="hotel_id";
	private static final String TAG_HOTELS="restaurants";
	
	private static final String RESTAURANT_URL="http://hackinrom.co.nf/android/all_hotels.php";
	private static final String ADDREVIEW_URL="http://hackinrom.co.nf/android/add_review.php";
	
	private ProgressDialog pDialog;
	private JSONArray resNames=null;
	List<String>namesList;
	JSONParser jParser=new JSONParser();
	
	Spinner hotelName;
	EditText reviewText;
	Button submitReview;
	TextView infoText;
	
	String username;
	String UserReview;
	String selectedRes;
	int success;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_review);
		
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		hotelName=(Spinner)findViewById(R.id.res_spinner);
		reviewText=(EditText)findViewById(R.id.review);
		submitReview=(Button)findViewById(R.id.reviewSubmit);
		infoText=(TextView)findViewById(R.id.infoTxt);
		
		infoText.setVisibility(View.INVISIBLE);
		hotelName.setVisibility(View.INVISIBLE);
		submitReview.setEnabled(false);
		
		SharedPreferences sp=PreferenceManager.getDefaultSharedPreferences(AddReview.this);
		username=sp.getString("gcm_uname", ""); //i will change after am done testing
		
		//perform connectivity check
		networkCheck();
		//new LoadResNames().execute();
		
		submitReview.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				UserReview= reviewText.getText().toString();
				selectedRes=hotelName.getSelectedItem().toString();
				if(UserReview.isEmpty())
				{
					Toast.makeText(AddReview.this, "Type something...", Toast.LENGTH_SHORT).show();
				}
				else
				{
					//call inner class to post the review
					new AddUserReview().execute();
				}
				
			}
		});
		
		
	}
	//update res names into spinner
	public void updateSpinnerData()
	{
		namesList=new ArrayList<String>();
		
		try
		{
			JSONObject json=jParser.getJSONFromUrl(RESTAURANT_URL);
			success=json.getInt(TAG_SUCCESS);
			resNames=json.getJSONArray(TAG_HOTELS);
			
			for(int i=0; i<resNames.length(); i++)
			{
				JSONObject c=resNames.getJSONObject(i);
				String hname=c.getString(TAG_HNAME);
				
				namesList.add(hname);
			}
		}
		catch(JSONException e)
		{
			e.printStackTrace();
		}
	}
	
	public void SetSpinnerVisible()
	{
		if(success==1)
		{
			ArrayAdapter<String>adapter=new ArrayAdapter<String>(this, 
					android.R.layout.simple_spinner_item, namesList);
			
			hotelName.setAdapter(adapter);
			hotelName.setVisibility(View.VISIBLE);
			infoText.setVisibility(View.VISIBLE);
			submitReview.setEnabled(true);
		}
		else
		{
			Toast.makeText(AddReview.this, "No restaurant added yet... retry", Toast.LENGTH_LONG).show();
		}
	}
	
	//inner class to load the names
	public class LoadResNames extends AsyncTask<Void, Void, Boolean>
	{
		@Override
		protected void onPreExecute()
		{
			super.onPreExecute();
			pDialog=new ProgressDialog(AddReview.this);
			pDialog.setMessage("Updating restaurant names...");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(true);
			pDialog.show();
		}
		@Override
		protected Boolean doInBackground(Void... arg9)
		{
			updateSpinnerData();
			return null;
		}
		@Override
		protected void onPostExecute(Boolean result)
		{
			super.onPostExecute(result);
			pDialog.dismiss();
			SetSpinnerVisible();
		}
	}
	//to perform network checks
			public void networkCheck()
			{
				if(!niggahHasActiveConnection())
				{
					new AlertDialog.Builder(AddReview.this).setIcon(android.R.drawable.ic_dialog_alert)
					.setTitle("Closing the App")
					.setMessage("No Internet connection, check your settings")
					.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							AddReview.this.startActivity(new Intent
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
					new LoadResNames().execute();
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
	//inner class to submit review
		public class AddUserReview extends AsyncTask<String, String, String>
		{
			@Override
			protected void onPreExecute()
			{
				super.onPreExecute();
				pDialog=new ProgressDialog(AddReview.this);
				pDialog.setMessage("Adding your review...");
				pDialog.setIndeterminate(false);
				pDialog.setCancelable(true);
				pDialog.show();
			}

		@Override
		protected String doInBackground(String... arg0) {
			try
			{
				List<NameValuePair>params=new ArrayList<NameValuePair>();
				params.add(new BasicNameValuePair("uname", username));
				params.add(new BasicNameValuePair("resname", selectedRes));
				params.add(new BasicNameValuePair("review", UserReview));
				
				Log.d("request!", "starting");
				JSONObject json=jParser.makeHttpRequest(ADDREVIEW_URL, "POST", params);
				
				Log.d("Update Attempt", json.toString());
				success=json.getInt(TAG_SUCCESS);
				if(success==1)
				{
					Log.d("Review added", json.toString());
					return json.getString(TAG_MESSAGE);
				}
				else
				{
					Log.d("Failure adding", json.getString(TAG_MESSAGE));
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
			if(success==1)
			{
				Toast.makeText(AddReview.this, "Added successfully", Toast.LENGTH_SHORT).show();
			}
			Intent ii=new Intent(AddReview.this, RestaurantReviews.class);
			startActivity(ii);
		}
			
		}
}
