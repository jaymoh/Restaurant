package com.jaymoh.restaurant;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.jaymoh.restaurant.RestaurantOwner.AddRestaurantOwner;

import android.location.Address;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

public class Restaurant extends MainActivity {
	
	//server location
	private static final String RESTAURANT_URL=
			"http://hackinrom.co.nf/android/all_hotels.php";
	
	int success;
	//json tags
	private static final String TAG_SUCCESS="success";
	private static final String TAG_MESSAGE = "message";
	private static final String TAG_RESTAURANTS = "restaurants";
	private static final String TAG_RESNAME = "hname";
	private static final String TAG_RESID = "hotel_id";
	
	private ProgressDialog pDialog;
	
	//json array to hold the restaurant names
	private JSONArray resNames=null;
	
	String[] resList=null;
	int[] imageId=null;
	String[] resId=null;
	
	GridView grid;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.restaurant_layout);
	super.onCreateDrawer();
	
	//check for Internet connectivity and load hotel names from server
	networkCheck();
	//new LoadResNames().execute();

}
	protected void onResume() {
		super.onResume();
		//new LoadResNames().execute();
	}
	//method to load restaurant list from server into a String array
	public void updateStringData()
	{
		JSONParser jParser=new JSONParser();
		
		JSONObject json=jParser.getJSONFromUrl(RESTAURANT_URL);
		
		try
		{
			success=json.getInt(TAG_SUCCESS);
			resNames=json.getJSONArray(TAG_RESTAURANTS);
			//loop through the returned names
			resList=new String[resNames.length()];
			resId=new String[resNames.length()];
			for(int i=0; i<resNames.length(); i++)
			{
				
				JSONObject c=resNames.getJSONObject(i);
				String hotel=c.getString(TAG_RESNAME);
				String hotel_id=c.getString(TAG_RESID);
				
				resList[i]=hotel;
				resId[i]=hotel_id;
			}
		}
		catch(JSONException e)
		{
			e.printStackTrace();
		}
	}
	
	//method to set image resource from res
	private void updateImageId()
	{
		imageId=new int[resList.length];
		
		for(int i=0; i<resList.length;i++)
		{
			imageId[i]=R.drawable.restaurant_;
		}
		updateGridView();
	}
	//update the user interface
	private void updateGridView()
	{
		CustomGrid adapter=new CustomGrid(Restaurant.this, resList, imageId, resId);
		grid=(GridView)findViewById(R.id.grid);
		grid.setAdapter(adapter);
		grid.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id)
			{
				TextView tv=(TextView)view.findViewById(R.id.grid_id);
				String hotel_id=tv.getText().toString();
				//Toast.makeText(Restaurant.this, hotel_id, Toast.LENGTH_SHORT).show();
				 Intent intent=new Intent(Restaurant.this, RestaurantSelected.class);
				intent.putExtra("hotel_id", hotel_id);
				startActivity(intent);
				
			}
		});
	}
	
	//inner class to load restaurant names from server
	public class LoadResNames extends AsyncTask<Void, Void, Boolean>
	{
		@Override
		protected void onPreExecute()
		{
			super.onPreExecute();
			pDialog=new ProgressDialog(Restaurant.this);
			pDialog.setMessage("Loading Restaurants");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(true);
			pDialog.show();
		}
		@Override
		protected Boolean doInBackground(Void... arg0)
		{
			updateStringData();
			return null;
		}
		@Override
		protected void onPostExecute(Boolean result)
		{
			super.onPostExecute(result);
			pDialog.dismiss();
			if(success==1)
			updateImageId();
			else
				Toast.makeText(getApplicationContext(), "No restaurant has been added yet", Toast.LENGTH_LONG).show();
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
							Restaurant.this.startActivity(new Intent
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
	
}